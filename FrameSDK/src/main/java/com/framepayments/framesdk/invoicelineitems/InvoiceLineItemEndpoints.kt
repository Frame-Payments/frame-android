package com.framepayments.framesdk.invoicelineitems

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class InvoiceLineItemEndpoints : FrameNetworkingEndpoints {
    data class ListInvoiceLineItems(val invoiceId: String) : InvoiceLineItemEndpoints()
    data class CreateInvoiceLineItem(val invoiceId: String) : InvoiceLineItemEndpoints()
    data class UpdateInvoiceLineItem(val invoiceId: String, val invoiceLineItemId: String) : InvoiceLineItemEndpoints()
    data class DeleteInvoiceLineItem(val invoiceId: String, val invoiceLineItemId: String) : InvoiceLineItemEndpoints()
    data class GetInvoiceLineItemWith(val invoiceId: String, val invoiceLineItemId: String) : InvoiceLineItemEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is CreateInvoiceLineItem ->
                "/v1/invoices/${this.invoiceId}/line_items"
            is ListInvoiceLineItems ->
                "/v1/invoices/${this.invoiceId}/line_items"
            is GetInvoiceLineItemWith ->
                "/v1/invoices/${this.invoiceId}/line_items/${this.invoiceLineItemId}"
            is UpdateInvoiceLineItem ->
                "/v1/invoices/${this.invoiceId}/line_items/${this.invoiceLineItemId}"
            is DeleteInvoiceLineItem ->
                "/v1/invoices/${this.invoiceId}/line_items/${this.invoiceLineItemId}"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateInvoiceLineItem -> "POST"
            is UpdateInvoiceLineItem -> "PATCH"
            is DeleteInvoiceLineItem -> "DELETE"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>? = null
}