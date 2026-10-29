package com.framepayments.framesdk.payoutsessions

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
    /** `true` when [expiresAt] is now or earlier. */
    fun isExpired(nowSeconds: Long = System.currentTimeMillis() / 1000): Boolean = expiresAt <= nowSeconds
}
