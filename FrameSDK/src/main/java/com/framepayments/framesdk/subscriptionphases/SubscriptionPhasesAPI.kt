package com.framepayments.framesdk.subscriptionphases
import com.framepayments.framesdk.FrameNetworking

object SubscriptionPhasesAPI {
    //MARK: Methods using coroutines
    suspend fun createSubscriptionPhase(subscriptionId: String, request: SubscriptionPhaseRequest.CreateSubscriptionPhaseRequest) : SubscriptionPhase? {
        val endpoint = SubscriptionPhaseEndpoints.CreateSubscriptionPhase(subscriptionId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            return FrameNetworking.parseResponse<SubscriptionPhase>(data)
        }
        return null
    }

    suspend fun updateSubscriptionPhase(subscriptionId: String, phaseId: String, request: SubscriptionPhaseRequest.UpdateSubscriptionPhaseRequest): SubscriptionPhase? {
        val endpoint = SubscriptionPhaseEndpoints.UpdateSubscriptionPhase(subscriptionId, phaseId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            return FrameNetworking.parseResponse<SubscriptionPhase>(data)
        }
        return null
    }

    suspend fun getSubscriptionPhaseWith(subscriptionId: String, phaseId: String): SubscriptionPhase? {
        val endpoint = SubscriptionPhaseEndpoints.GetSubscriptionPhaseWith(subscriptionId, phaseId)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<SubscriptionPhase>(data)
        }
        return null
    }

    suspend fun getSubscriptionPhases(subscriptionId: String): ListSubscriptionPhaseResponse? {
        val endpoint = SubscriptionPhaseEndpoints.GetSubscriptionPhases(subscriptionId)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<ListSubscriptionPhaseResponse>(data)
        }
        return null
    }

    suspend fun bulkUpdateSubscriptionPhases(subscriptionId: String, request: SubscriptionPhaseRequest.BulkUpdateSubscriptionPhaseRequest) : List<SubscriptionPhase>? {
        val endpoint = SubscriptionPhaseEndpoints.BulkUpdateSubscriptionPhases(subscriptionId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            return FrameNetworking.parseResponse<ListSubscriptionPhaseResponse>(data)?.phases
        }
        return null
    }

    suspend fun deleteSubscriptionPhaseWith(subscriptionId: String, phaseId: String): SubscriptionPhase? {
        val endpoint = SubscriptionPhaseEndpoints.DeleteSubscriptionPhase(subscriptionId, phaseId)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<SubscriptionPhase>(data)
        }
        return null
    }

    //MARK: Methods using callbacks
    fun createSubscriptionPhase(subscriptionId: String, request: SubscriptionPhaseRequest.CreateSubscriptionPhaseRequest, completionHandler: (SubscriptionPhase?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.CreateSubscriptionPhase(subscriptionId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<SubscriptionPhase>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun updateSubscriptionPhase(subscriptionId: String, phaseId: String, request: SubscriptionPhaseRequest.UpdateSubscriptionPhaseRequest, completionHandler: (SubscriptionPhase?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.UpdateSubscriptionPhase(subscriptionId, phaseId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<SubscriptionPhase>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun getSubscriptionWith(subscriptionId: String, phaseId: String, completionHandler: (SubscriptionPhase?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.GetSubscriptionPhaseWith(subscriptionId, phaseId)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<SubscriptionPhase>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun getSubscriptions(subscriptionId: String, completionHandler: (ListSubscriptionPhaseResponse?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.GetSubscriptionPhases(subscriptionId)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<ListSubscriptionPhaseResponse>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun bulkUpdateSubscriptionPhases(subscriptionId: String, request: SubscriptionPhaseRequest.BulkUpdateSubscriptionPhaseRequest, completionHandler: (List<SubscriptionPhase>?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.BulkUpdateSubscriptionPhases(subscriptionId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<ListSubscriptionPhaseResponse>(data)?.phases)
            } else {
                completionHandler(null)
            }
        }
    }

    fun deleteSubscriptionPhaseWith(subscriptionId: String, phaseId: String, completionHandler: (SubscriptionPhase?) -> Unit) {
        val endpoint = SubscriptionPhaseEndpoints.DeleteSubscriptionPhase(subscriptionId, phaseId)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<SubscriptionPhase>(data))
            } else {
                completionHandler(null)
            }
        }
    }
}

