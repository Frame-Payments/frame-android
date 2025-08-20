package com.framepayments.framesdk.configurations

import com.google.gson.annotations.SerializedName

object ConfigurationResponses {
    data class GetEvervaultConfigurationResponse(
        @SerializedName("app_id") val appId: String? = null,
        @SerializedName("team_id") val teamId: String? = null
    )

    data class GetSiftConfigurationResponse(
        @SerializedName("account_id") val accountId: String? = null,
        @SerializedName("beacon_key") val beaconKey: String? = null
    )
}