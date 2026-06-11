package com.framepayments.framesdk.productphases

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines the API endpoints available for product phase operations.
 *
 * Each case maps to a specific URL, HTTP method, and optional query parameters
 * used by the networking layer to build requests.
 */
sealed class ProductPhaseEndpoints: FrameNetworkingEndpoints {

    /**
     * Endpoint for retrieving all phases of a product (`GET /v1/products/{productId}/phases`).
     *
     * @property productId The unique identifier of the product whose phases to retrieve.
     */
    data class GetProductPhases(val productId: String): ProductPhaseEndpoints()

    /**
     * Endpoint for retrieving a specific phase by identifier (`GET /v1/products/{productId}/phases/{phaseId}`).
     *
     * @property productId The unique identifier of the product that owns the phase.
     * @property phaseId The unique identifier of the phase to retrieve.
     */
    data class GetProductPhaseWith(val productId: String, val phaseId: String): ProductPhaseEndpoints()

    /**
     * Endpoint for creating a new phase on a product (`POST /v1/products/{productId}/phases`).
     *
     * @property productId The unique identifier of the product to add a phase to.
     */
    data class CreateProductPhase(val productId: String): ProductPhaseEndpoints()

    /**
     * Endpoint for updating a specific phase on a product (`PATCH /v1/products/{productId}/phases/{phaseId}`).
     *
     * @property productId The unique identifier of the product that owns the phase.
     * @property phaseId The unique identifier of the phase to update.
     */
    data class UpdateProductPhase(val productId: String, val phaseId: String): ProductPhaseEndpoints()

    /**
     * Endpoint for deleting a specific phase from a product (`DELETE /v1/products/{productId}/phases/{phaseId}`).
     *
     * @property productId The unique identifier of the product that owns the phase.
     * @property phaseId The unique identifier of the phase to delete.
     */
    data class DeleteProductPhase(val productId: String, val phaseId: String): ProductPhaseEndpoints()

    /**
     * Endpoint for replacing all phases on a product in a single request (`PATCH /v1/products/{productId}/phases/bulk_update`).
     *
     * @property productId The unique identifier of the product whose phases to replace.
     */
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
