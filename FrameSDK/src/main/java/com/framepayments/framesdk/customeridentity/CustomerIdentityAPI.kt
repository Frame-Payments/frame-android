package com.framepayments.framesdk.customeridentity

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.FileUpload

object CustomerIdentityAPI {
    //MARK: Methods using coroutines
    suspend fun createCustomerIdentity(request: CustomerIdentityRequests.CreateCustomerIdentityRequest): Pair<CustomerIdentity?, NetworkingError?> {
        val endpoint = CustomerIdentityEndpoints.CreateCustomerIdentity
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
    }

    suspend fun getCustomerIdentityWith(customerIdentityId: String): Pair<CustomerIdentity?, NetworkingError?> {
        val endpoint = CustomerIdentityEndpoints.GetCustomerIdentityWith(customerIdentityId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
    }

    suspend fun createCustomerIdentityWith(customerId: String): Pair<CustomerIdentity?, NetworkingError?> {
        val endpoint = CustomerIdentityEndpoints.CreateCustomerIdentityWith(customerId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
    }

    suspend fun submitForVerification(customerIdentityId: String): Pair<CustomerIdentity?, NetworkingError?> {
        val endpoint = CustomerIdentityEndpoints.SubmitForVerification(customerIdentityId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
    }

    suspend fun uploadIdentityDocuments(customerIdentityId: String, filesToUpload: List<FileUpload>): Pair<CustomerIdentity?, NetworkingError?> {
        val endpoint = CustomerIdentityEndpoints.UploadIdentityDocuments(customerIdentityId)
        val (data, error) = FrameNetworking.performMultipartDataTask(endpoint, filesToUpload)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
    }

    //MARK: Methods using callbacks
    fun createCustomerIdentity(request: CustomerIdentityRequests.CreateCustomerIdentityRequest, completionHandler: (CustomerIdentity?, NetworkingError?) -> Unit) {
        val endpoint = CustomerIdentityEndpoints.CreateCustomerIdentity

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error )
        }
    }

    fun getCustomerIdentityWith(customerIdentityId: String, completionHandler: (CustomerIdentity?, NetworkingError?) -> Unit) {
        val endpoint = CustomerIdentityEndpoints.GetCustomerIdentityWith(customerIdentityId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error )
        }
    }

    fun createCustomerIdentityWith(customerId: String, completionHandler: (CustomerIdentity?, NetworkingError?) -> Unit) {
        val endpoint = CustomerIdentityEndpoints.CreateCustomerIdentityWith(customerId)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
        }
    }

    fun submitForVerification(customerIdentityId: String, completionHandler: (CustomerIdentity?, NetworkingError?) -> Unit) {
        val endpoint = CustomerIdentityEndpoints.SubmitForVerification(customerIdentityId)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
        }
    }

    fun uploadIdentityDocuments(customerIdentityId: String, filesToUpload: List<FileUpload>, completionHandler: (CustomerIdentity?, NetworkingError?) -> Unit) {
        val endpoint = CustomerIdentityEndpoints.UploadIdentityDocuments(customerIdentityId)
        FrameNetworking.performMultipartDataTask(endpoint, filesToUpload) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
        }
    }
}
