package com.framepayments.framesdk.refunds
import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines the API endpoints available for refund operations.
 *
 * Each case maps to a specific URL path and HTTP method used by [FrameNetworking].
 */
sealed class RefundEndpoints : FrameNetworkingEndpoints {

    /** Endpoint for creating a new refund via POST to `/v1/refunds`. */
    object CreateRefund : RefundEndpoints()

    /**
     * Endpoint for retrieving a paginated list of refunds via GET to `/v1/refunds`.
     *
     * @property chargeId Optional charge ID to filter results.
     * @property chargeIntentId Optional charge intent ID to filter results.
     * @property perPage Optional number of results per page.
     * @property page Optional page number to retrieve.
     */
    data class GetRefunds(val chargeId: String?, val chargeIntentId: String?, val perPage: Int?, val page : Int?) : RefundEndpoints()

    /**
     * Endpoint for retrieving a single refund via GET to `/v1/refunds/{refundId}`.
     *
     * @property refundId The unique ID of the refund to retrieve.
     */
    data class GetRefundWith(val refundId: String) : RefundEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is CreateRefund, is GetRefunds ->
                "/v1/refunds"
            is GetRefundWith ->
                "/v1/refunds/${this.refundId}"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateRefund -> "POST"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>?
        get() = when (this) {
            is GetRefunds -> {
                val items = mutableListOf<QueryItem>()
                chargeId?.let { items.add(QueryItem("charge_id", it.toString())) }
                chargeIntentId?.let { items.add(QueryItem("charge_intent", it.toString())) }
                perPage?.let { items.add(QueryItem("per_page", it.toString())) }
                page?.let { items.add(QueryItem("page", it.toString())) }
                items
            }
            else -> null
        }
}
