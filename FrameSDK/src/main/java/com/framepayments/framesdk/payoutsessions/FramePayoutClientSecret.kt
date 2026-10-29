package com.framepayments.framesdk.payoutsessions

import com.framepayments.framesdk.NetworkingError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * A short-lived `pyt_sess_` token that payout uses to list banks and create a transfer.
 *
 * Mint it on the host backend with `POST /v1/payout_sessions`. Payout refreshes it with the
 * secret key when [expiresAt] has passed or the API rejects it as expired.
 *
 * @property clientSecret The `pyt_sess_…` bearer token.
 * @property expiresAt When the token stops being accepted, as a Unix timestamp in seconds.
 * @property amountCents Withdrawable-balance cap, in the smallest currency unit. The keypad and
 *   create amount must stay in `(0, amountCents]`. Refresh replays it.
 * @property amountCurrency ISO 4217 code for [amountCents].
 */
class FramePayoutClientSecret(
    var clientSecret: String,
    var expiresAt: Long,
    var amountCents: Int,
    var amountCurrency: String = "usd",
) {
    private val refreshMutex = Mutex()
    private var inFlight: CompletableDeferred<Pair<String?, NetworkingError?>>? = null

    /** `true` when [expiresAt] is now or earlier. */
    fun isExpired(nowSeconds: Long = System.currentTimeMillis() / 1000): Boolean = expiresAt <= nowSeconds

    /** One in-flight mint at a time; overlapping callers share that result. */
    internal suspend fun serializedRefresh(
        body: suspend () -> Pair<String?, NetworkingError?>,
    ): Pair<String?, NetworkingError?> {
        val (deferred, leader) = refreshMutex.withLock {
            inFlight?.let { it to false } ?: run {
                val created = CompletableDeferred<Pair<String?, NetworkingError?>>()
                inFlight = created
                created to true
            }
        }
        if (leader) {
            try {
                deferred.complete(body())
            } catch (t: Throwable) {
                deferred.complete(Pair(null, null))
                throw t
            } finally {
                withContext(NonCancellable) {
                    refreshMutex.withLock {
                        if (inFlight === deferred) inFlight = null
                    }
                }
            }
        }
        return deferred.await()
    }
}
