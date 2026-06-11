package com.framepayments.framesdk.subscriptions
import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines the API endpoints available for subscription operations.
 *
 * Each case maps to a specific URL path and HTTP method used by [FrameNetworking].
 */
sealed class SubscriptionEndpoints: FrameNetworkingEndpoints {

    /** Endpoint for creating a new subscription via POST to `/v1/subscriptions`. */
    object CreateSubscription : SubscriptionEndpoints()

    /**
     * Endpoint for updating an existing subscription via PATCH to `/v1/subscriptions/{subscriptionId}`.
     *
     * @property subscriptionId The unique ID of the subscription to update.
     */
    data class UpdateSubscription(val subscriptionId: String) : SubscriptionEndpoints()

    /**
     * Endpoint for retrieving a paginated list of subscriptions via GET to `/v1/subscriptions`.
     *
     * @property perPage Optional number of results per page.
     * @property page Optional page number to retrieve.
     */
    data class GetSubscriptions(val perPage: Int? = null, val page: Int? = null)  : SubscriptionEndpoints()

    /**
     * Endpoint for retrieving a single subscription via GET to `/v1/subscriptions/{subscriptionId}`.
     *
     * @property subscriptionId The unique ID of the subscription to retrieve.
     */
    data class GetSubscriptionWith(val subscriptionId: String)  : SubscriptionEndpoints()

    /**
     * Endpoint for searching subscriptions via GET to `/v1/subscriptions/search`.
     *
     * @property status Optional subscription status to filter by (e.g., "active", "cancelled").
     * @property createdBefore Optional Unix timestamp; returns subscriptions created before this time.
     * @property createdAfter Optional Unix timestamp; returns subscriptions created after this time.
     */
    data class SearchSubscriptions(val status: String? = null, val createdBefore: Int? = null, val createdAfter: Int? = null) : SubscriptionEndpoints()

    /**
     * Endpoint for cancelling a subscription via POST to `/v1/subscriptions/{subscriptionId}/cancel`.
     *
     * @property subscriptionId The unique ID of the subscription to cancel.
     */
    data class CancelSubscription(val subscriptionId: String)  : SubscriptionEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is CreateSubscription, is GetSubscriptions ->
                "/v1/subscriptions"
            is GetSubscriptionWith ->
                "/v1/subscriptions/${this.subscriptionId}"
            is UpdateSubscription ->
                "/v1/subscriptions/${this.subscriptionId}"
            is SearchSubscriptions ->
                "/v1/subscriptions/search"
            is CancelSubscription ->
                "/v1/subscriptions/${this.subscriptionId}/cancel"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateSubscription, is CancelSubscription -> "POST"
            is UpdateSubscription -> "PATCH"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>?
        get() = when (this) {
            is GetSubscriptions -> {
                val items = mutableListOf<QueryItem>()
                perPage?.let { items.add(QueryItem("per_page", it.toString())) }
                page?.let { items.add(QueryItem("page", it.toString())) }
                items
            }
            is SearchSubscriptions -> {
                val items = mutableListOf<QueryItem>()
                status?.let { items.add(QueryItem("status", it.toString())) }
                createdBefore?.let { items.add(QueryItem("created_before", it.toString()))}
                createdAfter?.let { items.add(QueryItem("created_after", it.toString()))}
                items
            }
            else -> null
        }
}
