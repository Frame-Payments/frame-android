package com.framepayments.framesdk.refunds
import com.google.gson.annotations.SerializedName

object RefundRequests {
    data class CreateRefundRequest(
        val amount: Int,
        val charge: String,
        val reason: String,
        @SerializedName("charge_intent") val chargeIntent: String,
    )
}