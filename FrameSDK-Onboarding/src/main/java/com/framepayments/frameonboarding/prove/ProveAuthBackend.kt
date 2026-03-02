package com.framepayments.frameonboarding.prove

/**
 * Backend that provides Prove auth token and verify. Implement with your backend
 * (e.g. Frame proxy or app server) that calls Prove Start and Validate/Challenge.
 */
interface ProveAuthBackend {
    /**
     * Obtain an auth token for the Prove SDK (e.g. from your backend's Prove Start
     * with phone, DOB, flow type "mobile").
     */
    suspend fun getAuthToken(
        phoneNumber: String,
        dateOfBirth: String,
        flowType: String
    ): String

    /**
     * Verify the auth session and return user info (e.g. from your backend's
     * Prove Validate/Challenge).
     */
    suspend fun verify(authId: String): ProveUserInfo
}
