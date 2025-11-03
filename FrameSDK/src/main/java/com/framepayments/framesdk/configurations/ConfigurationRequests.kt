package com.framepayments.framesdk.configurations

import com.google.gson.annotations.SerializedName

object ConfigurationRequests {
    data class CreateSiftDetailsRequest(
        val email: String? = null,
        @SerializedName("ip_address") var ipAddress: String,
        @SerializedName("customer_id") val customerId: String
    )
}
