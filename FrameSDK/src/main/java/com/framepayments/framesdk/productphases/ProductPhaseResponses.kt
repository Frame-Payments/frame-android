package com.framepayments.framesdk.productphases

import com.framepayments.framesdk.subscriptionphases.SubscriptionPhase
import com.google.gson.annotations.SerializedName

/**
 * Response returned when retrieving or bulk-updating the phases of a product.
 *
 * @property phases The list of [SubscriptionPhase] objects associated with the product.
 * @property meta Metadata describing the result, such as the owning product and update count.
 */
data class ListProductPhaseResponse(
    val phases: List<SubscriptionPhase>?,
    val meta: MetaProductPhaseResponse?
)

/**
 * Metadata included in a product phase list or bulk-update response.
 *
 * @property productId The unique identifier of the product that owns the returned phases.
 * @property updatedCount The number of phases that were modified during a bulk-update operation.
 */
data class MetaProductPhaseResponse(
    @SerializedName("product_id") val productId: String?,
    @SerializedName("updated_count") val updatedCount: Int?
)
