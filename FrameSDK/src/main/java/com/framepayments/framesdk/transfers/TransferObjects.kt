package com.framepayments.framesdk.transfers

import com.framepayments.framesdk.FrameObjects
import com.google.gson.annotations.SerializedName

/**
 * Represents the lifecycle status of a transfer.
 */
enum class TransferStatus {
    /** The transfer has been created and is awaiting processing. */
    @SerializedName("pending") PENDING,

    /** The transfer has been successfully processed and funds have moved. */
    @SerializedName("completed") COMPLETED,

    /** The transfer could not be completed. */
    @SerializedName("failed") FAILED,

    /** The transfer was reversed after completion. */
    @SerializedName("reversed") REVERSED,

    /** The transfer was canceled before processing. */
    @SerializedName("canceled") CANCELED,

    /** The transfer was blocked, typically due to compliance or risk controls. */
    @SerializedName("blocked") BLOCKED
}

/**
 * Represents a transfer between payment methods returned by the Frame API.
 *
 * @property id Unique identifier for this transfer.
 * @property status The current lifecycle status of this transfer.
 * @property amount Transfer amount in the smallest currency unit (e.g., cents).
 * @property currency Three-letter ISO 4217 currency code.
 * @property description Optional description provided when the transfer was created.
 * @property payout Identifier of the associated payout, if any.
 * @property metadata Arbitrary key-value pairs attached to this transfer by the merchant.
 * @property livemode Whether this transfer was created in live mode.
 * @property created Unix timestamp (seconds) when this transfer was created.
 * @property transferObject The object type string returned by the API (e.g., "transfer").
 * @property platformFee Fee retained by the merchant's platform, in the smallest currency unit.
 * @property frameFee Fee retained by Frame, in the smallest currency unit.
 * @property totalFees Sum of all fees deducted from this transfer, in the smallest currency unit.
 * @property grossAmount Total amount before fees, in the smallest currency unit.
 * @property netAmount Amount received after all fees, in the smallest currency unit.
 * @property failureReason Human-readable reason why this transfer failed, or null if not failed.
 * @property chargeIntent Identifier of the charge intent associated with this transfer, if any.
 * @property billingAgreement Identifier of the billing agreement associated with this transfer, if any.
 * @property sourcePaymentMethod The payment method funds were pulled from.
 * @property destinationPaymentMethod The payment method funds were pushed to.
 */
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
