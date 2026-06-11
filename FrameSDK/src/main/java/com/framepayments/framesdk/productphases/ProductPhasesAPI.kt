package com.framepayments.framesdk.productphases

import com.framepayments.framesdk.subscriptionphases.SubscriptionPhase
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Provides suspend and callback-based methods for managing pricing phases on products.
 *
 * Product phases define the billing stages of a subscription-based product. Each operation
 * has two overloads: a coroutine-based suspend function that returns a [Pair] and a
 * callback-based variant for use outside of coroutine scopes.
 */
object ProductPhasesAPI {
    //MARK: Methods using coroutines

    /**
     * Creates a new phase for a product.
     *
     * @param productId The unique identifier of the product to add a phase to.
     * @param request The phase attributes to create.
     * @return A [Pair] containing the created [SubscriptionPhase] on success, or a [NetworkingError] on failure.
     */
    suspend fun createProductPhase(productId: String, request: ProductPhaseRequest.CreateProductPhaseRequest): Pair<SubscriptionPhase?, NetworkingError?> {
        val endpoint = ProductPhaseEndpoints.CreateProductPhase(productId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(FrameNetworking.parseResponse<SubscriptionPhase>(data), error)
    }

    /**
     * Updates a specific phase on a product.
     *
     * @param productId The unique identifier of the product that owns the phase.
     * @param phaseId The unique identifier of the phase to update.
     * @param request The phase attributes to update.
     * @return A [Pair] containing the updated [SubscriptionPhase] on success, or a [NetworkingError] on failure.
     */
    suspend fun updateProductPhase(productId: String, phaseId: String, request: ProductPhaseRequest.UpdateProductPhaseRequest): Pair<SubscriptionPhase?, NetworkingError?> {
        val endpoint = ProductPhaseEndpoints.UpdateProductPhase(productId, phaseId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(FrameNetworking.parseResponse<SubscriptionPhase>(data), error)
    }

    /**
     * Retrieves a specific phase on a product by its identifier.
     *
     * @param productId The unique identifier of the product that owns the phase.
     * @param phaseId The unique identifier of the phase to retrieve.
     * @return A [Pair] containing the matching [SubscriptionPhase] on success, or a [NetworkingError] on failure.
     */
    suspend fun getProductPhaseWith(productId: String, phaseId: String): Pair<SubscriptionPhase?, NetworkingError?> {
        val endpoint = ProductPhaseEndpoints.GetProductPhaseWith(productId, phaseId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(FrameNetworking.parseResponse<SubscriptionPhase>(data), error)
    }

    /**
     * Retrieves all phases associated with a product.
     *
     * @param productId The unique identifier of the product whose phases to retrieve.
     * @return A [Pair] containing a [ListProductPhaseResponse] on success, or a [NetworkingError] on failure.
     */
    suspend fun getProductPhases(productId: String): Pair<ListProductPhaseResponse?, NetworkingError?> {
        val endpoint = ProductPhaseEndpoints.GetProductPhases(productId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(FrameNetworking.parseResponse<ListProductPhaseResponse>(data), error)
    }

    /**
     * Replaces all phases on a product in a single request.
     *
     * @param productId The unique identifier of the product whose phases to replace.
     * @param request The complete set of phases to apply to the product.
     * @return A [Pair] containing a [ListProductPhaseResponse] reflecting the updated phases on success, or a [NetworkingError] on failure.
     */
    suspend fun bulkUpdateProductPhases(productId: String, request: ProductPhaseRequest.BulkUpdateProductPhaseRequest) : Pair<ListProductPhaseResponse?, NetworkingError?> {
        val endpoint = ProductPhaseEndpoints.BulkUpdateProductPhases(productId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(FrameNetworking.parseResponse<ListProductPhaseResponse>(data), error)
    }

    /**
     * Deletes a specific phase from a product.
     *
     * @param productId The unique identifier of the product that owns the phase.
     * @param phaseId The unique identifier of the phase to delete.
     * @return A [NetworkingError] if the request failed, or null on success.
     */
    suspend fun deleteProductPhaseWith(productId: String, phaseId: String): NetworkingError? {
        val endpoint = ProductPhaseEndpoints.DeleteProductPhase(productId, phaseId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return error
    }

    //MARK: Methods using callbacks

    /**
     * Creates a new phase for a product and delivers the result via a callback.
     *
     * @param productId The unique identifier of the product to add a phase to.
     * @param request The phase attributes to create.
     * @param completionHandler Invoked with the created [SubscriptionPhase] on success, or a [NetworkingError] on failure.
     */
    fun createProductPhase(productId: String, request: ProductPhaseRequest.CreateProductPhaseRequest, completionHandler: (SubscriptionPhase?, NetworkingError?) -> Unit) {
        val endpoint = ProductPhaseEndpoints.CreateProductPhase(productId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionPhase>(data) }, error)
        }
    }

    /**
     * Updates a specific phase on a product and delivers the result via a callback.
     *
     * @param productId The unique identifier of the product that owns the phase.
     * @param phaseId The unique identifier of the phase to update.
     * @param request The phase attributes to update.
     * @param completionHandler Invoked with the updated [SubscriptionPhase] on success, or a [NetworkingError] on failure.
     */
    fun updateProductPhase(productId: String, phaseId: String, request: ProductPhaseRequest.UpdateProductPhaseRequest, completionHandler: (SubscriptionPhase?, NetworkingError?) -> Unit) {
        val endpoint = ProductPhaseEndpoints.UpdateProductPhase(productId, phaseId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionPhase>(data) }, error)
        }
    }

    /**
     * Retrieves a specific phase on a product by its identifier and delivers the result via a callback.
     *
     * @param productId The unique identifier of the product that owns the phase.
     * @param phaseId The unique identifier of the phase to retrieve.
     * @param completionHandler Invoked with the matching [SubscriptionPhase] on success, or a [NetworkingError] on failure.
     */
    fun getProductWith(productId: String, phaseId: String, completionHandler: (SubscriptionPhase?, NetworkingError?) -> Unit) {
        val endpoint = ProductPhaseEndpoints.GetProductPhaseWith(productId, phaseId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionPhase>(data) }, error)
        }
    }

    /**
     * Retrieves all phases associated with a product and delivers the result via a callback.
     *
     * @param productId The unique identifier of the product whose phases to retrieve.
     * @param completionHandler Invoked with a [ListProductPhaseResponse] on success, or a [NetworkingError] on failure.
     */
    fun getProducts(productId: String, completionHandler: (ListProductPhaseResponse?, NetworkingError?) -> Unit) {
        val endpoint = ProductPhaseEndpoints.GetProductPhases(productId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ListProductPhaseResponse>(data) }, error)
        }
    }

    /**
     * Replaces all phases on a product in a single request and delivers the result via a callback.
     *
     * @param productId The unique identifier of the product whose phases to replace.
     * @param request The complete set of phases to apply to the product.
     * @param completionHandler Invoked with a [ListProductPhaseResponse] reflecting the updated phases on success, or a [NetworkingError] on failure.
     */
    fun bulkUpdateProductPhases(productId: String, request: ProductPhaseRequest.BulkUpdateProductPhaseRequest, completionHandler: (ListProductPhaseResponse?, NetworkingError?) -> Unit) {
        val endpoint = ProductPhaseEndpoints.BulkUpdateProductPhases(productId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ListProductPhaseResponse>(data) }, error)
        }
    }

    /**
     * Deletes a specific phase from a product and delivers the result via a callback.
     *
     * @param productId The unique identifier of the product that owns the phase.
     * @param phaseId The unique identifier of the phase to delete.
     * @param completionHandler Invoked with a [NetworkingError] if the request failed, or null on success.
     */
    fun deleteProductPhaseWith(productId: String, phaseId: String, completionHandler: (NetworkingError?) -> Unit) {
        val endpoint = ProductPhaseEndpoints.DeleteProductPhase(productId, phaseId)

        FrameNetworking.performDataTask(endpoint) { _, error ->
            completionHandler(error)
        }
    }
}


