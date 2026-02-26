package com.framepayments.framesdk.disputes

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

object DisputesAPI {
    suspend fun updateDispute(
        disputeId: String,
        request: DisputeRequests.UpdateDisputeRequest
    ): Pair<Dispute?, NetworkingError?> {
        val endpoint = DisputeEndpoints.UpdateDispute(disputeId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<Dispute>(data) }, error)
    }

    suspend fun getDispute(disputeId: String): Pair<Dispute?, NetworkingError?> {
        val endpoint = DisputeEndpoints.GetDispute(disputeId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<Dispute>(data) }, error)
    }

    suspend fun getDisputes(
        chargeId: String? = null,
        chargeIntentId: String? = null,
        perPage: Int = 10,
        page: Int = 1
    ): Pair<DisputeResponses.ListDisputesResponse?, NetworkingError?> {
        val endpoint = DisputeEndpoints.GetDisputes(
            chargeId = chargeId,
            chargeIntentId = chargeIntentId,
            perPage = perPage,
            page = page
        )
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(
            data?.let { FrameNetworking.parseResponse<DisputeResponses.ListDisputesResponse>(data) },
            error
        )
    }

    suspend fun closeDispute(disputeId: String): Pair<Dispute?, NetworkingError?> {
        val endpoint = DisputeEndpoints.CloseDispute(disputeId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(
            endpoint,
            com.framepayments.framesdk.EmptyRequest(description = null)
        )
        return Pair(data?.let { FrameNetworking.parseResponse<Dispute>(data) }, error)
    }

    fun updateDispute(
        disputeId: String,
        request: DisputeRequests.UpdateDisputeRequest,
        completionHandler: (Dispute?, NetworkingError?) -> Unit
    ) {
        val endpoint = DisputeEndpoints.UpdateDispute(disputeId)
        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Dispute>(data) }, error)
        }
    }

    fun getDispute(disputeId: String, completionHandler: (Dispute?, NetworkingError?) -> Unit) {
        val endpoint = DisputeEndpoints.GetDispute(disputeId)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Dispute>(data) }, error)
        }
    }

    fun getDisputes(
        chargeId: String? = null,
        chargeIntentId: String? = null,
        perPage: Int = 10,
        page: Int = 1,
        completionHandler: (DisputeResponses.ListDisputesResponse?, NetworkingError?) -> Unit
    ) {
        val endpoint = DisputeEndpoints.GetDisputes(
            chargeId = chargeId,
            chargeIntentId = chargeIntentId,
            perPage = perPage,
            page = page
        )
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(
                data?.let { FrameNetworking.parseResponse<DisputeResponses.ListDisputesResponse>(data) },
                error
            )
        }
    }

    fun closeDispute(disputeId: String, completionHandler: (Dispute?, NetworkingError?) -> Unit) {
        val endpoint = DisputeEndpoints.CloseDispute(disputeId)
        FrameNetworking.performDataTaskWithRequest(
            endpoint,
            com.framepayments.framesdk.EmptyRequest(description = null)
        ) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Dispute>(data) }, error)
        }
    }
}
