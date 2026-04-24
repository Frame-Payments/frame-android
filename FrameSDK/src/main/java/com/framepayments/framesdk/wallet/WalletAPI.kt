package com.framepayments.framesdk.wallet

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

object WalletAPI {
    //MARK: Methods using coroutines
    suspend fun getGooglePayConfiguration(): Pair<WalletResponses.GetGooglePayConfigurationResponse?, NetworkingError?> {
        val endpoint = WalletEndpoints.GetGooglePayConfiguration
        val (data, error) = FrameNetworking.performDataTask(endpoint, usePublishableKey = true)
        return Pair(data?.let { FrameNetworking.parseResponse<WalletResponses.GetGooglePayConfigurationResponse>(data) }, error)
    }

    //MARK: Methods using callbacks
    fun getGooglePayConfiguration(completionHandler: (WalletResponses.GetGooglePayConfigurationResponse?, NetworkingError?) -> Unit) {
        val endpoint = WalletEndpoints.GetGooglePayConfiguration

        FrameNetworking.performDataTask(endpoint, usePublishableKey = true) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<WalletResponses.GetGooglePayConfigurationResponse>(data) }, error)
        }
    }
}
