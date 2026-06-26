package com.framepayments.framesdk.wallet

import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Provides wallet-related API operations, including Google Pay configuration retrieval.
 *
 * Each operation is available as both a coroutine suspend function and a callback-based overload.
 */
object WalletAPI {
    //MARK: Methods using coroutines

    /**
     * Fetches the Google Pay merchant configuration using the merchant's publishable key.
     *
     * @return A [Pair] where the first element is the [WalletResponses.GetGooglePayConfigurationResponse]
     *   on success, and the second element is a [NetworkingError] on failure; one of the two will be null.
     */
    suspend fun getGooglePayConfiguration(): Pair<WalletResponses.GetGooglePayConfigurationResponse?, NetworkingError?> {
        val endpoint = WalletEndpoints.GetGooglePayConfiguration
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Publishable)
        return Pair(data?.let { FrameNetworking.parseResponse<WalletResponses.GetGooglePayConfigurationResponse>(data) }, error)
    }

    //MARK: Methods using callbacks

    /**
     * Fetches the Google Pay merchant configuration using the merchant's publishable key and
     * delivers the result to the provided callback.
     *
     * @param completionHandler Invoked with the parsed [WalletResponses.GetGooglePayConfigurationResponse]
     *   on success, or a [NetworkingError] on failure; one of the two arguments will be null.
     */
    fun getGooglePayConfiguration(completionHandler: (WalletResponses.GetGooglePayConfigurationResponse?, NetworkingError?) -> Unit) {
        val endpoint = WalletEndpoints.GetGooglePayConfiguration

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Publishable) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<WalletResponses.GetGooglePayConfigurationResponse>(data) }, error)
        }
    }
}
