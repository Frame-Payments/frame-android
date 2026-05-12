package com.framepayments.framesdk.transfers
import com.google.gson.annotations.SerializedName

object TransferRequests {
    data class CreateTransferRequest(
        val amount: Int,
        @SerializedName("account_id") val accountId: String,
        val currency: String? = null,
        @SerializedName("source_payment_method_id") val sourcePaymentMethodId: String? = null,
        @SerializedName("destination_payment_method_id") val destinationPaymentMethodId: String? = null,
        val description: String? = null,
        val metadata: Map<String, String>? = null
    )
}
