package com.framepayments.framesdk.refunds
import com.framepayments.framesdk.FrameMetadata

object RefundResponses {
    data class ListRefundsResponse (
        val meta: FrameMetadata?,
        val data: List<Refund>?
    )
}
