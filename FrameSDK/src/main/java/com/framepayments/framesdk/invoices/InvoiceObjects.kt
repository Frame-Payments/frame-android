package com.framepayments.framesdk.invoices

import com.framepayments.framesdk.FrameObjects
import com.google.gson.annotations.SerializedName

/** Specifies how payment is collected for an invoice. */
enum class InvoiceCollectionMethod {
    /** Automatically charges the customer's payment method on the due date. */
    @SerializedName("auto_charge") AUTO_CHARGE,
    /** Sends the customer a payment request and waits for them to pay manually. */
    @SerializedName("request_payment") REQUEST_PAYMENT
}

/** Represents the lifecycle state of an invoice. */
enum class InvoiceStatus {
    /** The invoice has been created but not yet issued to the customer. */
    @SerializedName("draft") DRAFT,
    /** The invoice has been issued and payment is expected but not yet due. */
    @SerializedName("outstanding") OUTSTANDING,
    /** The invoice payment is due today. */
    @SerializedName("due") DUE,
    /** The invoice payment is past its due date. */
    @SerializedName("overdue") OVERDUE,
    /** The invoice has been fully paid. */
    @SerializedName("paid") PAID,
    /** The invoice has been written off as uncollectable. */
    @SerializedName("written_off") WRITTEN_OFF,
    /** The invoice has been voided and is no longer payable. */
    @SerializedName("voided") VOIDED;
}

/**
 * Represents an invoice issued to a customer.
 *
 * @property id Unique identifier for the invoice.
 * @property customer The customer this invoice was issued to.
 * @property total The total amount due, in the smallest currency unit (e.g., cents).
 * @property currency The ISO 4217 currency code for the invoice amount.
 * @property status The current lifecycle status of the invoice.
 * @property memo An internal memo visible only to the merchant.
 * @property livemode Whether the invoice was created in live mode (`true`) or test mode (`false`).
 * @property metadata Arbitrary key-value pairs the merchant can attach for their own reference.
 * @property created Unix timestamp of when the invoice was created.
 * @property updated Unix timestamp of when the invoice was last updated.
 * @property invoiceObject The object type identifier returned by the API (e.g., `"invoice"`).
 * @property lineItems The list of line items included on the invoice.
 * @property collectionMethod How payment is collected for this invoice.
 * @property netTerms The number of days from issuance until the invoice is due.
 * @property invoiceNumber The merchant-assigned number for this invoice.
 * @property invoiceDescription A customer-facing description of the invoice.
 */
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

/**
 * Represents a single line item on an invoice.
 *
 * @property product The identifier of the product associated with this line item.
 * @property quantity The number of units of the product included.
 */
data class LineItem(
    val product: String?,
    val quantity: Int?
)
