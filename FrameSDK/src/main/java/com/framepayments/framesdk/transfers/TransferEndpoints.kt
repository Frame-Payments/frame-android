package com.framepayments.framesdk.transfers
import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class TransferEndpoints : FrameNetworkingEndpoints {
    object CreateTransfer : TransferEndpoints()
    data class GetTransferWith(val transferId: String) : TransferEndpoints()
    data class GetTransfers(val perPage: Int?, val page: Int?) : TransferEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is CreateTransfer, is GetTransfers ->
                "/v1/transfers"
            is GetTransferWith ->
                "/v1/transfers/${this.transferId}"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateTransfer -> "POST"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>?
        get() = when (this) {
            is GetTransfers -> {
                val items = mutableListOf<QueryItem>()
                perPage?.let { items.add(QueryItem("per_page", it.toString())) }
                page?.let { items.add(QueryItem("page", it.toString())) }
                items
            }
            else -> null
        }
}
