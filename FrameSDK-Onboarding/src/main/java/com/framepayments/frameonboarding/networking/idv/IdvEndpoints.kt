package com.framepayments.frameonboarding.networking.idv

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Routing definitions for the government-ID identity-verification (IDV) endpoints used by the
 * no-SSN onboarding path. Both endpoints authenticate the onboarding session via the
 * `client_secret` carried in the request body (see [IdvRequests]).
 */
sealed class IdvEndpoints : FrameNetworkingEndpoints {
    /** Creates (or resumes) a Persona inquiry for the session's account and returns its `inquiry_id`. */
    data object CreateSession : IdvEndpoints()

    /** Reports a completed Persona inquiry back to Frame and returns the server-authoritative verification result. */
    data object Complete : IdvEndpoints()

    override val endpointURL: String
        get() = when (this) {
            CreateSession -> "/v1/idv/session"
            Complete -> "/v1/idv/complete"
        }

    override val httpMethod: String
        get() = "POST"

    override val queryItems: List<QueryItem>?
        get() = null

    override val additionalHeaders: Map<String, String>
        get() = when (this) {
            // Ask the server for the JSON `{ "verified": <bool> }` variant of the complete
            // response (FRA-5363). Until that ships server-side, the endpoint replies with a
            // Turbo Stream / HTML body, which [IdvAPI.completeInquiry] treats as "pending".
            Complete -> mapOf("Accept" to "application/json")
            CreateSession -> emptyMap()
        }
}
