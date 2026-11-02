package com.framepayments.framesdk.managers

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.configurations.ConfigurationAPI
import com.framepayments.framesdk.configurations.ConfigurationResponses
import com.framepayments.framesdk.configurations.SecureConfigurationStorage
import siftscience.android.Sift
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader

object SiftManager {
    var userId: String = ""

    private val userIdLock = Any()

    /**
     * Associates a Sift user id. Prefer the account response's `sift_user_id` (identity id)
     * over the Frame account id so device data and server payment events share one Sift user.
     *
     * @param customerId The Sift `$user_id` to attach.
     * @param email Unused today; kept for call-site parity with iOS `$login` collection.
     * @param overwrite When `false`, leaves an already-set user id alone (redundancy call sites).
     */
    @JvmOverloads
    fun collectLoginEvent(customerId: String, email: String, overwrite: Boolean = true) {
        synchronized(userIdLock) {
            if (customerId.isEmpty()) return
            if (userId == customerId) return
            if (userId.isNotEmpty() && !overwrite) return
            try {
                Sift.setUserId(customerId)
                Sift.collect()
                userId = customerId
            } catch (e: Exception) {
                // Sift may not be initialized in test environments; still record intent.
                userId = customerId
            }
        }
    }

    fun initializeSift() {
        fun openSift(config: ConfigurationResponses.GetSiftConfigurationResponse?) {
            Sift.open(FrameNetworking.getContext(), Sift.Config.Builder()
                .withAccountId(config?.accountId)
                .withBeaconKey(config?.beaconKey)
                .build())
            Sift.collect()
        }

        val cached: ConfigurationResponses.GetSiftConfigurationResponse? = SecureConfigurationStorage.retrieve(FrameNetworking.getContext(), "sift")
        if (cached != null) {
            openSift(cached)
        } else {
            ConfigurationAPI.getSiftConfiguration { configFromAPI -> openSift(configFromAPI) }
        }
    }

    @Volatile
    private var cachedPublicIp: String? = null

    private val publicIpFetchLock = Any()

    private fun fetchPublicIpFromNetwork(): String? {
        return try {
            val url = URL("https://api.ipify.org")
            BufferedReader(InputStreamReader(url.openStream())).use { it.readLine() }?.trim()?.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            if (FrameNetworking.debugMode) {
                println("FrameSDK getPublicIp failed: ${e.message}")
            }
            e.printStackTrace()
            null
        }
    }

    /**
     * Returns a cached public IP after the first successful lookup; no further ipify calls until process exit.
     * Failed lookups are not cached, so the next call may retry.
     */
    fun getPublicIp(): String? {
        cachedPublicIp?.let { return it }

        synchronized(publicIpFetchLock) {
            cachedPublicIp?.let { return it }

            val ip = fetchPublicIpFromNetwork()
            if (ip != null) {
                cachedPublicIp = ip
                if (FrameNetworking.debugMode) {
                    println("FrameSDK getPublicIp: $ip (cached for app lifecycle)")
                }
            }
            return ip
        }
    }

    fun getIPAddress(): String? = getPublicIp()

    /**
     * Non-blocking accessor that returns the cached public IP if one is available
     * and `null` otherwise. Use this from the network-header path so requests don't
     * stall waiting on the first ipify lookup. The warmup launch in
     * [FrameNetworking.initializeWithAPIKey] (`sdkScope.launch { getPublicIp() }`)
     * populates the cache asynchronously so subsequent requests carry the header.
     */
    fun getCachedIPAddress(): String? = cachedPublicIp
}
