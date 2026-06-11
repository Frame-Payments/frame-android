package com.framepayments.framesdk.chargeintents
import com.framepayments.framesdk.FrameMetadata

/**
 * Contains response body models returned by charge intent API calls.
 */
object ChargeIntentResponses {

    /**
     * Response body for a paginated list of charge intents.
     *
     * @property meta Pagination metadata for the result set, or `null` if unavailable.
     * @property data The list of charge intents on the current page, or `null` if the response contains no results.
     */
    data class ListChargeIntentsResponse (
        val meta: FrameMetadata?,
        val data: List<ChargeIntent>?
    )
}
