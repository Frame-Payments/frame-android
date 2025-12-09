package com.framepayments.framesdk.products

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class ProductEndpoints : FrameNetworkingEndpoints {
    object CreateProduct: ProductEndpoints()
    data class UpdateProduct(val productId: String): ProductEndpoints()
    data class GetProducts(val perPage: Int? = null, val page: Int? = null): ProductEndpoints()
    data class GetProductWith(val productId: String): ProductEndpoints()
    data class SearchProducts(val name: String? = null, val active: Boolean? = null, val shippable: Boolean? = null): ProductEndpoints()
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