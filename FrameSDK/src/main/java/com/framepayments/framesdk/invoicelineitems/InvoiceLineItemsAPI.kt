package com.framepayments.framesdk.invoicelineitems

import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError

object InvoiceLineItemsAPI {
    //MARK: Methods using coroutines
    suspend fun createInvoiceLineItem(invoiceId: String, request: InvoiceLineItemRequests.CreateLineItemRequest): Pair<InvoiceLineItem?, NetworkingError?> {
        val endpoint = InvoiceLineItemEndpoints.CreateInvoiceLineItem(invoiceId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        val decodedResponse = data?.let { FrameNetworking.parseResponse<InvoiceLineItem>(data) }
        return Pair(decodedResponse, error)
    }

    suspend fun updateInvoiceLineItem(invoiceId: String, invoiceLineItemId: String, request: InvoiceLineItemRequests.UpdateLineItemRequest): Pair<InvoiceLineItem?, NetworkingError?> {
        val endpoint = InvoiceLineItemEndpoints.UpdateInvoiceLineItem(invoiceId, invoiceLineItemId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<InvoiceLineItem>(data) }, error)
    }

    suspend fun getInvoiceLineItems(invoiceId: String): Pair<InvoiceLineItemResponses.ListInvoiceLineItemsResponse?, NetworkingError?> {
        val endpoint = InvoiceLineItemEndpoints.ListInvoiceLineItems(invoiceId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<InvoiceLineItemResponses.ListInvoiceLineItemsResponse>(data) }, error)
    }

    suspend fun getInvoiceLineItemWith(invoiceId: String, invoiceLineItemId: String): Pair<InvoiceLineItem?, NetworkingError?> {
        val endpoint = InvoiceLineItemEndpoints.GetInvoiceLineItemWith(invoiceId, invoiceLineItemId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)

        val decodedResponse = data?.let { FrameNetworking.parseResponse<InvoiceLineItem>(data) }
        return Pair(decodedResponse, error)
    }

    suspend fun deleteInvoiceLineItem(invoiceId: String, invoiceLineItemId: String): Pair<InvoiceLineItemResponses.DeletedInvoiceLineItemResponse?, NetworkingError?> {
        val endpoint = InvoiceLineItemEndpoints.DeleteInvoiceLineItem(invoiceId, invoiceLineItemId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<InvoiceLineItemResponses.DeletedInvoiceLineItemResponse>(data) }, error)
    }

    //MARK: Methods using callbacks
    fun createInvoiceLineItem(invoiceId: String, request: InvoiceLineItemRequests.CreateLineItemRequest, completionHandler: (InvoiceLineItem?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceLineItemEndpoints.CreateInvoiceLineItem(invoiceId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            val decodedResponse = data?.let { FrameNetworking.parseResponse<InvoiceLineItem>(data) }
            completionHandler(decodedResponse, error)
        }
    }

    fun updateInvoiceLineItem(invoiceId: String, invoiceLineItemId: String, request: InvoiceLineItemRequests.UpdateLineItemRequest, completionHandler: (InvoiceLineItem?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceLineItemEndpoints.UpdateInvoiceLineItem(invoiceId, invoiceLineItemId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<InvoiceLineItem>(data) }, error)
        }
    }

    fun getInvoiceLineItems(invoiceId: String, completionHandler: (InvoiceLineItemResponses.ListInvoiceLineItemsResponse?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceLineItemEndpoints.ListInvoiceLineItems(invoiceId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<InvoiceLineItemResponses.ListInvoiceLineItemsResponse>(data) }, error)
        }
    }

    fun getInvoiceLineItemWith(invoiceId: String, invoiceLineItemId: String, completionHandler: (InvoiceLineItem?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceLineItemEndpoints.GetInvoiceLineItemWith(invoiceId, invoiceLineItemId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            val decodedResponse = data?.let { FrameNetworking.parseResponse<InvoiceLineItem>(data) }
            completionHandler(decodedResponse, error)
        }
    }

    fun deleteInvoiceLineItem(invoiceId: String, invoiceLineItemId: String, completionHandler: (InvoiceLineItemResponses.DeletedInvoiceLineItemResponse?, NetworkingError?) -> Unit) {
        val endpoint = InvoiceLineItemEndpoints.DeleteInvoiceLineItem(invoiceId, invoiceLineItemId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<InvoiceLineItemResponses.DeletedInvoiceLineItemResponse>(data) }, error)
        }
    }
}
