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

    fun initializeSift(userId: String) {
        Sift.setUserId(userId)

        val config: ConfigurationResponses.GetSiftConfigurationResponse? = SecureConfigurationStorage.retrieve(FrameNetworking.getContext(), "sift")
        if (config == null) {
            ConfigurationAPI.getSiftConfiguration { configFromAPI ->
                Sift.open(FrameNetworking.getContext(), Sift.Config.Builder()
                    .withAccountId(configFromAPI?.accountId)
                    .withBeaconKey(configFromAPI?.beaconKey)
                    .build())
                Sift.collect()
            }
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
}