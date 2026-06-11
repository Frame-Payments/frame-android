package com.framepayments.framesdk

import com.google.gson.annotations.SerializedName

/** Common Frame API domain objects shared across multiple SDK modules. */
object FrameObjects {
    /**
     * A postal billing or shipping address.
     *
     * @property city City name.
     * @property country ISO 3166-1 alpha-2 country code.
     * @property state State or province code.
     * @property postalCode ZIP or postal code.
     * @property addressLine1 Primary street address.
     * @property addressLine2 Apartment, suite, or other secondary address information.
     */
    data class BillingAddress(
        val city: String?,
        val country: String?,
        val state: String?,
        @SerializedName("postal_code") val postalCode: String?,
        @SerializedName("line_1") val addressLine1: String?,
        @SerializedName("line_2") val addressLine2: String?
    )

    /** Lifecycle status of a payment method on the Frame platform. */
    enum class PaymentMethodStatus {
        /** The payment method is active and can be used for transactions. */
        @SerializedName("active") ACTIVE,

        /** The payment method has been blocked and cannot be used. */
        @SerializedName("blocked") BLOCKED
    }

    /** The funding instrument type of a payment method. */
    enum class PaymentMethodType {
        /** A credit or debit card. */
        @SerializedName("card") CARD,

        /** An ACH bank account. */
        @SerializedName("ach") ACH
    }

    /** The type of a bank account used for ACH payments. */
    enum class PaymentAccountType {
        /** A checking account. */
        @SerializedName("checking") CHECKING,

        /** A savings account. */
        @SerializedName("savings") SAVINGS
    }

    /**
     * A payment method stored on the Frame platform.
     *
     * @property id Unique identifier for the payment method.
     * @property customerId The customer this payment method belongs to, if customer-scoped.
     * @property billing Billing address associated with this payment method.
     * @property type Whether this is a [PaymentMethodType.CARD] or [PaymentMethodType.ACH] method.
     * @property methodObject The Frame API object type string (e.g. `"payment_method"`).
     * @property created Unix timestamp when the payment method was created.
     * @property updated Unix timestamp when the payment method was last updated.
     * @property livemode `true` if this is a live-mode resource; `false` for test-mode.
     * @property card Card details when [type] is [PaymentMethodType.CARD].
     * @property ach Bank account details when [type] is [PaymentMethodType.ACH].
     * @property status Current [PaymentMethodStatus] of this payment method.
     */
    data class PaymentMethod(
        val id: String?,
        @SerializedName("customer_id") val customerId: String? = null,
        val billing: BillingAddress?,
        val type: PaymentMethodType?,
        @SerializedName("object") val methodObject: String?,
        val created: Int?,
        val updated: Int?,
        val livemode: Boolean?,
        val card: PaymentCard?,
        val ach: BankAccount?,
        val status: PaymentMethodStatus?
    )

    /**
     * Card details for a payment method of type [PaymentMethodType.CARD].
     *
     * @property brand Card network name (e.g. `"visa"`, `"mastercard"`).
     * @property expirationMonth Two-digit expiration month.
     * @property expirationYear Four-digit expiration year.
     * @property issuer The issuing bank name.
     * @property currency ISO 4217 billing currency code.
     * @property segment Card segment (e.g. `"consumer"`, `"commercial"`).
     * @property type Card funding type (e.g. `"debit"`, `"credit"`).
     * @property lastFourDigits The last four digits of the card number.
     * @property wallet Digital wallet details, if this card is tokenized in a wallet.
     */
    data class PaymentCard(
        val brand: String?,
        @SerializedName("exp_month") val expirationMonth: String?,
        @SerializedName("exp_year") val expirationYear: String?,
        val issuer: String?,
        val currency: String?,
        val segment: String?,
        val type: String?,
        @SerializedName("last_four") val lastFourDigits: String?,
        val wallet: Wallet? = null
    )

    /** The digital wallet type that tokenized a card. */
    enum class WalletType {
        /** Apple Pay. */
        @SerializedName("apple_pay") APPLE_PAY,

        /** Google Pay. */
        @SerializedName("google_pay") GOOGLE_PAY
    }

    /**
     * Digital wallet details for a card tokenized through Apple Pay or Google Pay.
     *
     * @property type The wallet provider.
     * @property dynamicLast4 The dynamic last-four digits assigned by the wallet (may differ from the physical card).
     */
    data class Wallet(
        val type: WalletType?,
        @SerializedName("dynamic_last4") val dynamicLast4: String?
    )

    /**
     * Bank account details for a payment method of type [PaymentMethodType.ACH].
     *
     * @property accountType Whether this is a [PaymentAccountType.CHECKING] or [PaymentAccountType.SAVINGS] account.
     * @property bankName Name of the financial institution.
     * @property accountNumber Full bank account number. Present only in contexts where it is explicitly returned.
     * @property routingNumber ABA routing number.
     * @property lastFour Last four digits of the account number.
     */
    data class BankAccount(
        @SerializedName("account_type") val accountType: PaymentAccountType?,
        @SerializedName("bank_name") val bankName: String?,
        @SerializedName("account_number") val accountNumber: String?,
        @SerializedName("routing_number") val routingNumber: String?,
        @SerializedName("last_four") val lastFour: String?
    )

    /** Lifecycle status of a customer on the Frame platform. */
    enum class CustomerStatus {
        /** The customer account is active. */
        @SerializedName("active") ACTIVE,

        /** The customer account has been blocked. */
        @SerializedName("blocked") BLOCKED
    }

    /**
     * A customer record on the Frame platform.
     *
     * @property id Unique identifier for the customer.
     * @property created Unix timestamp when the customer was created.
     * @property updated Unix timestamp when the customer was last updated.
     * @property livemode `true` if this is a live-mode resource; `false` for test-mode.
     * @property name Customer's full name.
     * @property status Current [CustomerStatus] of this customer.
     * @property phone Customer's phone number in E.164 format.
     * @property email Customer's email address.
     * @property description Optional merchant-supplied description for this customer.
     * @property dateOfBirth Date of birth in `YYYY-MM-DD` format.
     * @property customerObject The Frame API object type string (`"customer"`).
     * @property shippingAddress Customer's shipping address.
     * @property billingAddress Customer's billing address.
     * @property paymentMethods Payment methods attached to this customer.
     */
    data class Customer(
        val id: String?,
        val created: Int?,
        val updated: Int?,
        val livemode: Boolean?,
        val name: String?,
        val status: CustomerStatus?,
        val phone: String?,
        val email: String?,
        val description: String?,
        @SerializedName("date_of_birth") val dateOfBirth: String?,
        @SerializedName("object") val customerObject: String?,
        @SerializedName("shipping_address") val shippingAddress: BillingAddress?,
        @SerializedName("billing_address") val billingAddress: BillingAddress?,
        @SerializedName("payment_methods") val paymentMethods: List<PaymentMethod>?
    )
}

/**
 * An empty request body placeholder used for API calls that require a POST with no payload.
 *
 * @property description Unused field present to satisfy serialization requirements.
 */
data class EmptyRequest(
    val description: String?
)

/**
 * A single URL query-string parameter.
 *
 * @property name The parameter name.
 * @property value The parameter value, or null to omit the value.
 */
data class QueryItem(val name: String, val value: String?)

/**
 * Pagination metadata returned by Frame list endpoints.
 *
 * @property page The current page number.
 * @property url The URL of the current page.
 * @property hasMore Whether additional pages of results are available.
 */
data class FrameMetadata(
    val page: Int?,
    val url: String?,
    @SerializedName("has_more") val hasMore: Boolean?
)

/**
 * The multipart form-data field name for a document image upload.
 *
 * @property value The string value used as the form-data part name in the multipart upload.
 */
enum class FileUploadFieldName(val value: String) {
    /** The front side of an identity document. */
    FRONT("front"),

    /** The back side of an identity document. */
    BACK("back"),

    /** A selfie photo of the customer. */
    SELFIE("selfie")
}

/**
 * A bitmap image prepared for upload to the Frame document verification endpoint.
 *
 * @property bitmap The image to upload.
 * @property fieldName Which document side or selfie this image represents.
 */
data class FileUpload(
    val bitmap: android.graphics.Bitmap,
    val fieldName: FileUploadFieldName
) {
    /** The filename used in the multipart upload, derived from [fieldName]. */
    val fileName: String get() = "${fieldName.value}.jpg"

    /** MIME type for the uploaded image. */
    val mimeType: String = "image/jpeg"

    /**
     * Compresses the bitmap to a JPEG byte array at 30% quality for upload.
     *
     * @return JPEG-encoded bytes.
     */
    fun toByteArray(): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 30, stream)
        return stream.toByteArray()
    }
}
