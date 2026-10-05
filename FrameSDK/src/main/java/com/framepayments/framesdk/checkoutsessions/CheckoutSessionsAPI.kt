package com.framepayments.framesdk.checkoutsessions

import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.accounts.AccountObjects
import com.framepayments.framesdk.accounts.AccountsAPI
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI

/**
 * Mints and uses a checkout client secret (`chk_sess_`).
 *
 * `POST /v1/checkout_sessions` is secret-key only. Production apps mint the token on their
 * backend and pass [FrameCheckoutClientSecret] into checkout. Checkout calls the mint again
 * only when that token is expired and a secret key is configured.
 */
object CheckoutSessionsAPI {
    /**
     * Mints a checkout client secret for [accountId].
     *
     * @param accountId The account the token may read.
     * @return The session, and any networking error.
     */
    suspend fun createCheckoutSession(accountId: String): Pair<CheckoutSession?, NetworkingError?> {
        // Secret auth is replaced by an active onboarding session. The mint only accepts sk_.
        val (data, error) = FrameNetworking.performDataTaskWithRequest(
            CheckoutSessionEndpoints.CreateCheckoutSession,
            CreateCheckoutSessionRequest(accountId),
            auth = FrameAuthMode.ClientSecret(FrameNetworking.apiSecretKey),
        )
        if (error != null) return Pair(null, error)
        return Pair(data?.let { FrameNetworking.parseResponse<CheckoutSession>(it) }, null)
    }

    /**
     * Reads the account checkout is allowed to see. Refreshes [secret] once when it is expired
     * or the API rejects it.
     */
    suspend fun loadAccount(
        accountId: String,
        secret: FrameCheckoutClientSecret,
    ): Pair<AccountObjects.Account?, NetworkingError?> {
        return authorized(accountId, secret) { token ->
            AccountsAPI.getAccountWith(accountId, auth = FrameAuthMode.ClientSecret(token))
        }
    }

    /**
     * Reads the saved cards checkout is allowed to list. Refreshes [secret] once when it is
     * expired or the API rejects it.
     */
    suspend fun loadPaymentMethods(
        accountId: String,
        secret: FrameCheckoutClientSecret,
    ): Pair<List<FrameObjects.PaymentMethod>?, NetworkingError?> {
        return authorized(accountId, secret) { token ->
            PaymentMethodsAPI.getPaymentMethodsWithAccount(accountId, auth = FrameAuthMode.ClientSecret(token))
        }
    }

    private suspend fun <T> authorized(
        accountId: String,
        secret: FrameCheckoutClientSecret,
        read: suspend (String) -> Pair<T?, NetworkingError?>,
    ): Pair<T?, NetworkingError?> {
        val token = tokenForRead(accountId, secret)
            ?: return Pair(null, NetworkingError.ServerError(401, "Checkout client secret expired."))
        val (value, error) = read(token)
        if (!unauthorized(error)) return Pair(value, error)
        val refreshed = refresh(accountId, secret) ?: return Pair(null, error)
        return read(refreshed)
    }

    private suspend fun tokenForRead(accountId: String, secret: FrameCheckoutClientSecret): String? {
        if (!secret.isExpired() && secret.clientSecret.isNotEmpty()) return secret.clientSecret
        return refresh(accountId, secret)
    }

    private suspend fun refresh(accountId: String, secret: FrameCheckoutClientSecret): String? {
        if (FrameNetworking.apiSecretKey.isEmpty()) return null
        val (session, error) = createCheckoutSession(accountId)
        val clientSecret = session?.clientSecret?.takeIf { error == null && it.isNotEmpty() } ?: return null
        secret.clientSecret = clientSecret
        session.expiresAt?.let { secret.expiresAt = it }
        return clientSecret
    }

    private fun unauthorized(error: NetworkingError?): Boolean {
        return error is NetworkingError.ServerError && error.statusCode == 401
    }
}
