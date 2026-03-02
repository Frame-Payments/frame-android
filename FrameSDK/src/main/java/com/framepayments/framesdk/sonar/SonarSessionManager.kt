package com.framepayments.framesdk.sonar

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.QueryItem

typealias SessionId = String

private const val SESSION_STORAGE_KEY = "frame_charge_session_id"

data class SessionResponse(
    val sonar_session_id: String
)

data class SessionRequestBody(
    val fingerprint_visitor_id: String
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

interface SessionStorage {
    fun get(): SessionId?
    fun set(value: SessionId)
    fun clear()
}

class SharedPreferencesSessionStorage(
    private val prefs: SharedPreferences
) : SessionStorage {
    override fun get(): SessionId? = prefs.getString(SESSION_STORAGE_KEY, null)

    override fun set(value: SessionId) {
        prefs.edit().putString(SESSION_STORAGE_KEY, value).apply()
    }

    override fun clear() {
        prefs.edit().remove(SESSION_STORAGE_KEY).apply()
    }
}

class SessionManager(
    private var sessionId: SessionId?,
    private val visitorId: String,
    private val storage: SessionStorage
) {

    suspend fun initialize(): SessionId {
        return if (sessionId == null) {
            createSession()
        } else {
            updateSession()
        }
    }

    private suspend fun createSession(): SessionId {
        return try {
            val endpoint = SonarSessionEndpoints.Create
            val body = SessionRequestBody(fingerprint_visitor_id = visitorId)
            val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, body)

            if (error != null) {
                throw error
            }

            val response = FrameNetworking.parseResponse<SessionResponse>(data)
                ?: throw NetworkingError.DecodingFailed

            setSession(response.sonar_session_id)
            response.sonar_session_id
        } catch (e: Exception) {
            Log.e("SessionManager", "Failed to create charge session", e)
            throw e
        }
    }

    private suspend fun updateSession(): SessionId {
        val current = sessionId ?: return createSession()

        return try {
            val endpoint = SonarSessionEndpoints.Update(current)
            val body = SessionRequestBody(fingerprint_visitor_id = visitorId)
            val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, body)

            if (error != null) {
                throw error
            }

            val response = FrameNetworking.parseResponse<SessionResponse>(data)
                ?: throw NetworkingError.DecodingFailed

            setSession(response.sonar_session_id)
            response.sonar_session_id
        } catch (e: Exception) {
            Log.w("SessionManager", "Failed to update session, creating new one", e)
            clearStoredSessionId()
            createSession()
        }
    }

    private fun setSession(id: SessionId) {
        sessionId = id
        storage.set(id)
    }

    private fun clearStoredSessionId() {
        storage.clear()
        sessionId = null
    }

    fun getSessionId(): SessionId? = sessionId

    companion object {
        suspend fun initializeWithFrameNetworking(context: Context, visitorId: String): SessionManager {
            val prefs = context.getSharedPreferences("sonar_sessions", Context.MODE_PRIVATE)
            val storage = SharedPreferencesSessionStorage(prefs)
            val existingSessionId = storage.get()

            val manager = SessionManager(
                sessionId = existingSessionId,
                visitorId = visitorId,
                storage = storage
            )

            manager.initialize()
            return manager
        }
    }
}

