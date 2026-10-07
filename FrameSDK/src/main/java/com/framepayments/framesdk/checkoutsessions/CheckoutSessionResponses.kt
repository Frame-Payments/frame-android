package com.framepayments.framesdk.checkoutsessions

import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.accounts.AccountObjects
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

/**
 * Redacted account body returned when a checkout client secret reads `GET /v1/accounts/:id`.
 * Only name and email are present; phone, address, SSN, and birthdate stay off the wire.
 */
internal data class CheckoutAccountPayload(
    val id: String?,
    @SerializedName("object") val accountObject: String?,
    val profile: CheckoutAccountProfilePayload?,
) {
    fun toAccount(): AccountObjects.Account {
        val individual = profile?.individual?.let { payload ->
            AccountObjects.IndividualAccount(
                name = AccountObjects.IndividualAccountName(
                    firstName = payload.name?.firstName,
                    lastName = payload.name?.lastName,
                ),
                email = payload.email,
            )
        }
        return AccountObjects.Account(
            id = id,
            accountObject = accountObject,
            type = if (individual != null) AccountObjects.AccountType.INDIVIDUAL else null,
            status = null,
            profile = individual?.let { AccountObjects.AccountProfile(individual = it) },
            created = null,
            updated = null,
            livemode = null,
        )
    }
}

internal data class CheckoutAccountProfilePayload(
    val individual: CheckoutIndividualPayload?,
)

internal data class CheckoutIndividualPayload(
    val name: CheckoutIndividualNamePayload?,
    val email: String?,
)

internal data class CheckoutIndividualNamePayload(
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
)

/**
 * Redacted payment-method list returned when a checkout client secret reads
 * `GET /v1/accounts/:id/payment_methods`. Billing address and full PAN stay off the wire.
 */
internal data class CheckoutPaymentMethodList(
    val data: List<CheckoutPaymentMethodPayload>?,
)

internal data class CheckoutPaymentMethodPayload(
    val id: String?,
    val type: FrameObjects.PaymentMethodType?,
    @SerializedName("object") val methodObject: String?,
    val status: FrameObjects.PaymentMethodStatus?,
    val card: CheckoutCardPayload?,
    val ach: CheckoutAchPayload?,
) {
    fun toPaymentMethod(): FrameObjects.PaymentMethod {
        return FrameObjects.PaymentMethod(
            id = id,
            billing = null,
            type = type,
            methodObject = methodObject,
            created = 0,
            updated = 0,
            livemode = false,
            card = card?.toPaymentCard(),
            ach = ach?.toBankAccount(),
            status = status,
        )
    }
}

internal data class CheckoutCardPayload(
    val brand: String?,
    @SerializedName("last_four") val lastFour: String?,
    @SerializedName("exp_month") val expirationMonth: String?,
    @SerializedName("exp_year") val expirationYear: String?,
) {
    fun toPaymentCard(): FrameObjects.PaymentCard {
        return FrameObjects.PaymentCard(
            brand = brand,
            expirationMonth = expirationMonth,
            expirationYear = expirationYear,
            issuer = null,
            currency = null,
            segment = null,
            type = null,
            lastFourDigits = lastFour,
        )
    }
}

internal data class CheckoutAchPayload(
    @SerializedName("account_type") val accountType: FrameObjects.PaymentAccountType?,
    @SerializedName("last_four") val lastFour: String?,
) {
    fun toBankAccount(): FrameObjects.BankAccount {
        return FrameObjects.BankAccount(
            accountType = accountType,
            bankName = null,
            accountNumber = null,
            routingNumber = null,
            lastFour = lastFour,
        )
    }
}
