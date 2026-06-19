package com.framepayments.framesdk.subscriptions
import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Provides methods for creating, updating, retrieving, searching, and cancelling subscriptions.
 *
 * Each operation is available as a suspend function for coroutine-based callers
 * and as a callback-based overload for Java or non-coroutine callers.
 */
object SubscriptionsAPI {
    //MARK: Methods using coroutines

    /**
     * Creates a new subscription for a customer.
     *
     * @param request The subscription creation parameters.
     * @return A pair containing the created [Subscription] on success, or a [NetworkingError] on failure.
     */
    suspend fun createSubscription(request: SubscriptionRequest.CreateSubscriptionRequest): Pair<Subscription?, NetworkingError?> {
        val endpoint = SubscriptionEndpoints.CreateSubscription
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error)
    }

    /**
     * Updates an existing subscription with the provided fields.
     *
     * @param subscriptionId The unique ID of the subscription to update.
     * @param request The fields to update on the subscription.
     * @return A pair containing the updated [Subscription] on success, or a [NetworkingError] on failure.
     */
    suspend fun updateSubscription(subscriptionId: String, request: SubscriptionRequest.UpdateSubscriptionRequest): Pair<Subscription?, NetworkingError?> {
        val endpoint = SubscriptionEndpoints.UpdateSubscription(subscriptionId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error)
    }

    /**
     * Retrieves a single subscription by its unique identifier.
     *
     * @param subscriptionId The unique ID of the subscription to retrieve.
     * @return A pair containing the matching [Subscription] on success, or a [NetworkingError] on failure.
     */
    suspend fun getSubscriptionWith(subscriptionId: String): Pair<Subscription?, NetworkingError?> {
        val endpoint = SubscriptionEndpoints.GetSubscriptionWith(subscriptionId)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error)
    }

    /**
     * Retrieves a paginated list of all subscriptions.
     *
     * @param perPage Optional number of results to return per page.
     * @param page Optional page number to retrieve.
     * @return A pair containing a [SubscriptionResponses.ListSubscriptionsResponse] on success, or a [NetworkingError] on failure.
     */
    suspend fun getSubscriptions(perPage: Int?, page: Int?): Pair<SubscriptionResponses.ListSubscriptionsResponse?, NetworkingError?> {
        val endpoint = SubscriptionEndpoints.GetSubscriptions(perPage = perPage, page = page)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<SubscriptionResponses.ListSubscriptionsResponse>(data) }, error)
    }

    /**
     * Searches for subscriptions matching the given filter criteria.
     *
     * @param status Optional subscription status to filter by (e.g., "active", "cancelled").
     * @param createdBefore Optional Unix timestamp; returns subscriptions created before this time.
     * @param createdAfter Optional Unix timestamp; returns subscriptions created after this time.
     * @return A pair containing a list of matching [Subscription] objects on success, or a [NetworkingError] on failure.
     */
    suspend fun searchSubscriptions(status: String?, createdBefore: Int?, createdAfter: Int?) : Pair<List<Subscription>?, NetworkingError?> {
        val endpoint = SubscriptionEndpoints.SearchSubscriptions(status = status, createdBefore = createdBefore, createdAfter = createdAfter)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<SubscriptionResponses.ListSubscriptionsResponse>(data)?.data }, error)
    }

    /**
     * Cancels an active subscription immediately.
     *
     * @param subscriptionId The unique ID of the subscription to cancel.
     * @return A pair containing the cancelled [Subscription] on success, or a [NetworkingError] on failure.
     */
    suspend fun cancelSubscriptionWith(subscriptionId: String): Pair<Subscription?, NetworkingError?> {
        val endpoint = SubscriptionEndpoints.CancelSubscription(subscriptionId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null), FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error)
    }

    //MARK: Methods using callbacks

    /**
     * Creates a new subscription for a customer and delivers the result via callback.
     *
     * @param request The subscription creation parameters.
     * @param completionHandler Invoked with the created [Subscription] on success, or a [NetworkingError] on failure.
     */
    fun createSubscription(request: SubscriptionRequest.CreateSubscriptionRequest, completionHandler: (Subscription?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionEndpoints.CreateSubscription

        FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error )
        }
    }

    /**
     * Updates an existing subscription and delivers the result via callback.
     *
     * @param subscriptionId The unique ID of the subscription to update.
     * @param request The fields to update on the subscription.
     * @param completionHandler Invoked with the updated [Subscription] on success, or a [NetworkingError] on failure.
     */
    fun updateSubscription(subscriptionId: String, request: SubscriptionRequest.UpdateSubscriptionRequest, completionHandler: (Subscription?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionEndpoints.UpdateSubscription(subscriptionId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error )
        }
    }

    /**
     * Retrieves a single subscription by its unique identifier and delivers the result via callback.
     *
     * @param subscriptionId The unique ID of the subscription to retrieve.
     * @param completionHandler Invoked with the matching [Subscription] on success, or a [NetworkingError] on failure.
     */
    fun getSubscriptionWith(subscriptionId: String, completionHandler: (Subscription?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionEndpoints.GetSubscriptionWith(subscriptionId)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error )
        }
    }

    /**
     * Retrieves a paginated list of all subscriptions and delivers the result via callback.
     *
     * @param perPage Optional number of results to return per page.
     * @param page Optional page number to retrieve.
     * @param completionHandler Invoked with a [SubscriptionResponses.ListSubscriptionsResponse] on success, or a [NetworkingError] on failure.
     */
    fun getSubscriptions(perPage: Int?, page: Int?, completionHandler: (SubscriptionResponses.ListSubscriptionsResponse?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionEndpoints.GetSubscriptions(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionResponses.ListSubscriptionsResponse>(data) }, error )
        }
    }

    /**
     * Searches for subscriptions matching the given filter criteria and delivers the result via callback.
     *
     * @param status Optional subscription status to filter by (e.g., "active", "cancelled").
     * @param createdBefore Optional Unix timestamp; returns subscriptions created before this time.
     * @param createdAfter Optional Unix timestamp; returns subscriptions created after this time.
     * @param completionHandler Invoked with a list of matching [Subscription] objects on success, or a [NetworkingError] on failure.
     */
    fun searchSubscriptions(status: String?, createdBefore: Int?, createdAfter: Int?, completionHandler: (List<Subscription>?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionEndpoints.SearchSubscriptions(status = status, createdBefore = createdBefore, createdAfter = createdAfter)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionResponses.ListSubscriptionsResponse>(data)?.data }, error )
        }
    }

    /**
     * Cancels an active subscription immediately and delivers the result via callback.
     *
     * @param subscriptionId The unique ID of the subscription to cancel.
     * @param completionHandler Invoked with the cancelled [Subscription] on success, or a [NetworkingError] on failure.
     */
    fun cancelSubscriptionWith(subscriptionId: String, completionHandler: (Subscription?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionEndpoints.CancelSubscription(subscriptionId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null), FrameAuthMode.Secret) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<Subscription>(data) }, error )
        }
    }
}
