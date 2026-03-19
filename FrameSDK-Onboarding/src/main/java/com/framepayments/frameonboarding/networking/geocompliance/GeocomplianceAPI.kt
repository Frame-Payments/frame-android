package com.framepayments.frameonboarding.networking.geocompliance

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

object GeocomplianceAPI {

    // Methods using coroutines
    suspend fun listGeofences(): Pair<GeofencesResponse?, NetworkingError?> {
        val endpoint = GeocomplianceEndpoints.ListGeofences
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<GeofencesResponse>(data) }, error)
    }

    suspend fun getAccountGeoComplianceStatus(accountId: String): Pair<GeoComplianceStatusResponse?, NetworkingError?> {
        val endpoint = GeocomplianceEndpoints.AccountGeoCompliance(accountId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<GeoComplianceStatusResponse>(data) }, error)
    }

    // Methods using callbacks
    fun listGeofences(completionHandler: (GeofencesResponse?, NetworkingError?) -> Unit) {
        val endpoint = GeocomplianceEndpoints.ListGeofences

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<GeofencesResponse>(data) }, error)
        }
    }

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

