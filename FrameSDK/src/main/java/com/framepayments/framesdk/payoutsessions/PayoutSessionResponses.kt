package com.framepayments.framesdk.payoutsessions

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.accounts.AccountObjects
import com.framepayments.framesdk.transfersv2.TransferV2Money
import com.google.gson.annotations.SerializedName

/**
 * The payout session returned by `POST /v1/payout_sessions`.
 *
 * @property id The session identifier.
 * @property accountId The account the token may read and pay out from.
 * @property clientSecret The `pyt_sess_…` bearer token.
 * @property sessionObject The object type. Always `"payout_session"`.
 * @property expiresAt Unix timestamp, in seconds, when the token expires.
 * @property livemode `true` for a live-mode session.
 * @property amount Required cap the client amount must stay within.
 */
data class PayoutSession(
    val id: String?,
    @SerializedName("account_id") val accountId: String?,
    @SerializedName("client_secret") val clientSecret: String?,
    @SerializedName("object") val sessionObject: String?,
    @SerializedName("expires_at") val expiresAt: Long?,
    val livemode: Boolean?,
    val amount: TransferV2Money? = null,
)

/**
 * Redacted account body returned when a payout client secret reads `GET /v1/accounts/:id`.
 * Only the elected payout method id is present.
 */
internal data class PayoutAccountPayload(
    val id: String?,
    @SerializedName("object") val accountObject: String?,
    @SerializedName("payout_payment_method_id") val payoutPaymentMethodId: String?,
) {
    fun toAccount(): AccountObjects.Account {
        return AccountObjects.Account(
            id = id,
            accountObject = accountObject,
            type = AccountObjects.AccountType.INDIVIDUAL,
            status = AccountObjects.AccountStatus.ACTIVE,
            created = 0,
            updated = 0,
            livemode = false,
            payoutPaymentMethodId = payoutPaymentMethodId,
        )
    }
}

/**
 * Decodes a full or slim account resource. Error JSON is not a stub account.
 *
 * @param data Raw response bytes.
 * @return An account when [data] is an `"account"` object, otherwise `null`.
 */
internal fun decodePayoutAccountResource(data: ByteArray?): AccountObjects.Account? {
    val full = FrameNetworking.parseResponse<AccountObjects.Account>(data)
    if (full?.accountObject == "account") return full
    val payload = FrameNetworking.parseResponse<PayoutAccountPayload>(data) ?: return null
    if (payload.accountObject != "account") return null
    return payload.toAccount()
}
