package com.framepayments.framesdk.transfers
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

object TransfersAPI {
    //MARK: Methods using coroutines
    suspend fun createTransfer(request: TransferRequests.CreateTransferRequest): Pair<Transfer?, NetworkingError?> {
        val endpoint = TransferEndpoints.CreateTransfer
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<Transfer>(data) }, error)
    }

    suspend fun getTransferWith(transferId: String): Pair<Transfer?, NetworkingError?> {
        val endpoint = TransferEndpoints.GetTransferWith(transferId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<Transfer>(data) }, error)
    }

    suspend fun getTransfers(perPage: Int? = null, page: Int? = null): Pair<TransferResponses.ListTransfersResponse?, NetworkingError?> {
        val endpoint = TransferEndpoints.GetTransfers(perPage = perPage, page = page)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<TransferResponses.ListTransfersResponse>(data) }, error)
    }

    //MARK: Methods using callbacks
    fun createTransfer(request: TransferRequests.CreateTransferRequest, completionHandler: (Transfer?, NetworkingError?) -> Unit) {
        val endpoint = TransferEndpoints.CreateTransfer

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Transfer>(data) }, error)
        }
    }

    fun getTransferWith(transferId: String, completionHandler: (Transfer?, NetworkingError?) -> Unit) {
        val endpoint = TransferEndpoints.GetTransferWith(transferId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Transfer>(data) }, error)
        }
    }

    fun getTransfers(perPage: Int?, page: Int?, completionHandler: (TransferResponses.ListTransfersResponse?, NetworkingError?) -> Unit) {
        val endpoint = TransferEndpoints.GetTransfers(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<TransferResponses.ListTransfersResponse>(data) }, error)
        }
    }
}
