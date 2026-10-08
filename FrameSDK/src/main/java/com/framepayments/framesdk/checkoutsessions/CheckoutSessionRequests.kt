package com.framepayments.framesdk.checkoutsessions

import com.framepayments.framesdk.transfersv2.TransferV2Money
import com.google.gson.annotations.SerializedName

/**
 * Request body for `POST /v1/checkout_sessions`.
 *
 * The host backend mints this with a secret key. The SDK calls it only to replace a `chk_sess_`
 * token that has expired, and only when a secret key is configured.
 *
 * A session can create a transfer only when [amount] is set. Gson omits a null amount, which
 * leaves the session read-only.
 *
 * @property accountId The account the checkout token may read.
 * @property amount The locked charge amount. Refresh must replay it or the new session cannot charge.
 */
data class CreateCheckoutSessionRequest(
    @SerializedName("account_id") val accountId: String,
    val amount: TransferV2Money? = null,
)
