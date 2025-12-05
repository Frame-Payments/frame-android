package com.framepayments.framesdk.invoices

import com.framepayments.framesdk.FrameObjects
import com.google.gson.annotations.SerializedName

enum class InvoiceCollectionMethod {
    @SerializedName("auto_charge")
    AUTO_CHARGE,
    @SerializedName("request_payment")
    REQUEST_PAYMENT
}

enum class InvoiceStatus {

    @SerializedName("draft")
    DRAFT,

    @SerializedName("outstanding")
    OUTSTANDING,

    @SerializedName("due")
    DUE,

    @SerializedName("overdue")
    OVERDUE,

    @SerializedName("paid")
    PAID,

    @SerializedName("written_off")
    WRITTEN_OFF,

    @SerializedName("voided")
    VOIDED;
}

data class Invoice(
    val id: String?,
    val customer: FrameObjects.Customer?,
    val total: Int?,
    val currency: String?,
    val status: InvoiceStatus?,
    val memo: String?,
    val livemode: Boolean?,
    val metadata: Map<String, String> = emptyMap(),
    val created: Int?,
    val updated: Int?,
    @SerializedName("object") val invoiceObject: String?,
    @SerializedName("line_items") val lineItems: List<LineItem> = emptyList(),
    @SerializedName("collection_method") val collectionMethod: InvoiceCollectionMethod?,
    @SerializedName("net_terms") val netTerms: Int?,
    @SerializedName("invoice_number") val invoiceNumber: String?,
    @SerializedName("description") val invoiceDescription: String?,
)

data class LineItem(
    val product: String,
    val quantity: Int
)