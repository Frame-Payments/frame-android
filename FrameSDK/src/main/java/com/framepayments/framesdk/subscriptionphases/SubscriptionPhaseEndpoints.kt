package com.framepayments.framesdk.subscriptionphases

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class SubscriptionPhaseEndpoints: FrameNetworkingEndpoints {
    data class GetSubscriptionPhases(val subscriptionId: String): SubscriptionPhaseEndpoints()
    data class GetSubscriptionPhaseWith(val subscriptionId: String, val phaseId: String): SubscriptionPhaseEndpoints()
    data class CreateSubscriptionPhase(val subscriptionId: String): SubscriptionPhaseEndpoints()
    data class UpdateSubscriptionPhase(val subscriptionId: String, val phaseId: String): SubscriptionPhaseEndpoints()
    data class DeleteSubscriptionPhase(val subscriptionId: String, val phaseId: String): SubscriptionPhaseEndpoints()
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