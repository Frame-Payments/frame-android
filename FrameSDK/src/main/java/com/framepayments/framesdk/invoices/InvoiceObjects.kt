package com.framepayments.framesdk.invoices

import com.framepayments.framesdk.FrameObjects
import com.google.gson.annotations.SerializedName

enum class InvoiceCollectionMethod(val value: String) {
    AUTO_CHARGE("auto_charge"),
    REQUEST_PAYMENT("request_payment")
}

enum class InvoiceStatus(val value: String) {
    DRAFT("draft"),
    OUTSTANDING("outstanding"),
    DUE("due"),
    OVERDUE("overdue"),
    PAID("paid"),
    WRITTEN_OFF("written_off"),
    VOIDED("voided")
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