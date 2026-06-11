package com.framepayments.framesdk.invoices

import com.google.gson.annotations.SerializedName

/** Holds request body models for invoice API operations. */
object InvoiceRequests {

    /**
     * Request body for creating a new invoice.
     *
     * @property collectionMethod How payment will be collected for this invoice. Required.
     * @property netTerms The number of days from issuance until the invoice is due.
     * @property lineItems The products and quantities to include on the invoice.
     * @property customer The ID of the customer to invoice. Pass null to associate later.
     * @property account The ID of the connected account to create the invoice on behalf of.
     * @property dueDate An explicit due date for the invoice (ISO 8601 date string). Overrides [netTerms] when provided.
     * @property number A merchant-assigned number for the invoice.
     * @property description A customer-facing description of the invoice.
     * @property memo An internal memo visible only to the merchant.
     * @property metadata Arbitrary key-value pairs the merchant can attach for their own reference.
     */
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

    /**
     * Request body for updating an existing invoice.
     *
     * All fields are optional; only non-null values are applied to the invoice.
     *
     * @property collectionMethod Replaces how payment is collected for this invoice.
     * @property netTerms Replaces the number of days from issuance until the invoice is due.
     * @property lineItems Replaces the full list of line items on the invoice.
     * @property dueDate Replaces the explicit due date (ISO 8601 date string).
     * @property autoAdvance When `true`, automatically advances the invoice to the next status when its conditions are met.
     * @property number Replaces the merchant-assigned number for the invoice.
     * @property description Replaces the customer-facing description of the invoice.
     * @property memo Replaces the internal memo visible only to the merchant.
     * @property metadata Replaces the arbitrary key-value pairs attached to the invoice.
     */
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
