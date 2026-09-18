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
    /** Canceled before completion. */
    @SerializedName("canceled") CANCELED,
    /** Disputed by the customer. */
    @SerializedName("disputed") DISPUTED,
    /** A dispute was resolved in the merchant's favor. */
    @SerializedName("disputed_won") DISPUTED_WON,
    /** A dispute was resolved in the customer's favor. */
    @SerializedName("disputed_lost") DISPUTED_LOST,
    /** Expired before it could be completed. */
    @SerializedName("expired") EXPIRED,
    /** The charge attempt failed. */
    @SerializedName("failed") FAILED,
    /** Declined by fraud screening. */
    @SerializedName("fraud_declined") FRAUD_DECLINED,
    /** Held for manual fraud review. */
    @SerializedName("fraud_review") FRAUD_REVIEW,
    /** Created but not yet confirmed. */
    @SerializedName("incomplete") INCOMPLETE,
    /** Pending processing. */
    @SerializedName("pending") PENDING,
    /** Refunded. */
    @SerializedName("refunded") REFUNDED,
    /** Needs an account or customer before it can proceed. */
    @SerializedName("requires_account_or_customer") REQUIRES_ACCOUNT_OR_CUSTOMER,
    /** Authorized and awaiting a merchant-initiated capture. */
    @SerializedName("requires_capture") REQUIRES_CAPTURE,
    /** Waiting to be confirmed. */
    @SerializedName("requires_confirmation") REQUIRES_CONFIRMATION,
    /** The cardholder must complete a 3D Secure challenge before the charge can proceed. */
    @SerializedName("requires_payment_method") REQUIRES_PAYMENT_METHOD,

    /** A 3D Secure challenge is required to authenticate the cardholder. */
    @SerializedName("requires_3d_secure") REQUIRES_THREE_D_SECURE,

    /** The authorization was reversed before capture. */
    @SerializedName("reversed") REVERSED,
    /** Successfully processed. */
    @SerializedName("succeeded") SUCCEEDED,
    /** A status this SDK version does not recognize. */
    UNKNOWN;

    /** Whether the status will not change on its own, so polling should stop. [REQUIRES_CAPTURE] counts as terminal, or authorize-only merchants appear to hang. */
    val isTerminal: Boolean
        get() = this == SUCCEEDED || this == REQUIRES_CAPTURE || this == FAILED
}

/**
 * The follow-up action required before a charge intent can settle. Present only while
 * [ChargeIntentStatus.REQUIRES_THREE_D_SECURE].
 *
 * @property type The kind of action required. Currently only `"use_frame_sdk"`.
 * @property useFrameSDK Parameters for driving a 3D Secure challenge, when [type] is `"use_frame_sdk"`.
 */
data class NextAction(
    val type: String?,
    @SerializedName("use_frame_sdk") val useFrameSDK: UseFrameSDK? = null
)

/**
 * A 3D Secure challenge session for the client to complete.
 *
 * The API serialises only these two fields — there is no server-transaction identifier,
 * despite the browser SDK's types suggesting one.
 *
 * @property source The opaque session identifier the challenge is driven from. A credential for one challenge: never log or persist it.
 * @property directoryServerName The card network's directory server for this challenge (e.g. `"visa"`).
 * @property challengeUrl The issuer challenge page to present. Absent if the API could not build one.
 */
data class UseFrameSDK(
    val source: String?,
    @SerializedName("directory_server_name") val directoryServerName: String? = null,
    @SerializedName("challenge_url") val challengeUrl: String? = null
)

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
 * @property nextAction The follow-up action required before this intent can settle, present only while [status] is [ChargeIntentStatus.REQUIRES_THREE_D_SECURE].
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
    @SerializedName("object") val intentObject: String?,
    @SerializedName("next_action") val nextAction: NextAction? = null
) {
    /** Both halves are required: the status alone can be set without the server having produced a challenge session. */
    val requiresThreeDSecureChallenge: Boolean
        get() = status == ChargeIntentStatus.REQUIRES_THREE_D_SECURE && nextAction?.useFrameSDK != null
}

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
 * @property failureCode Machine-readable code explaining a charge failure, if applicable (e.g. `"card_declined"`).
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
    @SerializedName("failure_code") val failureCode: String? = null,
    @SerializedName("failure_message") val failureMessage: String?,
    @SerializedName("payment_method_details") val paymentMethodDetails: FrameObjects.PaymentMethod?,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("charge_intent") val chargeIntent : String?,
    @SerializedName("amount_captured") val amountCaptured : Int?,
    @SerializedName("amount_refunded") val amountRefunded : Int?
)
