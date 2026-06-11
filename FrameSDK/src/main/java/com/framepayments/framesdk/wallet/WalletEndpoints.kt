package com.framepayments.framesdk.wallet

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines the API endpoints used by wallet operations.
 *
 * Each case maps to a specific HTTP method and URL path used by [WalletAPI].
 */
sealed class WalletEndpoints : FrameNetworkingEndpoints {

    /**
     * Endpoint for retrieving the Google Pay merchant configuration.
     *
     * Sends a GET request to `/v1/client/wallet/google_pay`.
     */
    object GetGooglePayConfiguration : WalletEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is GetGooglePayConfiguration -> "/v1/client/wallet/google_pay"
        }

    override val httpMethod: String = "GET"
    override val queryItems: List<QueryItem>? = null
}
