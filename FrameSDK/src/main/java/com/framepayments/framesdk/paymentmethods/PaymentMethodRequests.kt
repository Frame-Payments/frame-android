package com.framepayments.framesdk.paymentmethods
import com.google.gson.annotations.SerializedName
import com.framepayments.framesdk.FrameObjects

/**
 * Contains all request body models used by [PaymentMethodsAPI].
 */
object PaymentMethodRequests {

    /**
     * Request body for creating a card payment method.
     *
     * @property type Payment method type; always [FrameObjects.PaymentMethodType.CARD].
     * @property cardNumber The card number. Encrypted via Evervault before transmission when
     *   [PaymentMethodsAPI.createCardPaymentMethod] is called with `encryptData = true`.
     * @property expMonth Two-digit card expiration month (e.g., `"04"`).
     * @property expYear Four-digit card expiration year (e.g., `"2027"`).
     * @property cvc The card verification code. Encrypted via Evervault before transmission when
     *   [PaymentMethodsAPI.createCardPaymentMethod] is called with `encryptData = true`.
     * @property customer Optional identifier of the customer to associate with this payment method.
     * @property account Optional identifier of the merchant account to associate with this payment method.
     * @property billing Optional billing address to attach to this payment method.
     */
    data class CreateCardPaymentMethodRequest(
        val type: FrameObjects.PaymentMethodType = FrameObjects.PaymentMethodType.CARD,
        @SerializedName("card_number") var cardNumber: String,
        @SerializedName("exp_month") val expMonth: String,
        @SerializedName("exp_year") val expYear: String,
        var cvc: String,
        val customer: String? = null,
        val account: String? = null,
        val billing: FrameObjects.BillingAddress? = null
    )

    /**
     * Request body for creating an ACH bank-account payment method.
     *
     * @property type Payment method type; always [FrameObjects.PaymentMethodType.ACH].
     * @property accountType The bank account type (e.g., checking or savings).
     * @property accountNumber The bank account number.
     * @property routingNumber The nine-digit ABA routing number.
     * @property customer Optional identifier of the customer to associate with this payment method.
     * @property account Optional identifier of the merchant account to associate with this payment method.
     * @property billing Optional billing address to attach to this payment method.
     */
    data class CreateACHPaymentMethodRequest(
        val type: FrameObjects.PaymentMethodType = FrameObjects.PaymentMethodType.ACH,
        @SerializedName("account_type") val accountType: FrameObjects.PaymentAccountType,
        @SerializedName("account_number") val accountNumber: String,
        @SerializedName("routing_number") val routingNumber: String,
        val customer: String? = null,
        val account: String? = null,
        val billing: FrameObjects.BillingAddress? = null
    )

    /**
     * Request body for updating an existing payment method.
     *
     * All fields are optional; only provided fields are updated.
     *
     * @property expMonth Updated two-digit expiration month. Applicable to card payment methods only.
     * @property expYear Updated four-digit expiration year. Applicable to card payment methods only.
     * @property billing Updated billing address.
     */
    data class UpdatePaymentMethodRequest(
        @SerializedName("exp_month") val expMonth: String? = null, // Only used for `card` type
        @SerializedName("exp_year") val expYear: String? = null, // Only used for `card` type
        val billing: FrameObjects.BillingAddress? = null
    )

    /**
     * Request body for attaching a payment method to a customer or merchant account.
     *
     * At least one of [customer] or [account] should be provided.
     *
     * @property customer Identifier of the customer to attach the payment method to.
     * @property account Identifier of the merchant account to attach the payment method to.
     */
    data class AttachPaymentMethodRequest(
        val customer: String? = null,
        val account: String? = null
    )

    /**
     * Decoded Google Pay payment data returned by the Google Pay API.
     *
     * @property apiVersion Major version number of the Google Pay API used to generate the token.
     * @property apiVersionMinor Minor version number of the Google Pay API used to generate the token.
     * @property email Customer email address returned by Google Pay, if present.
     * @property paymentMethodData The raw payment method data object returned by Google Pay.
     */
    data class GooglePayWalletData(
        val apiVersion: Int,
        val apiVersionMinor: Int,
        val email: String?,
        val paymentMethodData: Any
    )

    /**
     * Wrapper that identifies the wallet provider and carries the decoded Google Pay token.
     *
     * @property type Wallet type identifier; always `"google_pay"`.
     * @property googlePay The decoded Google Pay payment data.
     */
    data class GooglePayWallet(
        val type: String = "google_pay",
        @SerializedName("google_pay") val googlePay: GooglePayWalletData
    )

    /**
     * Request body for creating a payment method from a Google Pay wallet token.
     *
     * @property type Payment method type; always `"card"`.
     * @property wallet The Google Pay wallet wrapper containing the decoded token.
     * @property customer Optional identifier of the customer to associate with this payment method.
     * @property account Optional identifier of the merchant account to associate with this payment method.
     */
    data class CreateGooglePayPaymentMethodRequest(
        val type: String = "card",
        @SerializedName("_wallet") val wallet: GooglePayWallet,
        val customer: String? = null,
        val account: String? = null
    )

    /**
     * Request body for creating an ACH payment method by connecting a bank account via Plaid.
     *
     * @property account Identifier of the merchant account to associate with this payment method.
     * @property publicToken Short-lived public token obtained from the Plaid Link flow.
     * @property accountId Plaid account identifier selected by the customer during the Link flow.
     * @property institutionName Human-readable name of the financial institution, if available.
     * @property subtype Plaid account subtype (e.g., `"checking"` or `"savings"`), if available.
     */
    data class ConnectPlaidBankAccountRequest(
        val account: String,
        @SerializedName("public_token") val publicToken: String,
        @SerializedName("account_id") val accountId: String,
        @SerializedName("institution_name") val institutionName: String? = null,
        val subtype: String? = null
    )
}
