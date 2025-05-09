package com.framepayments.framesdk.refunds
import com.google.gson.annotations.SerializedName

data class Refund(
    val id: String,
    val charge: String?,
    val currency: String?,
    val description: String?,
    val status: String?,
    val created: Int,
    val updated: Int,
    @SerializedName("amount_captured") val amountCaptured: Int?,
    @SerializedName("amount_refunded") val amountRefunded: Int?,
    @SerializedName("charge_intent") val chargeIntent: String?,
    @SerializedName("failure_reason") val failureReason: String?,
    @SerializedName("object") val refundObject: String?,
)