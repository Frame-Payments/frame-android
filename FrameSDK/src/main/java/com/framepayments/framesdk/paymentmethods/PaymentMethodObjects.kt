package com.framepayments.framesdk.paymentmethods

import com.google.gson.annotations.SerializedName

object FrameObjects {
    data class PaymentMethod(
        val id: String,
        val customer: String? = null,
        val billing: BillingAddress? = null,
        val type: String,
        @SerializedName("object") val methodObject: String,
        val created: Int,
        val updated: Int,
        @SerializedName("livemode") val liveMode: Boolean,
        val card: PaymentCard? = null
    )

    data class BillingAddress(
        val city: String? = null,
        val country: String? = null,
        val state: String? = null,
        @SerializedName("postal_code") val postalCode: String,
        @SerializedName("line_1") val addressLine1: String? = null,
        @SerializedName("line_2") val addressLine2: String? = null
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
}
