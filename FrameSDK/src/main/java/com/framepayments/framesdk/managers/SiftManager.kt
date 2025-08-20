package com.framepayments.framesdk.managers

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.configurations.ConfigurationAPI
import com.framepayments.framesdk.configurations.ConfigurationResponses
import com.framepayments.framesdk.configurations.SecureConfigurationStorage
import siftscience.android.Sift

enum class SiftActivityName {
    sale,
    authorize,
    capture,
    refund
}

object SiftManager {
    fun initializeSift(userId: String) {
        Sift.setUserId(userId)

        val config: ConfigurationResponses.GetSiftConfigurationResponse? = SecureConfigurationStorage.retrieve(FrameNetworking.getContext(), "sift")
        if (config == null) {
            ConfigurationAPI.getSiftConfiguration { configFromAPI ->
                Sift.open(FrameNetworking.getContext(), Sift.Config.Builder()
                    .withAccountId(configFromAPI?.accountId)
                    .withBeaconKey(configFromAPI?.beaconKey)
                    .build())
            }
        }
    }

    fun addNewSiftEvent(activity: SiftActivityName) {
        Sift.open(FrameNetworking.getContext(), activity.toString())
        Sift.collect()
        Sift.upload()
    }
}