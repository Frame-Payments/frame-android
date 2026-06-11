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