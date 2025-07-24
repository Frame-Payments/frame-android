package com.framepayments.framesdk.subscriptions
import com.framepayments.framesdk.FrameNetworking

object SubscriptionsAPI {
    //MARK: Methods using coroutines
    suspend fun createSubscription(request: SubscriptionRequest.CreateSubscriptionRequest) : Subscription? {
        val endpoint = SubscriptionEndpoints.CreateSubscription
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            return FrameNetworking.parseResponse<Subscription>(data)
        }
        return null
    }

    suspend fun updateSubscription(subscriptionId: String, request: SubscriptionRequest.UpdateSubscriptionRequest): Subscription? {
        val endpoint = SubscriptionEndpoints.UpdateSubscription(subscriptionId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            return FrameNetworking.parseResponse<Subscription>(data)
        }
        return null
    }

    suspend fun getSubscriptionWith(subscriptionId: String): Subscription? {
        val endpoint = SubscriptionEndpoints.GetSubscriptionWith(subscriptionId)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<Subscription>(data)
        }
        return null
    }

    suspend fun getSubscriptions(perPage: Int?, page: Int?): List<Subscription>? {
        val endpoint = SubscriptionEndpoints.GetSubscriptions(perPage = perPage, page = page)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<SubscriptionResponses.ListSubscriptionsResponse>(data)?.data
        }
        return null
    }

    suspend fun searchSubscriptions(status: String?, createdBefore: Int?, createdAfter: Int?) : List<Subscription>? {
        val endpoint = SubscriptionEndpoints.SearchSubscriptions(status = status, createdBefore = createdBefore, createdAfter = createdAfter)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<SubscriptionResponses.ListSubscriptionsResponse>(data)?.data
        }
        return null
    }

    suspend fun cancelSubscriptionWith(subscriptionId: String): Subscription? {
        val endpoint = SubscriptionEndpoints.CancelSubscription(subscriptionId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, SubscriptionRequest.CancelSubscriptionRequest(description = null))

        if (data != null) {
            return FrameNetworking.parseResponse<Subscription>(data)
        }
        return null
    }

    //MARK: Methods using callbacks
    fun createSubscription(request: SubscriptionRequest.CreateSubscriptionRequest, completionHandler: (Subscription?) -> Unit) {
        val endpoint = SubscriptionEndpoints.CreateSubscription

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<Subscription>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun updateSubscription(subscriptionId: String, request: SubscriptionRequest.UpdateSubscriptionRequest, completionHandler: (Subscription?) -> Unit) {
        val endpoint = SubscriptionEndpoints.UpdateSubscription(subscriptionId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<Subscription>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun getSubscriptionWith(subscriptionId: String, completionHandler: (Subscription?) -> Unit) {
        val endpoint = SubscriptionEndpoints.GetSubscriptionWith(subscriptionId)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<Subscription>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun getSubscriptions(perPage: Int?, page: Int?, completionHandler: (List<Subscription>?) -> Unit) {
        val endpoint = SubscriptionEndpoints.GetSubscriptions(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse< SubscriptionResponses.ListSubscriptionsResponse>(data)?.data)
            } else {
                completionHandler(null)
            }
        }
    }

    fun searchSubscriptions(status: String?, createdBefore: Int?, createdAfter: Int?, completionHandler: (List<Subscription>?) -> Unit) {
        val endpoint = SubscriptionEndpoints.SearchSubscriptions(status = status, createdBefore = createdBefore, createdAfter = createdAfter)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse< SubscriptionResponses.ListSubscriptionsResponse>(data)?.data)
            } else {
                completionHandler(null)
            }
        }
    }

    fun cancelSubscriptionWith(subscriptionId: String, completionHandler: (Subscription?) -> Unit) {
        val endpoint = SubscriptionEndpoints.CancelSubscription(subscriptionId)

        FrameNetworking.performDataTaskWithRequest(endpoint, SubscriptionRequest.CancelSubscriptionRequest(description = null)) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<Subscription>(data))
            } else {
                completionHandler(null)
            }
        }
    }
}