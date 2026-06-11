package com.framepayments.framesdk.invoicelineitems

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines the network endpoints used by [InvoiceLineItemsAPI] to manage invoice line items.
 *
 * Each subclass represents a distinct API operation and supplies the correct [endpointURL]
 * and [httpMethod] for that operation.
 */
sealed class InvoiceLineItemEndpoints : FrameNetworkingEndpoints {

    /**
     * Endpoint for retrieving all line items belonging to an invoice.
     *
     * @property invoiceId The unique identifier of the invoice whose line items to list.
     */
    data class ListInvoiceLineItems(val invoiceId: String) : InvoiceLineItemEndpoints()

    /**
     * Endpoint for creating a new line item on an invoice.
     *
     * @property invoiceId The unique identifier of the invoice to add the line item to.
     */
    data class CreateInvoiceLineItem(val invoiceId: String) : InvoiceLineItemEndpoints()

    /**
     * Endpoint for updating an existing line item on an invoice.
     *
     * @property invoiceId The unique identifier of the invoice that owns the line item.
     * @property invoiceLineItemId The unique identifier of the line item to update.
     */
    data class UpdateInvoiceLineItem(val invoiceId: String, val invoiceLineItemId: String) : InvoiceLineItemEndpoints()

    /**
     * Endpoint for deleting a line item from an invoice.
     *
     * @property invoiceId The unique identifier of the invoice that owns the line item.
     * @property invoiceLineItemId The unique identifier of the line item to delete.
     */
    data class DeleteInvoiceLineItem(val invoiceId: String, val invoiceLineItemId: String) : InvoiceLineItemEndpoints()

    /**
     * Endpoint for retrieving a single line item by its identifier from an invoice.
     *
     * @property invoiceId The unique identifier of the invoice that owns the line item.
     * @property invoiceLineItemId The unique identifier of the line item to retrieve.
     */
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
