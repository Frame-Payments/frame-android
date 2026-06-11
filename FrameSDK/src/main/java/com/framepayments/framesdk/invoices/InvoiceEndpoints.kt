package com.framepayments.framesdk.invoices

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines the API endpoints available for invoice operations.
 *
 * Each case maps to a specific URL path, HTTP method, and optional query parameters
 * used by the networking layer to construct requests.
 */
sealed class InvoiceEndpoints : FrameNetworkingEndpoints {

    /** Endpoint for creating a new invoice (`POST /v1/invoices`). */
    object CreateInvoice: InvoiceEndpoints()

    /**
     * Endpoint for updating an existing invoice (`PATCH /v1/invoices/{invoiceId}`).
     *
     * @property invoiceId The unique identifier of the invoice to update.
     */
    data class UpdateInvoice(val invoiceId: String): InvoiceEndpoints()

    /**
     * Endpoint for retrieving a paginated, optionally filtered list of invoices (`GET /v1/invoices`).
     *
     * @property perPage The number of results to return per page.
     * @property page The page number to retrieve.
     * @property customer The customer ID to filter results by.
     * @property account The account ID to filter results by.
     * @property status The [InvoiceStatus] to filter results by.
     */
    data class GetInvoices(val perPage: Int? = null, val page: Int? = null, val customer: String? = null, val account: String? = null, val status: InvoiceStatus? = null): InvoiceEndpoints()

    /**
     * Endpoint for retrieving a single invoice by identifier (`GET /v1/invoices/{invoiceId}`).
     *
     * @property invoiceId The unique identifier of the invoice to retrieve.
     */
    data class GetInvoiceWith(val invoiceId: String): InvoiceEndpoints()

    /**
     * Endpoint for deleting an invoice (`DELETE /v1/invoices/{invoiceId}`).
     *
     * @property invoiceId The unique identifier of the invoice to delete.
     */
    data class DeleteInvoice(val invoiceId: String): InvoiceEndpoints()

    /**
     * Endpoint for issuing a draft invoice to the customer (`POST /v1/invoices/{invoiceId}/issue`).
     *
     * @property invoiceId The unique identifier of the invoice to issue.
     */
    data class IssueInvoice(val invoiceId: String): InvoiceEndpoints()

    /** The URL path for this endpoint. */
    override val endpointURL: String
        get() = when (this) {
            is CreateInvoice, is GetInvoices ->
                "/v1/invoices"
            is UpdateInvoice ->
                "/v1/invoices/${this.invoiceId}"
            is DeleteInvoice ->
                "/v1/invoices/${this.invoiceId}"
            is GetInvoiceWith ->
                "/v1/invoices/${this.invoiceId}"
            is IssueInvoice ->
                "/v1/invoices/${this.invoiceId}/issue"
        }

    /** The HTTP method for this endpoint. */
    override val httpMethod: String
        get() = when (this) {
            is CreateInvoice, is IssueInvoice -> "POST"
            is UpdateInvoice -> "PATCH"
            is DeleteInvoice -> "DELETE"
            else -> "GET"
        }

    /** The query parameters to append to the request URL, or null if none apply. */
    override val queryItems: List<QueryItem>?
        get() = when (this) {
            is GetInvoices -> {
                val items = mutableListOf<QueryItem>()
                perPage?.let { items.add(QueryItem("per_page", it.toString())) }
                page?.let { items.add(QueryItem("page", it.toString())) }
                customer?.let { items.add(QueryItem("customer", it)) }
                account?.let { items.add(QueryItem("account", it)) }
                status?.let { items.add(QueryItem("status", it.name.lowercase())) }
                items
            }
            else -> null
        }
}
