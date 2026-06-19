package com.framepayments.framesdk.invoices

import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/** Provides coroutine and callback entry points for all invoice-related API operations. */
object InvoicesAPI {
    //MARK: Methods using coroutines

    /**
     * Creates a new invoice.
     *
     * @param request The parameters for the invoice to create.
     * @return A pair containing the created [Invoice] on success, or a [NetworkingError] on failure.
     */
    suspend fun createInvoice(request: InvoiceRequests.CreateInvoiceRequest): Pair<Invoice?, NetworkingError?> {
        val endpoint = InvoiceEndpoints.CreateInvoice
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret)

        val decodedResponse = data?.let { FrameNetworking.parseResponse<Invoice>(data) }
        return Pair(decodedResponse, error)
    }

    /**
     * Updates an existing invoice.
     *
     * @param invoiceId The unique identifier of the invoice to update.
     * @param request The fields to update on the invoice.
     * @return A pair containing the updated [Invoice] on success, or a [NetworkingError] on failure.
     */
    suspend fun updateInvoice(invoiceId: String, request: InvoiceRequests.UpdateInvoiceRequest): Pair<Invoice?, NetworkingError?> {
        val endpoint = InvoiceEndpoints.UpdateInvoice(invoiceId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<Invoice>(data) }, error)
    }

    /**
     * Retrieves a paginated list of invoices, optionally filtered by customer, account, or status.
     *
     * @param page The page number to retrieve. Defaults to the first page when null.
     * @param perPage The number of invoices per page. Uses the API default when null.
     * @param customer The customer ID to filter results by. Returns invoices for all customers when null.
     * @param account The account ID to filter results by. Returns invoices for all accounts when null.
     * @param status The [InvoiceStatus] to filter results by. Returns invoices of all statuses when null.
     * @return A pair containing a [InvoiceResponses.ListInvoicesResponse] on success, or a [NetworkingError] on failure.
     */
    suspend fun getInvoices(page: Int? = null, perPage: Int? = null, customer: String? = null, account: String? = null, status: InvoiceStatus? = null): Pair<InvoiceResponses.ListInvoicesResponse?, NetworkingError?> {
        val endpoint = InvoiceEndpoints.GetInvoices(perPage = perPage, page = page, customer = customer, account = account, status = status)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<InvoiceResponses.ListInvoicesResponse>(data) }, error)
    }

    /**
     * Retrieves a single invoice by its identifier.
     *
     * @param invoiceId The unique identifier of the invoice to retrieve.
     * @return A pair containing the matching [Invoice] on success, or a [NetworkingError] on failure.
     */
    suspend fun getInvoiceWith(invoiceId: String): Pair<Invoice?, NetworkingError?> {
        val endpoint = InvoiceEndpoints.GetInvoiceWith(invoiceId)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)

        val decodedResponse = data?.let { FrameNetworking.parseResponse<Invoice>(data) }
        return Pair(decodedResponse, error)
    }

    /**
     * Deletes an invoice by its identifier.
     *
     * @param invoiceId The unique identifier of the invoice to delete.
     * @return A pair containing a [InvoiceResponses.DeletedInvoiceResponse] confirming deletion on success, or a [NetworkingError] on failure.
     */
    suspend fun deleteInvoice(invoiceId: String): Pair<InvoiceResponses.DeletedInvoiceResponse?, NetworkingError?> {
        val endpoint = InvoiceEndpoints.DeleteInvoice(invoiceId)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<InvoiceResponses.DeletedInvoiceResponse>(data) }, error)
    }

    /**
     * Issues a draft invoice, making it visible to the customer.
     *
     * @param invoiceId The unique identifier of the invoice to issue.
     * @return A pair containing the issued [Invoice] on success, or a [NetworkingError] on failure.
     */
    suspend fun issueInvoice(invoiceId: String): Pair<Invoice?, NetworkingError?> {
        val endpoint = InvoiceEndpoints.IssueInvoice(invoiceId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null), FrameAuthMode.Secret)

        val decodedResponse = data?.let { FrameNetworking.parseResponse<Invoice>(data) }
        return Pair(decodedResponse, error)
    }

    //MARK: Methods using callbacks

    /**
     * Creates a new invoice and delivers the result via a callback.
     *
     * @param request The parameters for the invoice to create.
     * @param completionHandler Invoked on completion with the created [Invoice] on success, or a [NetworkingError] on failure.
     */
    fun createInvoice(request: InvoiceRequests.CreateInvoiceRequest, completionHandler: (Invoice?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceEndpoints.CreateInvoice

        FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret) { data, error ->
            val decodedResponse = data?.let { FrameNetworking.parseResponse<Invoice>(data) }
            completionHandler(decodedResponse, error)
        }
    }

    /**
     * Updates an existing invoice and delivers the result via a callback.
     *
     * @param invoiceId The unique identifier of the invoice to update.
     * @param request The fields to update on the invoice.
     * @param completionHandler Invoked on completion with the updated [Invoice] on success, or a [NetworkingError] on failure.
     */
    fun updateInvoice(invoiceId: String, request: InvoiceRequests.UpdateInvoiceRequest, completionHandler: (Invoice?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceEndpoints.UpdateInvoice(invoiceId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Invoice>(data) }, error)
        }
    }

    /**
     * Retrieves a paginated list of invoices and delivers the result via a callback.
     *
     * @param page The page number to retrieve. Defaults to the first page when null.
     * @param perPage The number of invoices per page. Uses the API default when null.
     * @param customer The customer ID to filter results by. Returns invoices for all customers when null.
     * @param account The account ID to filter results by. Returns invoices for all accounts when null.
     * @param status The [InvoiceStatus] to filter results by. Returns invoices of all statuses when null.
     * @param completionHandler Invoked on completion with a [InvoiceResponses.ListInvoicesResponse] on success, or a [NetworkingError] on failure.
     */
    fun getInvoices(page: Int? = null, perPage: Int? = null, customer: String? = null, account: String? = null, status: InvoiceStatus? = null, completionHandler: (InvoiceResponses.ListInvoicesResponse?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceEndpoints.GetInvoices(perPage = perPage, page = page, customer = customer, account = account, status = status)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<InvoiceResponses.ListInvoicesResponse>(data) }, error)
        }
    }

    /**
     * Retrieves a single invoice by its identifier and delivers the result via a callback.
     *
     * @param invoiceId The unique identifier of the invoice to retrieve.
     * @param completionHandler Invoked on completion with the matching [Invoice] on success, or a [NetworkingError] on failure.
     */
    fun getInvoiceWith(invoiceId: String, completionHandler: (Invoice?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceEndpoints.GetInvoiceWith(invoiceId)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Invoice>(data) }, error)
        }
    }

    /**
     * Deletes an invoice by its identifier and delivers the result via a callback.
     *
     * @param invoiceId The unique identifier of the invoice to delete.
     * @param completionHandler Invoked on completion with a [InvoiceResponses.DeletedInvoiceResponse] confirming deletion on success, or a [NetworkingError] on failure.
     */
    fun deleteInvoice(invoiceId: String, completionHandler: (InvoiceResponses.DeletedInvoiceResponse?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceEndpoints.DeleteInvoice(invoiceId)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<InvoiceResponses.DeletedInvoiceResponse>(data) }, error)
        }
    }

    /**
     * Issues a draft invoice and delivers the result via a callback.
     *
     * @param invoiceId The unique identifier of the invoice to issue.
     * @param completionHandler Invoked on completion with the issued [Invoice] on success, or a [NetworkingError] on failure.
     */
    fun issueInvoice(invoiceId: String, completionHandler: (Invoice?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceEndpoints.IssueInvoice(invoiceId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null), FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Invoice>(data) }, error)
        }
    }
}
