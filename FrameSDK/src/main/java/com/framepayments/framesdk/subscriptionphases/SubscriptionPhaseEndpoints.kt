package com.framepayments.framesdk.subscriptionphases

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines all API endpoints for subscription phase operations.
 *
 * Each case carries the path parameters required to construct its URL and selects
 * the appropriate HTTP method.
 */
sealed class SubscriptionPhaseEndpoints: FrameNetworkingEndpoints {

    /**
     * Retrieves all phases for a subscription. Resolves to GET /v1/subscriptions/{subscriptionId}/phases.
     *
     * @property subscriptionId The ID of the subscription whose phases to list.
     */
    data class GetSubscriptionPhases(val subscriptionId: String): SubscriptionPhaseEndpoints()

    /**
     * Retrieves a single phase by ID. Resolves to GET /v1/subscriptions/{subscriptionId}/phases/{phaseId}.
     *
     * @property subscriptionId The ID of the subscription that owns the phase.
     * @property phaseId The ID of the phase to retrieve.
     */
    data class GetSubscriptionPhaseWith(val subscriptionId: String, val phaseId: String): SubscriptionPhaseEndpoints()

    /**
     * Creates a new phase on a subscription. Resolves to POST /v1/subscriptions/{subscriptionId}/phases.
     *
     * @property subscriptionId The ID of the subscription to add the phase to.
     */
    data class CreateSubscriptionPhase(val subscriptionId: String): SubscriptionPhaseEndpoints()

    /**
     * Updates an existing phase on a subscription. Resolves to PATCH /v1/subscriptions/{subscriptionId}/phases/{phaseId}.
     *
     * @property subscriptionId The ID of the subscription that owns the phase.
     * @property phaseId The ID of the phase to update.
     */
    data class UpdateSubscriptionPhase(val subscriptionId: String, val phaseId: String): SubscriptionPhaseEndpoints()

    /**
     * Deletes a phase from a subscription. Resolves to DELETE /v1/subscriptions/{subscriptionId}/phases/{phaseId}.
     *
     * @property subscriptionId The ID of the subscription that owns the phase.
     * @property phaseId The ID of the phase to delete.
     */
    data class DeleteSubscriptionPhase(val subscriptionId: String, val phaseId: String): SubscriptionPhaseEndpoints()

    /**
     * Replaces all phases on a subscription in a single request. Resolves to PATCH /v1/subscriptions/{subscriptionId}/phases/bulk_update.
     *
     * @property subscriptionId The ID of the subscription whose phases to replace.
     */
    data class BulkUpdateSubscriptionPhases(val subscriptionId: String): SubscriptionPhaseEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is GetSubscriptionPhases ->
                "/v1/subscriptions/${this.subscriptionId}/phases"
            is CreateSubscriptionPhase ->
                "/v1/subscriptions/${this.subscriptionId}/phases"
            is GetSubscriptionPhaseWith ->
                "/v1/subscriptions/${this.subscriptionId}/phases/${this.phaseId}"
            is UpdateSubscriptionPhase ->
                "/v1/subscriptions/${this.subscriptionId}/phases/${this.phaseId}"
            is DeleteSubscriptionPhase ->
                "/v1/subscriptions/${this.subscriptionId}/phases/${this.phaseId}"
            is BulkUpdateSubscriptionPhases ->
                "/v1/subscriptions/${this.subscriptionId}/phases/bulk_update"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateSubscriptionPhase -> "POST"
            is UpdateSubscriptionPhase, is BulkUpdateSubscriptionPhases -> "PATCH"
            is DeleteSubscriptionPhase -> "DELETE"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>? = null
}
