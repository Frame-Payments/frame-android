package com.framepayments.framesdk.onboardingsessions

import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Creates onboarding sessions via the Frame API.
 *
 * **Note:** `POST /v1/onboarding_sessions` is secret-key only. The host backend mints the session
 * and passes `onb_sess_…` to the onboarding flow as `clientSecret`.
 */
object OnboardingSessionsAPI {
    /**
     * Mints an onboarding session using the publishable key.
     *
     * The API rejects this unless the merchant is on the legacy client-credential flag. Mint the
     * session from your backend with a secret key and pass `clientSecret` instead. The onboarding
     * flow does not call this.
     *
     * @param request The request body specifying the account and onboarding steps.
     * @return A [Pair] containing the decoded [OnboardingSessionResponses.OnboardingSession] on
     *   success, or a [NetworkingError] on failure.
     */
    @Deprecated("Mint the onboarding session from your backend with sk_. A publishable key cannot create one.")
    suspend fun createOnboardingSessionWithPublishableKey(
        request: OnboardingSessionRequests.CreateOnboardingSessionRequest
    ): Pair<OnboardingSessionResponses.OnboardingSession?, NetworkingError?> {
        val endpoint = OnboardingSessionEndpoints.CreateOnboardingSession
        val (data, error) = FrameNetworking.performDataTaskWithRequest(
            endpoint,
            request,
            auth = FrameAuthMode.PublishableOnly
        )
        return Pair(
            data?.let { FrameNetworking.parseResponse<OnboardingSessionResponses.OnboardingSession>(it) },
            error
        )
    }

    //MARK: Methods using coroutines

    /**
     * Creates an onboarding session and returns its `onb_sess_…` client secret.
     *
     * @param request The request body specifying the account and onboarding steps.
     * @return A [Pair] containing the decoded [OnboardingSessionResponses.OnboardingSession] on
     *   success, or a [NetworkingError] on failure.
     */
    @Deprecated(
        "Server-only — call this from your backend with your secret key (sk_), not from the app. " +
            "Provided only so the example app can mint a token for testing."
    )
    suspend fun createOnboardingSession(
        request: OnboardingSessionRequests.CreateOnboardingSessionRequest
    ): Pair<OnboardingSessionResponses.OnboardingSession?, NetworkingError?> {
        val endpoint = OnboardingSessionEndpoints.CreateOnboardingSession
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(
            data?.let { FrameNetworking.parseResponse<OnboardingSessionResponses.OnboardingSession>(it) },
            error
        )
    }

    //MARK: Methods using callbacks

    /**
     * Callback variant of [createOnboardingSession].
     *
     * @param request The request body specifying the account and onboarding steps.
     * @param completionHandler Invoked with the decoded [OnboardingSessionResponses.OnboardingSession]
     *   on success, or a [NetworkingError] on failure.
     */
    @Deprecated(
        "Server-only — call this from your backend with your secret key (sk_), not from the app. " +
            "Provided only so the example app can mint a token for testing."
    )
    fun createOnboardingSession(
        request: OnboardingSessionRequests.CreateOnboardingSessionRequest,
        completionHandler: (OnboardingSessionResponses.OnboardingSession?, NetworkingError?) -> Unit
    ) {
        val endpoint = OnboardingSessionEndpoints.CreateOnboardingSession
        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(
                data?.let { FrameNetworking.parseResponse<OnboardingSessionResponses.OnboardingSession>(it) },
                error
            )
        }
    }
}
