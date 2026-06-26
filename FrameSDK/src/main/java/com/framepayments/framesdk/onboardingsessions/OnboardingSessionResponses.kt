package com.framepayments.framesdk.onboardingsessions
import com.google.gson.annotations.SerializedName

/**
 * Contains response body models returned by onboarding session API calls.
 */
object OnboardingSessionResponses {

    /**
     * The onboarding session returned by `POST /v1/onboarding_sessions`.
     *
     * @property id The unique identifier of the onboarding session.
     * @property accountId The account the session onboards.
     * @property clientSecret The onboarding-session token (`onb_sess_…`) passed to the onboarding
     *   flow as its `clientSecret`.
     * @property returnUrl Where the account holder is redirected after completion, if provided.
     * @property steps The ordered onboarding steps for the session.
     * @property sessionObject The object type. Always `"onboarding_session"`.
     * @property expiresAt The Unix timestamp at which the session token expires.
     * @property livemode `true` for live-mode sessions, `false` in sandbox.
     * @property url The hosted redirect URL for the account holder.
     */
    data class OnboardingSession(
        val id: String?,
        @SerializedName("account_id") val accountId: String?,
        @SerializedName("client_secret") val clientSecret: String?,
        @SerializedName("return_url") val returnUrl: String?,
        val steps: List<String>?,
        @SerializedName("object") val sessionObject: String?,
        @SerializedName("expires_at") val expiresAt: Long?,
        val livemode: Boolean?,
        val url: String?,
    )
}
