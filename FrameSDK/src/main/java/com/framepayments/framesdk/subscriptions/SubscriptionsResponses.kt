package com.framepayments.framesdk.subscriptions
import com.framepayments.framesdk.FrameMetadata

/**
 * Namespace for response models returned by the Subscriptions API.
 */
object SubscriptionResponses {

    /**
     * Paginated response returned when listing subscriptions.
     *
     * @property meta Pagination metadata for the result set, or null if unavailable.
     * @property data List of [Subscription] objects for the current page, or null if unavailable.
     */
    data class ListSubscriptionsResponse (
        val meta: FrameMetadata?,
        val data: List<Subscription>?
    )
}
