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

    fun collectLoginEvent(customerId: String, email: String) {
        try {
            if (userId.isNotEmpty()) return
            userId = customerId
            Sift.setUserId(customerId)
            Sift.collect()
        } catch (e: Exception) {
            // Sift may not be initialized in test environments
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