package com.framepayments.framesdk.productphases

import com.framepayments.framesdk.subscriptionphases.PhasePricingType
import com.framepayments.framesdk.subscriptionphases.SubscriptionPhase
import com.google.gson.annotations.SerializedName

/**
 * Contains request body models for the Product Phases API.
 */
object ProductPhaseRequest {

    /**
     * Request body for creating a new phase on a product.
     *
     * @property ordinal The zero-based position of this phase in the product's phase sequence.
     * @property name Optional display name for the phase.
     * @property pricingType The pricing strategy applied during this phase.
     * @property amountCents The fixed price for this phase in the smallest currency unit (e.g. cents). Used when [pricingType] is not discount-based.
     * @property discountPercentage The percentage discount applied during this phase. Required when [pricingType] is [PhasePricingType.STATIC].
     * @property periodCount The number of billing periods this phase lasts before advancing to the next phase.
     */
    data class CreateProductPhaseRequest(
        val ordinal: Int,
        val name: String?,
        @SerializedName("pricing_type") val pricingType: PhasePricingType,
        @SerializedName("amount_cents") val amountCents: Int?,
        @SerializedName("discount_percentage") val discountPercentage: Float?, // Required when pricing type is static
        @SerializedName("period_count") val periodCount: Int?
    )

    /**
     * Request body for updating an existing phase on a product.
     *
     * All fields are optional; only non-null fields are applied to the phase.
     *
     * @property ordinal Updated zero-based position of this phase in the product's phase sequence.
     * @property name Updated display name for the phase.
     * @property pricingType Updated pricing strategy applied during this phase.
     * @property amountCents Updated fixed price for this phase in the smallest currency unit (e.g. cents).
     * @property discountPercentage Updated percentage discount applied during this phase.
     * @property periodCount Updated number of billing periods this phase lasts.
     */
    data class UpdateProductPhaseRequest(
        val ordinal: Int? = null,
        val name: String? = null,
        @SerializedName("pricing_type") val pricingType: PhasePricingType? = null,
        @SerializedName("amount_cents") val amountCents: Int? = null,
        @SerializedName("discount_percentage") val discountPercentage: Float? = null,
        @SerializedName("period_count") val periodCount: Int? = null
    )

    /**
     * Request body for replacing all phases on a product in a single operation.
     *
     * @property phases The complete ordered list of [SubscriptionPhase] objects to apply to the product.
     */
    data class BulkUpdateProductPhaseRequest(
        val phases: List<SubscriptionPhase>
    )
}
