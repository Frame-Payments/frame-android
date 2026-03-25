package com.framepayments.framesdk.customeridentity
import com.google.gson.annotations.SerializedName
import com.framepayments.framesdk.FrameObjects

enum class CustomerIdentityStatus {
    @SerializedName("incomplete") INCOMPLETE,
    @SerializedName("pending") PENDING,
    @SerializedName("verified") VERIFIED,
    @SerializedName("failed") FAILED
}

enum class VerificationCheckStatus {
    @SerializedName("passed") PASSED,
    @SerializedName("failed") FAILED
}

data class IdentificationDocuments(
    @SerializedName("front_document_attached") val frontDocumentAttached: Boolean,
    @SerializedName("back_document_attached") val backDocumentAttached: Boolean,
    @SerializedName("selfie_attached") val selfieAttached: Boolean
)

data class IdentificationData(
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    @SerializedName("middle_name") val middleName: String?,
    @SerializedName("date_of_birth") val dateOfBirth: String?,
    @SerializedName("license_number") val licenseNumber: String?,
    val state: String?,
    @SerializedName("expiration_date") val expirationDate: String?,
    val address: String?
)

data class VerificationChecks(
    @SerializedName("id_photo_face_match") val idPhotoFaceMatch: VerificationCheckStatus,
    @SerializedName("id_age_over_18") val idAgeOver18: VerificationCheckStatus,
    @SerializedName("id_not_expired") val idNotExpired: VerificationCheckStatus,
    @SerializedName("id_tamper_detection") val idTamperDetection: VerificationCheckStatus
)

data class CustomerIdentity(
    val id: String,
    val status: CustomerIdentityStatus,
    val created: Int,
    val updated: Int,
    val pending: Int?,
    val verified: Int?,
    val failed: Int?,
    @SerializedName("verification_url") val verificationURL: String?,
    @SerializedName("object") val identityObject: String?,
    val documents: IdentificationDocuments?,
    val provider: String?,
    @SerializedName("provider_reference") val providerReference: String?,
    @SerializedName("extracted_data") val extractedData: IdentificationData?,
    @SerializedName("verification_checks") val verificationChecks: VerificationChecks?,
    val customer: FrameObjects.Customer?
)