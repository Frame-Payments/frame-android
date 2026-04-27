package com.framepayments.framesdk.wallet

import com.google.gson.annotations.SerializedName

object WalletResponses {
    data class GetGooglePayConfigurationResponse(
        val identifier: String,
        val environment: String,
        val processor: String,
        @SerializedName("processor_key") val processorKey: String
    )
}
