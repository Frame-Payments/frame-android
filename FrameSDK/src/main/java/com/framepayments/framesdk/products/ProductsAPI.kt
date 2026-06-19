package com.framepayments.framesdk.products

import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Provides suspend and callback-based methods for managing products in the Frame API.
 *
 * Each operation has two overloads: a coroutine-based suspend function that returns a [Pair]
 * and a callback-based variant for use outside of coroutine scopes.
 */
object ProductsAPI {
    //MARK: Methods using coroutines

    /**
     * Creates a new product.
     *
     * @param request The product attributes to create.
     * @return A [Pair] containing the created [Product] on success, or a [NetworkingError] on failure.
     */
    suspend fun createProduct(request: ProductsRequests.CreateProductRequest): Pair<Product?, NetworkingError?> {
        val endpoint = ProductEndpoints.CreateProduct
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<Product>(data) }, error)
    }

    /**
     * Updates an existing product.
     *
     * @param productId The unique identifier of the product to update.
     * @param request The product attributes to update.
     * @return A [Pair] containing the updated [Product] on success, or a [NetworkingError] on failure.
     */
    suspend fun updateProduct(productId: String, request: ProductsRequests.UpdateProductRequest): Pair<Product?, NetworkingError?> {
        val endpoint = ProductEndpoints.UpdateProduct(productId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<Product>(data) }, error)
    }

    /**
     * Retrieves a paginated list of products.
     *
     * @param page The page number to retrieve. Defaults to the first page when null.
     * @param perPage The number of products to return per page. Uses the API default when null.
     * @return A [Pair] containing a [ProductsResponses.ListProductsResponse] on success, or a [NetworkingError] on failure.
     */
    suspend fun getProducts(page: Int? = null, perPage: Int? = null): Pair<ProductsResponses.ListProductsResponse?, NetworkingError?> {
        val endpoint = ProductEndpoints.GetProducts(perPage = perPage, page = page)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<ProductsResponses.ListProductsResponse>(data) }, error)
    }

    /**
     * Retrieves a single product by its identifier.
     *
     * @param productId The unique identifier of the product to retrieve.
     * @return A [Pair] containing the matching [Product] on success, or a [NetworkingError] on failure.
     */
    suspend fun getProductWith(productId: String): Pair<Product?, NetworkingError?> {
        val endpoint = ProductEndpoints.GetProductWith(productId)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<Product>(data) }, error)
    }

    /**
     * Searches products by optional filter criteria.
     *
     * @param name Filters results to products whose name matches this value. Omitted when null.
     * @param active Filters results to active or inactive products. Omitted when null.
     * @param shippable Filters results to shippable or non-shippable products. Omitted when null.
     * @return A [Pair] containing the matching list of [Product] objects on success, or a [NetworkingError] on failure.
     */
    suspend fun searchProducts(name: String? = null, active: Boolean? = null, shippable: Boolean? = null): Pair<List<Product>?, NetworkingError?> {
        val endpoint = ProductEndpoints.SearchProducts(name, active, shippable)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<ProductsResponses.ListProductsResponse>(data)?.data }, error)
    }

    /**
     * Deletes a product by its identifier.
     *
     * @param productId The unique identifier of the product to delete.
     * @return A [Pair] containing a [ProductsResponses.DeleteProductsResponse] on success, or a [NetworkingError] on failure.
     */
    suspend fun deleteProduct(productId: String): Pair<ProductsResponses.DeleteProductsResponse?, NetworkingError?> {
        val endpoint = ProductEndpoints.DeleteProduct(productId)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<ProductsResponses.DeleteProductsResponse>(data) }, error)
    }

    //MARK: Methods using callbacks

    /**
     * Creates a new product and delivers the result via a callback.
     *
     * @param request The product attributes to create.
     * @param completionHandler Invoked with the created [Product] on success, or a [NetworkingError] on failure.
     */
    fun createProduct(request: ProductsRequests.CreateProductRequest, completionHandler: (Product?, NetworkingError?) -> Unit) {
        val endpoint = ProductEndpoints.CreateProduct

        FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Product>(data) }, error)
        }
    }

    /**
     * Updates an existing product and delivers the result via a callback.
     *
     * @param productId The unique identifier of the product to update.
     * @param request The product attributes to update.
     * @param completionHandler Invoked with the updated [Product] on success, or a [NetworkingError] on failure.
     */
    fun updateProduct(productId: String, request: ProductsRequests.UpdateProductRequest, completionHandler: (Product?, NetworkingError?) -> Unit) {
        val endpoint = ProductEndpoints.UpdateProduct(productId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Product>(data) }, error)
        }
    }

    /**
     * Retrieves a paginated list of products and delivers the result via a callback.
     *
     * @param page The page number to retrieve. Defaults to the first page when null.
     * @param perPage The number of products to return per page. Uses the API default when null.
     * @param completionHandler Invoked with a [ProductsResponses.ListProductsResponse] on success, or a [NetworkingError] on failure.
     */
    fun getProducts(page: Int? = null, perPage: Int? = null, completionHandler: (ProductsResponses.ListProductsResponse?, NetworkingError?) -> Unit) {
        val endpoint = ProductEndpoints.GetProducts(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<ProductsResponses.ListProductsResponse>(data) }, error)
        }
    }

    /**
     * Retrieves a single product by its identifier and delivers the result via a callback.
     *
     * @param productId The unique identifier of the product to retrieve.
     * @param completionHandler Invoked with the matching [Product] on success, or a [NetworkingError] on failure.
     */
    fun getProductWith(productId: String, completionHandler: (Product?, NetworkingError?) -> Unit) {
        val endpoint = ProductEndpoints.GetProductWith(productId)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<Product>(data) }, error)
        }
    }

    /**
     * Searches products by optional filter criteria and delivers the result via a callback.
     *
     * @param name Filters results to products whose name matches this value. Omitted when null.
     * @param active Filters results to active or inactive products. Omitted when null.
     * @param shippable Filters results to shippable or non-shippable products. Omitted when null.
     * @param completionHandler Invoked with the matching list of [Product] objects on success, or a [NetworkingError] on failure.
     */
    fun searchProducts(name: String? = null, active: Boolean? = null, shippable: Boolean? = null, completionHandler: (List<Product>?, NetworkingError?) -> Unit) {
        val endpoint = ProductEndpoints.SearchProducts(name, active, shippable)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<ProductsResponses.ListProductsResponse>(data)?.data }, error)
        }
    }

    /**
     * Deletes a product by its identifier and delivers the result via a callback.
     *
     * @param productId The unique identifier of the product to delete.
     * @param completionHandler Invoked with a [ProductsResponses.DeleteProductsResponse] on success, or a [NetworkingError] on failure.
     */
    fun deleteProduct(productId: String, completionHandler: (ProductsResponses.DeleteProductsResponse?, NetworkingError?) -> Unit) {
        val endpoint = ProductEndpoints.DeleteProduct(productId)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<ProductsResponses.DeleteProductsResponse>(data) }, error)
        }
    }
}
