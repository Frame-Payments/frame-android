package com.framepayments.framesdk.onboardingsessions
import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines the API endpoint used to create onboarding sessions.
 *
 * Implements [FrameNetworkingEndpoints] so the networking layer can dispatch requests uniformly.
 */
sealed class OnboardingSessionEndpoints : FrameNetworkingEndpoints {

    /** Creates a new onboarding session (`POST /v1/onboarding_sessions`). */
    object CreateOnboardingSession : OnboardingSessionEndpoints()

    /** Returns the relative URL path for this endpoint. */
    override val endpointURL: String
        get() = when (this) {
            is CreateOnboardingSession -> "/v1/onboarding_sessions"
        }

    /** Returns the HTTP method string for this endpoint. */
    override val httpMethod: String
        get() = when (this) {
            is CreateOnboardingSession -> "POST"
        }

    /** Returns the URL query parameters for this endpoint, or `null` if none apply. */
    override val queryItems: List<QueryItem>?
        get() = when (this) {
            is CreateOnboardingSession -> null
        }
}
