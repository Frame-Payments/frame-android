package com.framepayments.framesdk.subscriptions
import com.framepayments.framesdk.FrameMetadata

object SubscriptionResponses {
    data class ListSubscriptionsResponse (
        val meta: FrameMetadata,
        val data: List<Subscription>?
    )
}