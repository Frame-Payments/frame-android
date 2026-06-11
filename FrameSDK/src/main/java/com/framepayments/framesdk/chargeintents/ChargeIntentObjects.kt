package com.framepayments.framesdk.chargeintents
import com.framepayments.framesdk.FrameObjects
import com.google.gson.annotations.SerializedName

/**
 * Controls when the authorized funds are captured after a charge intent is confirmed.
 */
enum class AuthorizationMode {
    /** Captures funds immediately when the charge intent is confirmed. */
    @SerializedName("automatic") AUTOMATIC,

    /** Holds the authorized amount for later manual capture. */
    @SerializedName("manual") MANUAL
}

/**
 * Represents the lifecycle state of a charge intent.
 */
enum class ChargeIntentStatus {
    /** The charge intent was canceled before completion. */
    @SerializedName("canceled") CANCELED,

    /** The charge has been disputed by the customer. */
    @SerializedName("disputed") DISPUTED,

    /** The payment attempt failed. */
    @SerializedName("failed") FAILED,

    /** The charge intent has been created but not yet confirmed. */
    @SerializedName("incomplete") INCOMPLETE,

    /** The charge intent is awaiting processing. */
    @SerializedName("pending") PENDING,

    /** The charge has been fully refunded. */
    @SerializedName("refunded") REFUNDED,

    /** The authorization was reversed before capture. */
    @SerializedName("reversed") REVERSED,

    /** The charge was successfully completed. */
    @SerializedName("succeeded") SUCCEEDED
}

/**
 * Represents a charge intent returned by the Frame API.
 *
 * @property id Unique identifier for the charge intent.
 * @property currency Three-letter ISO 4217 currency code.
 * @property customer The customer associated with this charge intent.
 * @property shipping The shipping address for the order.
 * @property status Current lifecycle status of the charge intent.
 * @property description Merchant-supplied description of the charge.
 * @property amount Total amount in the smallest currency unit (e.g., cents).
 * @property created Unix timestamp of when the charge intent was created.
 * @property updated Unix timestamp of when the charge intent was last updated.
 * @property livemode `true` if this charge intent was created in live mode; `false` for test mode.
 * @property latestCharge The most recent charge attempt associated with this intent.
 * @property paymentMethod The payment method attached to this charge intent.
 * @property authorizationMode Whether funds are captured automatically or held for manual capture.
 * @property failureDescription Human-readable explanation of why the charge intent failed, if applicable.
 * @property intentObject The object type identifier returned by the API (typically `"charge_intent"`).
 */
data class ChargeIntent(
    val id: String?,
    val currency: String?,
    val customer: FrameObjects.Customer?,
    val shipping: FrameObjects.BillingAddress?,
    val status: ChargeIntentStatus?,
    val description: String?,
    val amount: Int?,
    val created: Int?,
    val updated: Int?,
    val livemode: Boolean?,
    @SerializedName("latest_charge") val latestCharge: LatestCharge?,
    @SerializedName("payment_method") val paymentMethod: FrameObjects.PaymentMethod?,
    @SerializedName("authorization_mode")val authorizationMode: AuthorizationMode?,
    @SerializedName("failure_description")val failureDescription: String?,
    @SerializedName("object") val intentObject: String?
)

/**
 * Represents the most recent charge attempt made against a charge intent.
 *
 * @property id Unique identifier for the charge.
 * @property currency Three-letter ISO 4217 currency code.
 * @property created Unix timestamp of when the charge was created.
 * @property updated Unix timestamp of when the charge was last updated.
 * @property livemode `true` if this charge was created in live mode; `false` for test mode.
 * @property captured `true` if the authorized funds have been captured.
 * @property disputed `true` if the customer has opened a dispute on this charge.
 * @property refunded `true` if the charge has been refunded.
 * @property description Merchant-supplied description of the charge.
 * @property status Current lifecycle status of the charge.
 * @property customer ID of the customer associated with this charge.
 * @property amount Total charge amount in the smallest currency unit (e.g., cents).
 * @property failureMessage Human-readable explanation of why the charge failed, if applicable.
 * @property paymentMethodDetails Full details of the payment method used for this charge.
 * @property paymentMethod ID of the payment method used for this charge.
 * @property chargeIntent ID of the parent charge intent.
 * @property amountCaptured Amount that has been captured, in the smallest currency unit.
 * @property amountRefunded Amount that has been refunded, in the smallest currency unit.
 */
data class LatestCharge (
    val id: String?,
    val currency : String?,
    val created : Int?,
    val updated : Int?,
    val livemode : Boolean?,
    val captured : Boolean?,
    val disputed : Boolean?,
    val refunded : Boolean?,
    val description: String?,
    val status: ChargeIntentStatus?,
    val customer: String?,
    val amount: Int?,
    @SerializedName("failure_message") val failureMessage: String?,
    @SerializedName("payment_method_details") val paymentMethodDetails: FrameObjects.PaymentMethod?,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("charge_intent") val chargeIntent : String?,
    @SerializedName("amount_captured") val amountCaptured : Int?,
    @SerializedName("amount_refunded") val amountRefunded : Int?
)
