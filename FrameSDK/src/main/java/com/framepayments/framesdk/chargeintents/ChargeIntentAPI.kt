package com.framepayments.framesdk.chargeintents

import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.managers.SiftActivityName
import com.framepayments.framesdk.managers.SiftManager

object ChargeIntentAPI {
    //MARK: Methods using coroutines
    suspend fun createChargeIntent(request: ChargeIntentsRequests.CreateChargeIntentRequest, forTesting: Boolean = false): Pair<ChargeIntent?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.CreateChargeIntent
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null && !forTesting) {
            SiftManager.addNewSiftEvent(SiftActivityName.sale)
        }
        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
    }

    suspend fun captureChargeIntent(intentId: String, request: ChargeIntentsRequests.CaptureChargeIntentRequest, forTesting: Boolean = false): Pair<ChargeIntent?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.CaptureChargeIntent(intentId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null && !forTesting) {
            SiftManager.addNewSiftEvent(SiftActivityName.capture)
        }
        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
    }

    suspend fun confirmChargeIntent(intentId: String, forTesting: Boolean = false): Pair<ChargeIntent?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.ConfirmChargeIntent(intentId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null))

        if (data != null && !forTesting) {
            SiftManager.addNewSiftEvent(SiftActivityName.authorize)
        }
        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
    }

    suspend fun cancelChargeIntent(intentId: String): Pair<ChargeIntent?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.CancelChargeIntent(intentId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null))
        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
    }

    suspend fun getAllChargeIntents(page: Int?, perPage: Int?): Pair<ChargeIntentResponses.ListChargeIntentsResponse?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.GetAllChargeIntents(page = page, perPage = perPage)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntentResponses.ListChargeIntentsResponse>(data) }, error)
    }

    suspend fun getChargeIntent(intentId: String): Pair<ChargeIntent?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.GetChargeIntent(intentId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
    }

    suspend fun updateChargeIntent(intentId: String, request: ChargeIntentsRequests.UpdateChargeIntentRequest): Pair<ChargeIntent?, NetworkingError?> {
        val endpoint = ChargeIntentEndpoints.UpdateChargeIntent(intentId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
    }

    //MARK: Methods using callbacks
    fun createChargeIntent(request: ChargeIntentsRequests.CreateChargeIntentRequest, completionHandler: (ChargeIntent?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.CreateChargeIntent

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            if (data != null) {
                SiftManager.addNewSiftEvent(SiftActivityName.sale)
            }
            completionHandler( data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
        }
    }

    fun captureChargeIntent(intentId: String, request: ChargeIntentsRequests.CaptureChargeIntentRequest, completionHandler: (ChargeIntent?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.CaptureChargeIntent(intentId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            if (data != null) {
                SiftManager.addNewSiftEvent(SiftActivityName.capture)
            }

            completionHandler( data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
        }
    }

    fun confirmChargeIntent(intentId: String, completionHandler: (ChargeIntent?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.ConfirmChargeIntent(intentId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null)) { data, error ->
            if (data != null) {
                SiftManager.addNewSiftEvent(SiftActivityName.authorize)
            }
            completionHandler( data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
        }
    }

    fun cancelChargeIntent(intentId: String, completionHandler: (ChargeIntent?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.CancelChargeIntent(intentId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null)) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
        }
    }

    fun getAllChargeIntents(page: Int?, perPage: Int?, completionHandler: (ChargeIntentResponses.ListChargeIntentsResponse?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.GetAllChargeIntents(page = page, perPage = perPage)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<ChargeIntentResponses.ListChargeIntentsResponse>(data) }, error)
        }
    }

    fun getChargeIntent(intentId: String, completionHandler: (ChargeIntent?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.GetChargeIntent(intentId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
        }
    }

    fun updateChargeIntent(intentId: String, request: ChargeIntentsRequests.UpdateChargeIntentRequest, completionHandler: (ChargeIntent?, NetworkingError?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.UpdateChargeIntent(intentId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<ChargeIntent>(data) }, error)
        }
    }
}