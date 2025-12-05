package com.framepayments.framesdk.chargeintents
import com.framepayments.framesdk.FrameObjects
import com.google.gson.annotations.SerializedName

enum class AuthorizationMode {
    @SerializedName("automatic") AUTOMATIC,
    @SerializedName("manual") MANUAL
}

enum class ChargeIntentStatus {
    @SerializedName("canceled") CANCELED,
    @SerializedName("disputed") DISPUTED,
    @SerializedName("failed") FAILED,
    @SerializedName("incomplete") INCOMPLETE,
    @SerializedName("pending") PENDING,
    @SerializedName("refunded") REFUNDED,
    @SerializedName("reversed") REVERSED,
    @SerializedName("succeeded") SUCCEEDED
}

data class ChargeIntent(
    val id: String,
    val currency: String,
    val customer: FrameObjects.Customer?,
    val shipping: FrameObjects.BillingAddress?,
    val status: ChargeIntentStatus,
    val description: String?,
    val amount: Int,
    val created: Int,
    val updated: Int,
    val livemode: Boolean,
    @SerializedName("latest_charge") val latestCharge: LatestCharge?,
    @SerializedName("payment_method") val paymentMethod: FrameObjects.PaymentMethod?,
    @SerializedName("authorization_mode")val authorizationMode: AuthorizationMode,
    @SerializedName("failure_description")val failureDescription: String?,
    @SerializedName("object") val intentObject: String
)

data class LatestCharge (
    val id: String,
    val currency : String,
    val created : Int,
    val updated : Int,
    val livemode : Boolean,
    val captured : Boolean,
    val disputed : Boolean,
    val refunded : Boolean,
    val description: String?,
    val status: ChargeIntentStatus?,
    val customer: String?,
    val amount: Int,
    @SerializedName("failure_message") val failureMessage: String?,
    @SerializedName("payment_method_details") val paymentMethodDetails: FrameObjects.PaymentMethod?,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("charge_intent") val chargeIntent : String,
    @SerializedName("amount_captured") val amountCaptured : Int,
    @SerializedName("amount_refunded") val amountRefunded : Int
)