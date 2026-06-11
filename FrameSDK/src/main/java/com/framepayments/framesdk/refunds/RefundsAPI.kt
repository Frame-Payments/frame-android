package com.framepayments.framesdk.refunds
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Provides methods for creating and retrieving refunds against existing charges.
 *
 * Each operation is available as a suspend function for coroutine-based callers
 * and as a callback-based overload for Java or non-coroutine callers.
 */
object RefundsAPI {
    //MARK: Methods using coroutines

    /**
     * Creates a new refund for the specified charge intent.
     *
     * @param request The refund creation parameters, including the required charge intent ID.
     * @return A pair containing the created [Refund] on success, or a [NetworkingError] on failure.
     */
    suspend fun createRefund(request: RefundRequests.CreateRefundRequest): Pair<Refund?, NetworkingError?> {
        val endpoint = RefundEndpoints.CreateRefund
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<Refund>(data) }, error)
    }

    /**
     * Retrieves a paginated list of refunds, optionally filtered by charge or charge intent.
     *
     * @param chargeId Optional ID of the charge to filter refunds by.
     * @param chargeIntentId Optional ID of the charge intent to filter refunds by.
     * @param perPage Optional number of results to return per page.
     * @param page Optional page number to retrieve.
     * @return A pair containing a [RefundResponses.ListRefundsResponse] on success, or a [NetworkingError] on failure.
     */
    suspend fun getRefunds(chargeId: String?, chargeIntentId: String?, perPage: Int?, page : Int?): Pair<RefundResponses.ListRefundsResponse?, NetworkingError?> {
        val endpoint = RefundEndpoints.GetRefunds(chargeId = chargeId, chargeIntentId = chargeIntentId, perPage = perPage, page = page)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<RefundResponses.ListRefundsResponse>(data) }, error)
    }

    /**
     * Retrieves a single refund by its unique identifier.
     *
     * @param refundId The unique ID of the refund to retrieve.
     * @return A pair containing the matching [Refund] on success, or a [NetworkingError] on failure.
     */
    suspend fun getRefundWith(refundId: String): Pair<Refund?, NetworkingError?> {
        val endpoint = RefundEndpoints.GetRefundWith(refundId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<Refund>(data) }, error)
    }

    //MARK: Methods using callbacks

    /**
     * Creates a new refund for the specified charge intent and delivers the result via callback.
     *
     * @param request The refund creation parameters, including the required charge intent ID.
     * @param completionHandler Invoked with the created [Refund] on success, or a [NetworkingError] on failure.
     */
    fun createRefund(request: RefundRequests.CreateRefundRequest, completionHandler: (Refund?, NetworkingError?) -> Unit) {
        val endpoint = RefundEndpoints.CreateRefund

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<Refund>(data) }, error)
        }
    }

    /**
     * Retrieves a paginated list of refunds and delivers the result via callback.
     *
     * @param chargeId Optional ID of the charge to filter refunds by.
     * @param chargeIntentId Optional ID of the charge intent to filter refunds by.
     * @param perPage Optional number of results to return per page.
     * @param page Optional page number to retrieve.
     * @param completionHandler Invoked with a [RefundResponses.ListRefundsResponse] on success, or a [NetworkingError] on failure.
     */
    fun getRefunds(chargeId: String?, chargeIntentId: String?, perPage: Int?, page : Int?, completionHandler: (RefundResponses.ListRefundsResponse?, NetworkingError?) -> Unit) {
        val endpoint = RefundEndpoints.GetRefunds(chargeId = chargeId, chargeIntentId = chargeIntentId, perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<RefundResponses.ListRefundsResponse>(data) }, error)
        }
    }

    /**
     * Retrieves a single refund by its unique identifier and delivers the result via callback.
     *
     * @param refundId The unique ID of the refund to retrieve.
     * @param completionHandler Invoked with the matching [Refund] on success, or a [NetworkingError] on failure.
     */
    fun getRefundWith(refundId: String, completionHandler: (Refund?, NetworkingError?) -> Unit) {
        val endpoint = RefundEndpoints.GetRefundWith(refundId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<Refund>(data) }, error)
        }
    }

}
