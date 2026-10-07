package com.framepayments.framesdk.checkoutsessions

import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.accounts.AccountObjects

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
            val (data, error) = FrameNetworking.performDataTask(
                CheckoutSessionEndpoints.GetAccount(accountId),
                auth = FrameAuthMode.ClientSecret(token),
            )
            if (error != null) return@authorized Pair(null, error)
            Pair(data?.let { FrameNetworking.parseResponse<CheckoutAccountPayload>(it)?.toAccount() }, null)
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
            val (data, error) = FrameNetworking.performDataTask(
                CheckoutSessionEndpoints.GetPaymentMethods(accountId),
                auth = FrameAuthMode.ClientSecret(token),
            )
            if (error != null) return@authorized Pair(null, error)
            val methods = data
                ?.let { FrameNetworking.parseResponse<CheckoutPaymentMethodList>(it)?.data }
                ?.map { it.toPaymentMethod() }
            Pair(methods, null)
        }
    }

    private suspend fun <T> authorized(
        accountId: String,
        secret: FrameCheckoutClientSecret,
        read: suspend (String) -> Pair<T?, NetworkingError?>,
    ): Pair<T?, NetworkingError?> {
        val (token, tokenError) = tokenForRead(accountId, secret)
        if (token == null) {
            return Pair(
                null,
                tokenError ?: NetworkingError.ServerError(401, "Checkout client secret expired."),
            )
        }
        val (value, error) = read(token)
        if (!unauthorized(error)) return Pair(value, error)
        // After a 401, keep that error if the refresh mint fails.
        val (refreshed, _) = refresh(accountId, secret)
        if (refreshed == null) return Pair(null, error)
        return read(refreshed)
    }

    private suspend fun tokenForRead(
        accountId: String,
        secret: FrameCheckoutClientSecret,
    ): Pair<String?, NetworkingError?> {
        if (!secret.isExpired() && secret.clientSecret.isNotEmpty()) {
            return Pair(secret.clientSecret, null)
        }
        return refresh(accountId, secret)
    }

    private suspend fun refresh(
        accountId: String,
        secret: FrameCheckoutClientSecret,
    ): Pair<String?, NetworkingError?> {
        if (FrameNetworking.apiSecretKey.isEmpty()) return Pair(null, null)
        val (session, error) = createCheckoutSession(accountId)
        val clientSecret = session?.clientSecret?.takeIf { error == null && it.isNotEmpty() }
            ?: return Pair(null, error)
        secret.clientSecret = clientSecret
        session.expiresAt?.let { secret.expiresAt = it }
        return Pair(clientSecret, null)
    }

    private fun unauthorized(error: NetworkingError?): Boolean {
        return error is NetworkingError.ServerError && error.statusCode == 401
    }
}
