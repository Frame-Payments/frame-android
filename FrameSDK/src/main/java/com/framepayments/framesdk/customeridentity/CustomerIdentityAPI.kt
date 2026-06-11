package com.framepayments.framesdk.customeridentity

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.FileUpload

/**
 * Provides API operations for creating, retrieving, and verifying customer identity records.
 *
 * Each operation is available as both a coroutine suspend function and a callback-based overload.
 * "Customer" refers to the merchant's end user whose identity is being verified.
 */
object CustomerIdentityAPI {
    //MARK: Methods using coroutines

    /**
     * Creates a new customer identity verification record with the supplied PII.
     *
     * @param request The request body containing the customer's personal information required
     *   to initiate identity verification.
     * @return A [Pair] where the first element is the created [CustomerIdentity] on success,
     *   and the second element is a [NetworkingError] on failure; one of the two will be null.
     */
    suspend fun createCustomerIdentity(request: CustomerIdentityRequests.CreateCustomerIdentityRequest): Pair<CustomerIdentity?, NetworkingError?> {
        val endpoint = CustomerIdentityEndpoints.CreateCustomerIdentity
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
    }

    /**
     * Retrieves an existing customer identity verification record by its identifier.
     *
     * @param customerIdentityId The unique identifier of the customer identity record to retrieve.
     * @return A [Pair] where the first element is the matching [CustomerIdentity] on success,
     *   and the second element is a [NetworkingError] on failure; one of the two will be null.
     */
    suspend fun getCustomerIdentityWith(customerIdentityId: String): Pair<CustomerIdentity?, NetworkingError?> {
        val endpoint = CustomerIdentityEndpoints.GetCustomerIdentityWith(customerIdentityId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
    }

    /**
     * Creates a new identity verification record attached to an existing customer.
     *
     * @param customerId The unique identifier of the existing customer to associate with the
     *   new identity verification record.
     * @return A [Pair] where the first element is the created [CustomerIdentity] on success,
     *   and the second element is a [NetworkingError] on failure; one of the two will be null.
     */
    suspend fun createCustomerIdentityWith(customerId: String): Pair<CustomerIdentity?, NetworkingError?> {
        val endpoint = CustomerIdentityEndpoints.CreateCustomerIdentityWith(customerId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
    }

    /**
     * Submits an existing customer identity record for KYC/identity verification processing.
     *
     * @param customerIdentityId The unique identifier of the customer identity record to submit.
     * @return A [Pair] where the first element is the updated [CustomerIdentity] on success,
     *   and the second element is a [NetworkingError] on failure; one of the two will be null.
     */
    suspend fun submitForVerification(customerIdentityId: String): Pair<CustomerIdentity?, NetworkingError?> {
        val endpoint = CustomerIdentityEndpoints.SubmitForVerification(customerIdentityId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
    }

    /**
     * Uploads identity document files for a customer identity record via a multipart request.
     *
     * @param customerIdentityId The unique identifier of the customer identity record to attach
     *   the documents to.
     * @param filesToUpload The list of [FileUpload] objects representing the document files to send.
     * @return A [Pair] where the first element is the updated [CustomerIdentity] on success,
     *   and the second element is a [NetworkingError] on failure; one of the two will be null.
     */
    suspend fun uploadIdentityDocuments(customerIdentityId: String, filesToUpload: List<FileUpload>): Pair<CustomerIdentity?, NetworkingError?> {
        val endpoint = CustomerIdentityEndpoints.UploadIdentityDocuments(customerIdentityId)
        val (data, error) = FrameNetworking.performMultipartDataTask(endpoint, filesToUpload)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
    }

    //MARK: Methods using callbacks

    /**
     * Creates a new customer identity verification record with the supplied PII and delivers
     * the result to the provided callback.
     *
     * @param request The request body containing the customer's personal information required
     *   to initiate identity verification.
     * @param completionHandler Invoked with the created [CustomerIdentity] on success, or a
     *   [NetworkingError] on failure; one of the two arguments will be null.
     */
    fun createCustomerIdentity(request: CustomerIdentityRequests.CreateCustomerIdentityRequest, completionHandler: (CustomerIdentity?, NetworkingError?) -> Unit) {
        val endpoint = CustomerIdentityEndpoints.CreateCustomerIdentity

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error )
        }
    }

    /**
     * Retrieves an existing customer identity verification record by its identifier and delivers
     * the result to the provided callback.
     *
     * @param customerIdentityId The unique identifier of the customer identity record to retrieve.
     * @param completionHandler Invoked with the matching [CustomerIdentity] on success, or a
     *   [NetworkingError] on failure; one of the two arguments will be null.
     */
    fun getCustomerIdentityWith(customerIdentityId: String, completionHandler: (CustomerIdentity?, NetworkingError?) -> Unit) {
        val endpoint = CustomerIdentityEndpoints.GetCustomerIdentityWith(customerIdentityId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error )
        }
    }

    /**
     * Creates a new identity verification record attached to an existing customer and delivers
     * the result to the provided callback.
     *
     * @param customerId The unique identifier of the existing customer to associate with the
     *   new identity verification record.
     * @param completionHandler Invoked with the created [CustomerIdentity] on success, or a
     *   [NetworkingError] on failure; one of the two arguments will be null.
     */
    fun createCustomerIdentityWith(customerId: String, completionHandler: (CustomerIdentity?, NetworkingError?) -> Unit) {
        val endpoint = CustomerIdentityEndpoints.CreateCustomerIdentityWith(customerId)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
        }
    }

    /**
     * Submits an existing customer identity record for KYC/identity verification processing
     * and delivers the result to the provided callback.
     *
     * @param customerIdentityId The unique identifier of the customer identity record to submit.
     * @param completionHandler Invoked with the updated [CustomerIdentity] on success, or a
     *   [NetworkingError] on failure; one of the two arguments will be null.
     */
    fun submitForVerification(customerIdentityId: String, completionHandler: (CustomerIdentity?, NetworkingError?) -> Unit) {
        val endpoint = CustomerIdentityEndpoints.SubmitForVerification(customerIdentityId)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
        }
    }

    /**
     * Uploads identity document files for a customer identity record via a multipart request
     * and delivers the result to the provided callback.
     *
     * @param customerIdentityId The unique identifier of the customer identity record to attach
     *   the documents to.
     * @param filesToUpload The list of [FileUpload] objects representing the document files to send.
     * @param completionHandler Invoked with the updated [CustomerIdentity] on success, or a
     *   [NetworkingError] on failure; one of the two arguments will be null.
     */
    fun uploadIdentityDocuments(customerIdentityId: String, filesToUpload: List<FileUpload>, completionHandler: (CustomerIdentity?, NetworkingError?) -> Unit) {
        val endpoint = CustomerIdentityEndpoints.UploadIdentityDocuments(customerIdentityId)
        FrameNetworking.performMultipartDataTask(endpoint, filesToUpload) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CustomerIdentity>(data) }, error)
        }
    }
}
