package com.framepayments.framesdk.customeridentity
import com.google.gson.annotations.SerializedName

enum class CustomerIdentityStatus {
    incomplete,
    pending,
    verified,
    failed
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