package com.framepayments.framesdk.onboardingsessions

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Creates onboarding sessions via the Frame API.
 *
 * **Warning:** Creating an onboarding session is a **server-only** operation. It authenticates with
 * your secret key (`sk_`), which must never ship inside an app binary. This API exists so the
 * example app can mint a session token for end-to-end testing — it is **not** the intended
 * production path. Production integrations mint the `onb_sess_…` token from their backend
 * (`POST /v1/onboarding_sessions`) and hand it to the onboarding flow as its `clientSecret`.
 */
object OnboardingSessionsAPI {
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
