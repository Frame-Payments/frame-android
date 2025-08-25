package com.framepayments.framesdk.subscriptionphases

import com.google.gson.annotations.SerializedName

object SubscriptionPhaseRequest {
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

    data class UpdateSubscriptionPhaseRequest(
        val ordinal: Int?,
        @SerializedName("pricing_type") val pricingType: PhasePricingType?,
        @SerializedName("duration_type") val durationType: PhaseDurationType?,
        val name: String?,
        @SerializedName("amount_cents") val amountCents: Int?,
        @SerializedName("discount_percentage") val discountPercentage: Float?,
        @SerializedName("period_count") val periodCount: Int?,
        val interval: String?,
        @SerializedName("interval_count") val intervalCount: Int?
    )

    data class BulkUpdateSubscriptionPhaseRequest(
        val phases: List<SubscriptionPhase>
    )
}