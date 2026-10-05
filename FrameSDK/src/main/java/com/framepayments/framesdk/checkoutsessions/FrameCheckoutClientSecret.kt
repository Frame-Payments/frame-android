package com.framepayments.framesdk.checkoutsessions

/**
 * A short-lived `chk_sess_` token that checkout uses to read a name, email, and saved cards.
 *
 * Mint it on the host backend with `POST /v1/checkout_sessions`. Checkout refreshes it with the
 * secret key when [expiresAt] has passed or the API rejects it as expired.
 *
 * @property clientSecret The `chk_sess_…` bearer token.
 * @property expiresAt When the token stops being accepted, as a Unix timestamp in seconds.
 */
class FrameCheckoutClientSecret(
    var clientSecret: String,
    var expiresAt: Long,
) {
    /** `true` when [expiresAt] is now or earlier. */
    fun isExpired(nowSeconds: Long = System.currentTimeMillis() / 1000): Boolean = expiresAt <= nowSeconds
}
