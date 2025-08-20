package com.framepayments.framesdk.paymentmethods
import com.google.gson.annotations.SerializedName
import com.framepayments.framesdk.FrameObjects

object PaymentMethodRequests {
    data class CreatePaymentMethodRequest(
        val type: String,
        @SerializedName("card_number") var cardNumber: String,
        @SerializedName("exp_month") val expMonth: String,
        @SerializedName("exp_year") val expYear: String,
        var cvc: String,
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
