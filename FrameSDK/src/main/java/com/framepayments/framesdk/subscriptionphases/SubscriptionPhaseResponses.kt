package com.framepayments.framesdk.subscriptionphases

import com.google.gson.annotations.SerializedName

/**
 * Response returned when listing all phases for a subscription.
 *
 * @property phases The list of [SubscriptionPhase] objects belonging to the subscription.
 * @property meta Metadata associated with the response, including the parent subscription ID.
 */
data class ListSubscriptionPhaseResponse(
    val phases: List<SubscriptionPhase>?,
    val meta: MetaSubscriptionPhaseResponse?
)

/**
 * Metadata included in a list subscription phases response.
 *
 * @property subscriptionId The ID of the subscription that owns the returned phases.
 */
data class MetaSubscriptionPhaseResponse(
    @SerializedName("subscription_id") val subscriptionId: String?
)
