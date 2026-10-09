package com.framepayments.framesdk.transfersv2

import com.framepayments.framesdk.FrameObjects
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Coarse lifecycle status on a V2 [TransferV2].
 *
 * Detail lives on nested [TransferV2Payment.status], [TransferV2Payout.status], or
 * [TransferV2AccountTransfer.status]. An unrecognized value decodes to [UNKNOWN].
 */
enum class TransferV2Status {
    /** Waiting to finish. */
    @SerializedName("pending") PENDING,
    /** The transfer finished successfully. */
    @SerializedName("completed") COMPLETED,
    /** The transfer failed. */
    @SerializedName("failed") FAILED,
    /** The transfer was canceled. */
    @SerializedName("canceled") CANCELED,
    /** The transfer was reversed. */
    @SerializedName("reversed") REVERSED,
    /** A status this SDK version does not recognize. */
    UNKNOWN;

    /** Looks up a [TransferV2Status] from its wire value. */
    companion object {
        private val byWireValue: Map<String, TransferV2Status> = entries.associateBy { status ->
            TransferV2Status::class.java.getField(status.name)
                .getAnnotation(SerializedName::class.java)?.value ?: status.name
        }

        /** Resolves a wire value to its status, or [UNKNOWN] when unrecognized or null. */
        internal fun fromWireValue(value: String?): TransferV2Status =
            value?.let { byWireValue[it] } ?: UNKNOWN
    }
}

/**
 * Maps unrecognized [TransferV2Status] wire values to [TransferV2Status.UNKNOWN] instead of
 * letting Gson's default enum adapter deserialize them as null.
 */
internal object TransferV2StatusAdapter : TypeAdapterFactory {
    override fun <T> create(gson: com.google.gson.Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType != TransferV2Status::class.java) return null
        @Suppress("UNCHECKED_CAST")
        return object : TypeAdapter<TransferV2Status>() {
            override fun write(out: JsonWriter, value: TransferV2Status?) {
                if (value == null || value == TransferV2Status.UNKNOWN) {
                    out.nullValue()
                    return
                }
                val wire = TransferV2Status::class.java.getField(value.name)
                    .getAnnotation(SerializedName::class.java)?.value
                    ?: value.name.lowercase()
                out.value(wire)
            }

            override fun read(input: JsonReader): TransferV2Status {
                if (input.peek() == JsonToken.NULL) {
                    input.nextNull()
                    return TransferV2Status.UNKNOWN
                }
                return TransferV2Status.fromWireValue(input.nextString())
            }
        } as TypeAdapter<T>
    }
}

/**
 * Discriminator for which nested detail a V2 transfer carries.
 *
 * An unrecognized value decodes to [UNKNOWN].
 */
enum class TransferV2Type {
    /** A charge. */
    @SerializedName("payment") PAYMENT,
    /** A payout. */
    @SerializedName("payout") PAYOUT,
    /** A transfer between accounts. */
    @SerializedName("account_transfer") ACCOUNT_TRANSFER,
    /** A type this SDK version does not recognize. */
    UNKNOWN;

    /** Looks up a [TransferV2Type] from its wire value. */
    companion object {
        private val byWireValue: Map<String, TransferV2Type> = entries.associateBy { type ->
            TransferV2Type::class.java.getField(type.name)
                .getAnnotation(SerializedName::class.java)?.value ?: type.name
        }

        /** Resolves a wire value to its type, or [UNKNOWN] when unrecognized or null. */
        internal fun fromWireValue(value: String?): TransferV2Type =
            value?.let { byWireValue[it] } ?: UNKNOWN
    }
}

/**
 * Maps unrecognized [TransferV2Type] wire values to [TransferV2Type.UNKNOWN].
 */
internal object TransferV2TypeAdapter : TypeAdapterFactory {
    override fun <T> create(gson: com.google.gson.Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType != TransferV2Type::class.java) return null
        @Suppress("UNCHECKED_CAST")
        return object : TypeAdapter<TransferV2Type>() {
            override fun write(out: JsonWriter, value: TransferV2Type?) {
                if (value == null || value == TransferV2Type.UNKNOWN) {
                    out.nullValue()
                    return
                }
                val wire = TransferV2Type::class.java.getField(value.name)
                    .getAnnotation(SerializedName::class.java)?.value
                    ?: value.name.lowercase()
                out.value(wire)
            }

            override fun read(input: JsonReader): TransferV2Type {
                if (input.peek() == JsonToken.NULL) {
                    input.nextNull()
                    return TransferV2Type.UNKNOWN
                }
                return TransferV2Type.fromWireValue(input.nextString())
            }
        } as TypeAdapter<T>
    }
}

/**
 * Amount expressed as `{ value, currency }` on the V2 Transfers API.
 *
 * @property value Amount in the smallest currency unit (e.g. cents for USD).
 * @property currency ISO 4217 currency code (e.g. `"usd"`).
 */
data class TransferV2Money(
    val value: Int,
    val currency: String? = null
)

/**
 * Nested payment detail on a payment-type V2 transfer.
 *
 * @property status Payment status, such as `"succeeded"` or `"requires_confirmation"`.
 * @property authorizationMode `"automatic"` or `"manual"` for a card payment.
 * @property receiptEmail Email the receipt was sent to, if any.
 * @property statementDescriptor Text shown on the cardholder statement, if any.
 * @property productId Product this payment was created for, if any.
 * @property paymentLinkId Payment link this payment was created from, if any.
 * @property subscriptionId Subscription this payment belongs to, if any.
 * @property invoiceId Invoice this payment belongs to, if any.
 * @property cartData Itemized cart attached to the payment, if any.
 * @property amountAuthorized Amount authorized, if any.
 * @property amountCaptured Amount captured, if any.
 * @property amountRefunded Amount refunded, if any.
 * @property failureCode Machine-readable failure code, if the payment failed.
 * @property failureReason Cardholder-safe failure text, if the payment failed.
 * @property shipping Shipping attached to the payment, if any.
 */
data class TransferV2Payment(
    val status: String? = null,
    @SerializedName("authorization_mode") val authorizationMode: String? = null,
    @SerializedName("receipt_email") val receiptEmail: String? = null,
    @SerializedName("statement_descriptor") val statementDescriptor: String? = null,
    @SerializedName("product_id") val productId: String? = null,
    @SerializedName("payment_link_id") val paymentLinkId: String? = null,
    @SerializedName("subscription_id") val subscriptionId: String? = null,
    @SerializedName("invoice_id") val invoiceId: String? = null,
    @SerializedName("cart_data") val cartData: Map<String, String>? = null,
    @SerializedName("amount_authorized") val amountAuthorized: TransferV2Money? = null,
    @SerializedName("amount_captured") val amountCaptured: TransferV2Money? = null,
    @SerializedName("amount_refunded") val amountRefunded: TransferV2Money? = null,
    @SerializedName("failure_code") val failureCode: String? = null,
    @SerializedName("failure_reason") val failureReason: String? = null,
    val shipping: TransferV2Shipping? = null
)

/**
 * Nested payout detail on a payout-type V2 transfer.
 *
 * @property status Payout status.
 * @property rail Rail the payout was sent on, if any.
 * @property speed Payout speed, if any.
 * @property failureCode Machine-readable failure code, if the payout failed.
 * @property failureReason Failure text, if the payout failed.
 */
data class TransferV2Payout(
    val status: String? = null,
    val rail: String? = null,
    val speed: String? = null,
    @SerializedName("failure_code") val failureCode: String? = null,
    @SerializedName("failure_reason") val failureReason: String? = null
)

/**
 * Nested account-transfer detail on an account_transfer-type V2 transfer.
 *
 * @property status Account-transfer status.
 * @property failureCode Machine-readable failure code, if the transfer failed.
 * @property failureReason Failure text, if the transfer failed.
 */
data class TransferV2AccountTransfer(
    val status: String? = null,
    @SerializedName("failure_code") val failureCode: String? = null,
    @SerializedName("failure_reason") val failureReason: String? = null
)

/**
 * Shipping block returned on a V2 payment.
 *
 * @property name Recipient name.
 * @property phone Recipient phone.
 * @property carrier Shipping carrier, if any.
 * @property trackingNumber Tracking number, if any.
 * @property address Shipping address, if any.
 */
data class TransferV2Shipping(
    val name: String? = null,
    val phone: String? = null,
    val carrier: String? = null,
    @SerializedName("tracking_number") val trackingNumber: String? = null,
    val address: TransferV2Address? = null
)

/**
 * Address fields used on V2 shipping / billing.
 *
 * @property line1 Primary street address.
 * @property line2 Apartment, suite, or other secondary address.
 * @property city City name.
 * @property state State or province code.
 * @property postalCode ZIP or postal code.
 * @property country ISO 3166-1 alpha-2 country code.
 */
data class TransferV2Address(
    @SerializedName("line_1") val line1: String? = null,
    @SerializedName("line_2") val line2: String? = null,
    val city: String? = null,
    val state: String? = null,
    @SerializedName("postal_code") val postalCode: String? = null,
    val country: String? = null
)

/**
 * A `source` or `destination` slot on a V2 transfer response.
 *
 * @property paymentMethod Payment method on this endpoint, if any.
 * @property account Account on this endpoint, if any.
 * @property wallet Wallet on this endpoint, if any.
 */
data class TransferV2Endpoint(
    @SerializedName("payment_method") val paymentMethod: FrameObjects.PaymentMethod? = null,
    val account: TransferV2AccountRef? = null,
    val wallet: TransferV2WalletRef? = null
)

/**
 * Minimal account reference on a V2 endpoint.
 *
 * @property id Account id.
 * @property accountObject The object type string. Always `"account"` when present.
 * @property name Account display name, if any.
 */
data class TransferV2AccountRef(
    val id: String,
    @SerializedName("object") val accountObject: String? = null,
    val name: String? = null
)

/**
 * Minimal wallet reference on a V2 endpoint.
 *
 * @property id Wallet id.
 * @property walletObject The object type string. Always `"wallet"` when present.
 * @property provider Wallet provider, if any.
 * @property chain Chain the wallet is on, if any.
 * @property token Token held by the wallet, if any.
 */
data class TransferV2WalletRef(
    val id: String,
    @SerializedName("object") val walletObject: String? = null,
    val provider: String? = null,
    val chain: String? = null,
    val token: String? = null
)

/**
 * A 3D Secure challenge session for the client to complete.
 *
 * @property source The opaque session identifier the challenge is driven from.
 * @property directoryServerName The card network's directory server (e.g. `"visa"`).
 * @property challengeUrl The issuer challenge page to present.
 */
data class UseFrameSDK(
    val source: String?,
    @SerializedName("directory_server_name") val directoryServerName: String? = null,
    @SerializedName("challenge_url") val challengeUrl: String? = null
)

/**
 * Challenge presentation fields for client-side 3DS (when the API exposes them).
 *
 * @property type The kind of action required. Currently `"use_frame_sdk"` or a redirect.
 * @property redirectUrl Legacy/simple redirect URL when the API does not emit `use_frame_sdk`.
 * @property useFrameSDK Parameters for driving a 3D Secure challenge, when [type] is `"use_frame_sdk"`.
 */
data class TransferV2NextAction(
    val type: String? = null,
    @SerializedName("redirect_url") val redirectUrl: String? = null,
    @SerializedName("use_frame_sdk") val useFrameSDK: UseFrameSDK? = null
)

/**
 * A V2 transfer (`Core::Transfer`) returned by `/v2/transfers`.
 *
 * @property id Unique identifier for this transfer.
 * @property transferObject The object type string returned by the API (e.g. `"transfer"`).
 * @property type Discriminator for which nested detail this transfer carries.
 * @property status Coarse lifecycle status.
 * @property description Optional description provided when the transfer was created.
 * @property amount Transfer amount as `{ value, currency }`.
 * @property fee Fee amount as `{ value, currency }`.
 * @property netAmount Amount after fees as `{ value, currency }`.
 * @property livemode Whether this transfer was created in live mode.
 * @property created Unix timestamp (seconds) when this transfer was created.
 * @property pendingAt Unix timestamp when the transfer entered pending, if any.
 * @property completedAt Unix timestamp when the transfer completed, if any.
 * @property failedAt Unix timestamp when the transfer failed, if any.
 * @property canceledAt Unix timestamp when the transfer was canceled, if any.
 * @property reversedAt Unix timestamp when the transfer was reversed, if any.
 * @property reference Merchant reference string, if any.
 * @property metadata Arbitrary key-value pairs attached by the merchant.
 * @property source Source endpoint (payment method / account / wallet).
 * @property destination Destination endpoint (payment method / account / wallet).
 * @property payment Nested payment detail when [type] is [TransferV2Type.PAYMENT].
 * @property payout Nested payout detail when [type] is [TransferV2Type.PAYOUT].
 * @property accountTransfer Nested account-transfer detail when [type] is [TransferV2Type.ACCOUNT_TRANSFER].
 * @property nextAction Challenge presentation fields when a client action is required.
 */
data class TransferV2(
    val id: String,
    @SerializedName("object") val transferObject: String? = null,
    val type: TransferV2Type? = null,
    val status: TransferV2Status? = null,
    val description: String? = null,
    val amount: TransferV2Money? = null,
    val fee: TransferV2Money? = null,
    @SerializedName("net_amount") val netAmount: TransferV2Money? = null,
    val livemode: Boolean? = null,
    val created: Int? = null,
    @SerializedName("pending_at") val pendingAt: Int? = null,
    @SerializedName("completed_at") val completedAt: Int? = null,
    @SerializedName("failed_at") val failedAt: Int? = null,
    @SerializedName("canceled_at") val canceledAt: Int? = null,
    @SerializedName("reversed_at") val reversedAt: Int? = null,
    val reference: String? = null,
    val metadata: Map<String, String>? = null,
    val source: TransferV2Endpoint? = null,
    val destination: TransferV2Endpoint? = null,
    val payment: TransferV2Payment? = null,
    val payout: TransferV2Payout? = null,
    @SerializedName("account_transfer") val accountTransfer: TransferV2AccountTransfer? = null,
    @SerializedName("next_action") val nextAction: TransferV2NextAction? = null
)
