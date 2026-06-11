package com.framepayments.framesdk.subscriptionphases
import com.google.gson.annotations.SerializedName

/**
 * Represents a single billing phase within a subscription.
 *
 * @property id Unique identifier for the phase.
 * @property ordinal Position of this phase in the subscription's phase sequence.
 * @property name Human-readable label for the phase.
 * @property pricingType Determines whether the phase price is [PhasePricingType.RELATIVE] or [PhasePricingType.STATIC].
 * @property durationType Determines whether the phase runs for a fixed period ([PhaseDurationType.FINITE]) or indefinitely ([PhaseDurationType.INFINITE]).
 * @property amount Billed amount for the phase in the smallest currency unit.
 * @property currency ISO 4217 currency code for the phase amount.
 * @property discountPercentage Percentage discount applied to the phase amount, if any.
 * @property periodCount Number of billing periods the phase spans when [durationType] is [PhaseDurationType.FINITE].
 * @property interval Billing interval unit (e.g. "month", "year").
 * @property intervalCount Number of interval units between each billing cycle.
 * @property livemode True when the phase exists in live mode; false in test mode.
 * @property created Unix timestamp of when the phase was created.
 * @property updated Unix timestamp of when the phase was last updated.
 * @property phaseObject API object type identifier, typically "subscription_phase".
 */
data class SubscriptionPhase (
    val id: String?,
    val ordinal: Int?,
    val name: String?,
    @SerializedName("pricing_type") val pricingType: PhasePricingType?,
    @SerializedName("duration_type") val durationType: PhaseDurationType?,
    val amount: Int?,
    val currency: String?,
    @SerializedName("discount_percentage") val discountPercentage: Float?,
    @SerializedName("period_count") val periodCount: Int?,
    val interval: String?,
    @SerializedName("interval_count") val intervalCount: Int?,
    val livemode: Boolean?,
    val created: Int?,
    val updated: Int?,
    @SerializedName("object") val phaseObject: String?
)

/**
 * Specifies how the price of a subscription phase is calculated.
 */
enum class PhasePricingType {
    /** Price is calculated relative to the base plan price. */
    @SerializedName("relative") RELATIVE,

    /** Price is a fixed amount regardless of the base plan. */
    @SerializedName("static") STATIC
}

/**
 * Specifies how long a subscription phase runs.
 */
enum class PhaseDurationType {
    /** Phase ends after a defined number of billing periods. */
    @SerializedName("finite") FINITE,

    /** Phase continues indefinitely until cancelled or superseded. */
    @SerializedName("infinite") INFINITE
}
