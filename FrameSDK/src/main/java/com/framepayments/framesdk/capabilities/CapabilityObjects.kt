package com.framepayments.framesdk.capabilities

import com.google.gson.annotations.SerializedName

object CapabilityObjects {
    data class CapabilityRequirement(
        val id: String,
        val `object`: String,
        val type: String,
        val status: String,
        val source: String? = null
    )

    data class Capability(
        val id: String,
        val `object`: String,
        val name: String,
        @SerializedName("account_id") val accountId: String,
        val status: String,
        @SerializedName("disabled_reason") val disabledReason: String? = null,
        @SerializedName("currently_due") val currentlyDue: List<String>? = null,
        val created: String,
        val updated: String,
        val disabled: Boolean? = null
    )
}
