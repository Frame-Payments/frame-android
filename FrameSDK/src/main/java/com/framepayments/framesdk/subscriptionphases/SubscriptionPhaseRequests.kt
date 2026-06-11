package com.framepayments.framesdk.subscriptionphases

import com.google.gson.annotations.SerializedName

/**
 * Namespace for request bodies used with the subscription phases API.
 */
object SubscriptionPhaseRequest {

    /**
     * Request body for creating a new subscription phase.
     *
     * @property ordinal Position of this phase in the subscription's phase sequence.
     * @property pricingType Pricing strategy for the phase.
     * @property durationType Whether the phase is finite or infinite.
     * @property name Human-readable label for the phase.
     * @property amountCents Billed amount in the smallest currency unit (e.g. cents).
     * @property discountPercentage Percentage discount to apply to the phase amount.
     * @property periodCount Number of billing periods the phase spans; required when [durationType] is [PhaseDurationType.FINITE].
     * @property interval Billing interval unit (e.g. "month", "year").
     * @property intervalCount Number of interval units between each billing cycle.
     */
    data class CreateSubscriptionPhaseRequest(
        val ordinal: Int,
        @SerializedName("pricing_type") val pricingType: PhasePricingType,
        @SerializedName("duration_type") val durationType: PhaseDurationType,
        val name: String?,
        @SerializedName("amount_cents") val amountCents: Int?,
        @SerializedName("discount_percentage") val discountPercentage: Float?,
        @SerializedName("period_count") val periodCount: Int?,
        val interval: String?,
        @SerializedName("interval_count") val intervalCount: Int?
    )

    /**
     * Request body for partially updating an existing subscription phase.
     *
     * Only non-null fields are sent to the API; omitted fields remain unchanged.
     *
     * @property ordinal Updated position of the phase in the subscription's phase sequence.
     * @property pricingType Updated pricing strategy for the phase.
     * @property durationType Updated duration type for the phase.
     * @property name Updated human-readable label for the phase.
     * @property amountCents Updated billed amount in the smallest currency unit.
     * @property discountPercentage Updated percentage discount for the phase.
     * @property periodCount Updated number of billing periods for a finite phase.
     * @property interval Updated billing interval unit.
     * @property intervalCount Updated number of interval units between billing cycles.
     */
    data class UpdateSubscriptionPhaseRequest(
        val ordinal: Int? = null,
        @SerializedName("pricing_type") val pricingType: PhasePricingType? = null,
        @SerializedName("duration_type") val durationType: PhaseDurationType? = null,
        val name: String? = null,
        @SerializedName("amount_cents") val amountCents: Int? = null,
        @SerializedName("discount_percentage") val discountPercentage: Float? = null,
        @SerializedName("period_count") val periodCount: Int? = null,
        val interval: String? = null,
        @SerializedName("interval_count") val intervalCount: Int? = null
    )

    /**
     * Request body for replacing all phases on a subscription in a single call.
     *
     * @property phases The complete ordered list of phases to apply to the subscription.
     */
    data class BulkUpdateSubscriptionPhaseRequest(
        val phases: List<SubscriptionPhase>
    )
}
