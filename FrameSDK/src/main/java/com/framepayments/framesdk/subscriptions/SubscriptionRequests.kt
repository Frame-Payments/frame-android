package com.framepayments.framesdk.subscriptions

object SubscriptionRequest {
    data class CreateSubscriptionRequest (
        val customer: String,
        val product: String,
        val currency: String,
        val defaultPaymentMethod: String,
        val description: String?
    )

    data class UpdateSubscriptionRequest (
        val description: String?,
        val defaultPaymentMethod: String?
    )
}