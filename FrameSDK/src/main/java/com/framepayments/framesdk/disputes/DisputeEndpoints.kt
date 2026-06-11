package com.framepayments.framesdk.disputes

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines the API endpoints available for dispute operations.
 *
 * Each case maps to a specific Frame API route and HTTP method. Callers construct the
 * appropriate case and pass it to the networking layer to execute the request.
 */
sealed class DisputeEndpoints : FrameNetworkingEndpoints {

    /**
     * Endpoint for updating an existing dispute with new evidence.
     *
     * @property disputeId The unique identifier of the dispute to update.
     */
    data class UpdateDispute(val disputeId: String) : DisputeEndpoints()

    /**
     * Endpoint for retrieving a single dispute by its identifier.
     *
     * @property disputeId The unique identifier of the dispute to retrieve.
     */
    data class GetDispute(val disputeId: String) : DisputeEndpoints()

    /**
     * Endpoint for retrieving a paginated list of disputes, optionally filtered by charge or charge intent.
     *
     * @property chargeId Optional identifier of the charge to filter results by.
     * @property chargeIntentId Optional identifier of the charge intent to filter results by.
     * @property perPage Number of disputes to return per page.
     * @property page The page number to retrieve.
     */
    data class GetDisputes(
        val chargeId: String?,
        val chargeIntentId: String?,
        val perPage: Int?,
        val page: Int?
    ) : DisputeEndpoints()

    /** The relative URL path for this endpoint. */
    override val endpointURL: String
        get() = when (this) {
            is UpdateDispute -> "/v1/disputes/${disputeId}"
            is GetDispute -> "/v1/disputes/${disputeId}"
            is GetDisputes -> "/v1/disputes"
        }

    /** The HTTP method used for this endpoint. */
    override val httpMethod: String
        get() = when (this) {
            is UpdateDispute -> "PATCH"
            else -> "GET"
        }

    /** The query parameters appended to the request URL, or `null` if none apply. */
    override val queryItems: List<QueryItem>?
        get() = when (this) {
            is GetDisputes -> {
                val items = mutableListOf<QueryItem>()
                chargeId?.let { items.add(QueryItem("charge", it)) }
                chargeIntentId?.let { items.add(QueryItem("charge_intent", it)) }
                perPage?.let { items.add(QueryItem("per_page", it.toString())) }
                page?.let { items.add(QueryItem("page", it.toString())) }
                items
            }
            else -> null
        }
}
