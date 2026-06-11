package com.framepayments.framesdk.customeridentity
import com.google.gson.annotations.SerializedName
import com.framepayments.framesdk.FrameObjects

/**
 * Represents the overall verification state of a customer identity record.
 */
enum class CustomerIdentityStatus {
    /** The identity record is missing required information and has not been submitted. */
    @SerializedName("incomplete") INCOMPLETE,

    /** The identity record has been submitted and is awaiting verification. */
    @SerializedName("pending") PENDING,

    /** The identity record has been successfully verified. */
    @SerializedName("verified") VERIFIED,

    /** The identity verification attempt has failed. */
    @SerializedName("failed") FAILED
}

/**
 * Represents the outcome of an individual verification check performed on an identity document.
 */
enum class VerificationCheckStatus {
    /** The verification check passed successfully. */
    @SerializedName("passed") PASSED,

    /** The verification check did not pass. */
    @SerializedName("failed") FAILED
}

/**
 * Tracks which identity document files have been attached to a customer identity record.
 *
 * @property frontDocumentAttached Whether the front side of the identity document has been uploaded.
 * @property backDocumentAttached Whether the back side of the identity document has been uploaded.
 * @property selfieAttached Whether a selfie image has been uploaded.
 */
data class IdentificationDocuments(
    @SerializedName("front_document_attached") val frontDocumentAttached: Boolean?,
    @SerializedName("back_document_attached") val backDocumentAttached: Boolean?,
    @SerializedName("selfie_attached") val selfieAttached: Boolean?
)

/**
 * Contains personal data extracted from the customer's identity document during verification.
 *
 * @property firstName The customer's first name as it appears on the document.
 * @property lastName The customer's last name as it appears on the document.
 * @property middleName The customer's middle name as it appears on the document, if present.
 * @property dateOfBirth The customer's date of birth extracted from the document.
 * @property licenseNumber The driver's license or ID number found on the document.
 * @property state The issuing state of the identity document.
 * @property expirationDate The expiration date of the identity document.
 * @property address The address printed on the identity document.
 */
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

/**
 * Holds the results of individual automated checks performed on the customer's identity document.
 *
 * @property idPhotoFaceMatch The result of comparing the document photo against the customer's selfie.
 * @property idAgeOver18 The result of verifying that the customer is 18 years of age or older.
 * @property idNotExpired The result of checking that the identity document has not expired.
 * @property idTamperDetection The result of checking the document for signs of tampering.
 */
data class VerificationChecks(
    @SerializedName("id_photo_face_match") val idPhotoFaceMatch: VerificationCheckStatus?,
    @SerializedName("id_age_over_18") val idAgeOver18: VerificationCheckStatus?,
    @SerializedName("id_not_expired") val idNotExpired: VerificationCheckStatus?,
    @SerializedName("id_tamper_detection") val idTamperDetection: VerificationCheckStatus?
)

/**
 * Represents a customer identity verification record returned by the Frame API.
 *
 * @property id The unique identifier for this identity verification record.
 * @property status The current overall status of the identity verification.
 * @property created Unix timestamp (seconds) indicating when the record was created.
 * @property updated Unix timestamp (seconds) indicating when the record was last updated.
 * @property pending Unix timestamp (seconds) indicating when the record entered the pending state.
 * @property verified Unix timestamp (seconds) indicating when the record was verified, if applicable.
 * @property failed Unix timestamp (seconds) indicating when the record entered the failed state, if applicable.
 * @property verificationURL A URL the customer can visit to complete the identity verification flow.
 * @property identityObject The API object type identifier for this resource.
 * @property documents The attachment status of the customer's identity document files.
 * @property provider The identity verification provider used to process this record.
 * @property providerReference The external reference identifier assigned by the verification provider.
 * @property extractedData Personal data extracted from the customer's identity document by the provider.
 * @property verificationChecks The results of automated checks performed on the identity document.
 * @property customer The Frame customer object associated with this identity record, if linked.
 */
data class CustomerIdentity(
    val id: String?,
    val status: CustomerIdentityStatus?,
    val created: Int?,
    val updated: Int?,
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
