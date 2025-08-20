package com.framepayments.framesdk.refunds
import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.chargeintents.ChargeIntent
import com.framepayments.framesdk.chargeintents.ChargeIntentEndpoints
import com.framepayments.framesdk.managers.SiftActivityName
import com.framepayments.framesdk.managers.SiftManager

object RefundsAPI {
    //MARK: Methods using coroutines
    suspend fun createRefund(request: RefundRequests.CreateRefundRequest, forTesting: Boolean = false): Refund? {
        val endpoint = RefundEndpoints.CreateRefund
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            if (!forTesting) {
                SiftManager.addNewSiftEvent(SiftActivityName.refund)
            }
            return FrameNetworking.parseResponse<Refund>(data)
        }
        return null
    }

    suspend fun getRefunds(chargeId: String?, chargeIntentId: String?, perPage: Int?, page : Int?): List<Refund>? {
        val endpoint = RefundEndpoints.GetRefunds(chargeId = chargeId, chargeIntentId = chargeIntentId, perPage = perPage, page = page)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<RefundResponses.ListRefundsResponse>(data)?.data
        }
        return null
    }

    suspend fun getRefundWith(refundId: String): Refund? {
        val endpoint = RefundEndpoints.GetRefundWith(refundId)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<Refund>(data)
        }
        return null
    }

    suspend fun cancelRefund(refundId: String): Refund? {
        val endpoint = RefundEndpoints.CancelRefund(refundId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null))

        if (data != null) {
            return FrameNetworking.parseResponse<Refund>(data)
        }
        return null
    }

    //MARK: Methods using callbacks
    fun createRefund(request: RefundRequests.CreateRefundRequest, completionHandler: (Refund?) -> Unit) {
        val endpoint = RefundEndpoints.CreateRefund

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                SiftManager.addNewSiftEvent(SiftActivityName.refund)
                completionHandler(FrameNetworking.parseResponse<Refund>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun getRefunds(chargeId: String?, chargeIntentId: String?, perPage: Int?, page : Int?, completionHandler: (List<Refund>?) -> Unit) {
        val endpoint = RefundEndpoints.GetRefunds(chargeId = chargeId, chargeIntentId = chargeIntentId, perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<RefundResponses.ListRefundsResponse>(data)?.data)
            } else {
                completionHandler(null)
            }
        }
    }

    fun getRefundWith(refundId: String, completionHandler: (Refund?) -> Unit) {
        val endpoint = RefundEndpoints.GetRefundWith(refundId)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<Refund>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun cancelRefund(refundId: String, completionHandler: (Refund?) -> Unit) {
        val endpoint = RefundEndpoints.CancelRefund(refundId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null)) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<Refund>(data))
            } else {
                completionHandler(null)
            }
        }
    }
}