package com.framepayments.framesdk.subscriptions
import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class SubscriptionEndpoints: FrameNetworkingEndpoints {
    object CreateSubscription : SubscriptionEndpoints()
    data class UpdateSubscription(val subscriptionId: String) : SubscriptionEndpoints()
    data class GetSubscriptions(val perPage: Int? = null, val page: Int? = null)  : SubscriptionEndpoints()
    data class GetSubscriptionWith(val subscriptionId: String)  : SubscriptionEndpoints()
    data class SearchSubscriptions(val status: String? = null, val createdBefore: Int? = null, val createdAfter: Int? = null) : SubscriptionEndpoints()
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