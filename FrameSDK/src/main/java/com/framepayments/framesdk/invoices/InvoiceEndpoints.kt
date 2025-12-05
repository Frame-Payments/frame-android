package com.framepayments.framesdk.invoices

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class InvoiceEndpoints : FrameNetworkingEndpoints {
    object CreateInvoice: InvoiceEndpoints()
    data class UpdateInvoice(val invoiceId: String): InvoiceEndpoints()
    data class GetInvoices(val perPage: Int? = null, val page: Int? = null, val customer: String? = null, val status: InvoiceStatus? = null): InvoiceEndpoints()
    data class GetInvoiceWith(val invoiceId: String): InvoiceEndpoints()
    data class DeleteInvoice(val invoiceId: String): InvoiceEndpoints()
    data class IssueInvoice(val invoiceId: String): InvoiceEndpoints()

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

    override val httpMethod: String
        get() = when (this) {
            is CreateInvoice, is IssueInvoice -> "POST"
            is UpdateInvoice -> "PATCH"
            is DeleteInvoice -> "DELETE"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>?
        get() = when (this) {
            is GetInvoices -> {
                val items = mutableListOf<QueryItem>()
                perPage?.let { items.add(QueryItem("per_page", it.toString())) }
                page?.let { items.add(QueryItem("page", it.toString())) }
                customer?.let { items.add(QueryItem("customer",it.toString())) }
                status?.let { items.add(QueryItem("status", it.toString())) }
                items
            }
            else -> null
        }
}