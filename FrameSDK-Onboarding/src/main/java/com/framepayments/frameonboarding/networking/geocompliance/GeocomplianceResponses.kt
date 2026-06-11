package com.framepayments.frameonboarding.networking.geocompliance

import com.google.gson.annotations.SerializedName

/**
 * Response envelope for the list-geofences endpoint.
 *
 * @property data The list of geofences configured for the account.
 */
data class GeofencesResponse(
    val data: List<Geofence>?
)

/** The geo-compliance status for an account. */
enum class GeoComplianceStatus {
    /** The account is in a permitted location. */
    @SerializedName("clear") CLEAR,
    /** The account is in a blocked location. */
    @SerializedName("blocked") BLOCKED,
    /** The account's location could not be determined. */
    @SerializedName("unknown") UNKNOWN
}

/** The reason an account's geo-compliance check returned [GeoComplianceStatus.BLOCKED]. */
enum class GeoComplianceBlockReason {
    /** The device is in a restricted geographic territory. */
    @SerializedName("restricted_territory") RESTRICTED_TERRITORY,
    /** A VPN was detected that masks the device's real location. */
    @SerializedName("vpn_detected") VPN_DETECTED,
    /** Location data was unavailable or not granted. */
    @SerializedName("no_location_data") NO_LOCATION_DATA
}

/**
 * A brief summary of the geofence involved in a geo-compliance evaluation.
 *
 * @property id Unique identifier of the matched geofence.
 * @property name Human-readable name of the matched geofence.
 */
data class GeoComplianceGeofenceSummary(
    val id: String?,
    val name: String?
)

/**
 * Full geo-compliance status for an account.
 *
 * @property status The compliance verdict for the account's current location.
 * @property reason The reason for a [GeoComplianceStatus.BLOCKED] result, or null if clear.
 * @property geofence The geofence that triggered the block, or null if not applicable.
 * @property sonarSessionId The Sonar session used to collect location data.
 * @property evaluatedAt Unix timestamp of when the compliance check was performed.
 */
data class GeoComplianceStatusResponse(
    val status: GeoComplianceStatus?,
    val reason: GeoComplianceBlockReason?,
    val geofence: GeoComplianceGeofenceSummary?,
    @SerializedName("sonar_session_id") val sonarSessionId: String?,
    @SerializedName("evaluated_at") val evaluatedAt: Int?
)
