package com.framepayments.framesdk.products

import com.google.gson.annotations.SerializedName

/**
 * Indicates whether a product is purchased once or on a recurring basis.
 */
enum class ProductPurchaseType {
    /** The customer is charged a single time at purchase. */
    @SerializedName("one_time") ONE_TIME,
    /** The customer is charged repeatedly on a defined interval. */
    @SerializedName("recurring") RECURRING
}

/**
 * Defines the billing cadence for a recurring product.
 */
enum class ProductRecurringInterval {
    /** Billed once per day. */
    @SerializedName("daily") DAILY,
    /** Billed once per week. */
    @SerializedName("weekly") WEEKLY,
    /** Billed once per month. */
    @SerializedName("monthly") MONTHLY,
    /** Billed once per year. */
    @SerializedName("yearly") YEARLY,
    /** Billed once every three months. */
    @SerializedName("every_3_months") EVERY_3_MONTHS,
    /** Billed once every six months. */
    @SerializedName("every_6_months") EVERY_6_MONTHS,
}

/**
 * Represents a product defined by the merchant in the Frame platform.
 *
 * @property id Unique identifier for the product.
 * @property name Display name of the product.
 * @property livemode Whether the product was created in live mode (`true`) or test mode (`false`).
 * @property image URL of the product image.
 * @property description Human-readable description of the product.
 * @property url URL for the product's external page or listing.
 * @property shippable Whether the product requires physical shipment to the customer.
 * @property active Whether the product is currently available for purchase.
 * @property metadata Merchant-defined key-value pairs attached to the product.
 * @property created Unix timestamp of when the product was created.
 * @property updated Unix timestamp of when the product was last updated.
 * @property defaultPrice The default price for the product, in the smallest currency unit (e.g. cents).
 * @property productObject The object type identifier returned by the API (typically `"product"`).
 */
data class Product(
    val id: String?,
    val name: String?,
    val livemode: Boolean?,
    val image: String?,
    val description: String?,
    val url: String?,
    val shippable: Boolean?,
    val active: Boolean?,
    val metadata: Map<String, String>?,
    val created: Int?,
    val updated: Int?,
    @SerializedName("default_price") val defaultPrice: Int?,
    @SerializedName("object") val productObject: String?
)
