package com.framepayments.framesdk.payoutsessions

import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.checkoutsessions.decodeSessionPaymentMethods
import com.framepayments.framesdk.transfersv2.TransferV2Money

/**
 * Mints and uses a payout client secret (`pyt_sess_`).
 *
 * `POST /v1/payout_sessions` is secret-key only. Production apps mint the token on their
 * backend and pass [FramePayoutClientSecret] into payout. Payout calls the mint again
 * only when that token is expired and a secret key is configured.
 */
object PayoutSessionsAPI {
    /**
     * Mints a payout client secret for [accountId].
     *
     * @param accountId The account the token may read and pay out from.
     * @param amount Required cap. The client amount must be in `(0, cap]` in this currency.
     * @return The session, and any networking error.
     */
    suspend fun createPayoutSession(
        accountId: String,
        amount: TransferV2Money,
    ): Pair<PayoutSession?, NetworkingError?> {
        // Secret auth is replaced by an active onboarding session. The mint only accepts sk_.
        val (data, error) = FrameNetworking.performDataTaskWithRequest(
            PayoutSessionEndpoints.CreatePayoutSession,
            CreatePayoutSessionRequest(accountId, amount),
            auth = FrameAuthMode.ClientSecret(FrameNetworking.apiSecretKey),
        )
        if (error != null) return Pair(null, error)
        return Pair(data?.let { FrameNetworking.parseResponse<PayoutSession>(it) }, null)
    }

    /**
     * Reads `payout_payment_method_id` for [accountId]. Refreshes [secret] once when it is expired
     * or the API rejects it.
     *
     * @param accountId The account the token may read.
     * @param secret The payout session used as the Bearer token.
     * @return The elected payout method id, if any, and any networking error.
     */
    suspend fun loadAccount(
        accountId: String,
        secret: FramePayoutClientSecret,
    ): Pair<String?, NetworkingError?> {
        return authorized(accountId, secret) { token ->
            val (data, error) = FrameNetworking.performDataTask(
                PayoutSessionEndpoints.GetAccount(accountId),
                auth = FrameAuthMode.ClientSecret(token),
            )
            if (error != null) return@authorized Pair(null, error)
            Pair(decodePayoutAccountResource(data)?.payoutPaymentMethodId, null)
        }
    }

    /**
     * Reads the saved payment methods payout is allowed to list. Refreshes [secret] once when it
     * is expired or the API rejects it.
     *
     * @param accountId The account the token may read.
     * @param secret The payout session used as the Bearer token.
     * @return Saved methods, and any networking error. Error JSON is `null`, not an empty list.
     */
    suspend fun loadPaymentMethods(
        accountId: String,
        secret: FramePayoutClientSecret,
    ): Pair<List<FrameObjects.PaymentMethod>?, NetworkingError?> {
        return authorized(accountId, secret) { token ->
            val (data, error) = FrameNetworking.performDataTask(
                PayoutSessionEndpoints.GetPaymentMethods(accountId),
                auth = FrameAuthMode.ClientSecret(token),
            )
            if (error != null) return@authorized Pair(null, error)
            Pair(decodeSessionPaymentMethods(data), null)
        }
    }

    /**
     * A live `pyt_sess_…` for create. Refreshes [secret] in place when it is expired.
     *
     * @param accountId The account the token was minted for.
     * @param secret The payout session to read or refresh.
     * @return A bearer token, or `null` when the session is expired and cannot be refreshed.
     */
    suspend fun authorizationToken(
        accountId: String,
        secret: FramePayoutClientSecret,
    ): String? = tokenForRead(accountId, secret).first

    private suspend fun <T> authorized(
        accountId: String,
        secret: FramePayoutClientSecret,
        read: suspend (String) -> Pair<T?, NetworkingError?>,
    ): Pair<T?, NetworkingError?> {
        val (token, tokenError) = tokenForRead(accountId, secret)
        if (token == null) {
            return Pair(
                null,
                tokenError ?: NetworkingError.ServerError(401, "Payout client secret expired."),
            )
        }
        val (value, error) = read(token)
        if (!unauthorized(error)) return Pair(value, error)
        val (refreshed, _) = refresh(accountId, secret)
        if (refreshed == null) return Pair(null, error)
        return read(refreshed)
    }

    private suspend fun tokenForRead(
        accountId: String,
        secret: FramePayoutClientSecret,
    ): Pair<String?, NetworkingError?> {
        if (!secret.isExpired() && secret.clientSecret.isNotEmpty()) {
            return Pair(secret.clientSecret, null)
        }
        val refreshed = refresh(accountId, secret)
        if (refreshed.first != null || refreshed.second != null) return refreshed
        if (secret.clientSecret.isNotEmpty()) return Pair(secret.clientSecret, null)
        return Pair(null, null)
    }

    private suspend fun refresh(
        accountId: String,
        secret: FramePayoutClientSecret,
    ): Pair<String?, NetworkingError?> {
        if (FrameNetworking.apiSecretKey.isEmpty()) return Pair(null, null)
        val amount = TransferV2Money(value = secret.amountCents, currency = secret.amountCurrency)
        val (session, error) = createPayoutSession(accountId, amount)
        val clientSecret = session?.clientSecret?.takeIf { error == null && it.isNotEmpty() }
            ?: return Pair(null, error)
        secret.clientSecret = clientSecret
        session.expiresAt?.let { secret.expiresAt = it }
        session.amount?.let {
            secret.amountCents = it.value
            if (!it.currency.isNullOrEmpty()) secret.amountCurrency = it.currency
        }
        return Pair(clientSecret, null)
    }

    private fun unauthorized(error: NetworkingError?): Boolean {
        return error is NetworkingError.ServerError && error.statusCode == 401
    }
}
