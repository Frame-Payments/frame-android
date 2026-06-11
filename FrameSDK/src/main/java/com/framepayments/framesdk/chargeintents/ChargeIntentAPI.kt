package com.framepayments.framesdk.chargeintents

import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.managers.SiftManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Provides coroutine-based and callback-based methods for managing charge intents via the Frame API.
 *
 * Each operation is available in two flavors: a `suspend` function for use with coroutines and an
 * overload that accepts a completion handler for callback-based callers.
 */
object ChargeIntentAPI {
    //MARK: Methods using coroutines

    /**
     * Creates a new charge intent, attaching the current Sonar session ID and live fraud signals before sending.
     *
     * @param request The parameters for the charge intent to create.
     * @return A [Pair] containing the created [ChargeIntent] on success, or a [NetworkingError] on failure.
     */
    suspend fun createChargeIntent(request: ChargeIntentsRequests.CreateChargeIntentRequest): Pair<ChargeIntent?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.CreateChargeIntent
        request.sonarSessionId = FrameNetworking.currentSonarSessionId()
        request.fraudSignals = ChargeIntentsRequests.FraudSignals(
            clientIp = withContext(Dispatchers.IO) { SiftManager.getPublicIp() }
        )
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
    }

    /**
     * Captures a previously authorized charge intent for the specified amount.
     *
     * @param intentId The ID of the charge intent to capture.
     * @param request The capture parameters, including the amount in cents to capture.
     * @return A [Pair] containing the updated [ChargeIntent] on success, or a [NetworkingError] on failure.
     */
    suspend fun captureChargeIntent(intentId: String, request: ChargeIntentsRequests.CaptureChargeIntentRequest): Pair<ChargeIntent?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.CaptureChargeIntent(intentId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
    }

    /**
     * Confirms a charge intent, triggering payment processing.
     *
     * @param intentId The ID of the charge intent to confirm.
     * @return A [Pair] containing the updated [ChargeIntent] on success, or a [NetworkingError] on failure.
     */
    suspend fun confirmChargeIntent(intentId: String): Pair<ChargeIntent?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.ConfirmChargeIntent(intentId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null))

        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
    }

    /**
     * Cancels a charge intent, preventing it from being confirmed or captured.
     *
     * @param intentId The ID of the charge intent to cancel.
     * @return A [Pair] containing the updated [ChargeIntent] on success, or a [NetworkingError] on failure.
     */
    suspend fun cancelChargeIntent(intentId: String): Pair<ChargeIntent?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.CancelChargeIntent(intentId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null))
        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
    }

    /**
     * Retrieves a paginated list of all charge intents for the merchant.
     *
     * @param page The 1-based page number to retrieve, or `null` to use the API default.
     * @param perPage The number of results per page, or `null` to use the API default.
     * @return A [Pair] containing a [ChargeIntentResponses.ListChargeIntentsResponse] on success, or a [NetworkingError] on failure.
     */
    suspend fun getAllChargeIntents(page: Int?, perPage: Int?): Pair<ChargeIntentResponses.ListChargeIntentsResponse?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.GetAllChargeIntents(page = page, perPage = perPage)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntentResponses.ListChargeIntentsResponse>(data) }, error)
    }

    /**
     * Retrieves a single charge intent by its ID.
     *
     * @param intentId The ID of the charge intent to retrieve.
     * @return A [Pair] containing the [ChargeIntent] on success, or a [NetworkingError] on failure.
     */
    suspend fun getChargeIntent(intentId: String): Pair<ChargeIntent?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.GetChargeIntent(intentId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
    }

    /**
     * Updates mutable fields on an existing charge intent.
     *
     * @param intentId The ID of the charge intent to update.
     * @param request The fields to update on the charge intent.
     * @return A [Pair] containing the updated [ChargeIntent] on success, or a [NetworkingError] on failure.
     */
    suspend fun updateChargeIntent(intentId: String, request: ChargeIntentsRequests.UpdateChargeIntentRequest): Pair<ChargeIntent?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.UpdateChargeIntent(intentId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
    }

    /**
     * Voids the uncaptured remainder of a partially captured charge intent.
     *
     * @param intentId The ID of the charge intent whose remaining authorized amount should be voided.
     * @return A [Pair] containing the updated [ChargeIntent] on success, or a [NetworkingError] on failure.
     */
    suspend fun voidRemainingChargeIntent(intentId: String): Pair<ChargeIntent?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.VoidRemainingChargeIntent(intentId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null))
        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
    }

    //MARK: Methods using callbacks

    /**
     * Creates a new charge intent, attaching the current Sonar session ID and live fraud signals before sending.
     *
     * Executes the network request on a background thread and delivers the result to [completionHandler].
     *
     * @param request The parameters for the charge intent to create.
     * @param completionHandler Called with the created [ChargeIntent] on success, or a [NetworkingError] on failure.
     */
    fun createChargeIntent(request: ChargeIntentsRequests.CreateChargeIntentRequest, completionHandler: (ChargeIntent?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.CreateChargeIntent
        request.sonarSessionId = FrameNetworking.currentSonarSessionId()
        FrameNetworking.okHttpClient.dispatcher.executorService.execute {
            request.fraudSignals = ChargeIntentsRequests.FraudSignals(clientIp = SiftManager.getPublicIp())
            FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
                completionHandler(data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
            }
        }
    }

    /**
     * Captures a previously authorized charge intent for the specified amount.
     *
     * @param intentId The ID of the charge intent to capture.
     * @param request The capture parameters, including the amount in cents to capture.
     * @param completionHandler Called with the updated [ChargeIntent] on success, or a [NetworkingError] on failure.
     */
    fun captureChargeIntent(intentId: String, request: ChargeIntentsRequests.CaptureChargeIntentRequest, completionHandler: (ChargeIntent?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.CaptureChargeIntent(intentId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
        }
    }

    /**
     * Confirms a charge intent, triggering payment processing.
     *
     * @param intentId The ID of the charge intent to confirm.
     * @param completionHandler Called with the updated [ChargeIntent] on success, or a [NetworkingError] on failure.
     */
    fun confirmChargeIntent(intentId: String, completionHandler: (ChargeIntent?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.ConfirmChargeIntent(intentId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null)) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
        }
    }

    /**
     * Cancels a charge intent, preventing it from being confirmed or captured.
     *
     * @param intentId The ID of the charge intent to cancel.
     * @param completionHandler Called with the updated [ChargeIntent] on success, or a [NetworkingError] on failure.
     */
    fun cancelChargeIntent(intentId: String, completionHandler: (ChargeIntent?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.CancelChargeIntent(intentId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null)) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
        }
    }

    /**
     * Retrieves a paginated list of all charge intents for the merchant.
     *
     * @param page The 1-based page number to retrieve, or `null` to use the API default.
     * @param perPage The number of results per page, or `null` to use the API default.
     * @param completionHandler Called with a [ChargeIntentResponses.ListChargeIntentsResponse] on success, or a [NetworkingError] on failure.
     */
    fun getAllChargeIntents(page: Int?, perPage: Int?, completionHandler: (ChargeIntentResponses.ListChargeIntentsResponse?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.GetAllChargeIntents(page = page, perPage = perPage)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<ChargeIntentResponses.ListChargeIntentsResponse>(data) }, error)
        }
    }

    /**
     * Retrieves a single charge intent by its ID.
     *
     * @param intentId The ID of the charge intent to retrieve.
     * @param completionHandler Called with the [ChargeIntent] on success, or a [NetworkingError] on failure.
     */
    fun getChargeIntent(intentId: String, completionHandler: (ChargeIntent?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.GetChargeIntent(intentId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
        }
    }

    /**
     * Updates mutable fields on an existing charge intent.
     *
     * @param intentId The ID of the charge intent to update.
     * @param request The fields to update on the charge intent.
     * @param completionHandler Called with the updated [ChargeIntent] on success, or a [NetworkingError] on failure.
     */
    fun updateChargeIntent(intentId: String, request: ChargeIntentsRequests.UpdateChargeIntentRequest, completionHandler: (ChargeIntent?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.UpdateChargeIntent(intentId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
        }
    }

    /**
     * Voids the uncaptured remainder of a partially captured charge intent.
     *
     * @param intentId The ID of the charge intent whose remaining authorized amount should be voided.
     * @param completionHandler Called with the updated [ChargeIntent] on success, or a [NetworkingError] on failure.
     */
    fun voidRemainingChargeIntent(intentId: String, completionHandler: (ChargeIntent?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.VoidRemainingChargeIntent(intentId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null)) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
        }
    }
}
