package com.framepayments.framesdk.subscriptionphases
import com.google.gson.annotations.SerializedName

data class SubscriptionPhase (
    val id: String,
    val ordinal: Int,
    val name: String?,
    @SerializedName("pricing_type") val pricingType: PhasePricingType,
    @SerializedName("duration_type") val durationType: PhaseDurationType,
    val amount: Int?,
    val currency: String,
    @SerializedName("discount_percentage") val discountPercentage: Float?,
    @SerializedName("period_count") val periodCount: Int?,
    val interval: String?,
    @SerializedName("interval_count") val intervalCount: Int?,
    val livemode: Boolean,
    val created: Int,
    val updated: Int,
    @SerializedName("object") val phaseObject: String
)

enum class PhasePricingType {
    relative,
    static
}

enum class PhaseDurationType {
    finite,
    infinite
}