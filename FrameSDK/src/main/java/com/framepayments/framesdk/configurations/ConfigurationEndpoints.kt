package com.framepayments.framesdk.configurations

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class ConfigurationEndpoints : FrameNetworkingEndpoints {
    object GetEvervaultConfiguration : ConfigurationEndpoints()
    object GetSiftConfiguration: ConfigurationEndpoints()
    object SendSiftConfigurationDetails: ConfigurationEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is GetEvervaultConfiguration ->
                "/v1/config/evervault"
            is GetSiftConfiguration ->
                "/v1/config/sift"
            is SendSiftConfigurationDetails ->
                "/v1/config/sift/details"
        }

    override val httpMethod: String
        get() = when (this) {
            is SendSiftConfigurationDetails -> "POST"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>? = null
}