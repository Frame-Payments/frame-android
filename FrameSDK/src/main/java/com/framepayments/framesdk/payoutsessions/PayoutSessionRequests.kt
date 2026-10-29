package com.framepayments.framesdk.payoutsessions

import com.framepayments.framesdk.transfersv2.TransferV2Money
import com.google.gson.annotations.SerializedName

/**
 * Request body for `POST /v1/payout_sessions`.
 *
 * The host backend mints this with a secret key. The SDK calls it only to replace a `pyt_sess_`
 * token that has expired, and only when a secret key is configured.
 *
 * [amount] is a required cap. The client amount on `POST /v2/transfers` must be in `(0, cap]` in
 * this currency.
 *
 * @property accountId The account the payout token may read and pay out from.
 * @property amount The withdrawable-balance cap. Refresh must replay it.
 */
data class CreatePayoutSessionRequest(
    @SerializedName("account_id") val accountId: String,
    val amount: TransferV2Money,
)
