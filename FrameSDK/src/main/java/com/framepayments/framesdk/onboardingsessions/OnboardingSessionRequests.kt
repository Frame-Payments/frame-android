package com.framepayments.framesdk.onboardingsessions
import com.google.gson.annotations.SerializedName

/**
 * Contains request body models used when creating onboarding sessions.
 *
 * **Note:** `POST /v1/onboarding_sessions` is secret-key only. The host backend mints the session
 * and passes `onb_sess_…` to the onboarding flow as `clientSecret`.
 */
object OnboardingSessionRequests {

    /**
     * An onboarding step the session can require. Gson serializes each constant by its
     * [SerializedName] wire value.
     */
    enum class OnboardingSessionStep {
        /** Identity verification (KYC). */
        @SerializedName("id_verification") ID_VERIFICATION,

        /** Geographic-compliance verification. */
        @SerializedName("geo_compliance") GEO_COMPLIANCE,

        /** Payment-method collection. */
        @SerializedName("payment_method") PAYMENT_METHOD,
    }

    /**
     * Request body for creating an onboarding session.
     *
     * @property accountId The existing Frame account the session onboards.
     * @property steps The ordered onboarding steps to present, or `null` to use the account's defaults.
     * @property returnUrl Where the account holder is redirected after completing the flow, or `null`.
     */
    data class CreateOnboardingSessionRequest(
        @SerializedName("account_id") val accountId: String,
        val steps: List<OnboardingSessionStep>? = null,
        @SerializedName("return_url") val returnUrl: String? = null,
    )
}
