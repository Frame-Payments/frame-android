package com.framepayments.framesdk.threedsecure

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Provides suspend and callback-based functions for managing 3D Secure verifications.
 */
object ThreeDSecureVerificationsAPI {

    /**
     * Creates a new 3D Secure verification intent.
     *
     * Returns a [Triple] whose first slot holds the created [ThreeDSecureVerification] on success.
     * When the API returns a structured error (e.g., an existing intent conflict), the second slot
     * is populated with a [ThreeDSecureVerificationError] and the first slot is null.
     * A transport-level failure populates the third slot with a [NetworkingError].
     *
     * @param request The request payload containing the payment method to verify.
     * @return A [Triple] of ([ThreeDSecureVerification]?, [ThreeDSecureVerificationError]?, [NetworkingError]?).
     */
    suspend fun create3DSecureVerification(
        request: ThreeDSecureRequests.CreateThreeDSecureVerification
    ): Triple<ThreeDSecureVerification?, ThreeDSecureVerificationError?, NetworkingError?> {
        val endpoint = ThreeDSecureEndpoints.Create3DSecureVerification
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        if (data != null) {
            val verificationError = FrameNetworking.parseResponse<ThreeDSecureVerificationError>(data)
            val errType = verificationError?.error?.type
            if (!errType.isNullOrEmpty()) {
                return Triple(null, verificationError, error)
            }
            val verification = FrameNetworking.parseResponse<ThreeDSecureVerification>(data)
            if (verification != null && !verification.id.isNullOrEmpty()) return Triple(verification, null, null)
        }
        return Triple(null, null, error)
    }

    /**
     * Retrieves an existing 3D Secure verification by its identifier.
     *
     * @param verificationId The unique identifier of the verification to retrieve.
     * @return A [Pair] of ([ThreeDSecureVerification]?, [NetworkingError]?).
     */
    suspend fun retrieve3DSecureVerification(
        verificationId: String
    ): Pair<ThreeDSecureVerification?, NetworkingError?> {
        val endpoint = ThreeDSecureEndpoints.Retrieve3DSecureVerification(verificationId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<ThreeDSecureVerification>(data) }, error)
    }

    /**
     * Resends a 3D Secure verification challenge to the customer.
     *
     * @param verificationId The unique identifier of the verification to resend.
     * @return A [Pair] of ([ThreeDSecureVerification]?, [NetworkingError]?).
     */
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

    /**
     * Creates a new 3D Secure verification intent and delivers the result via a callback.
     *
     * When the API returns a structured error (e.g., an existing intent conflict), the second
     * parameter of [completionHandler] is populated with a [ThreeDSecureVerificationError].
     * A transport-level failure populates the third parameter with a [NetworkingError].
     *
     * @param request The request payload containing the payment method to verify.
     * @param completionHandler Callback invoked with ([ThreeDSecureVerification]?, [ThreeDSecureVerificationError]?, [NetworkingError]?).
     */
    fun create3DSecureVerification(
        request: ThreeDSecureRequests.CreateThreeDSecureVerification,
        completionHandler: (ThreeDSecureVerification?, ThreeDSecureVerificationError?, NetworkingError?) -> Unit
    ) {
        val endpoint = ThreeDSecureEndpoints.Create3DSecureVerification
        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            if (data != null) {
                val verificationError = FrameNetworking.parseResponse<ThreeDSecureVerificationError>(data)
                val errType = verificationError?.error?.type
                if (!errType.isNullOrEmpty()) {
                    completionHandler(null, verificationError, error)
                    return@performDataTaskWithRequest
                }
                val verification = FrameNetworking.parseResponse<ThreeDSecureVerification>(data)
                if (verification != null && !verification.id.isNullOrEmpty()) {
                    completionHandler(verification, null, null)
                    return@performDataTaskWithRequest
                }
            }
            completionHandler(null, null, error)
        }
    }

    /**
     * Retrieves an existing 3D Secure verification by its identifier and delivers the result via a callback.
     *
     * @param verificationId The unique identifier of the verification to retrieve.
     * @param completionHandler Callback invoked with ([ThreeDSecureVerification]?, [NetworkingError]?).
     */
    fun retrieve3DSecureVerification(
        verificationId: String,
        completionHandler: (ThreeDSecureVerification?, NetworkingError?) -> Unit
    ) {
        val endpoint = ThreeDSecureEndpoints.Retrieve3DSecureVerification(verificationId)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<ThreeDSecureVerification>(data) }, error)
        }
    }

    /**
     * Resends a 3D Secure verification challenge to the customer and delivers the result via a callback.
     *
     * @param verificationId The unique identifier of the verification to resend.
     * @param completionHandler Callback invoked with ([ThreeDSecureVerification]?, [NetworkingError]?).
     */
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
