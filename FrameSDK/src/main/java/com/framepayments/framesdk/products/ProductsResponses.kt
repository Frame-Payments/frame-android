package com.framepayments.framesdk.products

import com.framepayments.framesdk.FrameMetadata
import com.google.gson.annotations.SerializedName

object ProductsResponses {
    data class ListProductsResponse(
        val meta: FrameMetadata? = null,
        val data: List<Product>? = null
    )

    data class DeleteProductsResponse(
        val id: String,
        @SerializedName("object") val productObject: String,
        val deleted: Boolean

    )
}