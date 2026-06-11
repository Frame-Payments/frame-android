package com.framepayments.framesdk.transfers

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines the network endpoints for the Transfers API.
 *
 * Each case maps to a specific API route and HTTP method used by [TransfersAPI].
 */
sealed class TransferEndpoints : FrameNetworkingEndpoints {

    /** Endpoint for creating a new transfer (POST /v1/transfers). */
    object CreateTransfer : TransferEndpoints()

    /**
     * Endpoint for retrieving a single transfer by identifier (GET /v1/transfers/{id}).
     *
     * @property transferId The unique identifier of the transfer to retrieve.
     */
    data class GetTransferWith(val transferId: String) : TransferEndpoints()

    /**
     * Endpoint for retrieving a paginated list of transfers (GET /v1/transfers).
     *
     * @property perPage The number of results per page, or null to use the API default.
     * @property page The page number to retrieve, or null to retrieve the first page.
     */
    data class GetTransfers(val perPage: Int?, val page: Int?) : TransferEndpoints()

    /** The resolved URL path for this endpoint. */
    override val endpointURL: String
        get() = when (this) {
            is CreateTransfer, is GetTransfers ->
                "/v1/transfers"
            is GetTransferWith ->
                "/v1/transfers/${this.transferId}"
        }

    /** The HTTP method for this endpoint. */
    override val httpMethod: String
        get() = when (this) {
            is CreateTransfer -> "POST"
            else -> "GET"
        }

    /** Query parameters appended to the request URL, populated for [GetTransfers] only. */
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
