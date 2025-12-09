package com.framepayments.framesdk.products

import com.google.gson.annotations.SerializedName

enum class ProductPurchaseType {
    @SerializedName("one_time") ONE_TIME,
    @SerializedName("recurring") RECURRING
}

enum class ProductRecurringInterval {
    @SerializedName("daily") DAILY,
    @SerializedName("weekly") WEEKLY,
    @SerializedName("monthly") MONTHLY,
    @SerializedName("yearly") YEARLY,
    @SerializedName("every_3_months") EVERY_3_MONTHS,
    @SerializedName("every_6_months") EVERY_6_MONTHS,
}

data class Product(
    val id: String,
    val name: String,
    val livemode: Boolean,
    val image: String?,
    val description: String?,
    val url: String?,
    val shippable: Boolean,
    val active: Boolean,
    val metadata: Map<String, String>?,
    val created: Int,
    val updated: Int?,
    @SerializedName("default_price") val defaultPrice: Int,
    @SerializedName("object") val productObject: String?
)