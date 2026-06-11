package com.framepayments.framesdk.configurations

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines the API endpoints used to retrieve third-party service configurations.
 */
sealed class ConfigurationEndpoints : FrameNetworkingEndpoints {
    /** Requests the Evervault configuration from `GET /v1/config/evervault`. */
    object GetEvervaultConfiguration : ConfigurationEndpoints()

    /** Requests the Sift configuration from `GET /v1/config/sift`. */
    object GetSiftConfiguration: ConfigurationEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is GetEvervaultConfiguration ->
                "/v1/config/evervault"
            is GetSiftConfiguration ->
                "/v1/config/sift"
        }

    override val httpMethod: String = "GET"
    override val queryItems: List<QueryItem>? = null
}