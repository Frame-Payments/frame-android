package com.framepayments.framesdk.configurations
import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking

/**
 * Fetches and caches third-party service configurations from the Frame API.
 *
 * Each method retrieves a configuration object from the network, persists it to
 * [SecureConfigurationStorage], and returns the parsed response. Both coroutine
 * (suspend) and callback variants are provided for each service.
 */
object ConfigurationAPI {
    //MARK: Methods using coroutines
    /**
     * Fetches the Evervault configuration from the API and caches it locally.
     *
     * @return The parsed [ConfigurationResponses.GetEvervaultConfigurationResponse], or `null` if
     *   the request fails or the response cannot be parsed.
     */
    suspend fun getEvervaultConfiguration(): ConfigurationResponses.GetEvervaultConfigurationResponse? {
        val endpoint = ConfigurationEndpoints.GetEvervaultConfiguration
        val (data, _) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Publishable)

        if (data != null) {
            val dataResponse = FrameNetworking.parseResponse<ConfigurationResponses.GetEvervaultConfigurationResponse>(data)

            if (dataResponse != null) {
                SecureConfigurationStorage.save(
                    context = FrameNetworking.getContext(),
                    key = "evervault",
                    value = dataResponse
                )
            }
            return dataResponse
        }
        return null
    }

    /**
     * Fetches the Sift configuration from the API and caches it locally.
     *
     * @return The parsed [ConfigurationResponses.GetSiftConfigurationResponse], or `null` if the
     *   request fails or the response cannot be parsed.
     */
    suspend fun getSiftConfiguration(): ConfigurationResponses.GetSiftConfigurationResponse? {
        val endpoint = ConfigurationEndpoints.GetSiftConfiguration
        val (data, _) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Publishable)

        if (data != null) {
            val dataResponse = FrameNetworking.parseResponse<ConfigurationResponses.GetSiftConfigurationResponse>(data)

            if (dataResponse != null) {
                SecureConfigurationStorage.save(
                    context = FrameNetworking.getContext(),
                    key = "sift",
                    value = dataResponse
                )
            }
            return dataResponse
        }
        return null
    }

    //MARK: Methods using callbacks
    /**
     * Fetches the Evervault configuration from the API and caches it locally, delivering the
     * result via a callback.
     *
     * @param completionHandler Invoked with the parsed
     *   [ConfigurationResponses.GetEvervaultConfigurationResponse], or `null` if the request fails
     *   or the response cannot be parsed.
     */
    fun getEvervaultConfiguration(completionHandler: (ConfigurationResponses.GetEvervaultConfigurationResponse?) -> Unit) {
        val endpoint = ConfigurationEndpoints.GetEvervaultConfiguration

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Publishable) { data, error ->
            if (data != null) {
                val dataResponse = FrameNetworking.parseResponse<ConfigurationResponses.GetEvervaultConfigurationResponse>(data)

                if (dataResponse != null) {
                    SecureConfigurationStorage.save(
                        context = FrameNetworking.getContext(),
                        key = "evervault",
                        value = dataResponse
                    )
                }
                completionHandler(dataResponse)
            } else {
                completionHandler(null)
            }
        }
    }

    /**
     * Fetches the Sift configuration from the API and caches it locally, delivering the result
     * via a callback.
     *
     * @param completionHandler Invoked with the parsed
     *   [ConfigurationResponses.GetSiftConfigurationResponse], or `null` if the request fails or
     *   the response cannot be parsed.
     */
    fun getSiftConfiguration(completionHandler: (ConfigurationResponses.GetSiftConfigurationResponse?) -> Unit) {
        val endpoint = ConfigurationEndpoints.GetSiftConfiguration

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Publishable) { data, error ->
            if (data != null) {
                val dataResponse = FrameNetworking.parseResponse<ConfigurationResponses.GetSiftConfigurationResponse>(data)

                if (dataResponse != null) {
                    SecureConfigurationStorage.save(
                        context = FrameNetworking.getContext(),
                        key = "sift",
                        value = dataResponse
                    )
                }
                completionHandler(dataResponse)
            } else {
                completionHandler(null)
            }
        }
    }
}

