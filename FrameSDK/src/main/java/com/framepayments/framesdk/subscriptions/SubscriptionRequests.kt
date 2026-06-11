package com.framepayments.framesdk.subscriptions

import com.google.gson.annotations.SerializedName

/**
 * Namespace for request models used by the Subscriptions API.
 */
object SubscriptionRequest {

    /**
     * Request body for creating a new subscription.
     *
     * @property customer The ID of the customer to subscribe. Required.
     * @property product The ID of the product the subscription is for. Required.
     * @property currency Three-letter ISO currency code for billing. Required.
     * @property defaultPaymentMethod The ID of the payment method to charge on each billing cycle. Required.
     * @property description Optional description for the subscription.
     */
    data class CreateSubscriptionRequest (
        val customer: String,
        val product: String,
        val currency: String,
        @SerializedName("default_payment_method") val defaultPaymentMethod: String,
        val description: String?
    )

    /**
     * Request body for updating an existing subscription.
     *
     * Only fields provided with non-null values are applied to the subscription.
     *
     * @property description Updated description for the subscription, or null to leave unchanged.
     * @property defaultPaymentMethod Updated payment method ID for future billing cycles, or null to leave unchanged.
     */
    data class UpdateSubscriptionRequest (
        val description: String?,
        @SerializedName("default_payment_method") val defaultPaymentMethod: String?
    )
}
