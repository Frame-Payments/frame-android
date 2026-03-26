package com.framepayments.frameonboarding.networking.phoneotpverification

import com.google.gson.annotations.SerializedName

data class PhoneOTPVerificationCreateResponse(
    val id: String,
    val type: String,
    val status: String,
    @SerializedName("prove_auth_token") val proveAuthToken: String?
)

data class PhoneOTPVerificationConfirmResponse(
    val id: String,
    val status: String,
    @SerializedName("prefill_status") val prefillStatus: String? = null
)

data class PhoneOTPVerificationError(
    val error: ErrorDetail? = null
) {
    data class ErrorDetail(
        val type: String? = null,
        val message: String? = null
    )
}
