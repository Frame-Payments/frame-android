package com.framepayments.framesdk.products

import com.framepayments.framesdk.FrameMetadata
import com.google.gson.annotations.SerializedName

/**
 * Contains response body models for the Products API.
 */
object ProductsResponses {

    /**
     * Response returned when retrieving a paginated list of products.
     *
     * @property meta Pagination metadata for the result set.
     * @property data The list of [Product] objects returned by the API.
     */
    data class ListProductsResponse(
        val meta: FrameMetadata? = null,
        val data: List<Product>? = null
    )

    /**
     * Response returned when a product is successfully deleted.
     *
     * @property id The unique identifier of the deleted product.
     * @property productObject The object type identifier returned by the API (typically `"product"`).
     * @property deleted Whether the product was successfully deleted.
     */
    data class DeleteProductsResponse(
        val id: String?,
        @SerializedName("object") val productObject: String?,
        val deleted: Boolean?

    )
}
