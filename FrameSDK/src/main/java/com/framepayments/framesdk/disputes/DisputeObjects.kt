package com.framepayments.framesdk.disputes

import com.google.gson.annotations.SerializedName

enum class DisputeReason {
    @SerializedName("duplicate") DUPLICATE,
    @SerializedName("fraudulent") FRAUDULENT,
    @SerializedName("general") GENERAL,
    @SerializedName("unrecognized") UNRECOGNIZED,
    @SerializedName("bank_cannot_process") BANK_CANNOT_PROCESS,
    @SerializedName("check_returned") CHECK_RETURNED,
    @SerializedName("credit_not_processed") CREDIT_NOT_PROCESSED,
    @SerializedName("customer_initiated") CUSTOMER_INITIATED,
    @SerializedName("debit_not_authorized") DEBIT_NOT_AUTHORIZED,
    @SerializedName("incorrect_account_details") INCORRECT_ACCOUNT_DETAILS,
    @SerializedName("insufficient_funds") INSUFFICIENT_FUNDS,
    @SerializedName("product_not_received") PRODUCT_NOT_RECEIVED,
    @SerializedName("product_unacceptable") PRODUCT_UNACCEPTABLE,
    @SerializedName("subscription_canceled") SUBSCRIPTION_CANCELED
}

enum class DisputeStatus {
    @SerializedName("won") WON,
    @SerializedName("lost") LOST,
    @SerializedName("warning_needs_response") WARNING_NEEDS_RESPONSE,
    @SerializedName("warning_under_review") WARNING_UNDER_REVIEW,
    @SerializedName("warning_closed") WARNING_CLOSED,
    @SerializedName("needs_response") NEEDS_RESPONSE,
    @SerializedName("under_review") UNDER_REVIEW
}

data class Dispute(
    val id: String,
    val amount: Int,
    val charge: String?,
    val currency: String,
    val evidence: DisputeEvidence?,
    @SerializedName("charge_intent") val chargeIntent: String?,
    val reason: DisputeReason,
    val status: DisputeStatus,
    @SerializedName("object") val disputeObject: String,
    val livemode: Boolean,
    val created: Int,
    val updated: Int
)

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
