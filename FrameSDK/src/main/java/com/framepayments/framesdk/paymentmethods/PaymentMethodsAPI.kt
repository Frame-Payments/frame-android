package com.framepayments.framesdk.paymentmethods

import com.evervault.sdk.Evervault
import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.managers.SiftManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PaymentMethodsAPI {
    //MARK: Methods using coroutines
    suspend fun getPaymentMethods(page: Int? = null, perPage: Int? = null): Pair<PaymentMethodResponses.ListPaymentMethodsResponse?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethods(perPage = perPage, page = page)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<PaymentMethodResponses.ListPaymentMethodsResponse>(data) }, error)
    }

    suspend fun getPaymentMethodWith(paymentMethodId: String): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    suspend fun getPaymentMethodsWithCustomer(customerId: String, forTesting: Boolean = false): Pair<List<FrameObjects.PaymentMethod>?, NetworkingError?> {
        if (!forTesting) {
            SiftManager.collectUserLogin(customerId, "")
        }

        val endpoint = PaymentMethodEndpoints.GetPaymentMethodsWithCustomer(customerId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<PaymentMethodResponses.ListPaymentMethodsResponse>(data)?.data }, error)
    }

    suspend fun createCardPaymentMethod(request: PaymentMethodRequests.CreateCardPaymentMethodRequest, encryptData: Boolean = true): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        if (!FrameNetworking.isEvervaultConfigured && encryptData) {
            FrameNetworking.configureEvervault()
        }
        val endpoint = PaymentMethodEndpoints.CreatePaymentMethod

        val encryptedRequest = request
        if (encryptData) {
            encryptedRequest.cardNumber = Evervault.shared.encrypt(request.cardNumber) as String
            encryptedRequest.cvc = Evervault.shared.encrypt(request.cvc) as String
        }
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, encryptedRequest)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    suspend fun createACHPaymentMethod(request: PaymentMethodRequests.CreateACHPaymentMethodRequest): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.CreatePaymentMethod
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    suspend fun updatePaymentMethodWith(paymentMethodId: String, request: PaymentMethodRequests.UpdatePaymentMethodRequest): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.UpdatePaymentMethodWith(paymentMethodId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    suspend fun attachPaymentMethodWith(paymentMethodId: String, request: PaymentMethodRequests.AttachPaymentMethodRequest):Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.AttachPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    suspend fun detachPaymentMethodWith(paymentMethodId: String): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.DetachPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null))
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    suspend fun blockPaymentMethodWith(paymentMethodId: String): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.BlockPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null))
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    suspend fun unblockPaymentMethodWith(paymentMethodId: String): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.UnblockPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null))
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    //MARK: Methods using callbacks
    fun getPaymentMethods(page: Int? = null, perPage: Int? = null, completionHandler: (PaymentMethodResponses.ListPaymentMethodsResponse?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethods(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<PaymentMethodResponses.ListPaymentMethodsResponse>(data) }, error )
        }
    }

    fun getPaymentMethodWith(paymentMethodId: String, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethodWith(paymentMethodId = paymentMethodId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }

    fun getPaymentMethodsWithCustomer(customerId: String, completionHandler: (List<FrameObjects.PaymentMethod>?, NetworkingError?) -> Unit) {
        SiftManager.collectUserLogin(customerId, "")
        val endpoint = PaymentMethodEndpoints.GetPaymentMethodsWithCustomer(customerId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<PaymentMethodResponses.ListPaymentMethodsResponse>(data)?.data }, error )
        }
    }

    fun createCardPaymentMethod(request: PaymentMethodRequests.CreateCardPaymentMethodRequest, encryptData: Boolean = true, scope: CoroutineScope, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        if (!FrameNetworking.isEvervaultConfigured) {
            FrameNetworking.configureEvervault()
        }
        val endpoint = PaymentMethodEndpoints.CreatePaymentMethod

        scope.launch(Dispatchers.IO) {
            val encryptedRequest = if (encryptData) request.copy(
                cardNumber = Evervault.shared.encrypt(request.cardNumber) as String,
                cvc = Evervault.shared.encrypt(request.cvc) as String
            ) else request

            FrameNetworking.performDataTaskWithRequest(endpoint, encryptedRequest) { data, error ->
                scope.launch(Dispatchers.Main) {
                    completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
                }
            }
        }
    }

    fun createACHPaymentMethod(request: PaymentMethodRequests.CreateACHPaymentMethodRequest, scope: CoroutineScope, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.CreatePaymentMethod

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }

    fun updatePaymentMethodWith(paymentMethodId: String, request: PaymentMethodRequests.UpdatePaymentMethodRequest, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.UpdatePaymentMethodWith(paymentMethodId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }

    fun attachPaymentMethodWith(paymentMethodId: String, request: PaymentMethodRequests.AttachPaymentMethodRequest, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.AttachPaymentMethodWith(paymentMethodId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }

    fun detachPaymentMethodWith(paymentMethodId: String, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.DetachPaymentMethodWith(paymentMethodId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null)) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }

    fun blockPaymentMethodWith(paymentMethodId: String, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.BlockPaymentMethodWith(paymentMethodId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null)) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }

    fun unblockPaymentMethodWith(paymentMethodId: String, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.UnblockPaymentMethodWith(paymentMethodId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null)) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }
}