package com.framepayments.framesdk.configurations

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem
import com.framepayments.framesdk.fingerprint.FingerprintCapability

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

    /** Requests every configuration block in one payload from `GET /v1/config/all`. */
    object GetAllConfiguration : ConfigurationEndpoints()

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
            is GetAllConfiguration ->
                "/v1/config/all"
        }

    override val httpMethod: String = "GET"
    override val queryItems: List<QueryItem>? = null

    /**
     * Declares what this build can handle, not which release it is: the API reads this to
     * decide which Fingerprint environment's key to serve. Saying nothing keeps the legacy
     * key, which is what older builds get.
     *
     * The aggregate endpoint carries it too: it is the only config request a normal launch
     * makes, and its cached fingerprint block is what every later [GetFingerprintConfiguration]
     * serves. Without the header here the whole process runs on a legacy key and never asks
     * for a sealed one.
     */
    override val additionalHeaders: Map<String, String>
        get() = when (this) {
            is GetFingerprintConfiguration, is GetAllConfiguration ->
                mapOf(FingerprintCapability.HEADER to FingerprintCapability.SEALED)
            else -> emptyMap()
        }
}