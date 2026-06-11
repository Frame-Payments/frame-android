package com.framepayments.framesdk.refunds
import com.google.gson.annotations.SerializedName

/**
 * Represents a refund issued against a charge or charge intent.
 *
 * @property id Unique identifier for the refund.
 * @property charge ID of the charge this refund is applied to.
 * @property currency Three-letter ISO currency code for the refund amount.
 * @property description Optional description provided when the refund was created.
 * @property status Current status of the refund (e.g., "succeeded", "pending", "failed").
 * @property created Unix timestamp of when the refund was created.
 * @property updated Unix timestamp of when the refund was last updated.
 * @property amountCaptured Amount that was captured on the original charge, in the smallest currency unit.
 * @property amountRefunded Amount refunded, in the smallest currency unit.
 * @property chargeIntent ID of the charge intent associated with this refund.
 * @property failureReason Reason for refund failure, if applicable.
 * @property refundObject String identifier for the object type, always "refund".
 */
data class Refund(
    val id: String?,
    val charge: String?,
    val currency: String?,
    val description: String?,
    val status: String?,
    val created: Int?,
    val updated: Int?,
    @SerializedName("amount_captured") val amountCaptured: Int?,
    @SerializedName("amount_refunded") val amountRefunded: Int?,
    @SerializedName("charge_intent") val chargeIntent: String?,
    @SerializedName("failure_reason") val failureReason: String?,
    @SerializedName("object") val refundObject: String?,
)
