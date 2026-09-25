package com.framepayments.framesdk.accountevents

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.framepayments.framesdk.NetworkingError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Buffers account/diagnostic events and flushes them to the backend in batches.
 *
 * Telemetry must never affect a payment flow: every method here is fire-and-forget and every
 * failure — a full queue, a transport error, a per-event rejection — is swallowed. Nothing this
 * type does can throw to, or block, a caller.
 *
 * In-memory only, by design: there is no disk persistence, so events still queued when the
 * process dies are lost. A flush started from [handleAppBackgrounded] is best-effort — a POST
 * begun at that point races process death and may never complete.
 *
 * [flushHandler] is the seam for testing against a non-prod backend: the queue itself never
 * reads networking constants, so a test supplies its own handler instead of the production
 * [AccountEventsAPI.record] default.
 */
class AccountEventQueue(
    private val maxQueueSize: Int = 200,
    private val flushSizeThreshold: Int = 20,
    private val flushIntervalMillis: Long = 20_000,
    private val flushHandler: suspend (List<AccountEventsRequests.Event>) -> Pair<RecordResponse?, NetworkingError?> =
        { events -> AccountEventsAPI.record(events) }
) {
    companion object {
        /** Matches the backend's per-request cap (POST /v1/client/account_events). */
        private const val MAX_BATCH_SIZE = 100

        /** Bounds the transport-failure retry so a persistently unreachable backend cannot loop forever. */
        private const val MAX_TRANSPORT_RETRIES = 3

        val shared = AccountEventQueue()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val pending = mutableListOf<AccountEventsRequests.Event>()
    private var timerJob: Job? = null
    private var isObservingLifecycle = false

    /** Queues an event, flushing immediately once the size threshold is reached. */
    suspend fun enqueue(event: AccountEventsRequests.Event) {
        val shouldFlush = mutex.withLock {
            if (pending.size >= maxQueueSize) pending.removeAt(0)
            pending.add(event)
            startTimerIfNeeded()
            pending.size >= flushSizeThreshold
        }
        if (shouldFlush) scope.launch { flushPending() }
    }

    /** Test-only inspection of what is currently queued, without triggering a flush. */
    suspend fun pendingEventNamesForTesting(): List<String> = mutex.withLock { pending.map { it.name } }

    /**
     * Starts the size/time-based flush and app-backgrounding observers. Safe to call more than
     * once, and safe to call from any thread: `Lifecycle.addObserver` requires the main thread,
     * so registration is dispatched there rather than trusting every caller (this is invoked
     * from [com.framepayments.framesdk.FrameNetworking.initializeWithAPIKey], a plain function a
     * host could call from a background thread).
     */
    fun startObservingLifecycleIfNeeded() {
        startTimerIfNeeded()
        if (isObservingLifecycle) return
        isObservingLifecycle = true
        CoroutineScope(Dispatchers.Main.immediate).launch {
            ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    scope.launch { handleAppBackgrounded() }
                }
            })
        }
    }

    /** Best-effort flush when the app goes to the background. Not guaranteed delivery — see the type documentation. */
    suspend fun handleAppBackgrounded() {
        flushPending()
    }

    /** Flushes immediately rather than waiting for [flushSizeThreshold] or the interval timer. */
    suspend fun flush() {
        flushPending()
    }

    private fun startTimerIfNeeded() {
        if (timerJob != null) return
        timerJob = scope.launch {
            while (isActive) {
                delay(flushIntervalMillis)
                flushPending()
            }
        }
    }

    private suspend fun flushPending() {
        val batch = mutex.withLock {
            if (pending.isEmpty()) return
            val taken = pending.take(MAX_BATCH_SIZE)
            repeat(taken.size) { pending.removeAt(0) }
            taken
        }
        send(batch, attempt = 1)
    }

    /**
     * Sends one batch, retrying only transport failures and only up to [MAX_TRANSPORT_RETRIES].
     * A rejected batch (malformed request, or a per-event rejection in a 202) is never retried —
     * that is a contract violation, not a connectivity blip.
     */
    private suspend fun send(batch: List<AccountEventsRequests.Event>, attempt: Int) {
        // flushHandler is caller-injectable (tests supply their own), so it is not trusted to
        // honor its documented Pair<..., NetworkingError?> contract — an uncaught throw here
        // would violate this type's "never throws to the caller" guarantee.
        val error = try {
            flushHandler(batch).second
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return
        }
        if (error == null || !error.isTransport || attempt >= MAX_TRANSPORT_RETRIES) return
        send(batch, attempt + 1)
    }
}
