package com.framepayments.framesdk.customeridentity

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class CustomerIdentityEndpoints : FrameNetworkingEndpoints {
    object CreateCustomerIdentity : CustomerIdentityEndpoints()
    data class GetCustomerIdentityWith(val customerIdentityId: String) : CustomerIdentityEndpoints()
    data class CreateCustomerIdentityWith(val customerId: String) : CustomerIdentityEndpoints()
    data class SubmitForVerification(val customerIdentityId: String) : CustomerIdentityEndpoints()
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
