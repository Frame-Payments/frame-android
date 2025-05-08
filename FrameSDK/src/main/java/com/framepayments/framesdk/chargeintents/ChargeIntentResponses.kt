package com.framepayments.framesdk.chargeintents
import com.framepayments.framesdk.FrameMetadata

object ChargeIntentResponses {
    data class ListChargeIntentsResponse (
        val meta: FrameMetadata?,
        val data: List<ChargeIntent>?
    )
}