package com.framepayments.frameonboarding.networking.idv

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * API methods for the government-ID identity-verification (IDV) flow used by the no-SSN
 * onboarding path. Backed by Persona; the Frame server owns the Persona template/environment
 * and is the source of truth for whether verification succeeded.
 */
object IdvAPI {

    /**
     * Creates (or resumes) a Persona inquiry for the onboarding session and returns its id.
     *
     * @param clientSecret The onboarding-session token (`onb_sess_…`).
     * @return A pair of (response, error). On success, `response.inquiryId` is the `inq_…` to
     *   launch the Persona SDK against.
     */
    suspend fun createSession(clientSecret: String): Pair<IdvSessionResponse?, NetworkingError?> {
        val (data, error) = FrameNetworking.performDataTaskWithRequest(
            IdvEndpoints.CreateSession,
            IdvRequests.CreateSession(clientSecret = clientSecret)
        )
        return Pair(data?.let { FrameNetworking.parseResponse<IdvSessionResponse>(it) }, error)
    }

    /**
     * Reports a completed Persona inquiry to Frame and returns the server-authoritative result.
     *
     * The Persona client callback is best-effort only — this call is the source of truth for
     * flipping the UI to "verified". Until the JSON variant of the endpoint ships (FRA-5363),
     * the server replies with a non-JSON body; that decodes to a null `verified`, which this
     * method surfaces verbatim so the caller can treat it as "pending" rather than verified.
     *
     * @param clientSecret The onboarding-session token (`onb_sess_…`).
     * @param inquiryId The Persona inquiry id returned by [createSession].
     * @return A pair of (response, error). Treat a null [IdvCompleteResponse.verified] (or a
     *   non-null error) as "not yet verified".
     */
    suspend fun completeInquiry(
        clientSecret: String,
        inquiryId: String
    ): Pair<IdvCompleteResponse?, NetworkingError?> {
        val (data, error) = FrameNetworking.performDataTaskWithRequest(
            IdvEndpoints.Complete,
            IdvRequests.Complete(clientSecret = clientSecret, inquiryId = inquiryId)
        )
        return Pair(data?.let { FrameNetworking.parseResponse<IdvCompleteResponse>(it) }, error)
    }
}
