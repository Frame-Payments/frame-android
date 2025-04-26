package com.framepayments.framesdk.customers
import com.framepayments.framesdk.FrameMetadata
import com.framepayments.framesdk.FrameObjects

object CustomersResponses {
    data class ListCustomersResponse(
        val meta: FrameMetadata? = null,
        val data: List<FrameObjects.Customer>? = null
    )
}