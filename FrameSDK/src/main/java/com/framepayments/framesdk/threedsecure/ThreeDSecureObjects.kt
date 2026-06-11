package com.framepayments.framesdk.threedsecure

import com.google.gson.annotations.SerializedName

/**
 * Represents the lifecycle status of a 3D Secure verification intent.
 */
enum class VerificationStatus {
    /** The verification has been created and is awaiting customer action. */
    @SerializedName("pending") PENDING,

    /** The customer successfully completed the verification challenge. */
    @SerializedName("succeeded") SUCCEEDED,

    /** The verification challenge was attempted but did not succeed. */
    @SerializedName("failed") FAILED,

    /** The verification challenge has been issued to the customer. */
    @SerializedName("issued") ISSUED
}

/**
 * Represents a 3D Secure verification intent returned by the Frame API.
 *
 * @property id Unique identifier for this verification intent.
 * @property customer Identifier of the customer associated with this verification.
 * @property paymentMethod Identifier of the payment method being verified.
 * @property verificationObject The object type string returned by the API (e.g., "3ds_intent").
 * @property livemode Whether this verification was created in live mode.
 * @property status The current lifecycle status of this verification.
 * @property challengeUrl URL the merchant should present to the customer to complete the challenge.
 * @property completed Unix timestamp (seconds) when the verification was completed, or null if not yet completed.
 * @property created Unix timestamp (seconds) when this verification was created.
 * @property updated Unix timestamp (seconds) when this verification was last updated.
 */
data class ThreeDSecureVerification(
    val id: String?,
    val customer: String?,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("object") val verificationObject: String?,
    val livemode: Boolean?,
    val status: VerificationStatus?,
    @SerializedName("challenge_url") val challengeUrl: String?,
    val completed: Int?,
    val created: Int?,
    val updated: Int?
)

/**
 * Represents a structured API error returned when a 3D Secure verification cannot be created.
 *
 * @property error The nested error detail, or null if absent.
 */
data class ThreeDSecureVerificationError(
    val error: VerificationError?
) {
    /**
     * Contains the details of a verification creation error.
     *
     * @property type Machine-readable error type identifying the failure reason (e.g., "existing_intent").
     * @property message Human-readable description of the error.
     * @property existingIntentId Identifier of the conflicting existing intent, if applicable.
     */
    data class VerificationError(
        val type: String?,
        val message: String?,
        @SerializedName("existing_intent_id") val existingIntentId: String?
    )
}
