package com.framepayments.frameonboarding.networking.phoneotpverification

import com.framepayments.framesdk.FrameNetworkingEndpoints

/** Routing definitions for the phone OTP verification API endpoints. */
sealed class PhoneOTPVerificationEndpoints : FrameNetworkingEndpoints {
    /** Creates a new phone verification for the given account. */
    data class Create(val accountId: String) : PhoneOTPVerificationEndpoints()
    /** Confirms an existing phone verification with an OTP code. */
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
