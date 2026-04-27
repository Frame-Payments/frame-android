package com.framepayments.framesdk.wallet

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class WalletEndpoints : FrameNetworkingEndpoints {
    object GetGooglePayConfiguration : WalletEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is GetGooglePayConfiguration -> "/v1/client/wallet/google_pay"
        }

    override val httpMethod: String = "GET"
    override val queryItems: List<QueryItem>? = null
}
