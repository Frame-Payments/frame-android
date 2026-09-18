package com.framepayments.framesdk.configurations

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines the API endpoints used to retrieve third-party service configurations.
 */
sealed class ConfigurationEndpoints : FrameNetworkingEndpoints {
    /** Requests the Evervault configuration from `GET /v1/config/evervault`. */
    object GetEvervaultConfiguration : ConfigurationEndpoints()

    /** Requests the Fingerprint configuration from `GET /v1/config/fingerprint`. */
    object GetFingerprintConfiguration : ConfigurationEndpoints()

    /** Requests the Sift configuration from `GET /v1/config/sift`. */
    object GetSiftConfiguration: ConfigurationEndpoints()

    /** Requests Frame's legal document URLs from `GET /v1/config/legal`. */
    object GetLegalConfiguration : ConfigurationEndpoints()

    /** Requests the Mapbox address-autocomplete configuration from `GET /v1/config/mapbox`. */
    object GetMapboxConfiguration : ConfigurationEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is GetEvervaultConfiguration ->
                "/v1/config/evervault"
            is GetFingerprintConfiguration ->
                "/v1/config/fingerprint"
            is GetSiftConfiguration ->
                "/v1/config/sift"
            is GetLegalConfiguration ->
                "/v1/config/legal"
            is GetMapboxConfiguration ->
                "/v1/config/mapbox"
        }

    override val httpMethod: String = "GET"
    override val queryItems: List<QueryItem>? = null
}