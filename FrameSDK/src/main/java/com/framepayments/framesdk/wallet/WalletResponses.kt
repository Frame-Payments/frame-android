package com.framepayments.framesdk.wallet

import com.google.gson.annotations.SerializedName

/**
 * Contains response model types returned by wallet API operations.
 */
object WalletResponses {

    /**
     * The Google Pay merchant configuration returned by the wallet API.
     *
     * @property identifier The merchant identifier registered with Google Pay.
     * @property environment The Google Pay environment (e.g., `"TEST"` or `"PRODUCTION"`).
     * @property processor The payment processor associated with this merchant.
     * @property processorKey The public key or token used to communicate with the processor.
     */
    data class GetGooglePayConfigurationResponse(
        val identifier: String?,
        val environment: String?,
        val processor: String?,
        @SerializedName("processor_key") val processorKey: String?
    )
}
