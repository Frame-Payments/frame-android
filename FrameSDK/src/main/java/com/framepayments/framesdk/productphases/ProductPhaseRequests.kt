package com.framepayments.framesdk.productphases

import com.framepayments.framesdk.subscriptionphases.PhasePricingType
import com.framepayments.framesdk.subscriptionphases.SubscriptionPhase
import com.google.gson.annotations.SerializedName

object ProductPhaseRequest {
    data class CreateProductPhaseRequest(
        val ordinal: Int,
        val name: String?,
        @SerializedName("pricing_type") val pricingType: PhasePricingType,
        @SerializedName("amount_cents") val amountCents: Int?,
        @SerializedName("discount_percentage") val discountPercentage: Float?, // Required when pricing type is static
        @SerializedName("period_count") val periodCount: Int?
    )

    data class UpdateProductPhaseRequest(
        val ordinal: Int? = null,
        val name: String? = null,
        @SerializedName("pricing_type") val pricingType: PhasePricingType? = null,
        @SerializedName("amount_cents") val amountCents: Int? = null,
        @SerializedName("discount_percentage") val discountPercentage: Float? = null,
        @SerializedName("period_count") val periodCount: Int? = null
    )

    data class BulkUpdateProductPhaseRequest(
        val phases: List<SubscriptionPhase>
    )
}