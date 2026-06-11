package com.framepayments.framesdk.disputes

/**
 * Contains response model classes returned by dispute-related API calls.
 */
object DisputeResponses {

    /**
     * Wraps the paginated list of disputes returned by the list-disputes endpoint.
     *
     * @property data The list of [Dispute] objects returned for the current page, or `null` if none exist.
     */
    data class ListDisputesResponse(
        val data: List<Dispute>?
    )
}
