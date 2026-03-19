package com.framepayments.framesdk.termsofservice

import com.google.gson.annotations.SerializedName

object TermsOfServiceRequests {
    data class UpdateRequest(
        val token: String,
        @SerializedName("accepted_at") val acceptedAt: Int? = null,
        @SerializedName("ip_address") val ipAddress: String? = null,
        @SerializedName("user_agent") val userAgent: String? = null
    )
}
