package com.framepayments.framesdk.disputes

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class DisputeEndpoints : FrameNetworkingEndpoints {
    data class UpdateDispute(val disputeId: String) : DisputeEndpoints()
    data class GetDispute(val disputeId: String) : DisputeEndpoints()
    data class GetDisputes(
        val chargeId: String?,
        val chargeIntentId: String?,
        val perPage: Int?,
        val page: Int?
    ) : DisputeEndpoints()
    data class CloseDispute(val disputeId: String) : DisputeEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is UpdateDispute -> "/v1/disputes/${disputeId}"
            is GetDispute -> "/v1/disputes/${disputeId}"
            is GetDisputes -> "/v1/disputes"
            is CloseDispute -> "/v1/disputes/${disputeId}/close"
        }

    override val httpMethod: String
        get() = when (this) {
            is UpdateDispute -> "PATCH"
            is CloseDispute -> "POST"
            else -> "GET"
        }

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
