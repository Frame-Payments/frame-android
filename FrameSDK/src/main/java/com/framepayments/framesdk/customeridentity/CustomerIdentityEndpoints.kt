package com.framepayments.framesdk.customeridentity

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines the API endpoints used by customer identity verification operations.
 *
 * Each case maps to a specific HTTP method and URL path used by [CustomerIdentityAPI].
 */
sealed class CustomerIdentityEndpoints : FrameNetworkingEndpoints {

    /**
     * Endpoint for creating a new customer identity verification record.
     *
     * Sends a POST request to `/v1/customer_identity_verifications`.
     */
    object CreateCustomerIdentity : CustomerIdentityEndpoints()

    /**
     * Endpoint for retrieving an existing customer identity verification record.
     *
     * Sends a GET request to `/v1/customer_identity_verifications/{customerIdentityId}`.
     *
     * @property customerIdentityId The unique identifier of the identity record to retrieve.
     */
    data class GetCustomerIdentityWith(val customerIdentityId: String) : CustomerIdentityEndpoints()

    /**
     * Endpoint for creating a new identity verification record attached to an existing customer.
     *
     * Sends a POST request to `/v1/customers/{customerId}/identity_verifications`.
     *
     * @property customerId The unique identifier of the existing customer to link the record to.
     */
    data class CreateCustomerIdentityWith(val customerId: String) : CustomerIdentityEndpoints()

    /**
     * Endpoint for submitting an identity verification record for KYC processing.
     *
     * Sends a POST request to `/v1/customer_identity_verifications/{customerIdentityId}/submit`.
     *
     * @property customerIdentityId The unique identifier of the identity record to submit.
     */
    data class SubmitForVerification(val customerIdentityId: String) : CustomerIdentityEndpoints()

    /**
     * Endpoint for uploading identity document files to a verification record.
     *
     * Sends a multipart POST request to
     * `/v1/customer_identity_verifications/{customerIdentityId}/upload_documents`.
     *
     * @property customerIdentityId The unique identifier of the identity record to attach documents to.
     */
    data class UploadIdentityDocuments(val customerIdentityId: String) : CustomerIdentityEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is CreateCustomerIdentity ->
                "/v1/customer_identity_verifications"
            is GetCustomerIdentityWith ->
                "/v1/customer_identity_verifications/${this.customerIdentityId}"
            is CreateCustomerIdentityWith ->
                "/v1/customers/${this.customerId}/identity_verifications"
            is SubmitForVerification ->
                "/v1/customer_identity_verifications/${this.customerIdentityId}/submit"
            is UploadIdentityDocuments ->
                "/v1/customer_identity_verifications/${this.customerIdentityId}/upload_documents"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateCustomerIdentity -> "POST"
            is CreateCustomerIdentityWith -> "POST"
            is SubmitForVerification -> "POST"
            is UploadIdentityDocuments -> "POST"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>? = null
}
