package com.framepayments.framesdk.invoicelineitems
import com.google.gson.annotations.SerializedName

data class InvoiceLineItem(
    val id: String,
    val description: String?,
    val quantity: Int,
    val created: Int,
    val updated: Int,
    @SerializedName("object") val lineItemObject: String?,
    @SerializedName("unit_amount_cents") val unitAmountCents: Int?,
    @SerializedName("unit_amount_currency") val unitAmountCurrency: Int?
)
