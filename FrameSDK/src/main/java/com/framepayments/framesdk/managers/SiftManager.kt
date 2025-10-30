package com.framepayments.framesdk.managers

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.configurations.ConfigurationAPI
import com.framepayments.framesdk.configurations.ConfigurationResponses
import com.framepayments.framesdk.configurations.SecureConfigurationStorage
import com.sift.api.representations.MobileEventJson
import siftscience.android.Sift
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


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

    fun collectUserLogin(customerId: String, email : String) {
        if (userId == "") {
            userId = customerId
            Sift.setUserId(customerId)

            val ipAddress = fetchPublicIp()

            Sift.open(FrameNetworking.getContext())
            Sift.collect()
            Sift.upload()
        }
    }

    fun fetchPublicIp(): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.ipify.org?format=json")
            .build()
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            val json = JSONObject(body)
            return json.optString("ip", null)
        }
    }
}