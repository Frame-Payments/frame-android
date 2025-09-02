package com.framepayments.framesdk.refunds
import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.chargeintents.ChargeIntent
import com.framepayments.framesdk.chargeintents.ChargeIntentEndpoints
import com.framepayments.framesdk.managers.SiftActivityName
import com.framepayments.framesdk.managers.SiftManager

object RefundsAPI {
    //MARK: Methods using coroutines
    suspend fun createRefund(request: RefundRequests.CreateRefundRequest, forTesting: Boolean = false): Pair<Refund?, NetworkingError?> {
        val endpoint = RefundEndpoints.CreateRefund
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null && !forTesting) {
            SiftManager.addNewSiftEvent(SiftActivityName.refund)
        }
        return Pair(data?.let { FrameNetworking.parseResponse<Refund>(data) }, error)
    }

    suspend fun getRefunds(chargeId: String?, chargeIntentId: String?, perPage: Int?, page : Int?): Pair<RefundResponses.ListRefundsResponse?, NetworkingError?> {
        val endpoint = RefundEndpoints.GetRefunds(chargeId = chargeId, chargeIntentId = chargeIntentId, perPage = perPage, page = page)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<RefundResponses.ListRefundsResponse>(data) }, error)
    }

    suspend fun getRefundWith(refundId: String): Pair<Refund?, NetworkingError?> {
        val endpoint = RefundEndpoints.GetRefundWith(refundId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<Refund>(data) }, error)
    }

    suspend fun cancelRefund(refundId: String): Pair<Refund?, NetworkingError?> {
        val endpoint = RefundEndpoints.CancelRefund(refundId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null))
        return Pair(data?.let { FrameNetworking.parseResponse<Refund>(data) }, error)
    }

    //MARK: Methods using callbacks
    fun createRefund(request: RefundRequests.CreateRefundRequest, completionHandler: (Refund?, NetworkingError?) -> Unit) {
        val endpoint = RefundEndpoints.CreateRefund

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            if (data != null) {
                SiftManager.addNewSiftEvent(SiftActivityName.refund)
            }
            completionHandler( data?.let { FrameNetworking.parseResponse<Refund>(data) }, error)
        }
    }

    fun getRefunds(chargeId: String?, chargeIntentId: String?, perPage: Int?, page : Int?, completionHandler: (RefundResponses.ListRefundsResponse?, NetworkingError?) -> Unit) {
        val endpoint = RefundEndpoints.GetRefunds(chargeId = chargeId, chargeIntentId = chargeIntentId, perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<RefundResponses.ListRefundsResponse>(data) }, error)
        }
    }

    fun getRefundWith(refundId: String, completionHandler: (Refund?, NetworkingError?) -> Unit) {
        val endpoint = RefundEndpoints.GetRefundWith(refundId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<Refund>(data) }, error)
        }
    }

    fun cancelRefund(refundId: String, completionHandler: (Refund?, NetworkingError?) -> Unit) {
        val endpoint = RefundEndpoints.CancelRefund(refundId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null)) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<Refund>(data) }, error)
        }
    }
}