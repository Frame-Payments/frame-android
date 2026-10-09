package com.framepayments.framesdk.transfersv2

import com.google.gson.annotations.SerializedName

/**
 * Contains request payload models for the Transfers V2 API (`/v2/transfers`).
 */
object TransferV2Requests {

    /**
     * Nested `{ value, currency }` money amount for V2 create/update/confirm/capture/refund.
     *
     * @property value Amount in the smallest currency unit (e.g. cents for USD).
     * @property currency ISO 4217 currency code (e.g. `"usd"`). Defaults to `"usd"`.
     */
    data class MoneyAmount(
        val value: Int,
        val currency: String? = "usd"
    )

    /**
     * Address fields for nested payment-method billing or shipping.
     *
     * @property line1 Primary street address.
     * @property line2 Apartment, suite, or other secondary address.
     * @property city City name.
     * @property state State or province code.
     * @property postalCode ZIP or postal code.
     * @property country ISO 3166-1 alpha-2 country code.
     */
    data class Address(
        @SerializedName("line_1") val line1: String? = null,
        @SerializedName("line_2") val line2: String? = null,
        val city: String? = null,
        val state: String? = null,
        @SerializedName("postal_code") val postalCode: String? = null,
        val country: String? = null
    )

    /**
     * Nested payment method create payload under `source` / `destination`.
     *
     * @property accountId Account that owns the new payment method.
     * @property type Payment method type, such as `"card"` or `"ach"`.
     * @property cardNumber Card number for a card method.
     * @property expMonth Card expiration month.
     * @property expYear Card expiration year.
     * @property cvc Card security code.
     * @property accountNumber Bank account number for an ACH method.
     * @property routingNumber ABA routing number for an ACH method.
     * @property accountType Bank account type, such as `"checking"`.
     * @property cashTag Cash App tag, when that method is used.
     * @property email Email for the payment method, when required.
     * @property phoneNumber Phone number for the payment method, when required.
     * @property handle Handle for the payment method, when required.
     * @property billing Billing address for the payment method.
     */
    data class NestedPaymentMethod(
        @SerializedName("account_id") val accountId: String? = null,
        val type: String? = null,
        @SerializedName("card_number") val cardNumber: String? = null,
        @SerializedName("exp_month") val expMonth: Int? = null,
        @SerializedName("exp_year") val expYear: Int? = null,
        val cvc: String? = null,
        @SerializedName("account_number") val accountNumber: String? = null,
        @SerializedName("routing_number") val routingNumber: String? = null,
        @SerializedName("account_type") val accountType: String? = null,
        @SerializedName("cash_tag") val cashTag: String? = null,
        val email: String? = null,
        @SerializedName("phone_number") val phoneNumber: String? = null,
        val handle: String? = null,
        val billing: Address? = null
    )

    /**
     * A `source` or `destination` slot on create/update/confirm.
     *
     * @property accountId Account id, when the endpoint is an account.
     * @property paymentMethodId Saved payment method id.
     * @property walletId Wallet id.
     * @property rail Payout rail, when required.
     * @property speed Payout speed, when required.
     * @property paymentMethod Inline payment method, instead of an id.
     */
    data class EndpointSlot(
        @SerializedName("account_id") val accountId: String? = null,
        @SerializedName("payment_method_id") val paymentMethodId: String? = null,
        @SerializedName("wallet_id") val walletId: String? = null,
        val rail: String? = null,
        val speed: String? = null,
        @SerializedName("payment_method") val paymentMethod: NestedPaymentMethod? = null
    )

    /**
     * Shipping fields on create/update.
     *
     * @property line1 Primary street address.
     * @property line2 Apartment, suite, or other secondary address.
     * @property city City name.
     * @property state State or province code.
     * @property postalCode ZIP or postal code.
     * @property country ISO 3166-1 alpha-2 country code.
     * @property name Recipient name.
     * @property phone Recipient phone.
     * @property carrier Shipping carrier.
     * @property trackingNumber Tracking number.
     */
    data class Shipping(
        @SerializedName("line_1") val line1: String? = null,
        @SerializedName("line_2") val line2: String? = null,
        val city: String? = null,
        val state: String? = null,
        @SerializedName("postal_code") val postalCode: String? = null,
        val country: String? = null,
        val name: String? = null,
        val phone: String? = null,
        val carrier: String? = null,
        @SerializedName("tracking_number") val trackingNumber: String? = null
    )

    /**
     * External 3DS cryptogram payload under `payment_method_options.card.external_3ds`.
     *
     * @property version 3DS protocol version.
     * @property transactionId Directory-server transaction id.
     * @property cryptogram Authentication cryptogram.
     * @property electronicCommerceIndicator Electronic commerce indicator.
     * @property aresTransStatus Authentication response transaction status.
     */
    data class External3DS(
        val version: String? = null,
        @SerializedName("transaction_id") val transactionId: String? = null,
        val cryptogram: String? = null,
        @SerializedName("electronic_commerce_indicator") val electronicCommerceIndicator: String? = null,
        @SerializedName("ares_trans_status") val aresTransStatus: String? = null
    )

    /**
     * Card options nested under `payment_method_options`.
     *
     * @property external3ds Cryptogram from a 3DS authentication that already ran.
     */
    data class CardPaymentMethodOptions(
        @SerializedName("external_3ds") val external3ds: External3DS? = null
    )

    /**
     * Payment method options on create/confirm.
     *
     * @property card Card-specific options, including an external 3DS cryptogram.
     */
    data class PaymentMethodOptions(
        val card: CardPaymentMethodOptions? = null
    )

    /**
     * Body for `POST /v2/transfers` and shared fields for update/confirm.
     *
     * @property amount Nested money amount (`value` + `currency`). Required.
     * @property source Source endpoint slot (payment method / account / wallet).
     * @property destination Destination endpoint slot when required.
     * @property confirm Pass `false` for deferred confirm (client 3DS). Defaults to API behaviour when null.
     * @property authorizationMode `"automatic"` or `"manual"` for card payments.
     * @property receiptEmail Email address to send the receipt to.
     * @property statementDescriptor Text shown on the cardholder statement.
     * @property productId Product this transfer is for.
     * @property paymentLinkId Payment link this transfer was created from.
     * @property subscriptionId Subscription this transfer belongs to.
     * @property invoiceId Invoice this transfer belongs to.
     * @property description Merchant description of the transfer.
     * @property reference Merchant reference string.
     * @property shipping Shipping details for the payment.
     * @property paymentMethodOptions Card options, including an external 3DS cryptogram.
     * @property cartData Itemized cart attached to the transfer.
     * @property metadata Arbitrary key-value pairs for the merchant.
     * @property sonarSessionId The account's Sonar session; set by [TransfersV2API] on payment creates.
     */
    data class CreateTransferRequest(
        val amount: MoneyAmount,
        val source: EndpointSlot? = null,
        val destination: EndpointSlot? = null,
        val confirm: Boolean? = null,
        @SerializedName("authorization_mode") val authorizationMode: String? = null,
        @SerializedName("receipt_email") val receiptEmail: String? = null,
        @SerializedName("statement_descriptor") val statementDescriptor: String? = null,
        @SerializedName("product_id") val productId: String? = null,
        @SerializedName("payment_link_id") val paymentLinkId: String? = null,
        @SerializedName("subscription_id") val subscriptionId: String? = null,
        @SerializedName("invoice_id") val invoiceId: String? = null,
        val description: String? = null,
        val reference: String? = null,
        val shipping: Shipping? = null,
        @SerializedName("payment_method_options") val paymentMethodOptions: PaymentMethodOptions? = null,
        @SerializedName("cart_data") val cartData: Map<String, String>? = null,
        val metadata: Map<String, String>? = null,
        @SerializedName("sonar_session_id") val sonarSessionId: String? = null
    )

    /**
     * Optional amount for capture or refund.
     *
     * @property amount Nested money amount, or null for a full capture/refund.
     */
    data class AmountOnlyRequest(
        val amount: MoneyAmount? = null
    )
}

/** Partial update body for `PATCH /v2/transfers/:id` (pre-confirm payment fields). */
typealias UpdateTransferRequest = TransferV2Requests.CreateTransferRequest
