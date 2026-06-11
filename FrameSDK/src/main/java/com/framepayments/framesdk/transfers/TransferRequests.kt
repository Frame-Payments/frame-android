package com.framepayments.framesdk.transfers

import com.google.gson.annotations.SerializedName

/**
 * Contains request payload models for the Transfers API.
 */
object TransferRequests {

    /**
     * Request payload for creating a new transfer.
     *
     * @property amount Transfer amount in the smallest currency unit (e.g., cents). Required.
     * @property accountId Identifier of the destination account to receive the transfer. Required.
     * @property currency Three-letter ISO 4217 currency code. Defaults to the account's currency if omitted.
     * @property sourcePaymentMethodId Identifier of the payment method to pull funds from. Uses the account default if omitted.
     * @property destinationPaymentMethodId Identifier of the payment method to push funds to. Uses the account default if omitted.
     * @property description Optional description that appears on the transfer record.
     * @property metadata Optional arbitrary key-value pairs to attach to the transfer.
     */
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
