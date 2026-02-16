package com.framepayments.framesdk.threedsecure

import com.framepayments.framesdk.FrameNetworkingEndpoints

sealed class ThreeDSecureEndpoints : FrameNetworkingEndpoints {
    object Create3DSecureVerification : ThreeDSecureEndpoints()
    data class Retrieve3DSecureVerification(val verificationId: String) : ThreeDSecureEndpoints()
    data class Resend3DSecureVerification(val verificationId: String) : ThreeDSecureEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is Create3DSecureVerification -> "/v1/3ds/intents"
            is Retrieve3DSecureVerification -> "/v1/3ds/intents/${verificationId}"
            is Resend3DSecureVerification -> "/v1/3ds/intents/${verificationId}/resend"
        }

    override val httpMethod: String
        get() = when (this) {
            is Retrieve3DSecureVerification -> "GET"
            else -> "POST"
        }

    override val queryItems: List<com.framepayments.framesdk.QueryItem>?
        get() = null
}
