package com.framepayments.framesdk.subscriptions

import com.google.gson.annotations.SerializedName

object SubscriptionRequest {
    data class CreateSubscriptionRequest (
        val customer: String,
        val product: String,
        val currency: String,
        @SerializedName("default_payment_method") val defaultPaymentMethod: String,
        val description: String?
    )

    data class UpdateSubscriptionRequest (
        val description: String?,
        @SerializedName("default_payment_method") val defaultPaymentMethod: String?
    )
}