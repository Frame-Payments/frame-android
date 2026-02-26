package com.framepayments.framesdk.threedsecure

import com.google.gson.annotations.SerializedName

enum class VerificationStatus {
    @SerializedName("pending") PENDING,
    @SerializedName("succeeded") SUCCEEDED,
    @SerializedName("failed") FAILED,
    @SerializedName("issued") ISSUED
}

data class ThreeDSecureVerification(
    val id: String? = null,
    val customer: String,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("object") val verificationObject: String,
    val livemode: Boolean,
    val status: VerificationStatus?,
    @SerializedName("challenge_url") val challengeUrl: String,
    val completed: Int?,
    val created: Int,
    val updated: Int
)

data class ThreeDSecureVerificationError(
    val error: VerificationError? = null
) {
    data class VerificationError(
        val type: String,
        val message: String,
        @SerializedName("existing_intent_id") val existingIntentId: String?
    )
}
