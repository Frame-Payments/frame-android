package com.framepayments.framesdk

import com.google.gson.annotations.SerializedName

object FrameObjects {
    data class BillingAddress(
        val city: String?,
        val country: String?,
        val state: String?,
        @SerializedName("postal_code") val postalCode: String,
        @SerializedName("line_1") val addressLine1: String?,
        @SerializedName("line_2") val addressLine2: String?
    )

    enum class PaymentMethodStatus {
        active, blocked
    }
    data class PaymentMethod(
        val id: String,
        val customer: String?,
        val billing: BillingAddress?,
        val type: String,
        @SerializedName("object") val methodObject: String,
        val created: Int,
        val updated: Int,
        val livemode: Boolean,
        val card: PaymentCard?,
        val status: PaymentMethodStatus?
    )

    data class PaymentCard(
        val brand: String,
        @SerializedName("exp_month") val expirationMonth: String,
        @SerializedName("exp_year") val expirationYear: String,
        val issuer: String? = null,
        val currency: String? = null,
        val segment: String? = null,
        val type: String? = null,
        @SerializedName("last_four") val lastFourDigits: String
    )

    enum class CustomerStatus {
        active,
        blocked
    }

    data class Customer(
        val id: String,
        val created: Int,
        val updated: Int?,
        val livemode: Boolean,
        val name: String,
        val status: CustomerStatus?,
        val phone: String?,
        val email: String?,
        val description: String?,
        @SerializedName("date_of_birth") val dateOfBirth: String?, // YYYY-MM-DD
        @SerializedName("object") val customerObject: String?,
        @SerializedName("shipping_address") val shippingAddress: BillingAddress?,
        @SerializedName("billing_address") val billingAddress: BillingAddress?,
        @SerializedName("payment_methods") val paymentMethods: List<PaymentMethod>?
    )
}

data class EmptyRequest (
    val description: String?
)

data class QueryItem(val name: String, val value: String?)

data class FrameMetadata(
    val page: Int,
    val url: String,
    @SerializedName("has_more") val hasMore: Boolean
)
