package com.framepayments.framesdk.transfersv2

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines the network endpoints for the Transfers V2 API (`/v2/transfers`).
 *
 * Each case maps to a specific API route and HTTP method used by [TransfersV2API].
 */
sealed class TransferV2Endpoints : FrameNetworkingEndpoints {

    /**
     * Endpoint for creating a new V2 transfer (POST /v2/transfers).
     *
     * @property idempotencyKey Value sent as the `Idempotency-Key` header.
     */
    data class CreateTransfer(val idempotencyKey: String) : TransferV2Endpoints()

    /**
     * Endpoint for retrieving a single V2 transfer by identifier (GET /v2/transfers/{id}).
     *
     * @property transferId The unique identifier of the transfer to retrieve.
     */
    data class GetTransferWith(val transferId: String) : TransferV2Endpoints()

    /**
     * Endpoint for retrieving a paginated list of V2 transfers (GET /v2/transfers).
     *
     * @property perPage The number of results per page, or null to use the API default.
     * @property page The page number to retrieve, or null to retrieve the first page.
     */
    data class GetTransfers(val perPage: Int?, val page: Int?) : TransferV2Endpoints()

    /**
     * Endpoint for updating a deferred-confirm payment transfer (PATCH /v2/transfers/{id}).
     *
     * @property transferId The unique identifier of the transfer to update.
     */
    data class UpdateTransfer(val transferId: String) : TransferV2Endpoints()

    /**
     * Endpoint for confirming a deferred-confirm payment transfer (POST /v2/transfers/{id}/confirm).
     *
     * Secret-key confirm sends [idempotencyKey]; publishable + `client_secret` passes null.
     *
     * @property transferId The unique identifier of the transfer to confirm.
     * @property idempotencyKey Optional `Idempotency-Key` for secret-key confirms.
     */
    data class ConfirmTransfer(
        val transferId: String,
        val idempotencyKey: String? = null
    ) : TransferV2Endpoints()

    /**
     * Endpoint for capturing a manually authorized payment transfer (POST /v2/transfers/{id}/capture).
     *
     * @property transferId The unique identifier of the transfer to capture.
     * @property idempotencyKey Value sent as the `Idempotency-Key` header.
     */
    data class CaptureTransfer(
        val transferId: String,
        val idempotencyKey: String
    ) : TransferV2Endpoints()

    /**
     * Endpoint for voiding a manually authorized payment transfer (POST /v2/transfers/{id}/void).
     *
     * @property transferId The unique identifier of the transfer to void.
     * @property idempotencyKey Value sent as the `Idempotency-Key` header.
     */
    data class VoidTransfer(
        val transferId: String,
        val idempotencyKey: String
    ) : TransferV2Endpoints()

    /**
     * Endpoint for refunding a succeeded payment transfer (POST /v2/transfers/{id}/refund).
     *
     * @property transferId The unique identifier of the transfer to refund.
     * @property idempotencyKey Value sent as the `Idempotency-Key` header.
     */
    data class RefundTransfer(
        val transferId: String,
        val idempotencyKey: String
    ) : TransferV2Endpoints()

    /** The resolved URL path for this endpoint. */
    override val endpointURL: String
        get() = when (this) {
            is CreateTransfer, is GetTransfers -> "/v2/transfers"
            is GetTransferWith -> "/v2/transfers/${this.transferId}"
            is UpdateTransfer -> "/v2/transfers/${this.transferId}"
            is ConfirmTransfer -> "/v2/transfers/${this.transferId}/confirm"
            is CaptureTransfer -> "/v2/transfers/${this.transferId}/capture"
            is VoidTransfer -> "/v2/transfers/${this.transferId}/void"
            is RefundTransfer -> "/v2/transfers/${this.transferId}/refund"
        }

    /** The HTTP method for this endpoint. */
    override val httpMethod: String
        get() = when (this) {
            is CreateTransfer, is ConfirmTransfer, is CaptureTransfer, is VoidTransfer, is RefundTransfer -> "POST"
            is UpdateTransfer -> "PATCH"
            is GetTransferWith, is GetTransfers -> "GET"
        }

    /** Query parameters appended to the request URL, populated for [GetTransfers] only. */
    override val queryItems: List<QueryItem>?
        get() = when (this) {
            is GetTransfers -> {
                val items = mutableListOf<QueryItem>()
                perPage?.let { items.add(QueryItem("per_page", it.toString())) }
                page?.let { items.add(QueryItem("page", it.toString())) }
                items
            }
            else -> null
        }

    /** Extra headers; mutating money-movement members send `Idempotency-Key`. */
    override val additionalHeaders: Map<String, String>
        get() = when (this) {
            is CreateTransfer -> mapOf("Idempotency-Key" to idempotencyKey)
            is CaptureTransfer -> mapOf("Idempotency-Key" to idempotencyKey)
            is VoidTransfer -> mapOf("Idempotency-Key" to idempotencyKey)
            is RefundTransfer -> mapOf("Idempotency-Key" to idempotencyKey)
            is ConfirmTransfer -> {
                val key = idempotencyKey
                if (key.isNullOrEmpty()) emptyMap() else mapOf("Idempotency-Key" to key)
            }
            else -> emptyMap()
        }
}
