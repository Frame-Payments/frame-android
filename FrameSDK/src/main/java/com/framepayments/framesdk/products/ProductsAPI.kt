package com.framepayments.framesdk.products

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

object ProductsAPI {
    //MARK: Methods using coroutines
    suspend fun createProduct(request: ProductsRequests.CreateProductRequest): Pair<Product?, NetworkingError?> {
        val endpoint = ProductEndpoints.CreateProduct
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<Product>(data) }, error)
    }

    suspend fun updateProduct(productId: String, request: ProductsRequests.UpdateProductRequest): Pair<Product?, NetworkingError?> {
        val endpoint = ProductEndpoints.UpdateProduct(productId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<Product>(data) }, error)
    }

    suspend fun getProducts(page: Int? = null, perPage: Int? = null): Pair<ProductsResponses.ListProductsResponse?, NetworkingError?> {
        val endpoint = ProductEndpoints.GetProducts(perPage = perPage, page = page)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<ProductsResponses.ListProductsResponse>(data) }, error)
    }

    suspend fun getProductWith(productId: String): Pair<Product?, NetworkingError?> {
        val endpoint = ProductEndpoints.GetProductWith(productId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<Product>(data) }, error)
    }

    suspend fun searchProducts(name: String? = null, active: Boolean? = null, shippable: Boolean? = null): Pair<List<Product>?, NetworkingError?> {
        val endpoint = ProductEndpoints.SearchProducts(name, active, shippable)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<ProductsResponses.ListProductsResponse>(data)?.data }, error)
    }

    suspend fun deleteProduct(productId: String): Pair<ProductsResponses.DeleteProductsResponse?, NetworkingError?> {
        val endpoint = ProductEndpoints.DeleteProduct(productId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<ProductsResponses.DeleteProductsResponse>(data) }, error)
    }

    //MARK: Methods using callbacks
    fun createProduct(request: ProductsRequests.CreateProductRequest, completionHandler: (Product?, NetworkingError?) -> Unit) {
        val endpoint = ProductEndpoints.CreateProduct

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Product>(data) }, error)
        }
    }

    fun updateProduct(productId: String, request: ProductsRequests.UpdateProductRequest, completionHandler: (Product?, NetworkingError?) -> Unit) {
        val endpoint = ProductEndpoints.UpdateProduct(productId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Product>(data) }, error)
        }
    }

    fun getProducts(page: Int? = null, perPage: Int? = null, completionHandler: (ProductsResponses.ListProductsResponse?, NetworkingError?) -> Unit) {
        val endpoint = ProductEndpoints.GetProducts(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<ProductsResponses.ListProductsResponse>(data) }, error)
        }
    }

    fun getProductWith(productId: String, completionHandler: (Product?, NetworkingError?) -> Unit) {
        val endpoint = ProductEndpoints.GetProductWith(productId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Product>(data) }, error)
        }
    }

    fun searchProducts(name: String? = null, active: Boolean? = null, shippable: Boolean? = null, completionHandler: (List<Product>?, NetworkingError?) -> Unit) {
        val endpoint = ProductEndpoints.SearchProducts(name, active, shippable)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<ProductsResponses.ListProductsResponse>(data)?.data }, error)
        }
    }

    fun deleteProduct(productId: String, completionHandler: (ProductsResponses.DeleteProductsResponse?, NetworkingError?) -> Unit) {
        val endpoint = ProductEndpoints.DeleteProduct(productId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<ProductsResponses.DeleteProductsResponse>(data) }, error)
        }
    }
}
