package com.framepayments.framesdk.transfers
import com.framepayments.framesdk.FrameMetadata

object TransferResponses {
    data class ListTransfersResponse (
        val meta: FrameMetadata?,
        val data: List<Transfer>?
    )
}
