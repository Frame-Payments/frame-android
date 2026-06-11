package com.framepayments.framesdk.invoicelineitems
import com.google.gson.annotations.SerializedName

/**
 * Represents a single line item attached to an invoice.
 *
 * @property id The unique identifier of this line item.
 * @property description A human-readable description of the line item.
 * @property quantity The number of units included in this line item.
 * @property created A Unix timestamp (seconds) indicating when the line item was created.
 * @property updated A Unix timestamp (seconds) indicating when the line item was last updated.
 * @property lineItemObject The object type string returned by the API (e.g. `"line_item"`).
 * @property unitAmountCents The price per unit expressed in the smallest currency unit (e.g. cents).
 * @property unitAmountCurrency The numeric currency code for [unitAmountCents].
 */
data class InvoiceLineItem(
    val id: String?,
    val description: String?,
    val quantity: Int?,
    val created: Int?,
    val updated: Int?,
    @SerializedName("object") val lineItemObject: String?,
    @SerializedName("unit_amount_cents") val unitAmountCents: Int?,
    @SerializedName("unit_amount_currency") val unitAmountCurrency: Int?
)
