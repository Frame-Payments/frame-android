package com.framepayments.framesdk.customeridentity

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class CustomerIdentityEndpoints : FrameNetworkingEndpoints {
    object CreateCustomerIdentity : CustomerIdentityEndpoints()
    data class GetCustomerIdentityWith(val customerIdentityId: String) : CustomerIdentityEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is CreateCustomerIdentity ->
                "/v1/customer_identity_verifications"
            is GetCustomerIdentityWith ->
                "/v1/customer_identity_verifications/${this.customerIdentityId}"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateCustomerIdentity -> "POST"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>? = null
}
