package com.framepayments.framesdk.checkoutsessions

import com.google.gson.annotations.SerializedName

/**
 * Request body for `POST /v1/checkout_sessions`.
 *
 * The host backend mints this with a secret key. The SDK calls it only to replace a `chk_sess_`
 * token that has expired, and only when a secret key is configured.
 *
 * @property accountId The account the checkout token may read.
 */
data class CreateCheckoutSessionRequest(
    @SerializedName("account_id") val accountId: String,
)
