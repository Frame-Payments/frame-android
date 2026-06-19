package com.framepayments.framesdk.disputes

import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Provides suspend and callback-based functions for managing disputes via the Frame API.
 */
object DisputesAPI {

    /**
     * Updates an existing dispute with the supplied evidence details.
     *
     * @param disputeId The unique identifier of the dispute to update.
     * @param request The evidence and supporting details to submit for the dispute.
     * @return A [Pair] containing the updated [Dispute] on success, or a [NetworkingError] on failure.
     */
    suspend fun updateDispute(
        disputeId: String,
        request: DisputeRequests.UpdateDisputeRequest
    ): Pair<Dispute?, NetworkingError?> {
        val endpoint = DisputeEndpoints.UpdateDispute(disputeId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<Dispute>(data) }, error)
    }

    /**
     * Retrieves a single dispute by its identifier.
     *
     * @param disputeId The unique identifier of the dispute to retrieve.
     * @return A [Pair] containing the [Dispute] on success, or a [NetworkingError] on failure.
     */
    suspend fun getDispute(disputeId: String): Pair<Dispute?, NetworkingError?> {
        val endpoint = DisputeEndpoints.GetDispute(disputeId)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<Dispute>(data) }, error)
    }

    /**
     * Retrieves a paginated list of disputes, optionally filtered by charge or charge intent.
     *
     * @param chargeId Optional identifier of the charge to filter disputes by.
     * @param chargeIntentId Optional identifier of the charge intent to filter disputes by.
     * @param perPage Number of disputes to return per page. Defaults to 10.
     * @param page The page number to retrieve. Defaults to 1.
     * @return A [Pair] containing the [DisputeResponses.ListDisputesResponse] on success, or a [NetworkingError] on failure.
     */
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
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(
            data?.let { FrameNetworking.parseResponse<DisputeResponses.ListDisputesResponse>(data) },
            error
        )
    }

    /**
     * Updates an existing dispute with the supplied evidence details and delivers the result via a callback.
     *
     * @param disputeId The unique identifier of the dispute to update.
     * @param request The evidence and supporting details to submit for the dispute.
     * @param completionHandler Callback invoked with the updated [Dispute] on success, or a [NetworkingError] on failure.
     */
    fun updateDispute(
        disputeId: String,
        request: DisputeRequests.UpdateDisputeRequest,
        completionHandler: (Dispute?, NetworkingError?) -> Unit
    ) {
        val endpoint = DisputeEndpoints.UpdateDispute(disputeId)
        FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Dispute>(data) }, error)
        }
    }

    /**
     * Retrieves a single dispute by its identifier and delivers the result via a callback.
     *
     * @param disputeId The unique identifier of the dispute to retrieve.
     * @param completionHandler Callback invoked with the [Dispute] on success, or a [NetworkingError] on failure.
     */
    fun getDispute(disputeId: String, completionHandler: (Dispute?, NetworkingError?) -> Unit) {
        val endpoint = DisputeEndpoints.GetDispute(disputeId)
        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Dispute>(data) }, error)
        }
    }

    /**
     * Retrieves a paginated list of disputes and delivers the result via a callback, optionally filtered by charge or charge intent.
     *
     * @param chargeId Optional identifier of the charge to filter disputes by.
     * @param chargeIntentId Optional identifier of the charge intent to filter disputes by.
     * @param perPage Number of disputes to return per page. Defaults to 10.
     * @param page The page number to retrieve. Defaults to 1.
     * @param completionHandler Callback invoked with the [DisputeResponses.ListDisputesResponse] on success, or a [NetworkingError] on failure.
     */
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
        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(
                data?.let { FrameNetworking.parseResponse<DisputeResponses.ListDisputesResponse>(data) },
                error
            )
        }
    }

}
