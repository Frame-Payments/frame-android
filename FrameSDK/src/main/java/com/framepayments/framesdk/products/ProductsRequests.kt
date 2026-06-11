package com.framepayments.framesdk.products

import com.google.gson.annotations.SerializedName

/**
 * Contains request body models for the Products API.
 */
object ProductsRequests {

    /**
     * Request body for creating a new product.
     *
     * @property name Display name of the product.
     * @property description Human-readable description of the product.
     * @property defaultPrice Default price for the product, in the smallest currency unit (e.g. cents).
     * @property purchaseType Whether the product is purchased once or on a recurring basis.
     * @property recurringInterval The billing cadence for the product. Required when [purchaseType] is [ProductPurchaseType.RECURRING].
     * @property shippable Whether the product requires physical shipment to the customer. Omitted when null.
     * @property url URL for the product's external page or listing. Omitted when null.
     * @property metadata Merchant-defined key-value pairs to attach to the product. Omitted when null.
     */
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

    /**
     * Request body for updating an existing product.
     *
     * All fields are optional; only non-null fields are applied to the product.
     *
     * @property name Updated display name of the product.
     * @property description Updated human-readable description of the product.
     * @property defaultPrice Updated default price, in the smallest currency unit (e.g. cents).
     * @property shippable Updated shippable flag for the product.
     * @property url Updated URL for the product's external page or listing.
     * @property metadata Updated merchant-defined key-value pairs for the product.
     */
    data class UpdateProductRequest(
        val name: String? = null,
        val description: String? = null,
        @SerializedName("default_price") val defaultPrice: Int? = null,
        val shippable: Boolean? = null,
        val url: String? = null,
        val metadata: Map<String, String>? = null
    )
}
