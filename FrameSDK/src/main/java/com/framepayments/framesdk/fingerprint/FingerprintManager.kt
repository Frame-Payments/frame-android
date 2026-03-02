package com.framepayments.framesdk.fingerprint

import android.content.Context
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
     * Retrieves a Fingerprint visitorId.
     *
     * If configuration is missing, this returns null so the caller can fall back
     * to an alternative visitor identifier.
     */
    fun getVisitorId(
        context: Context,
        timeoutMillis: Long? = null,
        completion: (visitorId: String?) -> Unit
    ) {
        val fpClient = configuredClient(context)
        if (fpClient == null) {
            completion(null)
            return
        }

        fpClient.getVisitorId(
            timeoutMillis = timeoutMillis,
            listener = { response ->
                completion(response.visitorId)
            },
            errorListener = {
                completion(null)
            }
        )
    }
}

