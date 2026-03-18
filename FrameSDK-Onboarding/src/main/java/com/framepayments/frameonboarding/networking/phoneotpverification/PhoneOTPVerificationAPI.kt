package com.framepayments.frameonboarding.networking.phoneotpverification

import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Result of creating a phone verification. Use proveAuthToken with the Prove SDK;
 * pass verificationId to confirmVerification after the SDK completes.
 */
data class CreateVerificationResult(
    val proveAuthToken: String,
    val verificationId: String
)

/**
 * API for Frame's account-scoped phone verification endpoints (create and confirm).
 */
class PhoneOTPVerificationAPI {

    /**
     * Create a phone verification. dateOfBirth must be YYYY-MM-DD.
     * Returns the Prove SDK token and verification id for use in confirm.
     */
    suspend fun createVerification(
        accountId: String,
        phoneNumber: String,
        dateOfBirth: String
    ): CreateVerificationResult {
        val endpoint = PhoneOTPVerificationEndpoints.Create(accountId)
        val request = PhoneOTPVerificationRequests.Create(
            phoneNumber = phoneNumber,
            dateOfBirth = dateOfBirth
        )
        val (data, networkingError) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (networkingError != null) {
            throw PhoneOTPVerificationAPIError.Networking(networkingError)
        }

        if (data == null) {
            throw PhoneOTPVerificationAPIError.MissingData
        }

        val createError = FrameNetworking.parseResponse<PhoneOTPVerificationError>(data)?.error
        if (createError != null) {
            throw PhoneOTPVerificationAPIError.Server(
                serverMessage = createError.message ?: "Unknown error"
            )
        }

        val response = FrameNetworking.parseResponse<PhoneOTPVerificationCreateResponse>(data)
            ?: throw PhoneOTPVerificationAPIError.MissingAuthToken

        return CreateVerificationResult(
            proveAuthToken = response.proveAuthToken,
            verificationId = response.id
        )
    }

    /**
     * Confirm the verification after the Prove SDK has completed. Empty body.
     * Prefilled data is stored on the account; fetch via GET account.
     */
    suspend fun confirmVerification(
        accountId: String,
        verificationId: String
    ): PhoneOTPVerificationConfirmResponse {
        val endpoint = PhoneOTPVerificationEndpoints.Confirm(accountId, verificationId)
        val (data, networkingError) = FrameNetworking.performDataTaskWithRequest(
            endpoint,
            EmptyRequest(description = null)
        )

        if (networkingError != null) {
            throw PhoneOTPVerificationAPIError.Networking(networkingError)
        }

        if (data == null) {
            throw PhoneOTPVerificationAPIError.MissingData
        }

        val confirmError = FrameNetworking.parseResponse<PhoneOTPVerificationError>(data)?.error
        if (confirmError != null) {
            throw PhoneOTPVerificationAPIError.Server(
                serverMessage = confirmError.message ?: "Unknown error"
            )
        }

        val response = FrameNetworking.parseResponse<PhoneOTPVerificationConfirmResponse>(data)
            ?: throw PhoneOTPVerificationAPIError.DecodingFailed

        return response
    }
}

sealed class PhoneOTPVerificationAPIError : Exception() {
    data object MissingData : PhoneOTPVerificationAPIError()
    data object MissingAuthToken : PhoneOTPVerificationAPIError()
    data object DecodingFailed : PhoneOTPVerificationAPIError()
    data class Networking(val error: NetworkingError) : PhoneOTPVerificationAPIError()
    data class Server(val serverMessage: String) : PhoneOTPVerificationAPIError() {
        override val message: String get() = serverMessage
    }
}
