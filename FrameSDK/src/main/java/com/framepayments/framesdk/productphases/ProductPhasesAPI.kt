package com.framepayments.framesdk.productphases

import com.framepayments.framesdk.subscriptionphases.SubscriptionPhase
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

object ProductPhasesAPI {
    //MARK: Methods using coroutines
    suspend fun createProductPhase(productId: String, request: ProductPhaseRequest.CreateProductPhaseRequest): Pair<SubscriptionPhase?, NetworkingError?> {
        val endpoint = ProductPhaseEndpoints.CreateProductPhase(productId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(FrameNetworking.parseResponse<SubscriptionPhase>(data), error)
    }

    suspend fun updateProductPhase(productId: String, phaseId: String, request: ProductPhaseRequest.UpdateProductPhaseRequest): Pair<SubscriptionPhase?, NetworkingError?> {
        val endpoint = ProductPhaseEndpoints.UpdateProductPhase(productId, phaseId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(FrameNetworking.parseResponse<SubscriptionPhase>(data), error)
    }

    suspend fun getProductPhaseWith(productId: String, phaseId: String): Pair<SubscriptionPhase?, NetworkingError?> {
        val endpoint = ProductPhaseEndpoints.GetProductPhaseWith(productId, phaseId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(FrameNetworking.parseResponse<SubscriptionPhase>(data), error)
    }

    suspend fun getProductPhases(productId: String): Pair<ListProductPhaseResponse?, NetworkingError?> {
        val endpoint = ProductPhaseEndpoints.GetProductPhases(productId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(FrameNetworking.parseResponse<ListProductPhaseResponse>(data), error)
    }

    suspend fun bulkUpdateProductPhases(productId: String, request: ProductPhaseRequest.BulkUpdateProductPhaseRequest) : Pair<List<SubscriptionPhase>?, NetworkingError?> {
        val endpoint = ProductPhaseEndpoints.BulkUpdateProductPhases(productId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(FrameNetworking.parseResponse<ListProductPhaseResponse>(data)?.phases, error)
    }

    suspend fun deleteProductPhaseWith(productId: String, phaseId: String): NetworkingError? {
        val endpoint = ProductPhaseEndpoints.DeleteProductPhase(productId, phaseId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return error
    }

    //MARK: Methods using callbacks
    fun createProductPhase(productId: String, request: ProductPhaseRequest.CreateProductPhaseRequest, completionHandler: (SubscriptionPhase?, NetworkingError?) -> Unit) {
        val endpoint = ProductPhaseEndpoints.CreateProductPhase(productId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionPhase>(data) }, error)
        }
    }

    fun updateProductPhase(productId: String, phaseId: String, request: ProductPhaseRequest.UpdateProductPhaseRequest, completionHandler: (SubscriptionPhase?, NetworkingError?) -> Unit) {
        val endpoint = ProductPhaseEndpoints.UpdateProductPhase(productId, phaseId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionPhase>(data) }, error)
        }
    }

    fun getProductWith(productId: String, phaseId: String, completionHandler: (SubscriptionPhase?, NetworkingError?) -> Unit) {
        val endpoint = ProductPhaseEndpoints.GetProductPhaseWith(productId, phaseId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<SubscriptionPhase>(data) }, error)
        }
    }

    fun getProducts(productId: String, completionHandler: (ListProductPhaseResponse?, NetworkingError?) -> Unit) {
        val endpoint = ProductPhaseEndpoints.GetProductPhases(productId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ListProductPhaseResponse>(data) }, error)
        }
    }

    fun bulkUpdateProductPhases(productId: String, request: ProductPhaseRequest.BulkUpdateProductPhaseRequest, completionHandler: (List<SubscriptionPhase>?, NetworkingError?) -> Unit) {
        val endpoint = ProductPhaseEndpoints.BulkUpdateProductPhases(productId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ListProductPhaseResponse>(data)?.phases }, error)
        }
    }

    fun deleteProductPhaseWith(productId: String, phaseId: String, completionHandler: (NetworkingError?) -> Unit) {
        val endpoint = ProductPhaseEndpoints.DeleteProductPhase(productId, phaseId)

        FrameNetworking.performDataTask(endpoint) { _, error ->
            completionHandler(error)
        }
    }
}


