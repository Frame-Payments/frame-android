package com.framepayments.framesdk.chargeintents
import com.framepayments.framesdk.FrameObjects
import com.google.gson.annotations.SerializedName

/**
 * Contains request body models used when creating, updating, and capturing charge intents.
 */
object ChargeIntentsRequests {

    /**
     * Identifies the type of payment method supplied in a request.
     */
    enum class PaymentMethodType {
        /** A card payment method. */
        card
    }

    /**
     * Request body for creating a new charge intent.
     *
     * @property amount Charge amount in the smallest currency unit (e.g., cents).
     * @property currency Three-letter ISO 4217 currency code.
     * @property customer ID of an existing customer to associate with this charge intent, or `null`.
     * @property description Merchant-supplied description of the charge.
     * @property confirm `true` to confirm and process the charge intent immediately upon creation.
     * @property paymentMethod ID of an existing payment method to attach to the charge intent, or `null`.
     * @property receiptEmail Email address to send a receipt to upon successful payment, or `null`.
     * @property authorizationMode Whether to capture funds automatically or hold them for manual capture.
     * @property customerData Inline customer name and email to create or match a customer record, or `null`.
     * @property paymentMethodData Inline card details used to create a new payment method, or `null`.
     * @property fraudSignals Device fraud signals attached automatically by the SDK before the request is sent.
     * @property useFrameSDK Indicates the request originated from the Frame Android SDK; defaults to `true`.
     * @property sonarSessionId Session identifier from the Sonar fraud-detection service, attached automatically by the SDK.
     */
    data class CreateChargeIntentRequest (
        val amount: Int,
        val currency : String,
        val customer : String?,
        val description : String?,
        val confirm: Boolean,
        @SerializedName("payment_method") val paymentMethod : String?,
        @SerializedName("receipt_email") val receiptEmail: String?,
        @SerializedName("authorization_mode") val authorizationMode: AuthorizationMode?,
        @SerializedName("customer_data") val customerData: CustomerData?,
        @SerializedName("payment_method_data") val paymentMethodData: PaymentMethodData?,
        @SerializedName("fraud_signals") var fraudSignals: FraudSignals? = null,
        @SerializedName("use_frame_sdk") val useFrameSDK: Boolean = true,
        @SerializedName("sonar_session_id") var sonarSessionId: String? = null
    )

    /**
     * Request body for updating mutable fields on an existing charge intent.
     *
     * @property amount Updated charge amount in the smallest currency unit, or `null` to leave unchanged.
     * @property currency Updated three-letter ISO 4217 currency code, or `null` to leave unchanged.
     * @property customer Updated customer ID, or `null` to leave unchanged.
     * @property description Updated merchant-supplied description, or `null` to leave unchanged.
     * @property confirm `true` to confirm the charge intent as part of this update, or `null` to leave unchanged.
     * @property paymentMethod Updated payment method ID, or `null` to leave unchanged.
     * @property receiptEmail Updated receipt email address, or `null` to leave unchanged.
     */
    data class UpdateChargeIntentRequest (
        val amount: Int?,
        val currency: String?,
        val customer: String?,
        val description: String?,
        val confirm: Boolean?,
        @SerializedName("payment_method") val paymentMethod: String?,
        @SerializedName("receipt_email") val receiptEmail: String?
    )

    /**
     * Request body for capturing an authorized charge intent.
     *
     * @property amountCapturedCents The amount to capture, in cents. Must not exceed the authorized amount.
     */
    data class CaptureChargeIntentRequest(
        @SerializedName("amount_captured_cents") val amountCapturedCents: Int
    )

    /**
     * Inline customer data used to create or match a customer record during charge intent creation.
     *
     * @property name Full name of the customer.
     * @property email Email address of the customer.
     */
    data class CustomerData (
        val name: String,
        val email: String
    )

    /**
     * Inline card details used to create a new payment method during charge intent creation.
     *
     * @property attach `true` to attach the resulting payment method to the customer for future use.
     * @property type The type of payment method being provided.
     * @property cardNumber The card number.
     * @property expMonth Two-digit expiration month (e.g., `"01"` for January).
     * @property expYear Four-digit expiration year (e.g., `"2027"`).
     * @property cvc The card verification code.
     * @property billing Billing address associated with the card, or `null`.
     */
    data class PaymentMethodData (
        val attach: Boolean?,
        val type: PaymentMethodType,
        @SerializedName("card_number") val cardNumber: String,
        @SerializedName("exp_month") val expMonth: String,
        @SerializedName("exp_year") val expYear: String,
        val cvc: String,
        val billing: FrameObjects.BillingAddress?
    )

    /**
     * Device-level fraud signals collected by the SDK and attached to charge intent requests.
     *
     * @property clientIp The public IP address of the device initiating the request.
     */
    data class FraudSignals (
        @SerializedName("client_ip") val clientIp: String?
    )
}
