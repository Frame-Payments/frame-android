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
     * Fetches the Fingerprint configuration from the API and caches it locally.
     *
     * @return The parsed [ConfigurationResponses.GetFingerprintConfigurationResponse], or `null` if
     *   the request fails or the response cannot be parsed.
     */
    suspend fun getFingerprintConfiguration(): ConfigurationResponses.GetFingerprintConfigurationResponse? {
        val endpoint = ConfigurationEndpoints.GetFingerprintConfiguration
        val (data, _) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Publishable)

        if (data != null) {
            val dataResponse = FrameNetworking.parseResponse<ConfigurationResponses.GetFingerprintConfigurationResponse>(data)

            if (dataResponse != null) {
                SecureConfigurationStorage.save(
                    context = FrameNetworking.getContext(),
                    key = "fingerprint",
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

    /**
     * Fetches Frame's legal document URLs from the API and caches them locally.
     *
     * @return The parsed [ConfigurationResponses.GetLegalConfigurationResponse], or `null` if
     *   the request fails or the response cannot be parsed.
     */
    suspend fun getLegalConfiguration(): ConfigurationResponses.GetLegalConfigurationResponse? {
        val endpoint = ConfigurationEndpoints.GetLegalConfiguration
        val (data, _) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Publishable)

        if (data != null) {
            val dataResponse = FrameNetworking.parseResponse<ConfigurationResponses.GetLegalConfigurationResponse>(data)

            if (dataResponse != null) {
                SecureConfigurationStorage.save(
                    context = FrameNetworking.getContext(),
                    key = "legal",
                    value = dataResponse
                )
            }
            return dataResponse
        }
        return null
    }

    /**
     * Fetches the Mapbox address-autocomplete configuration from the API and caches it locally.
     *
     * @return The parsed [ConfigurationResponses.GetMapboxConfigurationResponse], or `null` if
     *   the request fails or the response cannot be parsed.
     */
    suspend fun getMapboxConfiguration(): ConfigurationResponses.GetMapboxConfigurationResponse? {
        val endpoint = ConfigurationEndpoints.GetMapboxConfiguration
        val (data, _) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Publishable)

        if (data != null) {
            val dataResponse = FrameNetworking.parseResponse<ConfigurationResponses.GetMapboxConfigurationResponse>(data)

            if (dataResponse != null) {
                SecureConfigurationStorage.save(
                    context = FrameNetworking.getContext(),
                    key = "mapbox",
                    value = dataResponse
                )
            }
            return dataResponse
        }
        return null
    }

    /**
     * Fetches every configuration block in one request and caches each present block under the
     * same storage key its individual endpoint uses, turning those fetches into cache hits. An
     * omitted block is skipped rather than cleared, so a service that failed server-side keeps
     * its cache.
     *
     * @return The parsed [ConfigurationResponses.GetAllConfigurationResponse], or `null` if the
     *   request fails or the response cannot be parsed.
     */
    suspend fun getAllConfiguration(): ConfigurationResponses.GetAllConfigurationResponse? {
        val endpoint = ConfigurationEndpoints.GetAllConfiguration
        val (data, _) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Publishable)

        val dataResponse = data?.let { FrameNetworking.parseResponse<ConfigurationResponses.GetAllConfigurationResponse>(it) }
            ?: return null

        cache(dataResponse.evervault, "evervault")
        cache(dataResponse.fingerprint, "fingerprint")
        cache(dataResponse.legal, "legal")
        cache(dataResponse.mapbox, "mapbox")
        cache(dataResponse.sift, "sift")

        return dataResponse
    }

    private fun cache(block: Any?, key: String) {
        if (block == null) return
        SecureConfigurationStorage.save(context = FrameNetworking.getContext(), key = key, value = block)
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
     * Fetches the Fingerprint configuration from the API and caches it locally, delivering the
     * result via a callback.
     *
     * @param completionHandler Invoked with the parsed
     *   [ConfigurationResponses.GetFingerprintConfigurationResponse], or `null` if the request
     *   fails or the response cannot be parsed.
     */
    fun getFingerprintConfiguration(completionHandler: (ConfigurationResponses.GetFingerprintConfigurationResponse?) -> Unit) {
        val endpoint = ConfigurationEndpoints.GetFingerprintConfiguration

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Publishable) { data, error ->
            if (data != null) {
                val dataResponse = FrameNetworking.parseResponse<ConfigurationResponses.GetFingerprintConfigurationResponse>(data)

                if (dataResponse != null) {
                    SecureConfigurationStorage.save(
                        context = FrameNetworking.getContext(),
                        key = "fingerprint",
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

