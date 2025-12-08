package com.framepayments.framesdk.customeridentity
import com.google.gson.annotations.SerializedName

enum class CustomerIdentityStatus {
    @SerializedName("incomplete") INCOMPLETE,
    @SerializedName("pending") PENDING,
    @SerializedName("verified") VERIFIED,
    @SerializedName("failed") FAILED
}

data class CustomerIdentity(
    val id: String,
    val status: CustomerIdentityStatus,
    val created: Int,
    val updated: Int,
    val pending: Int?,
    val verified: Int?,
    val failed: Int?,
    @SerializedName("verification_url") val verificationURL: String?,
    @SerializedName("object") val identityObject: String?
)