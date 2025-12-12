package com.framepayments.framesdk.productphases

import com.framepayments.framesdk.subscriptionphases.SubscriptionPhase
import com.google.gson.annotations.SerializedName

data class ListProductPhaseResponse(
    val phases: List<SubscriptionPhase>?,
    val meta: MetaProductPhaseResponse?
)

data class MetaProductPhaseResponse(
    @SerializedName("product_id") val productId: String,
    @SerializedName("updated_count") val updatedCount: Int
)