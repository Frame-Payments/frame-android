package com.framepayments.framesdk.refunds
import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class RefundEndpoints : FrameNetworkingEndpoints {
    object CreateRefund : RefundEndpoints()
    data class CancelRefund(val refundId: String) : RefundEndpoints()
    data class GetRefunds(val chargeId: String?, val chargeIntentId: String?, val perPage: Int?, val page : Int?) : RefundEndpoints()
    data class GetRefundWith(val refundId: String) : RefundEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is CreateRefund, is GetRefunds ->
                "/v1/refunds"
            is GetRefundWith ->
                "/v1/refunds/${this.refundId}"
            is CancelRefund ->
                "/v1/refunds/${this.refundId}/cancel"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateRefund, is CancelRefund -> "POST"
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
