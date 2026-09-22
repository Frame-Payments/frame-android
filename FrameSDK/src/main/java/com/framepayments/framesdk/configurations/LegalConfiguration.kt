package com.framepayments.framesdk.configurations

import com.framepayments.framesdk.FrameNetworking

/**
 * Sync accessors for Frame's legal document URLs (Privacy Policy, Terms of Service, Platform
 * Agreement, and Card-Based Cash Terms & Conditions).
 *
 * URLs are fetched from the Frame configuration API and cached, then read synchronously by SDK
 * UI. If the fetch fails and no cached value is present, each accessor falls back to a
 * hardcoded URL so the SDK never renders a broken link.
 */
object LegalConfiguration {
    private const val FALLBACK_PRIVACY_URL = "https://framepayments.com/legal/privacy"
    private const val FALLBACK_TERMS_URL = "https://framepayments.com/legal/terms"
    private const val FALLBACK_PLATFORM_AGREEMENT_URL = "https://framepayments.com/legal/platform-agreement"
    private const val FALLBACK_CBC_TERMS_URL = "https://framepayments.com/legal/cbc-terms-and-conditions"

    // Memoized after the first successful read: `SecureConfigurationStorage.retrieve` rebuilds
    // EncryptedSharedPreferences (and its Keystore-backed MasterKey) on every call, so reading it
    // synchronously from every accessor below -- as these are commonly read during Compose
    // composition -- repeated that cost on every recomposition instead of once.
    @Volatile
    private var memoized: ConfigurationResponses.GetLegalConfigurationResponse? = null

    /** Fetches and caches the legal configuration. Call once at SDK init so the accessors below have a warm cache. */
    suspend fun prefetch() {
        memoized = ConfigurationAPI.getLegalConfiguration() ?: memoized
    }

    private fun cached(): ConfigurationResponses.GetLegalConfigurationResponse? {
        memoized?.let { return it }
        val fromStorage = SecureConfigurationStorage.retrieve<ConfigurationResponses.GetLegalConfigurationResponse>(
            FrameNetworking.getContext(),
            "legal"
        )
        if (fromStorage != null) memoized = fromStorage
        return fromStorage
    }

    private fun url(raw: String?, fallback: String): String = raw?.takeIf { it.isNotBlank() } ?: fallback

    /** URL of Frame's Privacy Policy, sourced from the configuration API cache or falling back to a bundled default. */
    val privacyUrl: String
        get() = url(cached()?.privacyUrl, FALLBACK_PRIVACY_URL)

    /** URL of Frame's Terms of Service, sourced from the configuration API cache or falling back to a bundled default. */
    val termsUrl: String
        get() = url(cached()?.termsUrl, FALLBACK_TERMS_URL)

    /** URL of Frame's Platform Agreement, sourced from the configuration API cache or falling back to a bundled default. */
    val platformAgreementUrl: String
        get() = url(cached()?.platformAgreementUrl, FALLBACK_PLATFORM_AGREEMENT_URL)

    /** URL of Frame's Card-Based Cash (CBC) Terms & Conditions, sourced from the configuration API cache or falling back to a bundled default. */
    val cbcTermsUrl: String
        get() = url(cached()?.cbcTermsAndConditions, FALLBACK_CBC_TERMS_URL)
}
