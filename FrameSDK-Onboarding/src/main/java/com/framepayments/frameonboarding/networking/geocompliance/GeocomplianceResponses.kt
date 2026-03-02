package com.framepayments.frameonboarding.networking.geocompliance

import com.google.gson.annotations.SerializedName

data class GeofencesResponse(
    val data: List<Geofence>
)

enum class GeoComplianceStatus {
    @SerializedName("clear") CLEAR,
    @SerializedName("blocked") BLOCKED,
    @SerializedName("unknown") UNKNOWN
}

enum class GeoComplianceBlockReason {
    @SerializedName("restricted_territory") RESTRICTED_TERRITORY,
    @SerializedName("vpn_detected") VPN_DETECTED,
    @SerializedName("no_location_data") NO_LOCATION_DATA
}

data class GeoComplianceGeofenceSummary(
    val id: String,
    val name: String
)

data class GeoComplianceStatusResponse(
    val status: GeoComplianceStatus,
    val reason: GeoComplianceBlockReason?,
    val geofence: GeoComplianceGeofenceSummary?,
    @SerializedName("sonar_session_id") val sonarSessionId: String?,
    @SerializedName("evaluated_at") val evaluatedAt: Int
)

