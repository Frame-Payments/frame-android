package com.framepayments.framesdk.disputes

import com.google.gson.annotations.SerializedName

/**
 * Identifies the reason a customer filed a dispute against a charge.
 */
enum class DisputeReason {
    /** The customer claims the charge is a duplicate of another transaction. */
    @SerializedName("duplicate") DUPLICATE,

    /** The customer claims the transaction was unauthorized or fraudulent. */
    @SerializedName("fraudulent") FRAUDULENT,

    /** A general dispute reason that does not fit a more specific category. */
    @SerializedName("general") GENERAL,

    /** The customer does not recognize the charge. */
    @SerializedName("unrecognized") UNRECOGNIZED,

    /** The customer's bank is unable to process the transaction. */
    @SerializedName("bank_cannot_process") BANK_CANNOT_PROCESS,

    /** The customer's check was returned unpaid. */
    @SerializedName("check_returned") CHECK_RETURNED,

    /** A credit owed to the customer was not processed. */
    @SerializedName("credit_not_processed") CREDIT_NOT_PROCESSED,

    /** The customer voluntarily initiated the dispute. */
    @SerializedName("customer_initiated") CUSTOMER_INITIATED,

    /** The customer claims a debit was not authorized. */
    @SerializedName("debit_not_authorized") DEBIT_NOT_AUTHORIZED,

    /** The account details used for the charge were incorrect. */
    @SerializedName("incorrect_account_details") INCORRECT_ACCOUNT_DETAILS,

    /** The customer's account had insufficient funds to cover the charge. */
    @SerializedName("insufficient_funds") INSUFFICIENT_FUNDS,

    /** The customer claims the product or service was never received. */
    @SerializedName("product_not_received") PRODUCT_NOT_RECEIVED,

    /** The customer claims the product or service was unacceptable or not as described. */
    @SerializedName("product_unacceptable") PRODUCT_UNACCEPTABLE,

    /** The customer claims a subscription was canceled before the charge was made. */
    @SerializedName("subscription_canceled") SUBSCRIPTION_CANCELED
}

/**
 * Represents the current lifecycle status of a dispute.
 */
enum class DisputeStatus {
    /** The dispute was resolved in the merchant's favor. */
    @SerializedName("won") WON,

    /** The dispute was resolved in the customer's favor. */
    @SerializedName("lost") LOST,

    /** An early-warning dispute is awaiting a response from the merchant. */
    @SerializedName("warning_needs_response") WARNING_NEEDS_RESPONSE,

    /** An early-warning dispute is currently under review. */
    @SerializedName("warning_under_review") WARNING_UNDER_REVIEW,

    /** An early-warning dispute has been closed. */
    @SerializedName("warning_closed") WARNING_CLOSED,

    /** The dispute requires a response from the merchant. */
    @SerializedName("needs_response") NEEDS_RESPONSE,

    /** The dispute is currently under review by the card network or issuing bank. */
    @SerializedName("under_review") UNDER_REVIEW
}

/**
 * Represents a dispute raised against a charge by a customer.
 *
 * @property id Unique identifier for the dispute.
 * @property amount Disputed amount in the smallest currency unit (e.g. cents).
 * @property charge Identifier of the charge associated with this dispute.
 * @property currency Three-letter ISO currency code for the disputed amount.
 * @property evidence Evidence submitted in support of the merchant's position.
 * @property chargeIntent Identifier of the charge intent associated with this dispute.
 * @property reason The reason the customer provided for the dispute.
 * @property status The current lifecycle status of the dispute.
 * @property disputeObject The object type string returned by the API.
 * @property livemode `true` if the dispute was created in live mode; `false` for test mode.
 * @property created Unix timestamp indicating when the dispute was created.
 * @property updated Unix timestamp indicating when the dispute was last updated.
 */
data class Dispute(
    val id: String?,
    val amount: Int?,
    val charge: String?,
    val currency: String?,
    val evidence: DisputeEvidence?,
    @SerializedName("charge_intent") val chargeIntent: String?,
    val reason: DisputeReason?,
    val status: DisputeStatus?,
    @SerializedName("object") val disputeObject: String?,
    val livemode: Boolean?,
    val created: Int?,
    val updated: Int?
)

/**
 * Contains the evidence fields a merchant can submit to contest a dispute.
 *
 * @property evidenceAccessActivityLog Log of customer access activity relevant to the dispute.
 * @property evidenceBillingAddress Billing address associated with the customer's payment method.
 * @property evidenceCancellationPolicy The merchant's cancellation policy as provided to the customer.
 * @property evidenceCancellationPolicyDisclosure Explanation of how the cancellation policy was disclosed to the customer.
 * @property evidenceCancellationRebuttal Rebuttal to the customer's claim that the cancellation policy was not honored.
 * @property evidenceCustomerEmailAddress Email address of the customer who made the purchase.
 * @property evidenceCustomerName Full name of the customer who made the purchase.
 * @property evidenceCustomerPurchaseIP IP address of the customer at the time of purchase.
 * @property evidenceDuplicateChargeExplanation Explanation clarifying why an apparently duplicate charge is valid.
 * @property evidenceDuplicateChargeId Identifier of the charge that the customer claims is a duplicate.
 * @property evidenceProductDescription Description of the product or service that was provided to the customer.
 * @property evidenceRefundPolicyDisclosure Explanation of how the refund policy was disclosed to the customer.
 * @property evidenceShippingTrackingNumber Tracking number for the shipment delivered to the customer.
 * @property evidenceUncategorizedText Any additional evidence text that does not fit a specific category.
 */
data class DisputeEvidence(
    @SerializedName("access_activity_log") val evidenceAccessActivityLog: String? = null,
    @SerializedName("billing_address") val evidenceBillingAddress: String? = null,
    @SerializedName("cancellation_policy") val evidenceCancellationPolicy: String? = null,
    @SerializedName("cancellation_policy_disclosure") val evidenceCancellationPolicyDisclosure: String? = null,
    @SerializedName("cancellation_rebuttal") val evidenceCancellationRebuttal: String? = null,
    @SerializedName("customer_email_address") val evidenceCustomerEmailAddress: String? = null,
    @SerializedName("customer_name") val evidenceCustomerName: String? = null,
    @SerializedName("customer_purchase_ip") val evidenceCustomerPurchaseIP: String? = null,
    @SerializedName("duplicate_charge_explanation") val evidenceDuplicateChargeExplanation: String? = null,
    @SerializedName("duplicate_charge_id") val evidenceDuplicateChargeId: String? = null,
    @SerializedName("product_description") val evidenceProductDescription: String? = null,
    @SerializedName("refund_policy_disclosure") val evidenceRefundPolicyDisclosure: String? = null,
    @SerializedName("shipping_tracking_number") val evidenceShippingTrackingNumber: String? = null,
    @SerializedName("uncategorized_text") val evidenceUncategorizedText: String? = null
)
