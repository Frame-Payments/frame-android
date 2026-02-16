package com.framepayments.framesdk.disputes

object DisputeResponses {
    data class ListDisputesResponse(
        val data: List<Dispute>?
    )
}
