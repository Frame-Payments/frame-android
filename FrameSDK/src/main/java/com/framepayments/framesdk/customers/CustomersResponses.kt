package com.framepayments.framesdk.customers
import com.framepayments.framesdk.FrameMetadata
import com.framepayments.framesdk.FrameObjects

/**
 * Contains response models returned by customer API operations.
 */
object CustomersResponses {

    /**
     * Response returned by list and search customer endpoints.
     *
     * @property meta Pagination metadata for the result set. Null if the server omits it.
     * @property data The list of customers included in this page of results. Null if the server omits it.
     */
    data class ListCustomersResponse(
        val meta: FrameMetadata? = null,
        val data: List<FrameObjects.Customer>? = null
    )
}
