package com.framepayments.framesdk

import com.google.gson.annotations.SerializedName

object FrameObjects {
    data class BillingAddress(
        val city: String? = null,
        val country: String? = null,
        val state: String? = null,
        @SerializedName("postal_code") val postalCode: String,
        @SerializedName("line_1") val addressLine1: String? = null,
        @SerializedName("line_2") val addressLine2: String? = null
    )
}

data class QueryItem(val name: String, val value: String?)

data class FrameMetadata(
    val page: Int,
    val url: String,
    @SerializedName("has_more") val hasMore: Boolean
)
