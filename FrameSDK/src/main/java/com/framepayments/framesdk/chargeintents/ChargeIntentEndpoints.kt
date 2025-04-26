package com.framepayments.framesdk.chargeintents
import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class ChargeIntentEndpoints : FrameNetworkingEndpoints {
    object CreateChargeIntent : ChargeIntentEndpoints()
    data class GetChargeIntent(val intentId: String) : ChargeIntentEndpoints()
    data class GetAllChargeIntents(val perPage: Int?, val page: Int?) : ChargeIntentEndpoints()
    data class UpdateChargeIntent(val intentId: String) : ChargeIntentEndpoints()
    data class CaptureChargeIntent(val intentId: String) : ChargeIntentEndpoints()
    data class ConfirmChargeIntent(val intentId: String) : ChargeIntentEndpoints()
    data class CancelChargeIntent(val intentId: String) : ChargeIntentEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is CreateChargeIntent, is GetAllChargeIntents ->
                "/v1/charge_intents/"
            is GetChargeIntent ->
                "/v1/charge_intents/${this.intentId}"
            is UpdateChargeIntent ->
                "/v1/charge_intents/${this.intentId}"
            is CaptureChargeIntent ->
                "/v1/charge_intents/${this.intentId}/capture"
            is ConfirmChargeIntent ->
                "/v1/charge_intents/${this.intentId}/confirm"
            is CancelChargeIntent ->
                "/v1/charge_intents/${this.intentId}/cancel"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateChargeIntent, is CancelChargeIntent, is CaptureChargeIntent, is ConfirmChargeIntent -> "POST"
            is UpdateChargeIntent -> "PATCH"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>?
        get() = when (this) {
            is GetAllChargeIntents -> {
                val items = mutableListOf<QueryItem>()
                perPage?.let { items.add(QueryItem("per_page", it.toString())) }
                page?.let { items.add(QueryItem("page", it.toString())) }
                items
            }
            else -> null
        }
}