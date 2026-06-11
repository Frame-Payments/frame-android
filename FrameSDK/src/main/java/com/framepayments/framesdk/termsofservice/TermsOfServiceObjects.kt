package com.framepayments.framesdk.termsofservice

/**
 * Namespace for data objects returned by the Terms of Service API.
 */
object TermsOfServiceObjects {

    /**
     * Response containing the token generated for a Terms of Service acceptance flow.
     *
     * @property token Short-lived token that identifies the acceptance session; pass this token to [TermsOfServiceRequests.UpdateRequest] to record acceptance.
     */
    data class TermsOfServiceTokenResponse(
        val token: String?
    )
}
