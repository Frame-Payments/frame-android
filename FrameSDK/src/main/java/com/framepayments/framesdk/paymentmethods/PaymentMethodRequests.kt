package com.framepayments.framesdk.paymentmethods
import com.google.gson.annotations.SerializedName
import com.framepayments.framesdk.FrameObjects

object PaymentMethodRequests {
    data class CreateCardPaymentMethodRequest(
        val type: FrameObjects.PaymentMethodType = FrameObjects.PaymentMethodType.CARD,
        @SerializedName("card_number") var cardNumber: String,
        @SerializedName("exp_month") val expMonth: String,
        @SerializedName("exp_year") val expYear: String,
        var cvc: String,
        val customer: String? = null,
        val billing: FrameObjects.BillingAddress? = null
    )

    data class CreateACHPaymentMethodRequest(
        val type: FrameObjects.PaymentMethodType = FrameObjects.PaymentMethodType.ACH,
        @SerializedName("account_type") val accountType: FrameObjects.PaymentAccountType,
        @SerializedName("account_number") val accountNumber: String,
        @SerializedName("routing_number") val routingNumber: String,
        val customer: String? = null,
        val billing: FrameObjects.BillingAddress? = null
    )

    data class UpdatePaymentMethodRequest(
        @SerializedName("exp_month") val expMonth: String? = null, // Only used for `card` type
        @SerializedName("exp_year") val expYear: String? = null, // Only used for `card` type
        val billing: FrameObjects.BillingAddress? = null
    )

    data class AttachPaymentMethodRequest(
        val customer: String
    )
}
