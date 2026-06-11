package com.framepayments.framesdk.refunds
import com.framepayments.framesdk.FrameMetadata

/**
 * Namespace for response models returned by the Refunds API.
 */
object RefundResponses {

    /**
     * Paginated response returned when listing refunds.
     *
     * @property meta Pagination metadata for the result set, or null if unavailable.
     * @property data List of [Refund] objects for the current page, or null if unavailable.
     */
    data class ListRefundsResponse (
        val meta: FrameMetadata?,
        val data: List<Refund>?
    )
}
