package com.framepayments.framesdk.configurations
import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Fetches and caches third-party service configurations from the Frame API.
 *
 * Each method retrieves a configuration object from the network, persists it to
 * [SecureConfigurationStorage], and returns the parsed response. Both coroutine
 * (suspend) and callback variants are provided for each service.
 *
 * After [getAllConfiguration] warms the cache (or any individual fetch succeeds), later
 * callers of the per-service getters resolve from storage instead of firing duplicate
 * network requests for the same launch. A block not refreshed this launch is fetched again,
 * falling back to its stored value if that fetch fails.
 */
object ConfigurationAPI {
    private val allConfigMutex = Mutex()
    private var allConfigInFlight: CompletableDeferred<ConfigurationResponses.GetAllConfigurationResponse?>? = null

    // Storage persists across launches, so only a block refreshed in this process counts as warm.
    private val refreshedThisLaunch: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private inline fun <reified T> stored(key: String): T? =
        runCatching {
            SecureConfigurationStorage.retrieve<T>(FrameNetworking.getContext(), key)
        }.getOrNull()

    private inline fun <reified T> cached(key: String): T? =
        if (key in refreshedThisLaunch) stored<T>(key) else null

    private suspend inline fun <reified T : Any> fetch(endpoint: ConfigurationEndpoints, key: String): T? {
        cached<T>(key)?.let { return it }

        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.PublishableOnly)

        // performDataTask still returns the error response body as `data` on a non-2xx status —
        // every field here is nullable, so Gson happily parses an error envelope into a non-null,
        // all-null "config" that would otherwise get cached and served as if it were real.
        if (data != null && error == null) {
            FrameNetworking.parseResponse<T>(data)?.let {
                cache(it, key)
                return it
            }
        }
        return stored(key)
    }

    private inline fun <reified T : Any> fetch(
        endpoint: ConfigurationEndpoints,
        key: String,
        crossinline completionHandler: (T?) -> Unit
    ) {
        cached<T>(key)?.let {
            completionHandler(it)
            return
        }

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.PublishableOnly) { data, error ->
            val fresh = if (data != null && error == null) FrameNetworking.parseResponse<T>(data) else null
            fresh?.let { cache(it, key) }
            completionHandler(fresh ?: stored(key))
        }
    }

    //MARK: Methods using coroutines
    /**
     * Fetches the Evervault configuration from the API and caches it locally.
     *
     * @return The parsed [ConfigurationResponses.GetEvervaultConfigurationResponse], or the last
     *   stored value (`null` if none) if the request fails or the response cannot be parsed.
     */
    suspend fun getEvervaultConfiguration(): ConfigurationResponses.GetEvervaultConfigurationResponse? {
        return fetch(ConfigurationEndpoints.GetEvervaultConfiguration, "evervault")
    }

    /**
     * Fetches the Fingerprint configuration from the API and caches it locally.
     *
     * @return The parsed [ConfigurationResponses.GetFingerprintConfigurationResponse], or the last
     *   stored value (`null` if none) if the request fails or the response cannot be parsed.
     */
    suspend fun getFingerprintConfiguration(): ConfigurationResponses.GetFingerprintConfigurationResponse? {
        return fetch(ConfigurationEndpoints.GetFingerprintConfiguration, "fingerprint")
    }

    /**
     * Fetches the Sift configuration from the API and caches it locally.
     *
     * @return The parsed [ConfigurationResponses.GetSiftConfigurationResponse], or the last
     *   stored value (`null` if none) if the request fails or the response cannot be parsed.
     */
    suspend fun getSiftConfiguration(): ConfigurationResponses.GetSiftConfigurationResponse? {
        return fetch(ConfigurationEndpoints.GetSiftConfiguration, "sift")
    }

    /**
     * Fetches Frame's legal document URLs from the API and caches them locally.
     *
     * @return The parsed [ConfigurationResponses.GetLegalConfigurationResponse], or the last
     *   stored value (`null` if none) if the request fails or the response cannot be parsed.
     */
    suspend fun getLegalConfiguration(): ConfigurationResponses.GetLegalConfigurationResponse? {
        return fetch(ConfigurationEndpoints.GetLegalConfiguration, "legal")
    }

    /**
     * Fetches the Mapbox address-autocomplete configuration from the API and caches it locally.
     *
     * @return The parsed [ConfigurationResponses.GetMapboxConfigurationResponse], or the last
     *   stored value (`null` if none) if the request fails or the response cannot be parsed.
     */
    suspend fun getMapboxConfiguration(): ConfigurationResponses.GetMapboxConfigurationResponse? {
        return fetch(ConfigurationEndpoints.GetMapboxConfiguration, "mapbox")
    }

    /**
     * Fetches every configuration block in one request and caches each present block under the
     * same storage key its individual endpoint uses, turning those fetches into cache hits. An
     * omitted block is skipped rather than cleared, so its individual getter still refreshes it
     * and falls back to the stored value if that fails.
     *
     * Concurrent callers join a single in-flight request so a launch never fans out into
     * duplicate `/config/all` round-trips.
     *
     * @return The parsed [ConfigurationResponses.GetAllConfigurationResponse], or `null` if the
     *   request fails or the response cannot be parsed.
     */
    suspend fun getAllConfiguration(): ConfigurationResponses.GetAllConfigurationResponse? {
        val (deferred, isLeader) = allConfigMutex.withLock {
            allConfigInFlight?.let { it to false }
                ?: CompletableDeferred<ConfigurationResponses.GetAllConfigurationResponse?>().let { created ->
                    allConfigInFlight = created
                    created to true
                }
        }

        if (isLeader) {
            try {
                deferred.complete(fetchAllConfigurationFromNetwork())
            } catch (t: Throwable) {
                deferred.complete(null)
                throw t
            } finally {
                withContext(NonCancellable) {
                    allConfigMutex.withLock {
                        if (allConfigInFlight === deferred) allConfigInFlight = null
                    }
                }
            }
        }

        return deferred.await()
    }

    private suspend fun fetchAllConfigurationFromNetwork(): ConfigurationResponses.GetAllConfigurationResponse? {
        val endpoint = ConfigurationEndpoints.GetAllConfiguration
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.PublishableOnly)
        if (error != null) return null

        val dataResponse = data?.let {
            FrameNetworking.parseResponse<ConfigurationResponses.GetAllConfigurationResponse>(it)
        } ?: return null

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
        refreshedThisLaunch += key
    }

    //MARK: Methods using callbacks
    /**
     * Fetches the Evervault configuration from the API and caches it locally, delivering the
     * result via a callback.
     *
     * @param completionHandler Invoked with the parsed
     *   [ConfigurationResponses.GetEvervaultConfigurationResponse], or the last stored
     *   value (`null` if none) if the request fails or the response cannot be parsed.
     */
    fun getEvervaultConfiguration(completionHandler: (ConfigurationResponses.GetEvervaultConfigurationResponse?) -> Unit) {
        fetch(ConfigurationEndpoints.GetEvervaultConfiguration, "evervault", completionHandler)
    }

    /**
     * Fetches the Fingerprint configuration from the API and caches it locally, delivering the
     * result via a callback.
     *
     * @param completionHandler Invoked with the parsed
     *   [ConfigurationResponses.GetFingerprintConfigurationResponse], or the last stored
     *   value (`null` if none) if the request fails or the response cannot be parsed.
     */
    fun getFingerprintConfiguration(completionHandler: (ConfigurationResponses.GetFingerprintConfigurationResponse?) -> Unit) {
        fetch(ConfigurationEndpoints.GetFingerprintConfiguration, "fingerprint", completionHandler)
    }

    /**
     * Fetches the Sift configuration from the API and caches it locally, delivering the result
     * via a callback.
     *
     * @param completionHandler Invoked with the parsed
     *   [ConfigurationResponses.GetSiftConfigurationResponse], or the last stored
     *   value (`null` if none) if the request fails or the response cannot be parsed.
     */
    fun getSiftConfiguration(completionHandler: (ConfigurationResponses.GetSiftConfigurationResponse?) -> Unit) {
        fetch(ConfigurationEndpoints.GetSiftConfiguration, "sift", completionHandler)
    }
}
