package com.framepayments.framesdk.products

import com.google.gson.annotations.SerializedName

object ProductsRequests {
    data class CreateProductRequest(
        val name: String,
        val description: String,
        @SerializedName("default_price") val defaultPrice: Int,
        @SerializedName("purchase_type") val purchaseType: ProductPurchaseType,
        @SerializedName("recurring_interval") val recurringInterval: ProductRecurringInterval?, // Required if purchase type is RECURRING
        val shippable: Boolean?,
        val url: String?,
        val metadata: Map<String, String>?
    )

    data class UpdateProductRequest(
        val name: String? = null,
        val description: String? = null,
        @SerializedName("default_price") val defaultPrice: Int? = null,
        val shippable: Boolean? = null,
        val url: String? = null,
        val metadata: Map<String, String>? = null
    )
}