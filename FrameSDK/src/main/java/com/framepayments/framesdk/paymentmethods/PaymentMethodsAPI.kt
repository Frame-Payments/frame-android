package com.framepayments.framesdk.paymentmethods
import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects

object PaymentMethodsAPI {
    //MARK: Methods using coroutines
    suspend fun getPaymentMethods(page: Int? = null, perPage: Int? = null): List<FrameObjects.PaymentMethod>? {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethods(perPage = perPage, page = page)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<PaymentMethodResponses.ListPaymentMethodsResponse>(data)?.data
        }
        return null
    }

    suspend fun getPaymentMethodWith(paymentMethodId: String): FrameObjects.PaymentMethod? {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data)
        }
        return null
    }

    suspend fun getPaymentMethodsWithCustomer(customerId: String): List<FrameObjects.PaymentMethod>? {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethodsWithCustomer(customerId)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<PaymentMethodResponses.ListPaymentMethodsResponse>(data)?.data
        }
        return null
    }

    suspend fun createPaymentMethod(request: PaymentMethodRequests.CreatePaymentMethodRequest, encryptData: Boolean = true): FrameObjects.PaymentMethod? {
        val endpoint = PaymentMethodEndpoints.CreatePaymentMethod
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            return FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data)
        }
        return null
    }

    suspend fun updatePaymentMethodWith(paymentMethodId: String, request: PaymentMethodRequests.UpdatePaymentMethodRequest): FrameObjects.PaymentMethod? {
        val endpoint = PaymentMethodEndpoints.UpdatePaymentMethodWith(paymentMethodId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            return FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data)
        }
        return null
    }

    suspend fun attachPaymentMethodWith(paymentMethodId: String, request: PaymentMethodRequests.AttachPaymentMethodRequest): FrameObjects.PaymentMethod? {
        val endpoint = PaymentMethodEndpoints.AttachPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            return FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data)
        }
        return null
    }

    suspend fun detachPaymentMethodWith(paymentMethodId: String): FrameObjects.PaymentMethod? {
        val endpoint = PaymentMethodEndpoints.DetachPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null))

        if (data != null) {
            return FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data)
        }
        return null
    }

    //MARK: Methods using callbacks
    fun getPaymentMethods(page: Int? = null, perPage: Int? = null, completionHandler: (List<FrameObjects.PaymentMethod>?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethods(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<PaymentMethodResponses.ListPaymentMethodsResponse>(data)?.data)
            } else {
                completionHandler(null)
            }
        }
    }

    fun getPaymentMethodWith(paymentMethodId: String, completionHandler: (FrameObjects.PaymentMethod?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethodWith(paymentMethodId = paymentMethodId)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun getPaymentMethodsWithCustomer(customerId: String, completionHandler: (List<FrameObjects.PaymentMethod>?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethodsWithCustomer(customerId)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<PaymentMethodResponses.ListPaymentMethodsResponse>(data)?.data)
            } else {
                completionHandler(null)
            }
        }
    }

    fun createPaymentMethod(request: PaymentMethodRequests.CreatePaymentMethodRequest, encryptData: Boolean = true, completionHandler: (FrameObjects.PaymentMethod?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.CreatePaymentMethod

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun updatePaymentMethodWith(paymentMethodId: String, request: PaymentMethodRequests.UpdatePaymentMethodRequest, completionHandler: (FrameObjects.PaymentMethod?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.UpdatePaymentMethodWith(paymentMethodId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun attachPaymentMethodWith(paymentMethodId: String, request: PaymentMethodRequests.AttachPaymentMethodRequest, completionHandler: (FrameObjects.PaymentMethod?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.AttachPaymentMethodWith(paymentMethodId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun detachPaymentMethodWith(paymentMethodId: String, completionHandler: (FrameObjects.PaymentMethod?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.DetachPaymentMethodWith(paymentMethodId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null)) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data))
            } else {
                completionHandler(null)
            }
        }
    }
}