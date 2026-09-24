package com.framepayments.framesdk.sonar

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.QueryItem
import com.framepayments.framesdk.accountevents.AccountEventDetail
import com.framepayments.framesdk.accountevents.AccountEventEmitter
import com.framepayments.framesdk.accountevents.AccountEventName
import com.framepayments.framesdk.accountevents.AccountEventScreen
import com.framepayments.framesdk.fingerprint.FingerprintIdentification
import com.framepayments.framesdk.fingerprint.FingerprintManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

typealias SessionId = String

private const val LEGACY_SESSION_STORAGE_KEY = "frame_charge_session_id"
private const val SESSION_KEY_PREFIX = "frame_sonar_session_id_"
private const val REFRESH_SUFFIX = "_refreshed_at"

data class SessionResponse(
    val sonar_session_id: String
)

/**
 * Request body for creating or updating a Sonar session.
 *
 * [accountId] is required for any session that will back a payment: the server resolves a
 * payment's session through the account, so one created without it is invisible to risk checks.
 * [sealedResult] is sent only when Fingerprint provides one; omitted rather than null so the
 * request looks the same as before sealed results existed.
 */
data class SessionRequestBody(
    val fingerprint_visitor_id: String? = null,
    val account_id: String? = null,
    val sealed_result: String? = null
)

sealed class SonarSessionEndpoints : FrameNetworkingEndpoints {
    object Create : SonarSessionEndpoints()
    data class Update(val id: String) : SonarSessionEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is Create -> "/v1/charge_sessions"
            is Update -> "/v1/charge_sessions/${this.id}"
        }

    override val httpMethod: String
        get() = when (this) {
            is Create -> "POST"
            is Update -> "PUT"
        }

    override val queryItems: List<QueryItem>?
        get() = null
}

/**
 * Backing store for Sonar session identifiers, keyed by Frame account. `accountId = null`
 * addresses the pre-account slot, holding a session minted before the account was known so it
 * can be adopted once it is.
 */
interface SessionStorage {
    fun get(accountId: String? = null): SessionId?
    fun set(value: SessionId, accountId: String? = null)
    fun clear(accountId: String? = null)
    fun lastRefresh(accountId: String? = null): Long?
    fun setLastRefresh(timestamp: Long, accountId: String? = null)
}

/**
 * [SessionStorage] backed by `SharedPreferences`. Keying per account is what stops one account's
 * session being reused by the next account on the same device.
 */
class SharedPreferencesSessionStorage(
    private val prefs: SharedPreferences
) : SessionStorage {
    private fun key(accountId: String?): String =
        if (accountId.isNullOrEmpty()) LEGACY_SESSION_STORAGE_KEY else SESSION_KEY_PREFIX + accountId

    override fun get(accountId: String?): SessionId? = prefs.getString(key(accountId), null)

    override fun set(value: SessionId, accountId: String?) {
        prefs.edit().putString(key(accountId), value).apply()
    }

    override fun clear(accountId: String?) {
        val k = key(accountId)
        prefs.edit().remove(k).remove(k + REFRESH_SUFFIX).apply()
    }

    override fun lastRefresh(accountId: String?): Long? {
        val value = prefs.getLong(key(accountId) + REFRESH_SUFFIX, -1L)
        return if (value < 0) null else value
    }

    override fun setLastRefresh(timestamp: Long, accountId: String?) {
        prefs.edit().putLong(key(accountId) + REFRESH_SUFFIX, timestamp).apply()
    }
}

/**
 * Manages the lifecycle of a Sonar fraud-detection session: creation, refresh, keep-alive, and
 * per-account local persistence.
 *
 * A session only backs a payment once it has been associated with a Frame account, and sessions
 * go stale — the server requires the session's latest device event to be recent, and only a
 * create or update call records one. Call [ensureSession] before taking a payment.
 */
class SessionManager(
    private var sessionId: SessionId?,
    private val storage: SessionStorage,
    /**
     * Mints a fresh Fingerprint identification for a single request. Called again for every
     * create, refresh, and keep-alive touch rather than held: a sealed result carries its own
     * timestamp and the server rejects one stamped more than ten minutes from now in either
     * direction, so a reused payload is a rejected payload — sharpest on a device that suspends
     * and resumes hours later.
     */
    private val identify: suspend () -> FingerprintIdentification?
) {
    private val sdkScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val inFlight = HashMap<String, Deferred<SessionId>>()
    private val inFlightLock = Mutex()

    /** The account whose session the keep-alive re-touches; null before an account is known. */
    @Volatile private var activeAccountId: String? = null
    private var keepAliveJob: Job? = null

    /**
     * Establishes a session for the legacy, pre-account flow this class originally supported.
     * Preserved for existing callers; [ensureSession] is the account-scoped equivalent.
     */
    suspend fun initialize(): SessionId {
        return if (sessionId == null) {
            val created = createSession(accountId = null)
            AccountEventEmitter.emit(AccountEventName.FRAUD_SESSION_STARTED, AccountEventScreen.CHECKOUT)
            created
        } else {
            updateSession(accountId = null)
        }
    }

    /**
     * Returns a session for [accountId] that is fresh enough to back a payment, creating or
     * refreshing one if necessary. Idempotent, and coalesces concurrent callers for the same
     * account onto a single round trip.
     */
    suspend fun ensureSession(accountId: String): SessionId {
        activeAccountId = accountId
        startKeepAlive()

        val existing = storage.get(accountId)
        if (existing != null && isFresh(accountId)) {
            sessionId = existing
            return existing
        }

        return establishCoalesced(accountId)
    }

    /** Refreshes or creates [accountId]'s session, joining any round trip already in flight for it. */
    private suspend fun establishCoalesced(accountId: String): SessionId {
        val deferred = inFlightLock.withLock {
            inFlight[accountId] ?: sdkScope.async { establishSession(accountId) }.also { inFlight[accountId] = it }
        }

        try {
            return deferred.await()
        } finally {
            withContext(NonCancellable) {
                inFlightLock.withLock { if (inFlight[accountId] === deferred) inFlight.remove(accountId) }
            }
        }
    }

    /**
     * Re-touches the live session after the app returns to the foreground and restarts the
     * keep-alive. Backgrounding is the most common way a session goes stale, since timers don't
     * fire while suspended.
     */
    suspend fun resume() {
        touchActiveSession()
        startKeepAlive()
    }

    /** Stops the keep-alive while the app is backgrounded. */
    fun pause() {
        keepAliveJob?.cancel()
        keepAliveJob = null
    }

    private fun startKeepAlive() {
        if (keepAliveJob != null) return
        keepAliveJob = sdkScope.launch {
            while (true) {
                delay(KEEP_ALIVE_INTERVAL_MS)
                touchActiveSession()
            }
        }
    }

    private suspend fun touchActiveSession() {
        val accountId = activeAccountId
        if (accountId == null) {
            // Pre-account warm-up session: refresh the anonymous slot so flow-entry keeps it fresh.
            try {
                storage.get(accountId = null)?.let { existing ->
                    val refreshed = refreshSession(existing, accountId = null)
                    store(refreshed, accountId = null)
                } ?: run {
                    val created = createSession(accountId = null)
                    store(created, accountId = null)
                }
            } catch (_: Exception) {
                // Swallowed deliberately — a missed keep-alive is recovered by the next ensureSession call.
            }
            return
        }
        try {
            establishCoalesced(accountId)
        } catch (_: Exception) {
            // Swallowed deliberately — a missed keep-alive is recovered by the next ensureSession call.
        }
    }

    /**
     * Records a Sonar device event when an SDK entry-point view is presented. Mirrors iOS
     * `refreshOnFlowEntry(accountId:)`: opportunistic, never blocks presentation, and refreshes
     * the pre-account session when [accountId] is null so adoption later preserves device history.
     */
    suspend fun refreshOnFlowEntry(accountId: String? = null) {
        val resolved = accountId?.takeIf { it.isNotEmpty() }
        if (resolved != null) {
            activeAccountId = resolved
            startKeepAlive()
            try {
                // Not ensureSession: a fresh session must still record this entry's device event.
                establishCoalesced(resolved)
            } catch (_: Exception) {
                // Fire-and-forget.
            }
            return
        }

        val stored = storage.get(accountId = null)
        if (stored != null) {
            try {
                val refreshed = refreshSession(stored, accountId = null)
                store(refreshed, accountId = null)
            } catch (_: Exception) {
                // Fire-and-forget.
            }
        } else {
            try {
                val created = createSession(accountId = null)
                store(created, accountId = null)
            } catch (_: Exception) {
                // Fire-and-forget.
            }
        }
    }

    private fun isFresh(accountId: String?): Boolean {
        val last = storage.lastRefresh(accountId) ?: return false
        return System.currentTimeMillis() - last < FRESHNESS_WINDOW_MS
    }

    private fun store(id: SessionId, accountId: String?) {
        storage.set(id, accountId)
        storage.setLastRefresh(System.currentTimeMillis(), accountId)
        sessionId = id
    }

    private suspend fun establishSession(accountId: String): SessionId {
        storage.get(accountId)?.let { existing ->
            val refreshed = refreshSession(existing, accountId)
            store(refreshed, accountId)
            return refreshed
        }

        storage.get(accountId = null)?.let { legacy ->
            val adopted = refreshSession(legacy, accountId)
            store(adopted, accountId)
            // Leaving the legacy slot readable would let the next account on this device adopt
            // the same session.
            storage.clear(accountId = null)
            AccountEventEmitter.emit(
                AccountEventName.FRAUD_SESSION_ADOPTED,
                AccountEventScreen.CHECKOUT,
                AccountEventDetail.FRAUD_SESSION_ADOPTED_FROM_ANONYMOUS
            )
            return adopted
        }

        val created = createSession(accountId)
        store(created, accountId)
        AccountEventEmitter.emit(AccountEventName.FRAUD_SESSION_STARTED, AccountEventScreen.CHECKOUT)
        return created
    }

    private suspend fun requestBody(accountId: String?): SessionRequestBody {
        val identification = identify()
        return SessionRequestBody(
            // Omit rather than send "" — an empty visitor id is not a usable identifier.
            fingerprint_visitor_id = identification?.visitorId?.takeIf { it.isNotEmpty() },
            account_id = accountId,
            sealed_result = identification?.sealedResult
        )
    }

    private suspend fun createSession(accountId: String?): SessionId {
        return try {
            val response = perform(SonarSessionEndpoints.Create, requestBody(accountId))
            store(response, accountId)
            response
        } catch (e: Exception) {
            Log.e("SessionManager", "Failed to create charge session", e)
            throw e
        }
    }

    private suspend fun refreshSession(session: SessionId, accountId: String?): SessionId {
        return try {
            val refreshed = perform(SonarSessionEndpoints.Update(session), requestBody(accountId))
            AccountEventEmitter.emit(AccountEventName.FRAUD_SESSION_REFRESHED, AccountEventScreen.CHECKOUT)
            refreshed
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // The server no longer recognises this session, so replace it rather than fail the payment.
            Log.w("SessionManager", "Failed to update session, creating new one", e)
            storage.clear(accountId)
            val recreated = createSession(accountId)
            AccountEventEmitter.emit(
                AccountEventName.FRAUD_SESSION_RECREATED,
                AccountEventScreen.CHECKOUT,
                AccountEventDetail.FRAUD_SESSION_REFRESH_FELL_BACK_TO_RECREATE
            )
            recreated
        }
    }

    private suspend fun updateSession(accountId: String?): SessionId {
        val current = sessionId ?: return createSession(accountId)
        val refreshed = refreshSession(current, accountId)
        store(refreshed, accountId)
        return refreshed
    }

    private suspend fun perform(endpoint: SonarSessionEndpoints, body: SessionRequestBody): SessionId {
        // Failures here are swallowed by every caller (the payment path independently calls
        // ensureSession) — this is the one place to surface them without disturbing that swallow.
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, body, FrameAuthMode.PublishableOnly)
        if (error != null) {
            AccountEventEmitter.emit(AccountEventName.SONAR_SESSION_FAILED, AccountEventScreen.CHECKOUT, "$error")
            throw error
        }
        val response = FrameNetworking.parseResponse<SessionResponse>(data) ?: run {
            AccountEventEmitter.emit(
                AccountEventName.SONAR_SESSION_FAILED,
                AccountEventScreen.CHECKOUT,
                "${NetworkingError.DecodingFailed}"
            )
            throw NetworkingError.DecodingFailed
        }
        return response.sonar_session_id
    }

    fun getSessionId(): SessionId? = sessionId

    companion object {
        /** Sits well inside the server's freshness window: refreshing early is cheap, being late fails the payment. */
        private const val FRESHNESS_WINDOW_MS = 15 * 60 * 1000L

        /** Shorter than [FRESHNESS_WINDOW_MS] so a refresh always lands before the window closes. */
        private const val KEEP_ALIVE_INTERVAL_MS = 10 * 60 * 1000L

        /**
         * Creates the SDK-wide session manager, establishing a session bound to [accountId] when
         * it's known at init — matching iOS's `SessionManager.initializeSession(accountId:)` — or
         * the legacy unscoped session otherwise.
         *
         * Fingerprint is identified fresh via [FingerprintManager.identify] for every request
         * this manager makes, not just once here — see [SessionManager]'s `identify` parameter.
         */
        suspend fun initializeWithFrameNetworking(context: Context, accountId: String? = null): SessionManager {
            // SharedPreferencesSessionStorage.key() already treats "" the same as null (both map
            // to the legacy anonymous-session key) -- normalize here too, or a caller passing ""
            // takes the account-scoped ensureSession("") path instead, starting an
            // account-keep-alive session and sending account_id="" to the server.
            val resolvedAccountId = accountId?.takeIf { it.isNotEmpty() }
            val appContext = context.applicationContext
            val prefs = appContext.getSharedPreferences("sonar_sessions", Context.MODE_PRIVATE)
            val storage = SharedPreferencesSessionStorage(prefs)
            val existingSessionId = storage.get(resolvedAccountId)

            val manager = SessionManager(
                sessionId = existingSessionId,
                storage = storage,
                identify = { FingerprintManager.identify(appContext) }
            )

            if (resolvedAccountId != null) {
                manager.ensureSession(resolvedAccountId)
            } else {
                manager.initialize()
            }
            return manager
        }
    }
}
