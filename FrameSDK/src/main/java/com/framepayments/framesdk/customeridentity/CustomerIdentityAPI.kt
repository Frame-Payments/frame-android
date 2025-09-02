package com.framepayments.framesdk.customeridentity

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.paymentmethods.PaymentMethodResponses

object CustomerIdentityAPI {
    //MARK: Methods using coroutines
    suspend fun createCustomerIdentity(request: CustomerIdentityRequests.CreateCustomerIdentityRequest): Pair<CustomerIdentity?, NetworkingError?> {
        val endpoint = CustomerIdentityEndpoints.CreateCustomerIdentity
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(it) }, error)
    }

    suspend fun getCustomerIdentityWith(customerIdentityId: String): Pair<CustomerIdentity?, NetworkingError?> {
        val endpoint = CustomerIdentityEndpoints.GetCustomerIdentityWith(customerIdentityId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(it) }, error)
    }

    //MARK: Methods using callbacks
    fun createCustomerIdentity(request: CustomerIdentityRequests.CreateCustomerIdentityRequest, completionHandler: (CustomerIdentity?, NetworkingError?) -> Unit) {
        val endpoint = CustomerIdentityEndpoints.CreateCustomerIdentity

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<CustomerIdentity>(it) }, error )
        }
    }

    fun getCustomerIdentityWith(customerIdentityId: String, completionHandler: (CustomerIdentity?, NetworkingError?) -> Unit) {
        val endpoint = CustomerIdentityEndpoints.GetCustomerIdentityWith(customerIdentityId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<CustomerIdentity>(it) }, error )
        }
    }
}
