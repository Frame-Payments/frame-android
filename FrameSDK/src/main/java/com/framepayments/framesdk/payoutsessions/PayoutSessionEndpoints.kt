package com.framepayments.framesdk.payoutsessions

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/** Endpoints a payout client secret can call. */
sealed class PayoutSessionEndpoints : FrameNetworkingEndpoints {
    /** Mints a payout client secret (`POST /v1/payout_sessions`). */
    object CreatePayoutSession : PayoutSessionEndpoints()

    /** Reads the redacted account (`GET /v1/accounts/:id`). */
    data class GetAccount(val accountId: String) : PayoutSessionEndpoints()

    /** Reads the redacted saved-method list (`GET /v1/accounts/:id/payment_methods`). */
    data class GetPaymentMethods(val accountId: String) : PayoutSessionEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is CreatePayoutSession -> "/v1/payout_sessions"
            is GetAccount -> "/v1/accounts/$accountId"
            is GetPaymentMethods -> "/v1/accounts/$accountId/payment_methods"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreatePayoutSession -> "POST"
            is GetAccount, is GetPaymentMethods -> "GET"
        }

    override val queryItems: List<QueryItem>?
        get() = null
}
