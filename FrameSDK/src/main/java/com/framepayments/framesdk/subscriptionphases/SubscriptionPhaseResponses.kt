package com.framepayments.framesdk.subscriptionphases

import com.google.gson.annotations.SerializedName

data class ListSubscriptionPhaseResponse(
    val phases: List<SubscriptionPhase>?,
    val meta: MetaSubscriptionPhaseResponse?
)

data class MetaSubscriptionPhaseResponse(
    @SerializedName("subscription_id") val subscriptionId: String
)