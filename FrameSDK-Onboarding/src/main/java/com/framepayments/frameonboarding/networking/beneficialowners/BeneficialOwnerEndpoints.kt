package com.framepayments.frameonboarding.networking.beneficialowners

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

internal sealed class BeneficialOwnerEndpoints : FrameNetworkingEndpoints {
    data class Index(val accountId: String) : BeneficialOwnerEndpoints()
    data class Create(val accountId: String) : BeneficialOwnerEndpoints()
    data class Update(val accountId: String, val ownerId: String) : BeneficialOwnerEndpoints()
    data class Destroy(val accountId: String, val ownerId: String) : BeneficialOwnerEndpoints()
    data class Confirm(val accountId: String) : BeneficialOwnerEndpoints()
    data class ResendInvite(val accountId: String, val ownerId: String) : BeneficialOwnerEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is Index -> "/v1/accounts/${accountId}/beneficial_owners"
            is Create -> "/v1/accounts/${accountId}/beneficial_owners"
            is Update -> "/v1/accounts/$accountId/beneficial_owners/$ownerId"
            is Destroy -> "/v1/accounts/$accountId/beneficial_owners/$ownerId"
            is Confirm -> "/v1/accounts/$accountId/beneficial_owners/confirm"
            is ResendInvite -> "/v1/accounts/$accountId/beneficial_owners/$ownerId/resend_invite"
        }

    override val httpMethod: String
        get() = when (this) {
            is Index -> "GET"
            is Create, is Confirm, is ResendInvite -> "POST"
            is Update -> "PATCH"
            is Destroy -> "DELETE"
        }

    override val queryItems: List<QueryItem>? = null
}
