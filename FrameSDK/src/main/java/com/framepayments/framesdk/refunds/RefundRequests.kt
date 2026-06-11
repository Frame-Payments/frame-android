package com.framepayments.framesdk.refunds
import com.google.gson.annotations.SerializedName

/**
 * Namespace for request models used by the Refunds API.
 */
object RefundRequests {

    /**
     * Request body for creating a new refund.
     *
     * @property chargeIntent The ID of the charge intent to refund. Required.
     * @property amount Amount to refund in the smallest currency unit (e.g., cents). Defaults to the full
     *   captured amount when null.
     * @property reason Optional reason for the refund (e.g., "duplicate", "fraudulent", "customer_request").
     */
    data class CreateRefundRequest(
        @SerializedName("charge_intent") val chargeIntent: String,
        val amount: Int? = null,
        val reason: String? = null
    )
}
