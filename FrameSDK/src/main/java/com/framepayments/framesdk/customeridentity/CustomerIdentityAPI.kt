package com.framepayments.framesdk.customeridentity

import com.framepayments.framesdk.FrameNetworking

object CustomerIdentityAPI {
    //MARK: Methods using coroutines
    suspend fun createCustomerIdentity(request: CustomerIdentityRequests.CreateCustomerIdentityRequest): CustomerIdentity? {
        val endpoint = CustomerIdentityEndpoints.CreateCustomerIdentity
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            return FrameNetworking.parseResponse<CustomerIdentity>(data)
        }
        return null
    }

    suspend fun getCustomerIdentityWith(customerId: String): CustomerIdentity? {
        val endpoint = CustomerIdentityEndpoints.GetCustomerIdentityWith(customerId)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<CustomerIdentity>(data)
        }
        return null
    }

    //MARK: Methods using callbacks
    fun createCustomerIdentity(request: CustomerIdentityRequests.CreateCustomerIdentityRequest, completionHandler: (CustomerIdentity?) -> Unit) {
        val endpoint = CustomerIdentityEndpoints.CreateCustomerIdentity

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<CustomerIdentity>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun getCustomerIdentityWith(customerId: String, completionHandler: (CustomerIdentity?) -> Unit) {
        val endpoint = CustomerIdentityEndpoints.GetCustomerIdentityWith(customerId)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<CustomerIdentity>(data))
            } else {
                completionHandler(null)
            }
        }
    }
}
