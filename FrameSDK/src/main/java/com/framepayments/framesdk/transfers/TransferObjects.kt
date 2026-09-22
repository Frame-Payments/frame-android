package com.framepayments.framesdk.transfers

import com.framepayments.framesdk.FrameObjects
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Represents the lifecycle status of a transfer.
 */
enum class TransferStatus {
    /** Created and awaiting processing. */
    @SerializedName("pending") PENDING,
    /** Created but not yet confirmed. */
    @SerializedName("incomplete") INCOMPLETE,
    /** Needs an account or customer before it can proceed. */
    @SerializedName("requires_account_or_customer") REQUIRES_ACCOUNT_OR_CUSTOMER,
    /** The cardholder must complete a 3D Secure challenge before the transfer can proceed. */
    @SerializedName("requires_payment_method") REQUIRES_PAYMENT_METHOD,
    /** Waiting to be confirmed. */
    @SerializedName("requires_confirmation") REQUIRES_CONFIRMATION,
    /** A 3D Secure challenge is required to authenticate the cardholder. */
    @SerializedName("requires_3d_secure") REQUIRES_THREE_D_SECURE,
    /** Authorized and awaiting a merchant-initiated capture. */
    @SerializedName("requires_capture") REQUIRES_CAPTURE,
    /** Being processed. */
    @SerializedName("processing") PROCESSING,
    /** Successfully processed. */
    @SerializedName("succeeded") SUCCEEDED,
    /** The transfer attempt failed. */
    @SerializedName("failed") FAILED,
    /** Expired before it could be completed. */
    @SerializedName("expired") EXPIRED,
    /** Canceled before processing. */
    @SerializedName("canceled") CANCELED,
    /** Reversed after completion. */
    @SerializedName("reversed") REVERSED,
    /** Refunded. */
    @SerializedName("refunded") REFUNDED,
    /** Disputed by the customer. */
    @SerializedName("disputed") DISPUTED,
    /** A dispute was resolved in the merchant's favor. */
    @SerializedName("disputed_won") DISPUTED_WON,
    /** A dispute was resolved in the customer's favor. */
    @SerializedName("disputed_lost") DISPUTED_LOST,
    /** Held for manual fraud review. */
    @SerializedName("fraud_review") FRAUD_REVIEW,
    /** Declined by fraud screening. */
    @SerializedName("fraud_declined") FRAUD_DECLINED,
    /** A status this SDK version does not recognize. */
    UNKNOWN,

    /** Not an API status. Use [SUCCEEDED]. Retained so existing call sites keep compiling. */
    @Deprecated("Not an API status. Use SUCCEEDED.")
    @SerializedName("completed") COMPLETED,

    /** Not an API status. Use [FRAUD_DECLINED] or [FAILED]. Retained so existing call sites keep compiling. */
    @Deprecated("Not an API status. Use FRAUD_DECLINED or FAILED.")
    @SerializedName("blocked") BLOCKED;

    /** Lookup support for [TransferStatusAdapter]. */
    companion object {
        private val byWireValue: Map<String, TransferStatus> = entries.associateBy { status ->
            TransferStatus::class.java.getField(status.name)
                .getAnnotation(SerializedName::class.java)?.value ?: status.name
        }

        /** Resolves a wire value to its status, or [UNKNOWN] when unrecognized or null. */
        internal fun fromWireValue(value: String?): TransferStatus =
            value?.let { byWireValue[it] } ?: UNKNOWN
    }
}

/**
 * Maps unrecognized [TransferStatus] wire values to [TransferStatus.UNKNOWN] instead of letting
 * Gson's default enum adapter deserialize them as null — a status this SDK version doesn't
 * recognize must still read as "not a known success/confirm state" everywhere status is checked.
 */
internal object TransferStatusAdapter : TypeAdapterFactory {
    override fun <T> create(gson: com.google.gson.Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType != TransferStatus::class.java) return null
        @Suppress("UNCHECKED_CAST")
        return object : TypeAdapter<TransferStatus>() {
            override fun write(out: JsonWriter, value: TransferStatus?) {
                out.value(value?.name?.lowercase())
            }

            override fun read(input: JsonReader): TransferStatus {
                if (input.peek() == JsonToken.NULL) {
                    input.nextNull()
                    return TransferStatus.UNKNOWN
                }
                return TransferStatus.fromWireValue(input.nextString())
            }
        } as TypeAdapter<T>
    }
}

/**
 * Represents a transfer between payment methods returned by the Frame API.
 *
 * @property id Unique identifier for this transfer.
 * @property status The current lifecycle status of this transfer.
 * @property amount Transfer amount in the smallest currency unit (e.g., cents).
 * @property currency Three-letter ISO 4217 currency code.
 * @property description Optional description provided when the transfer was created.
 * @property payout Identifier of the associated payout, if any.
 * @property metadata Arbitrary key-value pairs attached to this transfer by the merchant.
 * @property livemode Whether this transfer was created in live mode.
 * @property created Unix timestamp (seconds) when this transfer was created.
 * @property transferObject The object type string returned by the API (e.g., "transfer").
 * @property platformFee Fee retained by the merchant's platform, in the smallest currency unit.
 * @property frameFee Fee retained by Frame, in the smallest currency unit.
 * @property totalFees Sum of all fees deducted from this transfer, in the smallest currency unit.
 * @property grossAmount Total amount before fees, in the smallest currency unit.
 * @property netAmount Amount received after all fees, in the smallest currency unit.
 * @property failureReason Human-readable reason why this transfer failed, or null if not failed.
 * @property chargeIntent Identifier of the charge intent associated with this transfer, if any.
 * @property billingAgreement Identifier of the billing agreement associated with this transfer, if any.
 * @property sourcePaymentMethod The payment method funds were pulled from.
 * @property destinationPaymentMethod The payment method funds were pushed to.
 * @property clientSecret The wrapped charge intent's `client_secret` (`ci_<id>_secret_…`), present when [status] is [TransferStatus.REQUIRES_CONFIRMATION] or [TransferStatus.REQUIRES_THREE_D_SECURE].
 */
data class Transfer(
    val id: String?,
    val status: TransferStatus?,
    val amount: Int?,
    val currency: String?,
    val description: String?,
    val payout: String?,
    val metadata: Map<String, String>?,
    val livemode: Boolean?,
    val created: Int?,
    @SerializedName("object") val transferObject: String?,
    @SerializedName("platform_fee") val platformFee: Int?,
    @SerializedName("frame_fee") val frameFee: Int?,
    @SerializedName("total_fees") val totalFees: Int?,
    @SerializedName("gross_amount") val grossAmount: Int?,
    @SerializedName("net_amount") val netAmount: Int?,
    @SerializedName("failure_reason") val failureReason: String?,
    @SerializedName("charge_intent") val chargeIntent: String?,
    @SerializedName("billing_agreement") val billingAgreement: String?,
    @SerializedName("source_payment_method") val sourcePaymentMethod: FrameObjects.PaymentMethod?,
    @SerializedName("destination_payment_method") val destinationPaymentMethod: FrameObjects.PaymentMethod?,
    @SerializedName("client_secret") val clientSecret: String? = null
)
