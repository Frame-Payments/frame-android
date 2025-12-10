package com.framepayments.framesdk.subscriptionphases

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

object SubscriptionPhasesAPI {
    //MARK: Methods using coroutines
    suspend fun createSubscriptionPhase(subscriptionId: String, request: SubscriptionPhaseRequest.CreateSubscriptionPhaseRequest): Pair<SubscriptionPhase?, NetworkingError?> {
        val endpoint = SubscriptionPhaseEndpoints.CreateSubscriptionPhase(subscriptionId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(FrameNetworking.parseResponse<SubscriptionPhase>(data), error)
    }

    suspend fun updateSubscriptionPhase(subscriptionId: String, phaseId: String, request: SubscriptionPhaseRequest.UpdateSubscriptionPhaseRequest): Pair<SubscriptionPhase?, NetworkingError?> {
        val endpoint = SubscriptionPhaseEndpoints.UpdateSubscriptionPhase(subscriptionId, phaseId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(FrameNetworking.parseResponse<SubscriptionPhase>(data), error)
    }

    suspend fun getSubscriptionPhaseWith(subscriptionId: String, phaseId: String): Pair<SubscriptionPhase?, NetworkingError?> {
        val endpoint = SubscriptionPhaseEndpoints.GetSubscriptionPhaseWith(subscriptionId, phaseId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(FrameNetworking.parseResponse<SubscriptionPhase>(data), error)
    }

    suspend fun getSubscriptionPhases(subscriptionId: String): Pair<ListSubscriptionPhaseResponse?, NetworkingError?> {
        val endpoint = SubscriptionPhaseEndpoints.GetSubscriptionPhases(subscriptionId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(FrameNetworking.parseResponse<ListSubscriptionPhaseResponse>(data), error)
    }

    suspend fun bulkUpdateSubscriptionPhases(subscriptionId: String, request: SubscriptionPhaseRequest.BulkUpdateSubscriptionPhaseRequest) : Pair<List<SubscriptionPhase>?, NetworkingError?> {
        val endpoint = SubscriptionPhaseEndpoints.BulkUpdateSubscriptionPhases(subscriptionId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(FrameNetworking.parseResponse<ListSubscriptionPhaseResponse>(data)?.phases, error)
    }

    suspend fun deleteSubscriptionPhaseWith(subscriptionId: String, phaseId: String): Pair<SubscriptionPhase?, NetworkingError?> {
        val endpoint = SubscriptionPhaseEndpoints.DeleteSubscriptionPhase(subscriptionId, phaseId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(FrameNetworking.parseResponse<SubscriptionPhase>(data), error)
    }

    //MARK: Methods using callbacks
    fun createSubscriptionPhase(subscriptionId: String, request: SubscriptionPhaseRequest.CreateSubscriptionPhaseRequest, completionHandler: (SubscriptionPhase?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.CreateSubscriptionPhase(subscriptionId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionPhase>(data) }, error)
        }
    }

    fun updateSubscriptionPhase(subscriptionId: String, phaseId: String, request: SubscriptionPhaseRequest.UpdateSubscriptionPhaseRequest, completionHandler: (SubscriptionPhase?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.UpdateSubscriptionPhase(subscriptionId, phaseId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionPhase>(data) }, error)
        }
    }

    fun getSubscriptionWith(subscriptionId: String, phaseId: String, completionHandler: (SubscriptionPhase?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.GetSubscriptionPhaseWith(subscriptionId, phaseId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionPhase>(data) }, error)
        }
    }

    fun getSubscriptions(subscriptionId: String, completionHandler: (ListSubscriptionPhaseResponse?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.GetSubscriptionPhases(subscriptionId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ListSubscriptionPhaseResponse>(data) }, error)
        }
    }

    fun bulkUpdateSubscriptionPhases(subscriptionId: String, request: SubscriptionPhaseRequest.BulkUpdateSubscriptionPhaseRequest, completionHandler: (List<SubscriptionPhase>?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.BulkUpdateSubscriptionPhases(subscriptionId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ListSubscriptionPhaseResponse>(data)?.phases }, error)
        }
    }

    fun deleteSubscriptionPhaseWith(subscriptionId: String, phaseId: String, completionHandler: (SubscriptionPhase?, NetworkingError?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.DeleteSubscriptionPhase(subscriptionId, phaseId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionPhase>(data) }, error)
        }
    }
}

