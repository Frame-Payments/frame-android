package com.framepayments.framesdk.products

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines the API endpoints available for product operations.
 *
 * Each case maps to a specific URL, HTTP method, and optional query parameters
 * used by the networking layer to build requests.
 */
sealed class ProductEndpoints : FrameNetworkingEndpoints {

    /** Endpoint for creating a new product (`POST /v1/products`). */
    object CreateProduct: ProductEndpoints()

    /**
     * Endpoint for updating an existing product (`PATCH /v1/products/{productId}`).
     *
     * @property productId The unique identifier of the product to update.
     */
    data class UpdateProduct(val productId: String): ProductEndpoints()

    /**
     * Endpoint for retrieving a paginated list of products (`GET /v1/products`).
     *
     * @property perPage The number of products to return per page.
     * @property page The page number to retrieve.
     */
    data class GetProducts(val perPage: Int? = null, val page: Int? = null): ProductEndpoints()

    /**
     * Endpoint for retrieving a single product by identifier (`GET /v1/products/{productId}`).
     *
     * @property productId The unique identifier of the product to retrieve.
     */
    data class GetProductWith(val productId: String): ProductEndpoints()

    /**
     * Endpoint for searching products by filter criteria (`GET /v1/products/search`).
     *
     * @property name Filters results to products whose name matches this value.
     * @property active Filters results to active or inactive products.
     * @property shippable Filters results to shippable or non-shippable products.
     */
    data class SearchProducts(val name: String? = null, val active: Boolean? = null, val shippable: Boolean? = null): ProductEndpoints()

    /**
     * Endpoint for deleting a product (`DELETE /v1/products/{productId}`).
     *
     * @property productId The unique identifier of the product to delete.
     */
    data class DeleteProduct(val productId: String): ProductEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is CreateProduct, is GetProducts ->
                "/v1/products"
            is UpdateProduct ->
                "/v1/products/${this.productId}"
            is DeleteProduct ->
                "/v1/products/${this.productId}"
            is GetProductWith ->
                "/v1/products/${this.productId}"
            is SearchProducts ->
                "/v1/products/search"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateProduct -> "POST"
            is UpdateProduct -> "PATCH"
            is DeleteProduct -> "DELETE"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>?
        get() = when (this) {
            is GetProducts -> {
                val items = mutableListOf<QueryItem>()
                perPage?.let { items.add(QueryItem("per_page", it.toString())) }
                page?.let { items.add(QueryItem("page", it.toString())) }
                items
            }
            is SearchProducts -> {
                val items = mutableListOf<QueryItem>()
                name?.let { items.add(QueryItem("name", it.toString())) }
                active?.let { items.add(QueryItem("active", it.toString())) }
                shippable?.let { items.add(QueryItem("shippable", it.toString())) }
                items
            }
            else -> null
        }
}
