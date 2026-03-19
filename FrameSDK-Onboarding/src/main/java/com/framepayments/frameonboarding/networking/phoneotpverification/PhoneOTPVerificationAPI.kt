package com.framepayments.frameonboarding.networking.phoneotpverification

import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

object PhoneOTPVerificationAPI {

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
        verificationId: String
    ): Pair<PhoneOTPVerificationConfirmResponse?, NetworkingError?> {
        val endpoint = PhoneOTPVerificationEndpoints.Confirm(accountId, verificationId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(
            endpoint,
            EmptyRequest(description = null)
        )
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
        completionHandler: (PhoneOTPVerificationConfirmResponse?, NetworkingError?) -> Unit
    ) {
        val endpoint = PhoneOTPVerificationEndpoints.Confirm(accountId, verificationId)
        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null)) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<PhoneOTPVerificationConfirmResponse>(it) }, error)
        }
    }
}
