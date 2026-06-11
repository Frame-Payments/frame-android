package com.framepayments.framesdk
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

/** HTTP execution abstraction used by all Frame SDK API clients. */
interface URLSessionProtocol {
    /**
     * Executes [request] and returns the raw HTTP response.
     *
     * @param request The OkHttp request to execute.
     * @return The HTTP response. Callers are responsible for closing the response body.
     */
    suspend fun execute(request: Request): Response
}

/** Describes a single Frame API endpoint: its URL, HTTP method, and optional query parameters. */
interface FrameNetworkingEndpoints {
    /** The fully qualified URL string for this endpoint. */
    val endpointURL: String

    /** HTTP method, e.g. `"GET"`, `"POST"`, `"DELETE"`. */
    val httpMethod: String

    /** Optional query-string parameters appended to [endpointURL]. */
    val queryItems: List<QueryItem>?
}

/**
 * Errors that the Frame SDK network layer can surface to callers.
 *
 * Use [isTransport] to distinguish retryable connectivity failures from server-validation
 * errors that require the user to correct their input.
 */
sealed class NetworkingError : Exception() {
    /** The constructed URL was malformed and the request could not be sent. */
    data object InvalidURL : NetworkingError() {
        private fun readResolve(): Any = InvalidURL
    }

    /** The server responded successfully, but the response body could not be decoded. */
    data object DecodingFailed : NetworkingError() {
        private fun readResolve(): Any = DecodingFailed
    }

    /**
     * The server returned a non-2xx status code.
     *
     * @property statusCode The HTTP status code returned by the server.
     * @property errorDescription The raw response body, which may contain a Frame error envelope.
     */
    data class ServerError(val statusCode: Int, val errorDescription: String) : NetworkingError()

    /** An unexpected error occurred that does not fit a more specific category. */
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