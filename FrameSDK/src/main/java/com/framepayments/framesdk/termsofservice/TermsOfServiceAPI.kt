package com.framepayments.framesdk.termsofservice

import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Provides suspend and callback-based operations for managing Terms of Service acceptance.
 */
object TermsOfServiceAPI {
    // MARK: Coroutines

    /**
     * Creates a short-lived token used to record a customer's Terms of Service acceptance.
     *
     * @return A pair of a [TermsOfServiceObjects.TermsOfServiceTokenResponse] containing the token and any [NetworkingError] that occurred.
     */
    suspend fun createToken(): Pair<TermsOfServiceObjects.TermsOfServiceTokenResponse?, NetworkingError?> {
        val endpoint = TermsOfServiceEndpoints.CreateToken
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, emptyMap<String, String>(), FrameAuthMode.Publishable)
        return Pair(data?.let { FrameNetworking.parseResponse<TermsOfServiceObjects.TermsOfServiceTokenResponse>(it) }, error)
    }

    /**
     * Records a customer's acceptance of the Terms of Service using a previously created token.
     *
     * @param request The acceptance details including the token and optional metadata.
     * @return A pair of a [TermsOfServiceObjects.TermsOfServiceTokenResponse] and any [NetworkingError] that occurred.
     */
    suspend fun update(request: TermsOfServiceRequests.UpdateRequest): Pair<TermsOfServiceObjects.TermsOfServiceTokenResponse?, NetworkingError?> {
        val endpoint = TermsOfServiceEndpoints.Update
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Publishable)
        return Pair(data?.let { FrameNetworking.parseResponse<TermsOfServiceObjects.TermsOfServiceTokenResponse>(it) }, error)
    }

    // MARK: Callbacks

    /**
     * Creates a short-lived token used to record a customer's Terms of Service acceptance and delivers the result via callback.
     *
     * @param completionHandler Callback invoked with the [TermsOfServiceObjects.TermsOfServiceTokenResponse] and any [NetworkingError].
     */
    fun createToken(completionHandler: (TermsOfServiceObjects.TermsOfServiceTokenResponse?, NetworkingError?) -> Unit) {
        val endpoint = TermsOfServiceEndpoints.CreateToken
        FrameNetworking.performDataTaskWithRequest(endpoint, emptyMap<String, String>(), FrameAuthMode.Publishable) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<TermsOfServiceObjects.TermsOfServiceTokenResponse>(it) }, error)
        }
    }

    /**
     * Records a customer's acceptance of the Terms of Service using a previously created token and delivers the result via callback.
     *
     * @param request The acceptance details including the token and optional metadata.
     * @param completionHandler Callback invoked with the [TermsOfServiceObjects.TermsOfServiceTokenResponse] and any [NetworkingError].
     */
    fun update(request: TermsOfServiceRequests.UpdateRequest, completionHandler: (TermsOfServiceObjects.TermsOfServiceTokenResponse?, NetworkingError?) -> Unit) {
        val endpoint = TermsOfServiceEndpoints.Update
        FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Publishable) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<TermsOfServiceObjects.TermsOfServiceTokenResponse>(it) }, error)
        }
    }
}
