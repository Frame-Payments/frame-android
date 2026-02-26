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
        val status: String,
        @SerializedName("disabled_reason") val disabledReason: String? = null,
        val requirements: List<CapabilityRequirement>? = null,
        @SerializedName("created_at") val createdAt: Int,
        @SerializedName("updated_at") val updatedAt: Int
    )
}
