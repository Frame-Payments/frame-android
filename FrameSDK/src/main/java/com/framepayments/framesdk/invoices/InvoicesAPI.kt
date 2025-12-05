package com.framepayments.framesdk.invoices

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

object InvoicesAPI {
    //MARK: Methods using coroutines
    suspend fun createInvoice(request: InvoiceRequests.CreateInvoiceRequest): Pair<Invoice?, NetworkingError?> {
        val endpoint = InvoiceEndpoints.CreateInvoice
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        val decodedResponse = data?.let { FrameNetworking.parseResponse<Invoice>(data) }
        return Pair(decodedResponse, error)
    }

    suspend fun updateInvoice(invoiceId: String, request: InvoiceRequests.UpdateInvoiceRequest): Pair<Invoice?, NetworkingError?> {
        val endpoint = InvoiceEndpoints.UpdateInvoice(invoiceId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<Invoice>(data) }, error)
    }

    suspend fun getInvoices(page: Int? = null, perPage: Int? = null): Pair<InvoiceResponses.ListInvoicesResponse?, NetworkingError?> {
        val endpoint = InvoiceEndpoints.GetInvoices(perPage = perPage, page = page)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<InvoiceResponses.ListInvoicesResponse>(data) }, error)
    }

    suspend fun getInvoiceWith(invoiceId: String): Pair<Invoice?, NetworkingError?> {
        val endpoint = InvoiceEndpoints.GetInvoiceWith(invoiceId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)

        val decodedResponse = data?.let { FrameNetworking.parseResponse<Invoice>(data) }
        return Pair(decodedResponse, error)
    }

    suspend fun deleteInvoice(invoiceId: String): Pair<Invoice?, NetworkingError?> {
        val endpoint = InvoiceEndpoints.DeleteInvoice(invoiceId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<Invoice>(data) }, error)
    }

    suspend fun issueInvoice(invoiceId: String): Pair<Invoice?, NetworkingError?> {
        val endpoint = InvoiceEndpoints.IssueInvoice(invoiceId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)

        val decodedResponse = data?.let { FrameNetworking.parseResponse<Invoice>(data) }
        return Pair(decodedResponse, error)
    }

    //MARK: Methods using callbacks
    fun createInvoice(request: InvoiceRequests.CreateInvoiceRequest, completionHandler: (Invoice?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceEndpoints.CreateInvoice

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            val decodedResponse = data?.let { FrameNetworking.parseResponse<Invoice>(data) }
            completionHandler(decodedResponse, error)
        }
    }

    fun updateInvoice(invoiceId: String, request: InvoiceRequests.UpdateInvoiceRequest, completionHandler: (Invoice?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceEndpoints.UpdateInvoice(invoiceId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Invoice>(data) }, error)
        }
    }

    fun getInvoices(page: Int? = null, perPage: Int? = null, completionHandler: (InvoiceResponses.ListInvoicesResponse?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceEndpoints.GetInvoices(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<InvoiceResponses.ListInvoicesResponse>(data) }, error)
        }
    }

    fun getInvoiceWith(invoiceId: String, completionHandler: (Invoice?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceEndpoints.GetInvoiceWith(invoiceId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Invoice>(data) }, error)
        }
    }

    fun deleteInvoice(invoiceId: String, completionHandler: (Invoice?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceEndpoints.DeleteInvoice(invoiceId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Invoice>(data) }, error)
        }
    }

    fun issueInvoice(invoiceId: String, completionHandler: (Invoice?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceEndpoints.IssueInvoice(invoiceId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Invoice>(data) }, error)
        }
    }
}
