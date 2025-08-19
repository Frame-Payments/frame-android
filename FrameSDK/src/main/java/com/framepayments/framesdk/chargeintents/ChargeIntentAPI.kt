package com.framepayments.framesdk.chargeintents

import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.managers.SiftActivityName
import com.framepayments.framesdk.managers.SiftManager

object ChargeIntentAPI {
    //MARK: Methods using coroutines
    suspend fun createChargeIntent(request: ChargeIntentsRequests.CreateChargeIntentRequest): ChargeIntent? {
        val endpoint = ChargeIntentEndpoints.CreateChargeIntent
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            SiftManager.addNewSiftEvent(SiftActivityName.sale)
            return FrameNetworking.parseResponse<ChargeIntent>(data)
        }
        return null
    }

    suspend fun captureChargeIntent(intentId: String, request: ChargeIntentsRequests.CaptureChargeIntentRequest): ChargeIntent? {
        val endpoint = ChargeIntentEndpoints.CaptureChargeIntent(intentId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            SiftManager.addNewSiftEvent(SiftActivityName.capture)
            return FrameNetworking.parseResponse<ChargeIntent>(data)
        }
        return null
    }

    suspend fun confirmChargeIntent(intentId: String): ChargeIntent? {
        val endpoint = ChargeIntentEndpoints.ConfirmChargeIntent(intentId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null))

        if (data != null) {
            SiftManager.addNewSiftEvent(SiftActivityName.authorize)
            return FrameNetworking.parseResponse<ChargeIntent>(data)
        }
        return null
    }

    suspend fun cancelChargeIntent(intentId: String): ChargeIntent? {
        val endpoint = ChargeIntentEndpoints.CancelChargeIntent(intentId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null))

        if (data != null) {
            return FrameNetworking.parseResponse<ChargeIntent>(data)
        }
        return null
    }

    suspend fun getAllChargeIntents(page: Int?, perPage: Int?): List<ChargeIntent>? {
        val endpoint = ChargeIntentEndpoints.GetAllChargeIntents(page = page, perPage = perPage)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<ChargeIntentResponses.ListChargeIntentsResponse>(data)?.data
        }
        return null
    }

    suspend fun getChargeIntent(intentId: String): ChargeIntent? {
        val endpoint = ChargeIntentEndpoints.GetChargeIntent(intentId)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<ChargeIntent>(data)
        }
        return null
    }

    suspend fun updateChargeIntent(intentId: String, request: ChargeIntentsRequests.UpdateChargeIntentRequest): ChargeIntent? {
        val endpoint = ChargeIntentEndpoints.UpdateChargeIntent(intentId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            return FrameNetworking.parseResponse<ChargeIntent>(data)
        }
        return null
    }

    //MARK: Methods using callbacks
    fun createChargeIntent(request: ChargeIntentsRequests.CreateChargeIntentRequest, completionHandler: (ChargeIntent?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.CreateChargeIntent

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                SiftManager.addNewSiftEvent(SiftActivityName.sale)
                completionHandler(FrameNetworking.parseResponse<ChargeIntent>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun captureChargeIntent(intentId: String, request: ChargeIntentsRequests.CaptureChargeIntentRequest, completionHandler: (ChargeIntent?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.CaptureChargeIntent(intentId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                SiftManager.addNewSiftEvent(SiftActivityName.capture)
                completionHandler(FrameNetworking.parseResponse<ChargeIntent>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun confirmChargeIntent(intentId: String, completionHandler: (ChargeIntent?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.ConfirmChargeIntent(intentId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null)) { data, response, error ->
            if (data != null) {
                SiftManager.addNewSiftEvent(SiftActivityName.authorize)
                completionHandler(FrameNetworking.parseResponse<ChargeIntent>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun cancelChargeIntent(intentId: String, completionHandler: (ChargeIntent?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.CancelChargeIntent(intentId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null)) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<ChargeIntent>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun getAllChargeIntents(page: Int?, perPage: Int?, completionHandler: (List<ChargeIntent>?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.GetAllChargeIntents(page = page, perPage = perPage)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<ChargeIntentResponses.ListChargeIntentsResponse>(data)?.data)
            } else {
                completionHandler(null)
            }
        }
    }

    fun getChargeIntent(intentId: String, completionHandler: (ChargeIntent?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.GetChargeIntent(intentId)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<ChargeIntent>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun updateChargeIntent(intentId: String, request: ChargeIntentsRequests.UpdateChargeIntentRequest, completionHandler: (ChargeIntent?) -> Unit) {
        val endpoint = ChargeIntentEndpoints.UpdateChargeIntent(intentId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<ChargeIntent>(data))
            } else {
                completionHandler(null)
            }
        }
    }
}