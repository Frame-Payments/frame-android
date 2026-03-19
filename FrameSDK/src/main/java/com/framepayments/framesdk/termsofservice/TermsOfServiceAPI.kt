package com.framepayments.framesdk.termsofservice

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

object TermsOfServiceAPI {
    // MARK: Coroutines

    suspend fun createToken(): Pair<TermsOfServiceObjects.TermsOfServiceTokenResponse?, NetworkingError?> {
        val endpoint = TermsOfServiceEndpoints.CreateToken
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, emptyMap<String, String>())
        return Pair(data?.let { FrameNetworking.parseResponse<TermsOfServiceObjects.TermsOfServiceTokenResponse>(it) }, error)
    }

    suspend fun update(request: TermsOfServiceRequests.UpdateRequest): Pair<TermsOfServiceObjects.TermsOfServiceTokenResponse?, NetworkingError?> {
        val endpoint = TermsOfServiceEndpoints.Update
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<TermsOfServiceObjects.TermsOfServiceTokenResponse>(it) }, error)
    }

    // MARK: Callbacks

    fun createToken(completionHandler: (TermsOfServiceObjects.TermsOfServiceTokenResponse?, NetworkingError?) -> Unit) {
        val endpoint = TermsOfServiceEndpoints.CreateToken
        FrameNetworking.performDataTaskWithRequest(endpoint, emptyMap<String, String>()) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<TermsOfServiceObjects.TermsOfServiceTokenResponse>(it) }, error)
        }
    }

    fun update(request: TermsOfServiceRequests.UpdateRequest, completionHandler: (TermsOfServiceObjects.TermsOfServiceTokenResponse?, NetworkingError?) -> Unit) {
        val endpoint = TermsOfServiceEndpoints.Update
        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<TermsOfServiceObjects.TermsOfServiceTokenResponse>(it) }, error)
        }
    }
}
