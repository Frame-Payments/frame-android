package com.framepayments.framesdk.transfersv2

import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError
import kotlinx.coroutines.CancellationException
import java.util.UUID

/**
 * Provides suspend and callback-based functions for managing V2 transfers (`/v2/transfers`).
 *
 * Secret-key calls use [com.framepayments.framesdk.FrameAuthMode.Secret]. Checkout creates and
 * confirms with `Authorization: Bearer chk_sess_…` when a checkout client secret is passed.
 */
object TransfersV2API {

    /**
     * Attaches the account's Sonar session to a payment-source create, which the server rejects
     * without a live one. Establishes the session rather than reading the cache, since a stored
     * but stale session no longer backs a payment.
     */
    private suspend fun withSonarSession(
        request: TransferV2Requests.CreateTransferRequest
    ): TransferV2Requests.CreateTransferRequest {
        val accountId = request.source?.paymentMethod?.accountId
            ?: request.source?.accountId
        val hasPaymentSource = request.source?.paymentMethodId != null
            || request.source?.paymentMethod != null
        if (!hasPaymentSource || accountId.isNullOrEmpty()) return request

        val manager = FrameNetworking.sonarSessionManagerOrNull() ?: return request
        // A failure must not block the transfer; the server's rejection is the authoritative answer.
        val sessionId = try {
            manager.ensureSession(accountId)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
        return if (sessionId != null) request.copy(sonarSessionId = sessionId) else request
    }

    private suspend fun postOrPatch(
        endpoint: TransferV2Endpoints,
        request: Any?
    ): Pair<ByteArray?, NetworkingError?> {
        // OkHttp requires a body on POST/PATCH; send `{}` when the caller omits a payload.
        return FrameNetworking.performDataTaskWithRequest(endpoint, request ?: emptyMap<String, Any>())
    }

    //MARK: Methods using coroutines

    /**
     * Creates a V2 transfer. Requires an `Idempotency-Key` (generated when [idempotencyKey] is null or blank).
     *
     * @param request The request payload describing the transfer to create.
     * @param idempotencyKey Optional idempotency key; a UUID is generated when null or blank.
     * @return A [Pair] of ([TransferV2]?, [NetworkingError]?).
     */
    suspend fun createTransfer(
        request: TransferV2Requests.CreateTransferRequest,
        idempotencyKey: String? = null
    ): Pair<TransferV2?, NetworkingError?> =
        createTransfer(request, idempotencyKey, FrameAuthMode.Secret)

    /**
     * Creates a V2 transfer under a checkout session.
     *
     * The session must have been minted with a locked amount. The server stamps that amount,
     * authorization mode, and checkout session id; the client still sends `Idempotency-Key`.
     *
     * @param checkoutClientSecret The `chk_sess_…` token. An empty token returns `(null, null)`.
     */
    suspend fun createTransfer(
        request: TransferV2Requests.CreateTransferRequest,
        checkoutClientSecret: String,
        idempotencyKey: String? = null,
    ): Pair<TransferV2?, NetworkingError?> {
        if (checkoutClientSecret.isEmpty()) return Pair(null, null)
        return createTransfer(request, idempotencyKey, FrameAuthMode.ClientSecret(checkoutClientSecret))
    }

    private suspend fun createTransfer(
        request: TransferV2Requests.CreateTransferRequest,
        idempotencyKey: String?,
        auth: FrameAuthMode,
    ): Pair<TransferV2?, NetworkingError?> {
        val key = if (!idempotencyKey.isNullOrEmpty()) idempotencyKey else UUID.randomUUID().toString()
        val endpoint = TransferV2Endpoints.CreateTransfer(key)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(
            endpoint,
            withSonarSession(request),
            auth
        )
        return Pair(data?.let { FrameNetworking.parseResponse<TransferV2>(data) }, error)
    }

    /**
     * Retrieves an existing V2 transfer by its identifier.
     *
     * @param transferId The unique identifier of the transfer to retrieve.
     * @return A [Pair] of ([TransferV2]?, [NetworkingError]?).
     */
    suspend fun getTransferWith(transferId: String): Pair<TransferV2?, NetworkingError?> {
        if (transferId.isEmpty()) return Pair(null, null)
        val endpoint = TransferV2Endpoints.GetTransferWith(transferId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<TransferV2>(data) }, error)
    }

    /**
     * Retrieves a paginated list of V2 transfers.
     *
     * @param perPage The number of transfers to return per page, or null to use the API default.
     * @param page The page number to retrieve, or null to retrieve the first page.
     * @return A [Pair] of ([TransferV2Responses.ListTransfersResponse]?, [NetworkingError]?).
     */
    suspend fun getTransfers(
        perPage: Int? = null,
        page: Int? = null
    ): Pair<TransferV2Responses.ListTransfersResponse?, NetworkingError?> {
        val endpoint = TransferV2Endpoints.GetTransfers(perPage = perPage, page = page)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(
            data?.let { FrameNetworking.parseResponse<TransferV2Responses.ListTransfersResponse>(data) },
            error
        )
    }

    /**
     * Updates a deferred-confirm payment transfer before confirm.
     *
     * @param transferId The unique identifier of the transfer to update.
     * @param request The update payload.
     * @return A [Pair] of ([TransferV2]?, [NetworkingError]?).
     */
    suspend fun updateTransfer(
        transferId: String,
        request: UpdateTransferRequest
    ): Pair<TransferV2?, NetworkingError?> {
        if (transferId.isEmpty()) return Pair(null, null)
        val endpoint = TransferV2Endpoints.UpdateTransfer(transferId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<TransferV2>(data) }, error)
    }

    /**
     * Confirms a deferred-confirm payment transfer (secret key).
     *
     * Sends an `Idempotency-Key` (generated when [idempotencyKey] is null/blank).
     *
     * @param transferId The unique identifier of the transfer to confirm.
     * @param request Optional confirm payload (source / payment method options).
     * @param idempotencyKey Optional idempotency key; a UUID is generated when null.
     * @return A [Pair] of ([TransferV2]?, [NetworkingError]?).
     */
    suspend fun confirmTransfer(
        transferId: String,
        request: TransferV2Requests.CreateTransferRequest? = null,
        idempotencyKey: String? = null
    ): Pair<TransferV2?, NetworkingError?> {
        if (transferId.isEmpty()) return Pair(null, null)
        val key = if (!idempotencyKey.isNullOrEmpty()) idempotencyKey else UUID.randomUUID().toString()
        val endpoint = TransferV2Endpoints.ConfirmTransfer(transferId, key)
        val (data, error) = postOrPatch(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<TransferV2>(data) }, error)
    }

    /**
     * Confirms a V2 transfer with a checkout-session bearer.
     *
     * Confirm does not send `Idempotency-Key`. An empty id or token returns `(null, null)`.
     */
    suspend fun confirmTransfer(
        transferId: String,
        checkoutClientSecret: String
    ): Pair<TransferV2?, NetworkingError?> {
        if (transferId.isEmpty() || checkoutClientSecret.isEmpty()) return Pair(null, null)
        val endpoint = TransferV2Endpoints.ConfirmTransfer(transferId, idempotencyKey = null)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(
            endpoint,
            emptyMap<String, Any>(),
            FrameAuthMode.ClientSecret(checkoutClientSecret)
        )
        return Pair(data?.let { FrameNetworking.parseResponse<TransferV2>(data) }, error)
    }

    /**
     * Retrieves a V2 transfer with a checkout-session bearer.
     *
     * The transfer must belong to the session. An empty id or token returns `(null, null)`.
     */
    suspend fun getTransferWith(
        transferId: String,
        checkoutClientSecret: String
    ): Pair<TransferV2?, NetworkingError?> {
        if (transferId.isEmpty() || checkoutClientSecret.isEmpty()) return Pair(null, null)
        val endpoint = TransferV2Endpoints.GetTransferWith(transferId)
        val (data, error) = FrameNetworking.performDataTask(
            endpoint,
            FrameAuthMode.ClientSecret(checkoutClientSecret)
        )
        return Pair(data?.let { FrameNetworking.parseResponse<TransferV2>(data) }, error)
    }

    /**
     * Captures a manually authorized payment transfer (optional partial amount).
     *
     * @param transferId The unique identifier of the transfer to capture.
     * @param request Optional amount for a partial capture.
     * @param idempotencyKey Optional idempotency key; a UUID is generated when null.
     * @return A [Pair] of ([TransferV2]?, [NetworkingError]?).
     */
    suspend fun captureTransfer(
        transferId: String,
        request: TransferV2Requests.AmountOnlyRequest? = null,
        idempotencyKey: String? = null
    ): Pair<TransferV2?, NetworkingError?> {
        if (transferId.isEmpty()) return Pair(null, null)
        val key = if (!idempotencyKey.isNullOrEmpty()) idempotencyKey else UUID.randomUUID().toString()
        val endpoint = TransferV2Endpoints.CaptureTransfer(transferId, key)
        val (data, error) = postOrPatch(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<TransferV2>(data) }, error)
    }

    /**
     * Voids a manually authorized payment transfer.
     *
     * @param transferId The unique identifier of the transfer to void.
     * @param idempotencyKey Optional idempotency key; a UUID is generated when null.
     * @return A [Pair] of ([TransferV2]?, [NetworkingError]?).
     */
    suspend fun voidTransfer(
        transferId: String,
        idempotencyKey: String? = null
    ): Pair<TransferV2?, NetworkingError?> {
        if (transferId.isEmpty()) return Pair(null, null)
        val key = if (!idempotencyKey.isNullOrEmpty()) idempotencyKey else UUID.randomUUID().toString()
        val endpoint = TransferV2Endpoints.VoidTransfer(transferId, key)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, emptyMap<String, Any>())
        return Pair(data?.let { FrameNetworking.parseResponse<TransferV2>(data) }, error)
    }

    /**
     * Refunds a succeeded payment transfer (optional partial amount).
     *
     * @param transferId The unique identifier of the transfer to refund.
     * @param request Optional amount for a partial refund.
     * @param idempotencyKey Optional idempotency key; a UUID is generated when null.
     * @return A [Pair] of ([TransferV2]?, [NetworkingError]?).
     */
    suspend fun refundTransfer(
        transferId: String,
        request: TransferV2Requests.AmountOnlyRequest? = null,
        idempotencyKey: String? = null
    ): Pair<TransferV2?, NetworkingError?> {
        if (transferId.isEmpty()) return Pair(null, null)
        val key = if (!idempotencyKey.isNullOrEmpty()) idempotencyKey else UUID.randomUUID().toString()
        val endpoint = TransferV2Endpoints.RefundTransfer(transferId, key)
        val (data, error) = postOrPatch(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<TransferV2>(data) }, error)
    }

    //MARK: Methods using callbacks

    /**
     * Creates a V2 transfer and delivers the result via a callback.
     *
     * @param request The request payload describing the transfer to create.
     * @param idempotencyKey Optional idempotency key; a UUID is generated when null or blank.
     * @param completionHandler Callback invoked with ([TransferV2]?, [NetworkingError]?).
     */
    fun createTransfer(
        request: TransferV2Requests.CreateTransferRequest,
        idempotencyKey: String? = null,
        completionHandler: (TransferV2?, NetworkingError?) -> Unit
    ) {
        val key = if (!idempotencyKey.isNullOrEmpty()) idempotencyKey else UUID.randomUUID().toString()
        val endpoint = TransferV2Endpoints.CreateTransfer(key)
        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<TransferV2>(data) }, error)
        }
    }

    /**
     * Retrieves an existing V2 transfer by its identifier and delivers the result via a callback.
     *
     * @param transferId The unique identifier of the transfer to retrieve.
     * @param completionHandler Callback invoked with ([TransferV2]?, [NetworkingError]?).
     */
    fun getTransferWith(transferId: String, completionHandler: (TransferV2?, NetworkingError?) -> Unit) {
        if (transferId.isEmpty()) {
            completionHandler(null, null)
            return
        }
        val endpoint = TransferV2Endpoints.GetTransferWith(transferId)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<TransferV2>(data) }, error)
        }
    }

    /**
     * Retrieves a paginated list of V2 transfers and delivers the result via a callback.
     *
     * @param perPage The number of transfers to return per page, or null to use the API default.
     * @param page The page number to retrieve, or null to retrieve the first page.
     * @param completionHandler Callback invoked with ([TransferV2Responses.ListTransfersResponse]?, [NetworkingError]?).
     */
    fun getTransfers(
        perPage: Int?,
        page: Int?,
        completionHandler: (TransferV2Responses.ListTransfersResponse?, NetworkingError?) -> Unit
    ) {
        val endpoint = TransferV2Endpoints.GetTransfers(perPage = perPage, page = page)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(
                data?.let { FrameNetworking.parseResponse<TransferV2Responses.ListTransfersResponse>(data) },
                error
            )
        }
    }

    /**
     * Confirms a deferred-confirm payment transfer and delivers the result via a callback.
     *
     * @param transferId The unique identifier of the transfer to confirm.
     * @param request Optional confirm payload.
     * @param completionHandler Callback invoked with ([TransferV2]?, [NetworkingError]?).
     */
    fun confirmTransfer(
        transferId: String,
        request: TransferV2Requests.CreateTransferRequest? = null,
        completionHandler: (TransferV2?, NetworkingError?) -> Unit
    ) {
        if (transferId.isEmpty()) {
            completionHandler(null, null)
            return
        }
        val key = UUID.randomUUID().toString()
        val endpoint = TransferV2Endpoints.ConfirmTransfer(transferId, key)
        FrameNetworking.performDataTaskWithRequest(
            endpoint,
            request ?: emptyMap<String, Any>()
        ) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<TransferV2>(data) }, error)
        }
    }

    /**
     * Confirms a V2 transfer with a checkout-session bearer.
     *
     * Confirm does not send `Idempotency-Key`.
     */
    fun confirmTransfer(
        transferId: String,
        checkoutClientSecret: String,
        completionHandler: (TransferV2?, NetworkingError?) -> Unit
    ) {
        if (transferId.isEmpty() || checkoutClientSecret.isEmpty()) {
            completionHandler(null, null)
            return
        }
        val endpoint = TransferV2Endpoints.ConfirmTransfer(transferId, idempotencyKey = null)
        FrameNetworking.performDataTaskWithRequest(
            endpoint,
            emptyMap<String, Any>(),
            FrameAuthMode.ClientSecret(checkoutClientSecret)
        ) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<TransferV2>(data) }, error)
        }
    }

    /**
     * Retrieves a V2 transfer with a checkout-session bearer.
     */
    fun getTransferWith(
        transferId: String,
        checkoutClientSecret: String,
        completionHandler: (TransferV2?, NetworkingError?) -> Unit
    ) {
        if (transferId.isEmpty() || checkoutClientSecret.isEmpty()) {
            completionHandler(null, null)
            return
        }
        val endpoint = TransferV2Endpoints.GetTransferWith(transferId)
        FrameNetworking.performDataTask(
            endpoint,
            FrameAuthMode.ClientSecret(checkoutClientSecret)
        ) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<TransferV2>(data) }, error)
        }
    }
}
