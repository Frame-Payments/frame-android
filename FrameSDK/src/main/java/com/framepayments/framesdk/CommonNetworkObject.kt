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
        @SerializedName("active") ACTIVE,
        @SerializedName("blocked") BLOCKED
    }

    enum class PaymentMethodType {
        @SerializedName("card") CARD,
        @SerializedName("ach") ACH
    }

    enum class PaymentAccountType {
        @SerializedName("checking") CHECKING,
        @SerializedName("savings") SAVINGS
    }

    data class PaymentMethod(
        val id: String,
        val customer: String?,
        val billing: BillingAddress?,
        val type: PaymentMethodType,
        @SerializedName("object") val methodObject: String,
        val created: Int,
        val updated: Int,
        val livemode: Boolean,
        val card: PaymentCard?,
        val ach: BankAccount?,
        val status: PaymentMethodStatus?
    )

    data class PaymentCard(
        val brand: String,
        @SerializedName("exp_month") val expirationMonth: String,
        @SerializedName("exp_year") val expirationYear: String,
        val issuer: String?,
        val currency: String?,
        val segment: String?,
        val type: String?,
        @SerializedName("last_four") val lastFourDigits: String
    )

    data class BankAccount(
        @SerializedName("account_type") val accountType: PaymentAccountType?,
        @SerializedName("bank_name") val bankName: String?,
        @SerializedName("account_number") val accountNumber: String?,
        @SerializedName("routing_number") val routingNumber: String?,
        @SerializedName("last_four") val lastFour: String?
    )

    enum class CustomerStatus {
        @SerializedName("active") ACTIVE,
        @SerializedName("blocked") BLOCKED
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
