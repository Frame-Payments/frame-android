package com.framepayments.framesdk
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

interface URLSessionProtocol {
    suspend fun execute(request: Request): Response
}

interface FrameNetworkingEndpoints {
    val endpointURL: String
    val httpMethod: String
    val queryItems: List<QueryItem>?
}

sealed class NetworkingError : Exception() {
    data object InvalidURL : NetworkingError() {
        private fun readResolve(): Any = InvalidURL
    }
    data object DecodingFailed : NetworkingError() {
        private fun readResolve(): Any = DecodingFailed
    }
    data class ServerError(val statusCode: Int, val errorDescription: String) : NetworkingError()
    data object UnknownError : NetworkingError() {
        private fun readResolve(): Any = UnknownError
    }

    /**
     * True for connectivity-class failures the user can retry. False for server-validation
     * errors that should stay in the form so the user can correct the input.
     */
    val isTransport: Boolean
        get() = when (this) {
            InvalidURL, DecodingFailed, UnknownError -> true
            is ServerError -> false
        }

    /**
     * User-facing message for the snackbar surface, prefixed with "Error: " for clarity. For
     * [ServerError], parses the standard Frame envelope (`{"error_details":{"message":"…"},"error":"…"}`)
     * and prefers `error_details.message`. For transport-class errors or unparseable bodies,
     * returns [fallback] prefixed the same way.
     */
    fun toastMessage(fallback: String = "Something went wrong. Please try again."): String {
        val body = if (this is ServerError) {
            extractEnvelopeMessage(errorDescription) ?: fallback
        } else {
            fallback
        }
        return "Error: $body"
    }

    private companion object {
        /// Pull a user-facing message from the Frame error envelope JSON. The server's
        /// `error_details` field is polymorphic — sometimes an object with a `message` key,
        /// sometimes a plain string (e.g. `"Card submitted is not a test card"` for 422
        /// validation failures). Both shapes are handled; `error_details` is preferred over
        /// the generic `error` key (which tends to be the HTTP status name like
        /// `"Unprocessable Entity"`). Returns null when the body isn't valid JSON or no
        /// usable field is present.
        fun extractEnvelopeMessage(raw: String): String? {
            if (raw.isEmpty()) return null
            return try {
                val envelope = JSONObject(raw)
                val nestedMessage = envelope.optJSONObject("error_details")?.optString("message").orEmpty()
                if (nestedMessage.isNotEmpty()) return nestedMessage
                // optString returns "" both when the key is absent and when the value is a non-string
                // (e.g. an object), so we have to check the underlying type before trusting it.
                val rawDetails = envelope.opt("error_details")
                if (rawDetails is String && rawDetails.isNotEmpty()) return rawDetails
                val error = envelope.optString("error")
                if (error.isNotEmpty()) error else null
            } catch (_: Exception) {
                null
            }
        }
    }
}