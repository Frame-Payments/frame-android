package com.framepayments.framesdk.paymentmethods
import com.framepayments.framesdk.FrameMetadata
import com.framepayments.framesdk.FrameObjects

/**
 * Contains all response body models returned by [PaymentMethodsAPI].
 */
object PaymentMethodResponses {

    /**
     * Paginated response returned when listing payment methods.
     *
     * @property meta Pagination metadata (page, per-page count, total records, etc.), if present.
     * @property data The list of payment methods on the current page, if present.
     */
    data class ListPaymentMethodsResponse(
        val meta: FrameMetadata? = null,
        val data: List<FrameObjects.PaymentMethod>? = null
    )
}
