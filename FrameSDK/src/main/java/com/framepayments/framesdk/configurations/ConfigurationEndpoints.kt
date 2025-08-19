package com.framepayments.framesdk.configurations

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class ConfigurationEndpoints : FrameNetworkingEndpoints {
    object GetEvervaultConfiguration : ConfigurationEndpoints()
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