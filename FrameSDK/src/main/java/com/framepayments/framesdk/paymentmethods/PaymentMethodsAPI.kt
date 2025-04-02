package com.framepayments.framesdk.paymentmethods
import com.framepayments.framesdk.FrameNetworking

class PaymentMethodsAPI {
    //MARK: Methods using coroutines
    suspend fun getPaymentMethods(page: Int? = null, perPage: Int? = null): List<FrameObjects.PaymentMethod>? {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethods(perPage = perPage, page = page)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return try {
                val jsonString = String(data, Charsets.UTF_8)
                val response = FrameNetworking.gson.fromJson(
                    jsonString,
                    PaymentMethodResponses.ListPaymentMethodsResponse::class.java
                )
                response.data
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    suspend fun getPaymentMethodWith(paymentMethodId: String): FrameObjects.PaymentMethod? {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return try {
                val jsonString = String(data, Charsets.UTF_8)
                val response = FrameNetworking.gson.fromJson(
                    jsonString,
                    FrameObjects.PaymentMethod::class.java
                )
                response
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    suspend fun createPaymentMethod(request: PaymentMethodRequests.CreatePaymentMethodRequest, encryptData: Boolean = true): FrameObjects.PaymentMethod? {
        val endpoint = PaymentMethodEndpoints.CreatePaymentMethod
        val requestBody: ByteArray? = try {
            FrameNetworking.gson.toJson(request).toByteArray(Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }

        val (data, _) = FrameNetworking.performDataTask(endpoint, requestBody)

        if (data != null) {
            return try {
                val jsonString = String(data, Charsets.UTF_8)
                val response = FrameNetworking.gson.fromJson(
                    jsonString,
                    FrameObjects.PaymentMethod::class.java
                )
                response
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    suspend fun updatePaymentMethodWith(paymentMethodId: String, request: PaymentMethodRequests.UpdatePaymentMethodRequest): FrameObjects.PaymentMethod? {
        val endpoint = PaymentMethodEndpoints.UpdatePaymentMethodWith(paymentMethodId = paymentMethodId)
        val requestBody: ByteArray? = try {
            FrameNetworking.gson.toJson(request).toByteArray(Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }

        val (data, _) = FrameNetworking.performDataTask(endpoint, requestBody)

        if (data != null) {
            return try {
                val jsonString = String(data, Charsets.UTF_8)
                val response = FrameNetworking.gson.fromJson(
                    jsonString,
                    FrameObjects.PaymentMethod::class.java
                )
                response
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    suspend fun attachPaymentMethodWith(paymentMethodId: String, request: PaymentMethodRequests.AttachPaymentMethodRequest): FrameObjects.PaymentMethod? {
        val endpoint = PaymentMethodEndpoints.AttachPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return try {
                val jsonString = String(data, Charsets.UTF_8)
                val response = FrameNetworking.gson.fromJson(
                    jsonString,
                    FrameObjects.PaymentMethod::class.java
                )
                response
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    suspend fun detachPaymentMethodWith(paymentMethodId: String): FrameObjects.PaymentMethod? {
        val endpoint = PaymentMethodEndpoints.DetachPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return try {
                val jsonString = String(data, Charsets.UTF_8)
                val response = FrameNetworking.gson.fromJson(
                    jsonString,
                    FrameObjects.PaymentMethod::class.java
                )
                response
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    //MARK: Methods using callbacks
    fun getPaymentMethods(page: Int? = null, perPage: Int? = null, completionHandler: (List<FrameObjects.PaymentMethod>?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethods(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                try {
                    val jsonString = String(data, Charsets.UTF_8)
                    val decodedResponse = FrameNetworking.gson.fromJson(
                        jsonString,
                        PaymentMethodResponses.ListPaymentMethodsResponse::class.java
                    )
                    completionHandler(decodedResponse.data)
                } catch (e: Exception) {
                    completionHandler(null)
                }
            } else {
                completionHandler(null)
            }
        }
    }
}