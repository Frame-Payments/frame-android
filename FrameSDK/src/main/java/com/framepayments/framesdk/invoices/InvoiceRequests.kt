package com.framepayments.framesdk.invoices

import com.google.gson.annotations.SerializedName

object InvoiceRequests {
    data class CreateInvoiceRequest (
        @SerializedName("collection_method") val collectionMethod: InvoiceCollectionMethod,
        @SerializedName("net_terms") val netTerms: Int?,
        @SerializedName("line_items") val lineItems: List<LineItem>?,
        val customer: String? = null,
        val account: String? = null,
        @SerializedName("due_date") val dueDate: String? = null,
        val number: String?,
        val description: String?,
        val memo: String?,
        val metadata: Map<String,String>?
    )

    data class UpdateInvoiceRequest(
        @SerializedName("collection_method") val collectionMethod: InvoiceCollectionMethod? = null,
        @SerializedName("net_terms") val netTerms: Int? = null,
        @SerializedName("line_items") val lineItems: List<LineItem>? = null,
        @SerializedName("due_date") val dueDate: String? = null,
        @SerializedName("auto_advance") val autoAdvance: Boolean? = null,
        val number: String? = null,
        val description: String? = null,
        val memo: String? = null,
        val metadata: Map<String,String>? = null
    )
}