package com.framepayments.framesdk.chargeintents
import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines every API endpoint used to manage charge intents.
 *
 * Each case carries the data required to build its URL, HTTP method, and query parameters.
 * Implement [FrameNetworkingEndpoints] so the networking layer can dispatch requests uniformly.
 */
sealed class ChargeIntentEndpoints : FrameNetworkingEndpoints {

    /** Creates a new charge intent (`POST /v1/charge_intents`). */
    object CreateChargeIntent : ChargeIntentEndpoints()

    /**
     * Retrieves a single charge intent by ID (`GET /v1/charge_intents/{intentId}`).
     *
     * @property intentId The ID of the charge intent to retrieve.
     */
    data class GetChargeIntent(val intentId: String) : ChargeIntentEndpoints()

    /**
     * Retrieves a paginated list of all charge intents (`GET /v1/charge_intents`).
     *
     * @property perPage Number of results to return per page, or `null` to use the API default.
     * @property page 1-based page number to retrieve, or `null` to use the API default.
     */
    data class GetAllChargeIntents(val perPage: Int?, val page: Int?) : ChargeIntentEndpoints()

    /**
     * Updates an existing charge intent (`PATCH /v1/charge_intents/{intentId}`).
     *
     * @property intentId The ID of the charge intent to update.
     */
    data class UpdateChargeIntent(val intentId: String) : ChargeIntentEndpoints()

    /**
     * Captures an authorized charge intent (`POST /v1/charge_intents/{intentId}/capture`).
     *
     * @property intentId The ID of the charge intent to capture.
     */
    data class CaptureChargeIntent(val intentId: String) : ChargeIntentEndpoints()

    /**
     * Confirms a charge intent, triggering payment processing (`POST /v1/charge_intents/{intentId}/confirm`).
     *
     * @property intentId The ID of the charge intent to confirm.
     */
    data class ConfirmChargeIntent(val intentId: String) : ChargeIntentEndpoints()

    /**
     * Cancels a charge intent before it is captured (`POST /v1/charge_intents/{intentId}/cancel`).
     *
     * @property intentId The ID of the charge intent to cancel.
     */
    data class CancelChargeIntent(val intentId: String) : ChargeIntentEndpoints()

    /**
     * Voids the uncaptured remainder of a partially captured charge intent
     * (`POST /v1/charge_intents/{intentId}/void_remaining`).
     *
     * @property intentId The ID of the charge intent whose remaining authorized amount should be voided.
     */
    data class VoidRemainingChargeIntent(val intentId: String) : ChargeIntentEndpoints()

    /** Returns the relative URL path for this endpoint. */
    override val endpointURL: String
        get() = when (this) {
            is CreateChargeIntent, is GetAllChargeIntents ->
                "/v1/charge_intents"
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
            is VoidRemainingChargeIntent ->
                "/v1/charge_intents/${this.intentId}/void_remaining"
        }

    /** Returns the HTTP method string for this endpoint. */
    override val httpMethod: String
        get() = when (this) {
            is CreateChargeIntent, is CancelChargeIntent, is CaptureChargeIntent,
            is ConfirmChargeIntent, is VoidRemainingChargeIntent -> "POST"
            is UpdateChargeIntent -> "PATCH"
            else -> "GET"
        }

    /** Returns the URL query parameters for this endpoint, or `null` if none apply. */
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
