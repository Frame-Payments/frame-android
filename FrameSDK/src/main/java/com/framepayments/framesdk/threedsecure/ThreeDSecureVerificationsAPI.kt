package com.framepayments.framesdk.threedsecure

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

object ThreeDSecureVerificationsAPI {

    suspend fun create3DSecureVerification(
        request: ThreeDSecureRequests.CreateThreeDSecureVerification
    ): Triple<ThreeDSecureVerification?, ThreeDSecureVerificationError?, NetworkingError?> {
        val endpoint = ThreeDSecureEndpoints.Create3DSecureVerification
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        if (data != null) {
            val verification = FrameNetworking.parseResponse<ThreeDSecureVerification>(data)
            if (verification != null && (verification.id?.isNotEmpty() == true)) return Triple(verification, null, error)
            val verificationError = FrameNetworking.parseResponse<ThreeDSecureVerificationError>(data)
            if (verificationError != null && (verificationError.error?.type?.isNotEmpty() == true)) return Triple(null, verificationError, error)
        }
        return Triple(null, null, error)
    }

    suspend fun retrieve3DSecureVerification(
        verificationId: String
    ): Pair<ThreeDSecureVerification?, NetworkingError?> {
        val endpoint = ThreeDSecureEndpoints.Retrieve3DSecureVerification(verificationId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<ThreeDSecureVerification>(data) }, error)
    }

    suspend fun resend3DSecureVerification(
        verificationId: String
    ): Pair<ThreeDSecureVerification?, NetworkingError?> {
        val endpoint = ThreeDSecureEndpoints.Resend3DSecureVerification(verificationId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(
            endpoint,
            com.framepayments.framesdk.EmptyRequest(description = null)
        )
        return Pair(data?.let { FrameNetworking.parseResponse<ThreeDSecureVerification>(data) }, error)
    }

    fun create3DSecureVerification(
        request: ThreeDSecureRequests.CreateThreeDSecureVerification,
        completionHandler: (ThreeDSecureVerification?, ThreeDSecureVerificationError?, NetworkingError?) -> Unit
    ) {
        val endpoint = ThreeDSecureEndpoints.Create3DSecureVerification
        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            if (data != null) {
                val verification = FrameNetworking.parseResponse<ThreeDSecureVerification>(data)
                if (verification != null && (verification.id?.isNotEmpty() == true)) {
                    completionHandler(verification, null, error)
                    return@performDataTaskWithRequest
                }
                val verificationError = FrameNetworking.parseResponse<ThreeDSecureVerificationError>(data)
                if (verificationError != null && (verificationError.error?.type?.isNotEmpty() == true)) {
                    completionHandler(null, verificationError, error)
                    return@performDataTaskWithRequest
                }
            }
            completionHandler(null, null, error)
        }
    }

    fun retrieve3DSecureVerification(
        verificationId: String,
        completionHandler: (ThreeDSecureVerification?, NetworkingError?) -> Unit
    ) {
        val endpoint = ThreeDSecureEndpoints.Retrieve3DSecureVerification(verificationId)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<ThreeDSecureVerification>(data) }, error)
        }
    }

    fun resend3DSecureVerification(
        verificationId: String,
        completionHandler: (ThreeDSecureVerification?, NetworkingError?) -> Unit
    ) {
        val endpoint = ThreeDSecureEndpoints.Resend3DSecureVerification(verificationId)
        FrameNetworking.performDataTaskWithRequest(
            endpoint,
            com.framepayments.framesdk.EmptyRequest(description = null)
        ) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<ThreeDSecureVerification>(data) }, error)
        }
    }
}
