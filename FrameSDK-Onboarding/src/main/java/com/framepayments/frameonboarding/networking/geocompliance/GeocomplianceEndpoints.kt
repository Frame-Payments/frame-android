package com.framepayments.frameonboarding.networking.geocompliance

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class GeocomplianceEndpoints : FrameNetworkingEndpoints {
    data object ListGeofences : GeocomplianceEndpoints()
    data class AccountGeoCompliance(val accountId: String) : GeocomplianceEndpoints()

    override val endpointURL: String
        get() = when (this) {
            ListGeofences -> "/v1/geofences"
            is AccountGeoCompliance -> "/v1/accounts/$accountId/geo_compliance"
        }

    override val httpMethod: String
        get() = "GET"

    override val queryItems: List<QueryItem>?
        get() = null
}

