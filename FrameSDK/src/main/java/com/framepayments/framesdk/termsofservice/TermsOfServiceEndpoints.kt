package com.framepayments.framesdk.termsofservice

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class TermsOfServiceEndpoints : FrameNetworkingEndpoints {
    object CreateToken : TermsOfServiceEndpoints()
    object Update : TermsOfServiceEndpoints()

    override val endpointURL: String
        get() = "/v1/terms_of_service"

    override val httpMethod: String
        get() = when (this) {
            is CreateToken -> "POST"
            is Update -> "PATCH"
        }

    override val queryItems: List<QueryItem>?
        get() = null
}
