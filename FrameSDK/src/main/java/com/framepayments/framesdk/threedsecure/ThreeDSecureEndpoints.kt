package com.framepayments.framesdk.threedsecure

import com.framepayments.framesdk.FrameNetworkingEndpoints

/**
 * Defines the network endpoints for the 3D Secure Verifications API.
 *
 * Each case maps to a specific API route and HTTP method used by [ThreeDSecureVerificationsAPI].
 */
sealed class ThreeDSecureEndpoints : FrameNetworkingEndpoints {

    /** Endpoint for creating a new 3D Secure verification intent (POST /v1/3ds/intents). */
    object Create3DSecureVerification : ThreeDSecureEndpoints()

    /**
     * Endpoint for retrieving an existing 3D Secure verification intent (GET /v1/3ds/intents/{id}).
     *
     * @property verificationId The unique identifier of the verification to retrieve.
     */
    data class Retrieve3DSecureVerification(val verificationId: String) : ThreeDSecureEndpoints()

    /**
     * Endpoint for resending a 3D Secure verification challenge (POST /v1/3ds/intents/{id}/resend).
     *
     * @property verificationId The unique identifier of the verification whose challenge to resend.
     */
    data class Resend3DSecureVerification(val verificationId: String) : ThreeDSecureEndpoints()

    /** The resolved URL path for this endpoint. */
    override val endpointURL: String
        get() = when (this) {
            is Create3DSecureVerification -> "/v1/3ds/intents"
            is Retrieve3DSecureVerification -> "/v1/3ds/intents/${verificationId}"
            is Resend3DSecureVerification -> "/v1/3ds/intents/${verificationId}/resend"
        }

    /** The HTTP method for this endpoint. */
    override val httpMethod: String
        get() = when (this) {
            is Retrieve3DSecureVerification -> "GET"
            else -> "POST"
        }

    /** Query parameters for this endpoint; always null for 3D Secure endpoints. */
    override val queryItems: List<com.framepayments.framesdk.QueryItem>?
        get() = null
}
