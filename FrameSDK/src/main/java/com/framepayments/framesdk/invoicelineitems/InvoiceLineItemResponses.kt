package com.framepayments.framesdk.invoicelineitems

import com.google.gson.annotations.SerializedName

object InvoiceLineItemResponses {
    data class ListInvoiceLineItemsResponse (
        val data: List<InvoiceLineItem>?
    )

    data class DeletedInvoiceLineItemResponse (
        @SerializedName("object") val deletedObject: String,
        val deleted: Boolean
    )
}