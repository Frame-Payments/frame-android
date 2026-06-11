package com.framepayments.framesdk.invoicelineitems

import com.google.gson.annotations.SerializedName

/**
 * Namespace for response models returned by [InvoiceLineItemsAPI].
 */
object InvoiceLineItemResponses {

    /**
     * Response containing a list of invoice line items.
     *
     * @property data The collection of [InvoiceLineItem] objects returned by the API, or null if absent.
     */
    data class ListInvoiceLineItemsResponse (
        val data: List<InvoiceLineItem>?
    )

    /**
     * Response confirming the deletion of an invoice line item.
     *
     * @property deletedObject The object type string identifying what was deleted (e.g. `"line_item"`).
     * @property deleted True if the line item was successfully deleted.
     */
    data class DeletedInvoiceLineItemResponse (
        @SerializedName("object") val deletedObject: String?,
        val deleted: Boolean?
    )
}
