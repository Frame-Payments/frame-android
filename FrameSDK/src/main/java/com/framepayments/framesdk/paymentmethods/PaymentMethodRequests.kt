package com.framepayments.framesdk.paymentmethods

import com.google.gson.annotations.SerializedName

object PaymentMethodRequests {
    data class CreatePaymentMethodRequest(
        val type: String,
        @SerializedName("card_number") val cardNumber: String,
        @SerializedName("exp_month") val expMonth: String,
        @SerializedName("exp_year") val expYear: String,
        val cvc: String,
        val customer: String? = null,
        val billing: FrameObjects.BillingAddress? = null
    )

    data class UpdatePaymentMethodRequest(
        @SerializedName("exp_month") val expMonth: String? = null,
        @SerializedName("exp_year") val expYear: String? = null,
        val billing: FrameObjects.BillingAddress? = null
    )

    data class AttachPaymentMethodRequest(
        val customer: String
    )
}
