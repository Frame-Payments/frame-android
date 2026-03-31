package com.framepayments.framesdk.refunds
import com.google.gson.annotations.SerializedName

object RefundRequests {
    data class CreateRefundRequest(
        @SerializedName("charge_intent") val chargeIntent: String,
        val amount: Int? = null,
        val reason: String? = null
    )
}