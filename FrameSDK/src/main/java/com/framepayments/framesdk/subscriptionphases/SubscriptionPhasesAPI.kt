package com.framepayments.framesdk.subscriptionphases

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Provides suspend and callback-based operations for managing subscription phases.
 */
object SubscriptionPhasesAPI {
    //MARK: Methods using coroutines

    /**
     * Creates a new phase for the specified subscription.
     *
     * @param subscriptionId The ID of the subscription to add the phase to.
     * @param request The details of the phase to create.
     * @return A pair of the created [SubscriptionPhase] and any [NetworkingError] that occurred.
     */
    suspend fun createSubscriptionPhase(subscriptionId: String, request: SubscriptionPhaseRequest.CreateSubscriptionPhaseRequest): Pair<SubscriptionPhase?, NetworkingError?> {
        val endpoint = SubscriptionPhaseEndpoints.CreateSubscriptionPhase(subscriptionId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(FrameNetworking.parseResponse<SubscriptionPhase>(data), error)
    }

    /**
     * Updates an existing phase on the specified subscription.
     *
     * @param subscriptionId The ID of the subscription that owns the phase.
     * @param phaseId The ID of the phase to update.
     * @param request The fields to update on the phase.
     * @return A pair of the updated [SubscriptionPhase] and any [NetworkingError] that occurred.
     */
    suspend fun updateSubscriptionPhase(subscriptionId: String, phaseId: String, request: SubscriptionPhaseRequest.UpdateSubscriptionPhaseRequest): Pair<SubscriptionPhase?, NetworkingError?> {
        val endpoint = SubscriptionPhaseEndpoints.UpdateSubscriptionPhase(subscriptionId, phaseId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(FrameNetworking.parseResponse<SubscriptionPhase>(data), error)
    }

    /**
     * Fetches a single phase by ID from the specified subscription.
     *
     * @param subscriptionId The ID of the subscription that owns the phase.
     * @param phaseId The ID of the phase to retrieve.
     * @return A pair of the matching [SubscriptionPhase] and any [NetworkingError] that occurred.
     */
    suspend fun getSubscriptionPhaseWith(subscriptionId: String, phaseId: String): Pair<SubscriptionPhase?, NetworkingError?> {
        val endpoint = SubscriptionPhaseEndpoints.GetSubscriptionPhaseWith(subscriptionId, phaseId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(FrameNetworking.parseResponse<SubscriptionPhase>(data), error)
    }

    /**
     * Fetches all phases for the specified subscription.
     *
     * @param subscriptionId The ID of the subscription whose phases to retrieve.
     * @return A pair of a [ListSubscriptionPhaseResponse] and any [NetworkingError] that occurred.
     */
    suspend fun getSubscriptionPhases(subscriptionId: String): Pair<ListSubscriptionPhaseResponse?, NetworkingError?> {
        val endpoint = SubscriptionPhaseEndpoints.GetSubscriptionPhases(subscriptionId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(FrameNetworking.parseResponse<ListSubscriptionPhaseResponse>(data), error)
    }

    /**
     * Replaces all phases on the specified subscription in a single request.
     *
     * @param subscriptionId The ID of the subscription whose phases to replace.
     * @param request The full set of phases to apply.
     * @return A pair of the updated list of [SubscriptionPhase] objects and any [NetworkingError] that occurred.
     */
    suspend fun bulkUpdateSubscriptionPhases(subscriptionId: String, request: SubscriptionPhaseRequest.BulkUpdateSubscriptionPhaseRequest) : Pair<List<SubscriptionPhase>?, NetworkingError?> {
        val endpoint = SubscriptionPhaseEndpoints.BulkUpdateSubscriptionPhases(subscriptionId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(FrameNetworking.parseResponse<ListSubscriptionPhaseResponse>(data)?.phases, error)
    }

    /**
     * Deletes a single phase from the specified subscription.
     *
     * @param subscriptionId The ID of the subscription that owns the phase.
     * @param phaseId The ID of the phase to delete.
     * @return A [NetworkingError] if the request failed, or null on success.
     */
    suspend fun deleteSubscriptionPhaseWith(subscriptionId: String, phaseId: String): NetworkingError? {
        val endpoint = SubscriptionPhaseEndpoints.DeleteSubscriptionPhase(subscriptionId, phaseId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return error
    }

    //MARK: Methods using callbacks

    /**
     * Creates a new phase for the specified subscription and delivers the result via callback.
     *
     * @param subscriptionId The ID of the subscription to add the phase to.
     * @param request The details of the phase to create.
     * @param completionHandler Callback invoked with the created [SubscriptionPhase] and any [NetworkingError].
     */
    fun createSubscriptionPhase(subscriptionId: String, request: SubscriptionPhaseRequest.CreateSubscriptionPhaseRequest, completionHandler: (SubscriptionPhase?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.CreateSubscriptionPhase(subscriptionId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionPhase>(data) }, error)
        }
    }

    /**
     * Updates an existing phase on the specified subscription and delivers the result via callback.
     *
     * @param subscriptionId The ID of the subscription that owns the phase.
     * @param phaseId The ID of the phase to update.
     * @param request The fields to update on the phase.
     * @param completionHandler Callback invoked with the updated [SubscriptionPhase] and any [NetworkingError].
     */
    fun updateSubscriptionPhase(subscriptionId: String, phaseId: String, request: SubscriptionPhaseRequest.UpdateSubscriptionPhaseRequest, completionHandler: (SubscriptionPhase?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.UpdateSubscriptionPhase(subscriptionId, phaseId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionPhase>(data) }, error)
        }
    }

    /**
     * Fetches a single phase by ID from the specified subscription and delivers the result via callback.
     *
     * @param subscriptionId The ID of the subscription that owns the phase.
     * @param phaseId The ID of the phase to retrieve.
     * @param completionHandler Callback invoked with the matching [SubscriptionPhase] and any [NetworkingError].
     */
    fun getSubscriptionWith(subscriptionId: String, phaseId: String, completionHandler: (SubscriptionPhase?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.GetSubscriptionPhaseWith(subscriptionId, phaseId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionPhase>(data) }, error)
        }
    }

    /**
     * Fetches all phases for the specified subscription and delivers the result via callback.
     *
     * @param subscriptionId The ID of the subscription whose phases to retrieve.
     * @param completionHandler Callback invoked with a [ListSubscriptionPhaseResponse] and any [NetworkingError].
     */
    fun getSubscriptions(subscriptionId: String, completionHandler: (ListSubscriptionPhaseResponse?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.GetSubscriptionPhases(subscriptionId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ListSubscriptionPhaseResponse>(data) }, error)
        }
    }

    /**
     * Replaces all phases on the specified subscription in a single request and delivers the result via callback.
     *
     * @param subscriptionId The ID of the subscription whose phases to replace.
     * @param request The full set of phases to apply.
     * @param completionHandler Callback invoked with the updated list of [SubscriptionPhase] objects and any [NetworkingError].
     */
    fun bulkUpdateSubscriptionPhases(subscriptionId: String, request: SubscriptionPhaseRequest.BulkUpdateSubscriptionPhaseRequest, completionHandler: (List<SubscriptionPhase>?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.BulkUpdateSubscriptionPhases(subscriptionId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ListSubscriptionPhaseResponse>(data)?.phases }, error)
        }
    }

    /**
     * Deletes a single phase from the specified subscription and delivers the result via callback.
     *
     * @param subscriptionId The ID of the subscription that owns the phase.
     * @param phaseId The ID of the phase to delete.
     * @param completionHandler Callback invoked with a [NetworkingError] on failure, or null on success.
     */
    fun deleteSubscriptionPhaseWith(subscriptionId: String, phaseId: String, completionHandler: (NetworkingError?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.DeleteSubscriptionPhase(subscriptionId, phaseId)

        FrameNetworking.performDataTask(endpoint) { _, error ->
            completionHandler(error)
        }
    }
}
