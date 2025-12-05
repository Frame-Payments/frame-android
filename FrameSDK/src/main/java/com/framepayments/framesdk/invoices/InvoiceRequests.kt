package com.framepayments.framesdk.invoices

import com.google.gson.annotations.SerializedName

object InvoiceRequests {
    data class CreateInvoiceRequest (
        @SerializedName("collection_method") val collectionMethod: InvoiceCollectionMethod,
        @SerializedName("net_terms") val netTerms: Int?,
        @SerializedName("line_items") val lineItems: List<LineItem>?,
        val customer: String,
        val number: String?,
        val description: String?,
        val memo: String?,
        val metadata: Map<String,String>?
    )

    data class UpdateInvoiceRequest(
        @SerializedName("collection_method") val collectionMethod: InvoiceCollectionMethod,
        @SerializedName("net_terms") val netTerms: Int?,
        @SerializedName("line_items") val lineItems: List<LineItem>?,
        val number: String?,
        val description: String?,
        val memo: String?,
        val metadata: Map<String,String>?
    )
}