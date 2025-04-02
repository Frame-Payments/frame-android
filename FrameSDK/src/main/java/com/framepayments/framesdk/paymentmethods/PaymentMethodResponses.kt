package com.framepayments.framesdk.paymentmethods
import com.framepayments.framesdk.FrameMetadata

object PaymentMethodResponses {
    data class ListPaymentMethodsResponse(
        val meta: FrameMetadata? = null,
        val data: List<FrameObjects.PaymentMethod>? = null
    )
}