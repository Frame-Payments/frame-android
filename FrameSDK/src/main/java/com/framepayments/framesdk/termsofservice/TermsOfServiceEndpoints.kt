package com.framepayments.framesdk.termsofservice

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines all API endpoints for Terms of Service operations.
 *
 * Both cases resolve to /v1/terms_of_service and differ only by HTTP method.
 */
sealed class TermsOfServiceEndpoints : FrameNetworkingEndpoints {

    /** Creates a new Terms of Service acceptance token. Resolves to POST /v1/terms_of_service. */
    object CreateToken : TermsOfServiceEndpoints()

    /** Records a customer's acceptance of the Terms of Service. Resolves to PATCH /v1/terms_of_service. */
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
