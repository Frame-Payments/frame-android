package com.framepayments.framesdk.accountevents

import com.google.gson.annotations.SerializedName

object AccountEventsRequests {
    /** A single diagnostic/telemetry event describing something that happened during an SDK flow. */
    data class Event(
        @SerializedName("account_id") val accountId: String,
        val name: String,
        val screen: String,
        val platform: String,
        @SerializedName("sdk_version") val sdkVersion: String,
        @SerializedName("host_sdk_version") val hostSdkVersion: String?,
        @SerializedName("occurred_at") val occurredAt: String,
        val detail: String?
    )

    /** Always a batch, even for one event. */
    data class RecordRequest(val events: List<Event>)
}
