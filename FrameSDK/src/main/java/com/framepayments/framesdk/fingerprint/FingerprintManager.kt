package com.framepayments.framesdk.fingerprint

import android.content.Context
import android.util.Log
import com.fingerprintjs.android.fpjs_pro.Configuration
import com.fingerprintjs.android.fpjs_pro.FingerprintJSFactory

/**
 * Configuration container for the Fingerprint Android SDK used by Frame.
 *
 * The host app is responsible for setting [apiKey] (and optionally [region])
 * before Frame initializes network/session tracking.
 */
object FingerprintConfig {
    /**
     * Public API key obtained from the Fingerprint dashboard.
     */
    @JvmStatic
    var apiKey: String = "YQnpZMcPLRUoK0998JIm"

    /**
     * Backend region associated with the API key.
     * Defaults to US. Ensure this matches your workspace region.
     */
    @JvmStatic
    var region: Configuration.Region = Configuration.Region.US

    /**
     * Whether to request extended response format from Fingerprint.
     */
    @JvmStatic
    var extendedResponseFormat: Boolean = false
}

object FingerprintManager {
    @Volatile
    private var client: com.fingerprintjs.android.fpjs_pro.FingerprintJS? = null

    private fun configuredClient(context: Context): com.fingerprintjs.android.fpjs_pro.FingerprintJS? {
        val existing = client
        if (existing != null) {
            return existing
        }

        if (FingerprintConfig.apiKey.isEmpty()) {
            // SDK consumer must set FingerprintConfig.apiKey before initialization.
            return null
        }

        val configuration = Configuration(
            apiKey = FingerprintConfig.apiKey,
            region = FingerprintConfig.region,
            extendedResponseFormat = FingerprintConfig.extendedResponseFormat
        )

        val instance = FingerprintJSFactory(context.applicationContext).createInstance(configuration)
        client = instance
        return instance
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
        val fpClient = configuredClient(context)
        if (fpClient == null) {
            Log.w("FingerprintManager", "Fingerprint client unavailable — FingerprintConfig.apiKey is empty")
            completion(null)
            return
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
