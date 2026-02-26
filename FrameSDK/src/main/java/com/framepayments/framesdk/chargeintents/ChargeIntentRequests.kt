package com.framepayments.framesdk.chargeintents
import com.framepayments.framesdk.FrameObjects
import com.google.gson.annotations.SerializedName

object ChargeIntentsRequests {
    enum class PaymentMethodType {
        card
    }

    data class CreateChargeIntentRequest (
        val amount: Int,
        val currency : String,
        val customer : String?,
        val description : String?,
        val confirm: Boolean,
        @SerializedName("payment_method") val paymentMethod : String?,
        @SerializedName("receipt_email") val receiptEmail: String?,
        @SerializedName("authorization_mode") val authorizationMode: AuthorizationMode?,
        @SerializedName("customer_data") val customerData: CustomerData?,
        @SerializedName("payment_method_data") val paymentMethodData: PaymentMethodData?,
        @SerializedName("fraud_signals") var fraudSignals: FraudSignals? = null,
        @SerializedName("use_frame_sdk") val useFrameSDK: Boolean = true,
        @SerializedName("sonar_session_id") val sonarSessionId: String? = null
    )

    data class UpdateChargeIntentRequest (
        val amount: Int?,
        val currency: String?,
        val customer: String?,
        val description: String?,
        val confirm: Boolean?,
        @SerializedName("payment_method") val paymentMethod: String?,
        @SerializedName("receipt_email") val receiptEmail: String?
    )

    data class CaptureChargeIntentRequest(
        @SerializedName("amount_captured_cents") val amountCapturedCents: Int
    )

    data class CustomerData (
        val name: String,
        val email: String
    )

    data class PaymentMethodData (
        val attach: Boolean?,
        val type: PaymentMethodType,
        @SerializedName("card_number") val cardNumber: String,
        @SerializedName("exp_month") val expMonth: String,
        @SerializedName("exp_year") val expYear: String,
        val cvc: String,
        val billing: FrameObjects.BillingAddress?
    )

    data class FraudSignals (
        @SerializedName("client_ip") val clientIp: String?
    )
}