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

    fun getPublicIp(): String? {
        return try {
            val url = URL("https://api.ipify.org")
            BufferedReader(InputStreamReader(url.openStream())).use { it.readLine() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}