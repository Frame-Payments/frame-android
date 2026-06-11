package com.framepayments.frameonboarding.networking.phoneotpverification

import com.google.gson.annotations.SerializedName

/**
 * Response from the create-phone-verification endpoint.
 *
 * @property id Unique identifier for the verification attempt.
 * @property type The verification type (e.g. `"phone"`).
 * @property status Current status of the verification.
 * @property proveAuthToken Auth token for the Prove mobile SDK; null when using the Twilio OTP path.
 */
data class PhoneOTPVerificationCreateResponse(
    val id: String?,
    val type: String?,
    val status: String?,
    @SerializedName("prove_auth_token") val proveAuthToken: String?
)

/**
 * Response from the confirm-phone-verification endpoint.
 *
 * @property id Unique identifier for the verification attempt.
 * @property status Final status of the verification after confirmation.
 * @property prefillStatus Prefill status from the Prove identity prefill, if applicable.
 */
data class PhoneOTPVerificationConfirmResponse(
    val id: String?,
    val status: String?,
    @SerializedName("prefill_status") val prefillStatus: String? = null
)

/**
 * Error wrapper returned by the phone verification endpoint on validation failure.
 *
 * @property error Structured error detail from the Frame API.
 */
data class PhoneOTPVerificationError(
    val error: ErrorDetail? = null
) {
    /**
     * Structured error detail from the Frame API.
     *
     * @property type Machine-readable error type string.
     * @property message Human-readable error message.
     */
    data class ErrorDetail(
        val type: String? = null,
        val message: String? = null
    )
}
