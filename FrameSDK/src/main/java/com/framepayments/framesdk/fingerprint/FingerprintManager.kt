package com.framepayments.framesdk.fingerprint

import android.content.Context
import android.util.Log
import com.fingerprintjs.android.fpjs_pro.Configuration
import com.fingerprintjs.android.fpjs_pro.FingerprintJSFactory
import com.framepayments.framesdk.configurations.ConfigurationAPI
import com.framepayments.framesdk.configurations.ConfigurationResponses
import com.framepayments.framesdk.configurations.SecureConfigurationStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** The Fingerprint capability this build declares when fetching configuration. */
object FingerprintCapability {
    /** The header the configuration endpoint reads to pick an environment. */
    const val HEADER = "X-Frame-Sonar"

    /** The capability that earns the sealed environment's key. */
    const val SEALED = "sealed"
}

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
     *
     * Forced on regardless of this setting when the configuration API serves a sealed key,
     * since the sealed result is only present on the extended response.
     */
    @JvmStatic
    var extendedResponseFormat: Boolean = false
}

/**
 * What we hand the sonar-session API to identify this device: the sealed result when
 * Fingerprint served one, and the plaintext visitor id when the environment still serves that.
 *
 * Both travel together on purpose. Before the sealed environment is activated Fingerprint
 * returns both; after activation the visitor id is withheld and only the sealed result carries
 * identity. Sending the pair means neither side of that switch breaks us — and an environment
 * with no sealing at all still identifies by visitor id, which is the behavior we have today.
 *
 * @property visitorId Empty once the environment withholds it — the sealed result identifies instead.
 * @property sealedResult Base64 sealed `/v4/events` payload. Absent when sealing is off or unavailable.
 */
data class FingerprintIdentification(
    val visitorId: String,
    val sealedResult: String?
) {
    /** Whether this carries anything the API can identify the device by. */
    val isUsable: Boolean
        get() = visitorId.isNotEmpty() || sealedResult != null
}

object FingerprintManager {
    @Volatile
    private var client: com.fingerprintjs.android.fpjs_pro.FingerprintJS? = null

    /**
     * Fetches the Fingerprint configuration from the Frame API, falling back to the
     * encrypted-storage cached copy when the network request is unavailable.
     *
     * Whatever the API serves is used as-is: the key is the whole instruction, and a client
     * that refused a legacy key could not be moved back to one. The cached copy is only
     * trusted when it carries an environment stamp — an unstamped one predates this build
     * declaring a capability and says nothing about which key it holds.
     */
    private fun fetchConfiguration(
        context: Context,
        completion: (ConfigurationResponses.GetFingerprintConfigurationResponse?) -> Unit
    ) {
        ConfigurationAPI.getFingerprintConfiguration { configFromAPI ->
            if (configFromAPI != null) {
                completion(configFromAPI)
                return@getFingerprintConfiguration
            }
            val cached = SecureConfigurationStorage.retrieve<ConfigurationResponses.GetFingerprintConfigurationResponse>(context, "fingerprint")
            completion(cached?.takeIf { it.hasEnvironmentStamp })
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

            // A sealed key forces the extended format on regardless of the integrator's setting.
            // Left to the public flag — which defaults to off — sealedResult comes back null for
            // every request and the session silently posts nothing to identify the device with.
            val configuration = Configuration(
                apiKey = apiKey,
                region = region(config.region),
                extendedResponseFormat = config.isSealed || FingerprintConfig.extendedResponseFormat
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
        identify(context, timeoutMillis) { identification -> completion(identification?.visitorId) }
    }

    /**
     * Asks Fingerprint to identify this device, returning both the visitor id and the sealed
     * result (present only once the environment serves a sealed key with the extended
     * response format).
     *
     * The result is never cached by this call: the API rejects a sealed payload stamped
     * outside a ten-minute window in either direction, so every request that needs one has to
     * mint its own.
     *
     * If configuration is missing, this returns null so the caller can fall back to an
     * alternative visitor identifier. The timeout defaults to
     * [DEFAULT_GET_VISITOR_ID_TIMEOUT_MS] — pass `0` explicitly to disable.
     */
    fun identify(
        context: Context,
        timeoutMillis: Int? = null,
        completion: (FingerprintIdentification?) -> Unit
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
                    completion(FingerprintIdentification(visitorId = response.visitorId, sealedResult = response.sealedResult))
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

    /** Suspend variant of [identify], for callers already on a coroutine. */
    suspend fun identify(context: Context, timeoutMillis: Int? = null): FingerprintIdentification? =
        suspendCancellableCoroutine { continuation ->
            identify(context, timeoutMillis) { identification -> continuation.resume(identification) }
        }
}
