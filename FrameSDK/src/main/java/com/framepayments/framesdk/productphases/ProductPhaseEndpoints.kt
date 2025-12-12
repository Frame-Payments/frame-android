package com.framepayments.framesdk.productphases

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class ProductPhaseEndpoints: FrameNetworkingEndpoints {
    data class GetProductPhases(val productId: String): ProductPhaseEndpoints()
    data class GetProductPhaseWith(val productId: String, val phaseId: String): ProductPhaseEndpoints()
    data class CreateProductPhase(val productId: String): ProductPhaseEndpoints()
    data class UpdateProductPhase(val productId: String, val phaseId: String): ProductPhaseEndpoints()
    data class DeleteProductPhase(val productId: String, val phaseId: String): ProductPhaseEndpoints()
    data class BulkUpdateProductPhases(val productId: String): ProductPhaseEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is GetProductPhases ->
                "/v1/products/${this.productId}/phases"
            is CreateProductPhase ->
                "/v1/products/${this.productId}/phases"
            is GetProductPhaseWith ->
                "/v1/products/${this.productId}/phases/${this.phaseId}"
            is UpdateProductPhase ->
                "/v1/products/${this.productId}/phases/${this.phaseId}"
            is DeleteProductPhase ->
                "/v1/products/${this.productId}/phases/${this.phaseId}"
            is BulkUpdateProductPhases ->
                "/v1/products/${this.productId}/phases/bulk_update"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateProductPhase -> "POST"
            is UpdateProductPhase, is BulkUpdateProductPhases -> "PATCH"
            is DeleteProductPhase -> "DELETE"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>? = null
}