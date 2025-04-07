package com.framepayments.framesdk.paymentmethods

import com.framepayments.framesdk.FrameObjects
import com.google.gson.annotations.SerializedName

data class PaymentMethod(
    val id: String,
    val customer: String? = null,
    val billing: FrameObjects.BillingAddress? = null,
    val type: String,
    @SerializedName("object") val methodObject: String,
    val created: Int,
    val updated: Int,
    @SerializedName("livemode") val liveMode: Boolean,
    val card: PaymentCard? = null
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
