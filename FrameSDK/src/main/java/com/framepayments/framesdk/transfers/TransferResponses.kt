package com.framepayments.framesdk.transfers

import com.framepayments.framesdk.FrameMetadata

/**
 * Contains response payload models for the Transfers API.
 */
object TransferResponses {

    /**
     * Paginated response returned when listing transfers.
     *
     * @property meta Pagination metadata such as total count and current page.
     * @property data The list of [Transfer] objects on the current page.
     */
    data class ListTransfersResponse(
        val meta: FrameMetadata?,
        val data: List<Transfer>?
    )
}
