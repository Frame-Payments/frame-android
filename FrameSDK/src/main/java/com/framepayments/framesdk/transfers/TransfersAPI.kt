package com.framepayments.framesdk.transfers

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Provides suspend and callback-based functions for managing transfers.
 */
object TransfersAPI {

    //MARK: Methods using coroutines

    /**
     * Creates a new transfer.
     *
     * @param request The request payload describing the transfer to create.
     * @return A [Pair] of ([Transfer]?, [NetworkingError]?).
     */
    suspend fun createTransfer(request: TransferRequests.CreateTransferRequest): Pair<Transfer?, NetworkingError?> {
        val endpoint = TransferEndpoints.CreateTransfer
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<Transfer>(data) }, error)
    }

    /**
     * Retrieves an existing transfer by its identifier.
     *
     * @param transferId The unique identifier of the transfer to retrieve.
     * @return A [Pair] of ([Transfer]?, [NetworkingError]?).
     */
    suspend fun getTransferWith(transferId: String): Pair<Transfer?, NetworkingError?> {
        val endpoint = TransferEndpoints.GetTransferWith(transferId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<Transfer>(data) }, error)
    }

    /**
     * Retrieves a paginated list of transfers.
     *
     * @param perPage The number of transfers to return per page, or null to use the API default.
     * @param page The page number to retrieve, or null to retrieve the first page.
     * @return A [Pair] of ([TransferResponses.ListTransfersResponse]?, [NetworkingError]?).
     */
    suspend fun getTransfers(perPage: Int? = null, page: Int? = null): Pair<TransferResponses.ListTransfersResponse?, NetworkingError?> {
        val endpoint = TransferEndpoints.GetTransfers(perPage = perPage, page = page)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<TransferResponses.ListTransfersResponse>(data) }, error)
    }

    //MARK: Methods using callbacks

    /**
     * Creates a new transfer and delivers the result via a callback.
     *
     * @param request The request payload describing the transfer to create.
     * @param completionHandler Callback invoked with ([Transfer]?, [NetworkingError]?).
     */
    fun createTransfer(request: TransferRequests.CreateTransferRequest, completionHandler: (Transfer?, NetworkingError?) -> Unit) {
        val endpoint = TransferEndpoints.CreateTransfer

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Transfer>(data) }, error)
        }
    }

    /**
     * Retrieves an existing transfer by its identifier and delivers the result via a callback.
     *
     * @param transferId The unique identifier of the transfer to retrieve.
     * @param completionHandler Callback invoked with ([Transfer]?, [NetworkingError]?).
     */
    fun getTransferWith(transferId: String, completionHandler: (Transfer?, NetworkingError?) -> Unit) {
        val endpoint = TransferEndpoints.GetTransferWith(transferId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Transfer>(data) }, error)
        }
    }

    /**
     * Retrieves a paginated list of transfers and delivers the result via a callback.
     *
     * @param perPage The number of transfers to return per page, or null to use the API default.
     * @param page The page number to retrieve, or null to retrieve the first page.
     * @param completionHandler Callback invoked with ([TransferResponses.ListTransfersResponse]?, [NetworkingError]?).
     */
    fun getTransfers(perPage: Int?, page: Int?, completionHandler: (TransferResponses.ListTransfersResponse?, NetworkingError?) -> Unit) {
        val endpoint = TransferEndpoints.GetTransfers(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<TransferResponses.ListTransfersResponse>(data) }, error)
        }
    }
}
