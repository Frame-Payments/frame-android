package com.framepayments.framesdk.configurations

import com.framepayments.framesdk.fingerprint.FingerprintCapability
import com.google.gson.annotations.SerializedName

/** Contains response models for configuration API calls. */
object ConfigurationResponses {
    /**
     * Holds the Evervault credentials returned by the configuration endpoint.
     *
     * @property appId The Evervault application identifier.
     * @property teamId The Evervault team identifier.
     */
    data class GetEvervaultConfigurationResponse(
        @SerializedName("app_id") val appId: String? = null,
        @SerializedName("team_id") val teamId: String? = null
    )

    /**
     * Holds the Fingerprint credentials returned by the configuration endpoint.
     *
     * The three fields are one credential, not three settings. A key only works in the region
     * it was minted for, and [environment] names the regime that key belongs to — so they are
     * cached and read as a unit, never merged field by field against an older copy.
     *
     * @property apiKey The Fingerprint public API key.
     * @property region The Fingerprint region associated with the API key (e.g. "us", "eu", "ap").
     * @property environment The Fingerprint environment the key belongs to (`"sealed"` or `"legacy"`). The API answers a capability it did not recognize with the legacy key and HTTP 200, so this stamp is the only way a client can tell which regime it was actually served.
     */
    data class GetFingerprintConfigurationResponse(
        @SerializedName("api_key") val apiKey: String? = null,
        @SerializedName("region") val region: String? = null,
        @SerializedName("environment") val environment: String? = null
    ) {
        /** Whether the served credentials belong to the sealed environment. */
        val isSealed: Boolean
            get() = environment == FingerprintCapability.SEALED

        /**
         * Whether the response names the environment its key belongs to. A cached copy without
         * one was written before this build began declaring a capability, so nothing about it
         * says which regime its key is from.
         */
        val hasEnvironmentStamp: Boolean
            get() = !environment.isNullOrEmpty()
    }

    /**
     * Holds the Sift credentials returned by the configuration endpoint.
     *
     * @property accountId The Sift account identifier.
     * @property beaconKey The Sift beacon key used for device fingerprinting.
     */
    data class GetSiftConfigurationResponse(
        @SerializedName("account_id") val accountId: String? = null,
        @SerializedName("beacon_key") val beaconKey: String? = null
    )

    /**
     * Holds Frame's legal document URLs returned by the configuration endpoint.
     *
     * @property privacyUrl URL of Frame's Privacy Policy.
     * @property termsUrl URL of Frame's Terms of Service.
     * @property platformAgreementUrl URL of Frame's Platform Agreement.
     * @property cbcTermsAndConditions URL of Frame's Card-Based Cash Terms & Conditions.
     */
    data class GetLegalConfigurationResponse(
        @SerializedName("privacy_url") val privacyUrl: String? = null,
        @SerializedName("terms_url") val termsUrl: String? = null,
        @SerializedName("platform_agreement_url") val platformAgreementUrl: String? = null,
        @SerializedName("cbc_terms_and_conditions") val cbcTermsAndConditions: String? = null
    )

    /**
     * Holds the search-scoped Mapbox access token used for address autocomplete.
     *
     * @property accessToken The Mapbox access token, scoped to search and geocoding only.
     * @property expiresAt When the token stops working, as an ISO-8601 string, or null when it does not expire.
     */
    data class GetMapboxConfigurationResponse(
        @SerializedName("access_token") val accessToken: String? = null,
        @SerializedName("expires_at") val expiresAt: String? = null
    ) {
        /** Whether [expiresAt] is in the past. False when absent or unparseable. */
        val hasExpired: Boolean
            get() {
                val raw = expiresAt ?: return false
                val expiry = runCatching { java.time.Instant.parse(raw) }.getOrNull() ?: return false
                return expiry <= java.time.Instant.now()
            }
    }

    /**
     * Holds every configuration block in one payload, from `GET /v1/config/all`.
     *
     * Blocks are optional because the API omits one whose service failed rather than nulling
     * it, so an absent block leaves the cached copy intact.
     *
     * @property evervault The Evervault block, if the service succeeded.
     * @property fingerprint The Fingerprint block, if the service succeeded.
     * @property legal The legal-URLs block, if the service succeeded.
     * @property mapbox The Mapbox block, if the service succeeded.
     * @property sift The Sift block, if the service succeeded.
     */
    data class GetAllConfigurationResponse(
        val evervault: GetEvervaultConfigurationResponse? = null,
        val fingerprint: GetFingerprintConfigurationResponse? = null,
        val legal: GetLegalConfigurationResponse? = null,
        val mapbox: GetMapboxConfigurationResponse? = null,
        val sift: GetSiftConfigurationResponse? = null
    )
}