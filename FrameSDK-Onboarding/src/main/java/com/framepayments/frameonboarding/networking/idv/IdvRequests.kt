package com.framepayments.frameonboarding.networking.idv

import com.google.gson.annotations.SerializedName

/** Request body types for the IDV (government-ID) endpoints. */
object IdvRequests {
    /**
     * Body for `POST /idv/session`. The onboarding-session `client_secret` authenticates the
     * request and scopes the created Persona inquiry to the session's account.
     *
     * @property clientSecret The onboarding-session token (`onb_sess_…`) minted by the backend.
     */
    data class CreateSession(
        @SerializedName("client_secret") val clientSecret: String
    )

    /**
     * Body for `POST /idv/complete`.
     *
     * @property clientSecret The onboarding-session token (`onb_sess_…`) minted by the backend.
     * @property inquiryId The Persona inquiry id (`inq_…`) returned by `POST /idv/session`.
     */
    data class Complete(
        @SerializedName("client_secret") val clientSecret: String,
        @SerializedName("inquiry_id") val inquiryId: String
    )
}
