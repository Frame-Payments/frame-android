package com.framepayments.framesdk.invoices

import com.framepayments.framesdk.FrameMetadata
import com.google.gson.annotations.SerializedName

/** Holds response body models for invoice API operations. */
object InvoiceResponses {

    /**
     * Response returned by the list-invoices endpoint.
     *
     * @property meta Pagination metadata for the result set.
     * @property data The invoices returned for the current page.
     */
    data class ListInvoicesResponse (
        val meta: FrameMetadata?,
        val data: List<Invoice>?
    )

    /**
     * Response returned after successfully deleting an invoice.
     *
     * @property deletedObject The object type identifier of the deleted resource (e.g., `"invoice"`).
     * @property deleted Whether the deletion was successful.
     */
    data class DeletedInvoiceResponse (
        @SerializedName("object") val deletedObject: String?,
        val deleted: Boolean?
    )
}
