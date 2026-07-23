package com.framepayments.framesdk.onboardingsessions

import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Creates onboarding sessions via the Frame API.
 *
 * **Note:** `POST /v1/onboarding_sessions` accepts a **publishable key** (`pk_`), so the onboarding
 * flow can mint its own account-scoped session on-device without a secret key — see
 * [createOnboardingSessionWithPublishableKey]. The deprecated [createOnboardingSession] methods
 * below default to the configured key (typically `sk_` in the example app) and remain for backward
 * compatibility; production integrations that mint from their own backend hand the resulting
 * `onb_sess_…` to the onboarding flow as its `clientSecret`.
 */
object OnboardingSessionsAPI {
    /**
     * Mints an onboarding session using the SDK's **publishable key** (`pk_`), the client-safe
     * credential accepted by `POST /v1/onboarding_sessions`. The onboarding flow calls this to bind
     * itself to a freshly-created account so subsequent requests (e.g. IDV) authenticate as the
     * session rather than falling back to the configured key. Not deprecated — unlike
     * [createOnboardingSession], this never sends a secret key.
     *
     * @param request The request body specifying the account and onboarding steps.
     * @return A [Pair] containing the decoded [OnboardingSessionResponses.OnboardingSession] on
     *   success, or a [NetworkingError] on failure.
     */
    suspend fun createOnboardingSessionWithPublishableKey(
        request: OnboardingSessionRequests.CreateOnboardingSessionRequest
    ): Pair<OnboardingSessionResponses.OnboardingSession?, NetworkingError?> {
        val endpoint = OnboardingSessionEndpoints.CreateOnboardingSession
        val (data, error) = FrameNetworking.performDataTaskWithRequest(
            endpoint,
            request,
            auth = FrameAuthMode.Publishable
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
