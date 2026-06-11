package com.framepayments.frameonboarding.networking.geocompliance

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/** API methods for fetching geofences and account geo-compliance status. */
object GeocomplianceAPI {

    /** Fetches all configured geofences (suspend variant). */
    suspend fun listGeofences(): Pair<GeofencesResponse?, NetworkingError?> {
        val endpoint = GeocomplianceEndpoints.ListGeofences
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<GeofencesResponse>(data) }, error)
    }

    /**
     * Fetches the geo-compliance status for the given account (suspend variant).
     *
     * @param accountId The account to evaluate.
     */
    suspend fun getAccountGeoComplianceStatus(accountId: String): Pair<GeoComplianceStatusResponse?, NetworkingError?> {
        val endpoint = GeocomplianceEndpoints.AccountGeoCompliance(accountId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<GeoComplianceStatusResponse>(data) }, error)
    }

    /** Fetches all configured geofences (callback variant). */
    fun listGeofences(completionHandler: (GeofencesResponse?, NetworkingError?) -> Unit) {
        val endpoint = GeocomplianceEndpoints.ListGeofences

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<GeofencesResponse>(data) }, error)
        }
    }

    /**
     * Fetches the geo-compliance status for the given account (callback variant).
     *
     * @param accountId The account to evaluate.
     * @param completionHandler Called with the status response or a networking error.
     */
    fun getAccountGeoComplianceStatus(
        accountId: String,
        completionHandler: (GeoComplianceStatusResponse?, NetworkingError?) -> Unit
    ) {
        val endpoint = GeocomplianceEndpoints.AccountGeoCompliance(accountId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<GeoComplianceStatusResponse>(data) }, error)
        }
    }
}
