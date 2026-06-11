package com.framepayments.frameonboarding.networking.phoneotpverification

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/** API methods for creating and confirming phone OTP verifications. */
object PhoneOTPVerificationAPI {

    private suspend fun confirmRequestBody(code: String?): Any =
        if (code == null) emptyMap<String, String>() else PhoneOTPVerificationRequests.Confirm(code)

    /**
     * Creates a phone verification for the given account (suspend variant).
     *
     * @param accountId The account to associate the verification with.
     * @param phoneNumber Customer phone number in E.164 format.
     * @param dateOfBirth Customer date of birth in YYYY-MM-DD format.
     */
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

    /**
     * Confirms a pending phone verification (suspend variant).
     *
     * @param accountId The account the verification belongs to.
     * @param verificationId The verification ID returned by [createVerification].
     * @param code OTP code entered by the customer; null for Prove-path (empty body).
     */
    suspend fun confirmVerification(
        accountId: String,
        verificationId: String,
        code: String? = null
    ): Pair<PhoneOTPVerificationConfirmResponse?, NetworkingError?> {
        val endpoint = PhoneOTPVerificationEndpoints.Confirm(accountId, verificationId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, confirmRequestBody(code))
        return Pair(data?.let { FrameNetworking.parseResponse<PhoneOTPVerificationConfirmResponse>(it) }, error)
    }

    /**
     * Creates a phone verification for the given account (callback variant).
     *
     * @param accountId The account to associate the verification with.
     * @param phoneNumber Customer phone number in E.164 format.
     * @param dateOfBirth Customer date of birth in YYYY-MM-DD format.
     * @param completionHandler Called with the response or a networking error.
     */
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

    /**
     * Confirms a pending phone verification (callback variant).
     *
     * @param accountId The account the verification belongs to.
     * @param verificationId The verification ID returned by [createVerification].
     * @param code OTP code entered by the customer; null for Prove-path (empty body).
     * @param completionHandler Called with the response or a networking error.
     */
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
