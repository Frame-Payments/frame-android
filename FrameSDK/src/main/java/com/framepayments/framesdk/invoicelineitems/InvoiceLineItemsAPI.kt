package com.framepayments.framesdk.invoicelineitems

import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError

/**
 * Provides coroutine-based and callback-based methods for managing invoice line items.
 *
 * Each operation returns or delivers a pair of a nullable result and a nullable [NetworkingError].
 * Exactly one of the two will be non-null on completion.
 */
object InvoiceLineItemsAPI {
    //MARK: Methods using coroutines

    /**
     * Creates a new line item on the specified invoice.
     *
     * @param invoiceId The unique identifier of the invoice to add the line item to.
     * @param request The details of the line item to create.
     * @return A [Pair] where the first element is the created [InvoiceLineItem] on success,
     * and the second element is a [NetworkingError] on failure.
     */
    suspend fun createInvoiceLineItem(invoiceId: String, request: InvoiceLineItemRequests.CreateLineItemRequest): Pair<InvoiceLineItem?, NetworkingError?> {
        val endpoint = InvoiceLineItemEndpoints.CreateInvoiceLineItem(invoiceId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret)

        val decodedResponse = data?.let { FrameNetworking.parseResponse<InvoiceLineItem>(data) }
        return Pair(decodedResponse, error)
    }

    /**
     * Updates an existing line item on the specified invoice.
     *
     * @param invoiceId The unique identifier of the invoice that owns the line item.
     * @param invoiceLineItemId The unique identifier of the line item to update.
     * @param request The fields to update on the line item.
     * @return A [Pair] where the first element is the updated [InvoiceLineItem] on success,
     * and the second element is a [NetworkingError] on failure.
     */
    suspend fun updateInvoiceLineItem(invoiceId: String, invoiceLineItemId: String, request: InvoiceLineItemRequests.UpdateLineItemRequest): Pair<InvoiceLineItem?, NetworkingError?> {
        val endpoint = InvoiceLineItemEndpoints.UpdateInvoiceLineItem(invoiceId, invoiceLineItemId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<InvoiceLineItem>(data) }, error)
    }

    /**
     * Retrieves all line items belonging to the specified invoice.
     *
     * @param invoiceId The unique identifier of the invoice whose line items to fetch.
     * @return A [Pair] where the first element is a [InvoiceLineItemResponses.ListInvoiceLineItemsResponse]
     * containing the list on success, and the second element is a [NetworkingError] on failure.
     */
    suspend fun getInvoiceLineItems(invoiceId: String): Pair<InvoiceLineItemResponses.ListInvoiceLineItemsResponse?, NetworkingError?> {
        val endpoint = InvoiceLineItemEndpoints.ListInvoiceLineItems(invoiceId)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<InvoiceLineItemResponses.ListInvoiceLineItemsResponse>(data) }, error)
    }

    /**
     * Retrieves a single line item by its identifier from the specified invoice.
     *
     * @param invoiceId The unique identifier of the invoice that owns the line item.
     * @param invoiceLineItemId The unique identifier of the line item to retrieve.
     * @return A [Pair] where the first element is the matching [InvoiceLineItem] on success,
     * and the second element is a [NetworkingError] on failure.
     */
    suspend fun getInvoiceLineItemWith(invoiceId: String, invoiceLineItemId: String): Pair<InvoiceLineItem?, NetworkingError?> {
        val endpoint = InvoiceLineItemEndpoints.GetInvoiceLineItemWith(invoiceId, invoiceLineItemId)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)

        val decodedResponse = data?.let { FrameNetworking.parseResponse<InvoiceLineItem>(data) }
        return Pair(decodedResponse, error)
    }

    /**
     * Deletes a line item from the specified invoice.
     *
     * @param invoiceId The unique identifier of the invoice that owns the line item.
     * @param invoiceLineItemId The unique identifier of the line item to delete.
     * @return A [Pair] where the first element is a [InvoiceLineItemResponses.DeletedInvoiceLineItemResponse]
     * confirming deletion on success, and the second element is a [NetworkingError] on failure.
     */
    suspend fun deleteInvoiceLineItem(invoiceId: String, invoiceLineItemId: String): Pair<InvoiceLineItemResponses.DeletedInvoiceLineItemResponse?, NetworkingError?> {
        val endpoint = InvoiceLineItemEndpoints.DeleteInvoiceLineItem(invoiceId, invoiceLineItemId)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<InvoiceLineItemResponses.DeletedInvoiceLineItemResponse>(data) }, error)
    }

    //MARK: Methods using callbacks

    /**
     * Creates a new line item on the specified invoice, delivering the result via a callback.
     *
     * @param invoiceId The unique identifier of the invoice to add the line item to.
     * @param request The details of the line item to create.
     * @param completionHandler Called on completion with the created [InvoiceLineItem] on success,
     * or a [NetworkingError] on failure.
     */
    fun createInvoiceLineItem(invoiceId: String, request: InvoiceLineItemRequests.CreateLineItemRequest, completionHandler: (InvoiceLineItem?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceLineItemEndpoints.CreateInvoiceLineItem(invoiceId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret) { data, error ->
            val decodedResponse = data?.let { FrameNetworking.parseResponse<InvoiceLineItem>(data) }
            completionHandler(decodedResponse, error)
        }
    }

    /**
     * Updates an existing line item on the specified invoice, delivering the result via a callback.
     *
     * @param invoiceId The unique identifier of the invoice that owns the line item.
     * @param invoiceLineItemId The unique identifier of the line item to update.
     * @param request The fields to update on the line item.
     * @param completionHandler Called on completion with the updated [InvoiceLineItem] on success,
     * or a [NetworkingError] on failure.
     */
    fun updateInvoiceLineItem(invoiceId: String, invoiceLineItemId: String, request: InvoiceLineItemRequests.UpdateLineItemRequest, completionHandler: (InvoiceLineItem?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceLineItemEndpoints.UpdateInvoiceLineItem(invoiceId, invoiceLineItemId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<InvoiceLineItem>(data) }, error)
        }
    }

    /**
     * Retrieves all line items belonging to the specified invoice, delivering the result via a callback.
     *
     * @param invoiceId The unique identifier of the invoice whose line items to fetch.
     * @param completionHandler Called on completion with a [InvoiceLineItemResponses.ListInvoiceLineItemsResponse]
     * on success, or a [NetworkingError] on failure.
     */
    fun getInvoiceLineItems(invoiceId: String, completionHandler: (InvoiceLineItemResponses.ListInvoiceLineItemsResponse?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceLineItemEndpoints.ListInvoiceLineItems(invoiceId)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<InvoiceLineItemResponses.ListInvoiceLineItemsResponse>(data) }, error)
        }
    }

    /**
     * Retrieves a single line item by its identifier from the specified invoice, delivering the result via a callback.
     *
     * @param invoiceId The unique identifier of the invoice that owns the line item.
     * @param invoiceLineItemId The unique identifier of the line item to retrieve.
     * @param completionHandler Called on completion with the matching [InvoiceLineItem] on success,
     * or a [NetworkingError] on failure.
     */
    fun getInvoiceLineItemWith(invoiceId: String, invoiceLineItemId: String, completionHandler: (InvoiceLineItem?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceLineItemEndpoints.GetInvoiceLineItemWith(invoiceId, invoiceLineItemId)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            val decodedResponse = data?.let { FrameNetworking.parseResponse<InvoiceLineItem>(data) }
            completionHandler(decodedResponse, error)
        }
    }

    /**
     * Deletes a line item from the specified invoice, delivering the result via a callback.
     *
     * @param invoiceId The unique identifier of the invoice that owns the line item.
     * @param invoiceLineItemId The unique identifier of the line item to delete.
     * @param completionHandler Called on completion with a [InvoiceLineItemResponses.DeletedInvoiceLineItemResponse]
     * confirming deletion on success, or a [NetworkingError] on failure.
     */
    fun deleteInvoiceLineItem(invoiceId: String, invoiceLineItemId: String, completionHandler: (InvoiceLineItemResponses.DeletedInvoiceLineItemResponse?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceLineItemEndpoints.DeleteInvoiceLineItem(invoiceId, invoiceLineItemId)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<InvoiceLineItemResponses.DeletedInvoiceLineItemResponse>(data) }, error)
        }
    }
}
