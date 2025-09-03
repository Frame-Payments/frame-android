package com.framepayments.framesdk.subscriptions
import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

object SubscriptionsAPI {
    //MARK: Methods using coroutines
    suspend fun createSubscription(request: SubscriptionRequest.CreateSubscriptionRequest): Pair<Subscription?, NetworkingError?> {
        val endpoint = SubscriptionEndpoints.CreateSubscription
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error)
    }

    suspend fun updateSubscription(subscriptionId: String, request: SubscriptionRequest.UpdateSubscriptionRequest): Pair<Subscription?, NetworkingError?> {
        val endpoint = SubscriptionEndpoints.UpdateSubscription(subscriptionId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error)
    }

    suspend fun getSubscriptionWith(subscriptionId: String): Pair<Subscription?, NetworkingError?> {
        val endpoint = SubscriptionEndpoints.GetSubscriptionWith(subscriptionId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error)
    }

    suspend fun getSubscriptions(perPage: Int?, page: Int?): Pair<SubscriptionResponses.ListSubscriptionsResponse?, NetworkingError?> {
        val endpoint = SubscriptionEndpoints.GetSubscriptions(perPage = perPage, page = page)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<SubscriptionResponses.ListSubscriptionsResponse>(data) }, error)
    }

    suspend fun searchSubscriptions(status: String?, createdBefore: Int?, createdAfter: Int?) : Pair<List<Subscription>?, NetworkingError?> {
        val endpoint = SubscriptionEndpoints.SearchSubscriptions(status = status, createdBefore = createdBefore, createdAfter = createdAfter)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<SubscriptionResponses.ListSubscriptionsResponse>(data)?.data }, error)
    }

    suspend fun cancelSubscriptionWith(subscriptionId: String): Pair<Subscription?, NetworkingError?> {
        val endpoint = SubscriptionEndpoints.CancelSubscription(subscriptionId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null))
        return Pair(data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error)
    }

    //MARK: Methods using callbacks
    fun createSubscription(request: SubscriptionRequest.CreateSubscriptionRequest, completionHandler: (Subscription?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionEndpoints.CreateSubscription

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error )
        }
    }

    fun updateSubscription(subscriptionId: String, request: SubscriptionRequest.UpdateSubscriptionRequest, completionHandler: (Subscription?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionEndpoints.UpdateSubscription(subscriptionId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error )
        }
    }

    fun getSubscriptionWith(subscriptionId: String, completionHandler: (Subscription?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionEndpoints.GetSubscriptionWith(subscriptionId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error )
        }
    }

    fun getSubscriptions(perPage: Int?, page: Int?, completionHandler: (SubscriptionResponses.ListSubscriptionsResponse?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionEndpoints.GetSubscriptions(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionResponses.ListSubscriptionsResponse>(data) }, error )
        }
    }

    fun searchSubscriptions(status: String?, createdBefore: Int?, createdAfter: Int?, completionHandler: (List<Subscription>?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionEndpoints.SearchSubscriptions(status = status, createdBefore = createdBefore, createdAfter = createdAfter)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionResponses.ListSubscriptionsResponse>(data)?.data }, error )
        }
    }

    fun cancelSubscriptionWith(subscriptionId: String, completionHandler: (Subscription?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionEndpoints.CancelSubscription(subscriptionId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null)) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error )
        }
    }
}
