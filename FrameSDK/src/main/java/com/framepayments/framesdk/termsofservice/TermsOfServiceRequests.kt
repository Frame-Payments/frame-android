package com.framepayments.framesdk.termsofservice

import com.google.gson.annotations.SerializedName

/**
 * Namespace for request bodies used with the Terms of Service API.
 */
object TermsOfServiceRequests {

    /**
     * Request body for recording a customer's Terms of Service acceptance.
     *
     * @property token The short-lived token obtained from [TermsOfServiceAPI.createToken]; identifies the acceptance session.
     * @property acceptedAt Unix timestamp of when the customer accepted the Terms of Service. Defaults to the server time when omitted.
     * @property ipAddress IP address of the customer at the time of acceptance, used for audit purposes.
     * @property userAgent User-agent string of the customer's device at the time of acceptance, used for audit purposes.
     */
    data class UpdateRequest(
        val token: String,
        @SerializedName("accepted_at") val acceptedAt: Int? = null,
        @SerializedName("ip_address") val ipAddress: String? = null,
        @SerializedName("user_agent") val userAgent: String? = null
    )
}
