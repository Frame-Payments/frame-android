package com.framepayments.framesdk.invoicelineitems

object InvoiceLineItemRequests {
    data class CreateLineItemRequest(
        val product: String,
        val quantity: Int // Note: Must be greater than 0
    )

    data class UpdateLineItemRequest(
        val product: String?,
        val quantity: Int? // Note: Must be greater than 0
    )
}