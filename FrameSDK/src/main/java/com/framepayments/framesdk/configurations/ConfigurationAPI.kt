package com.framepayments.framesdk.configurations
import com.framepayments.framesdk.FrameNetworking

object ConfigurationAPI {
    //MARK: Methods using coroutines
    suspend fun getEvervaultConfiguration(): ConfigurationResponses.GetEvervaultConfigurationResponse? {
        val endpoint = ConfigurationEndpoints.GetEvervaultConfiguration
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            val dataResponse = FrameNetworking.parseResponse<ConfigurationResponses.GetEvervaultConfigurationResponse>(data)

            SecureConfigurationStorage.save(
                context = FrameNetworking.getContext(),
                key = "evervault",
                value = dataResponse
            )
            return dataResponse
        }
        return null
    }

    suspend fun getSiftConfiguration(): ConfigurationResponses.GetSiftConfigurationResponse? {
        val endpoint = ConfigurationEndpoints.GetSiftConfiguration
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            val dataResponse = FrameNetworking.parseResponse<ConfigurationResponses.GetSiftConfigurationResponse>(data)

            SecureConfigurationStorage.save(
                context = FrameNetworking.getContext(),
                key = "sift",
                value = dataResponse
            )
            return dataResponse
        }
        return null
    }

    //MARK: Methods using callbacks
    fun getEvervaultConfiguration(completionHandler: (ConfigurationResponses.GetEvervaultConfigurationResponse?) -> Unit) {
        val endpoint = ConfigurationEndpoints.GetEvervaultConfiguration

        FrameNetworking.performDataTask(endpoint) { data, error ->
            if (data != null) {
                val dataResponse = FrameNetworking.parseResponse<ConfigurationResponses.GetEvervaultConfigurationResponse>(data)

                SecureConfigurationStorage.save(
                    context = FrameNetworking.getContext(),
                    key = "evervault",
                    value = dataResponse
                )
                completionHandler(dataResponse)
            } else {
                completionHandler(null)
            }
        }
    }

    fun getSiftConfiguration(completionHandler: (ConfigurationResponses.GetSiftConfigurationResponse?) -> Unit) {
        val endpoint = ConfigurationEndpoints.GetEvervaultConfiguration

        FrameNetworking.performDataTask(endpoint) { data, error ->
            if (data != null) {
                val dataResponse = FrameNetworking.parseResponse<ConfigurationResponses.GetSiftConfigurationResponse>(data)

                SecureConfigurationStorage.save(
                    context = FrameNetworking.getContext(),
                    key = "sift",
                    value = dataResponse
                )
                completionHandler(dataResponse)
            } else {
                completionHandler(null)
            }
        }
    }
}

