package com.framepayments.framesdk.fingerprint

import android.content.Context
import android.util.Log
import com.fingerprintjs.android.fpjs_pro.Configuration
import com.fingerprintjs.android.fpjs_pro.FingerprintJSFactory
import com.framepayments.framesdk.configurations.ConfigurationAPI
import com.framepayments.framesdk.configurations.ConfigurationResponses
import com.framepayments.framesdk.configurations.SecureConfigurationStorage

/**
 * Configuration container for the Fingerprint Android SDK used by Frame.
 *
 * The Fingerprint public API key and region are fetched from the Frame
 * configuration API (and cached in encrypted storage), so only client-side
 * behaviour toggles are exposed here.
 */
object FingerprintConfig {
    /**
     * Whether to request extended response format from Fingerprint.
     */
    @JvmStatic
    var extendedResponseFormat: Boolean = false
}

object FingerprintManager {
    @Volatile
    private var client: com.fingerprintjs.android.fpjs_pro.FingerprintJS? = null

    /**
     * Fetches the Fingerprint configuration from the Frame API, falling back to
     * the encrypted-storage cached copy when the network request is unavailable.
     */
    private fun fetchConfiguration(
        context: Context,
        completion: (ConfigurationResponses.GetFingerprintConfigurationResponse?) -> Unit
    ) {
        ConfigurationAPI.getFingerprintConfiguration { configFromAPI ->
            if (configFromAPI != null) {
                completion(configFromAPI)
            } else {
                completion(SecureConfigurationStorage.retrieve(context, "fingerprint"))
            }
        }
    }

    private fun region(rawValue: String?): Configuration.Region =
        when (rawValue) {
            "eu" -> Configuration.Region.EU
            "ap" -> Configuration.Region.AP
            else -> Configuration.Region.US
        }

    private fun configuredClient(
        context: Context,
        completion: (com.fingerprintjs.android.fpjs_pro.FingerprintJS?) -> Unit
    ) {
        val existing = client
        if (existing != null) {
            completion(existing)
            return
        }

        fetchConfiguration(context) { config ->
            val apiKey = config?.apiKey
            if (apiKey.isNullOrEmpty()) {
                // Fingerprint credentials are unavailable from both the Frame API and the local cache.
                completion(null)
                return@fetchConfiguration
            }

            val configuration = Configuration(
                apiKey = apiKey,
                region = region(config.region),
                extendedResponseFormat = FingerprintConfig.extendedResponseFormat
            )

            val instance = FingerprintJSFactory(context.applicationContext).createInstance(configuration)
            client = instance
            completion(instance)
        }
    }

    /**
     * Default request timeout for [getVisitorId]. The FingerprintPro SDK treats `0`
     * as "no timeout" and keeps the callback pending indefinitely on a slow / blocked
     * device, which stalls every downstream caller that awaits the visitorId. 3000ms
     * is comfortably above a healthy device's typical fingerprint time (~500-1500ms)
     * while still letting the Sonar fallback path fire promptly when the device or
     * network is degraded.
     */
    const val DEFAULT_GET_VISITOR_ID_TIMEOUT_MS: Int = 3000

    /**
     * Retrieves a Fingerprint visitorId.
     *
     * If configuration is missing, this returns null so the caller can fall back
     * to an alternative visitor identifier. The timeout defaults to
     * [DEFAULT_GET_VISITOR_ID_TIMEOUT_MS] — pass `0` explicitly to disable.
     */
    fun getVisitorId(
        context: Context,
        timeoutMillis: Int? = null,
        completion: (visitorId: String?) -> Unit
    ) {
        configuredClient(context) { fpClient ->
            if (fpClient == null) {
                Log.w("FingerprintManager", "Fingerprint client unavailable — configuration could not be fetched")
                completion(null)
                return@configuredClient
            }

            fpClient.getVisitorId(
                timeoutMillis = timeoutMillis ?: DEFAULT_GET_VISITOR_ID_TIMEOUT_MS,
                listener = { response ->
                    completion(response.visitorId)
                },
                errorListener = { error ->
                    Log.e(
                        "FingerprintManager",
                        "Fingerprint getVisitorId failed: ${error::class.simpleName} — ${error.description} (requestId=${error.requestId})"
                    )
                    completion(null)
                }
            )
        }
    }
}
