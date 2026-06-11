package com.framepayments.frameonboarding.networking.geocompliance

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/** Routing definitions for the Frame geo-compliance API endpoints. */
sealed class GeocomplianceEndpoints : FrameNetworkingEndpoints {
    /** Fetches all geofences configured for the merchant account. */
    data object ListGeofences : GeocomplianceEndpoints()
    /** Fetches the geo-compliance status for a specific account. */
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
