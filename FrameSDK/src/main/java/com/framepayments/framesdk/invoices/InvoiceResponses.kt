package com.framepayments.framesdk.invoices

import com.framepayments.framesdk.FrameMetadata
import com.google.gson.annotations.SerializedName

object InvoiceResponses {
    data class ListInvoicesResponse (
        val meta: FrameMetadata?,
        val data: List<Invoice>?
    )

    data class DeletedInvoiceResponse (
        @SerializedName("object") val deletedObject: String,
        val deleted: Boolean
    )
}
