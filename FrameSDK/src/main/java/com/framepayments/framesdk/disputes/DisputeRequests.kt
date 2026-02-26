package com.framepayments.framesdk.disputes

object DisputeRequests {
    data class UpdateDisputeRequest(
        val evidence: DisputeEvidence,
        val submit: Boolean
    )
}
