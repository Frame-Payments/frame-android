package com.framepayments.framesdk.transfers
import com.framepayments.framesdk.FrameObjects
import com.google.gson.annotations.SerializedName

enum class TransferStatus {
    @SerializedName("pending") PENDING,
    @SerializedName("completed") COMPLETED,
    @SerializedName("failed") FAILED,
    @SerializedName("reversed") REVERSED,
    @SerializedName("canceled") CANCELED,
    @SerializedName("blocked") BLOCKED
}

data class Transfer(
    val id: String?,
    val status: TransferStatus?,
    val amount: Int?,
    val currency: String?,
    val description: String?,
    val payout: String?,
    val metadata: Map<String, String>?,
    val livemode: Boolean?,
    val created: Int?,
    @SerializedName("object") val transferObject: String?,
    @SerializedName("platform_fee") val platformFee: Int?,
    @SerializedName("frame_fee") val frameFee: Int?,
    @SerializedName("total_fees") val totalFees: Int?,
    @SerializedName("gross_amount") val grossAmount: Int?,
    @SerializedName("net_amount") val netAmount: Int?,
    @SerializedName("failure_reason") val failureReason: String?,
    @SerializedName("charge_intent") val chargeIntent: String?,
    @SerializedName("billing_agreement") val billingAgreement: String?,
    @SerializedName("source_payment_method") val sourcePaymentMethod: FrameObjects.PaymentMethod?,
    @SerializedName("destination_payment_method") val destinationPaymentMethod: FrameObjects.PaymentMethod?
)
