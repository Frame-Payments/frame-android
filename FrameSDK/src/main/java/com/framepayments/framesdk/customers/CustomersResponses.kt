package com.framepayments.framesdk.customers
import com.framepayments.framesdk.FrameMetadata

object CustomersResponses {
    data class ListCustomersResponse(
        val meta: FrameMetadata? = null,
        val data: List<Customer>? = null
    )
}