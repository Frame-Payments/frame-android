package com.framepayments.framesdk.configurations

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
     * @property apiKey The Fingerprint public API key.
     * @property region The Fingerprint region associated with the API key (e.g. "us", "eu", "ap").
     */
    data class GetFingerprintConfigurationResponse(
        @SerializedName("api_key") val apiKey: String? = null,
        @SerializedName("region") val region: String? = null
    )

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
}