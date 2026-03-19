package com.framepayments.frameonboarding.networking.phoneotpverification

import com.framepayments.framesdk.FrameNetworkingEndpoints

sealed class PhoneOTPVerificationEndpoints : FrameNetworkingEndpoints {
    data class Create(val accountId: String) : PhoneOTPVerificationEndpoints()
    data class Confirm(val accountId: String, val verificationId: String) : PhoneOTPVerificationEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is Create -> "/v1/accounts/$accountId/phone_verifications"
            is Confirm -> "/v1/accounts/$accountId/phone_verifications/$verificationId/confirm"
        }

    override val httpMethod: String
        get() = "POST"

    override val queryItems: List<com.framepayments.framesdk.QueryItem>?
        get() = null
}
