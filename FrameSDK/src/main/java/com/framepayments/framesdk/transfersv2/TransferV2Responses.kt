package com.framepayments.framesdk.transfersv2

import com.framepayments.framesdk.FrameMetadata

/**
 * Contains response payload models for the Transfers V2 API.
 */
object TransferV2Responses {

    /**
     * Paginated response returned when listing V2 transfers.
     *
     * @property meta Pagination metadata such as total count and current page.
     * @property data The list of [TransferV2] objects on the current page.
     */
    data class ListTransfersResponse(
        val meta: FrameMetadata?,
        val data: List<TransferV2>?
    )
}
