package com.framepayments.framesdk.checkoutsessions

import com.framepayments.framesdk.FrameNetworkingEndpoints

/** Endpoints a checkout client secret can call. */
sealed class CheckoutSessionEndpoints : FrameNetworkingEndpoints {
    /** Mints a checkout client secret (`POST /v1/checkout_sessions`). */
    object CreateCheckoutSession : CheckoutSessionEndpoints()

    /** Reads the redacted account (`GET /v1/accounts/:id`). */
    data class GetAccount(val accountId: String) : CheckoutSessionEndpoints()

    /** Reads the redacted saved-card list (`GET /v1/accounts/:id/payment_methods`). */
    data class GetPaymentMethods(val accountId: String) : CheckoutSessionEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is CreateCheckoutSession -> "/v1/checkout_sessions"
            is GetAccount -> "/v1/accounts/$accountId"
            is GetPaymentMethods -> "/v1/accounts/$accountId/payment_methods"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateCheckoutSession -> "POST"
            is GetAccount, is GetPaymentMethods -> "GET"
        }

    override val queryItems: List<com.framepayments.framesdk.QueryItem>?
        get() = null
}
