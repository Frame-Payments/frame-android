package com.framepayments.frameonboarding.networking.phoneotpverification

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

object PhoneOTPVerificationAPI {

    private suspend fun confirmRequestBody(code: String?): Any =
        if (code == null) emptyMap<String, String>() else PhoneOTPVerificationRequests.Confirm(code)

    // Coroutine methods
    suspend fun createVerification(
        accountId: String,
        phoneNumber: String,
        dateOfBirth: String
    ): Pair<PhoneOTPVerificationCreateResponse?, NetworkingError?> {
        val endpoint = PhoneOTPVerificationEndpoints.Create(accountId)
        val request = PhoneOTPVerificationRequests.Create(
            phoneNumber = phoneNumber,
            dateOfBirth = dateOfBirth
        )
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<PhoneOTPVerificationCreateResponse>(it) }, error)
    }

    suspend fun confirmVerification(
        accountId: String,
        verificationId: String,
        code: String? = null
    ): Pair<PhoneOTPVerificationConfirmResponse?, NetworkingError?> {
        val endpoint = PhoneOTPVerificationEndpoints.Confirm(accountId, verificationId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, confirmRequestBody(code))
        return Pair(data?.let { FrameNetworking.parseResponse<PhoneOTPVerificationConfirmResponse>(it) }, error)
    }

    // Callback methods
    fun createVerification(
        accountId: String,
        phoneNumber: String,
        dateOfBirth: String,
        completionHandler: (PhoneOTPVerificationCreateResponse?, NetworkingError?) -> Unit
    ) {
        val endpoint = PhoneOTPVerificationEndpoints.Create(accountId)
        val request = PhoneOTPVerificationRequests.Create(
            phoneNumber = phoneNumber,
            dateOfBirth = dateOfBirth
        )
        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<PhoneOTPVerificationCreateResponse>(it) }, error)
        }
    }

    fun confirmVerification(
        accountId: String,
        verificationId: String,
        code: String? = null,
        completionHandler: (PhoneOTPVerificationConfirmResponse?, NetworkingError?) -> Unit
    ) {
        val endpoint = PhoneOTPVerificationEndpoints.Confirm(accountId, verificationId)
        val body: Any = if (code == null) emptyMap<String, String>() else PhoneOTPVerificationRequests.Confirm(code)
        FrameNetworking.performDataTaskWithRequest(endpoint, body) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<PhoneOTPVerificationConfirmResponse>(it) }, error)
        }
    }
}
