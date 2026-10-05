package com.framepayments.framesdk.checkoutsessions

import com.google.gson.annotations.SerializedName

/**
 * The checkout session returned by `POST /v1/checkout_sessions`.
 *
 * @property id The session identifier.
 * @property accountId The account the token may read.
 * @property clientSecret The `chk_sess_…` bearer token.
 * @property sessionObject The object type. Always `"checkout_session"`.
 * @property expiresAt Unix timestamp, in seconds, when the token expires.
 * @property livemode `true` for a live-mode session.
 */
data class CheckoutSession(
    val id: String?,
    @SerializedName("account_id") val accountId: String?,
    @SerializedName("client_secret") val clientSecret: String?,
    @SerializedName("object") val sessionObject: String?,
    @SerializedName("expires_at") val expiresAt: Long?,
    val livemode: Boolean?,
)
