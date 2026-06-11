package com.framepayments.framesdk.accounts

import com.framepayments.framesdk.FrameObjects
import com.google.gson.annotations.SerializedName

/** Namespace for account request body types. */
object AccountRequests {

    /**
     * Profile data for creating an individual account holder.
     *
     * @property name Name components of the account holder.
     * @property email Contact email address. Either email or phone is required.
     * @property phone Contact phone number. Either email or phone is required.
     * @property address Residential address.
     * @property birthdate Date of birth in YYYY-MM-DD format.
     * @property ssn Full Social Security Number.
     * @property ssnLast4 Last four digits of the Social Security Number.
     * @property profileURL URL pointing to a profile image.
     */
    data class CreateIndividualAccount(
        val name: AccountObjects.IndividualAccountName? = null,
        val email: String? = null, // Either email or phone number is required to create an account
        val phone: AccountObjects.AccountPhoneNumber? = null,
        val address: FrameObjects.BillingAddress? = null,
        val birthdate: String? = null,
        val ssn: String? = null,
        @SerializedName("ssn_last4") val ssnLast4: String? = null,
        @SerializedName("profile_url") val profileURL: String? = null
    )

    /**
     * Profile container used when creating an account; holds business or individual data.
     *
     * @property business Business profile details.
     * @property individual Individual profile details.
     */
    data class CreateAccountProfile(
        val business: AccountObjects.BusinessAccount? = null,
        val individual: CreateIndividualAccount? = null
    )

    /**
     * Request body for creating a new account.
     *
     * @property type Whether this is an individual or business account.
     * @property externalId Merchant-assigned identifier for this account.
     * @property termsOfService Terms-of-service acceptance record to attach at creation.
     * @property metadata Arbitrary key-value pairs to store with the account.
     * @property profile Business or individual profile data.
     * @property capabilities List of capability keys to request for this account.
     */
    data class CreateAccountRequest(
        val type: AccountObjects.AccountType,
        @SerializedName("external_id") val externalId: String? = null,
        @SerializedName("terms_of_service") val termsOfService: AccountObjects.AccountTermsOfService? = null,
        val metadata: Map<String, String>? = null,
        val profile: CreateAccountProfile,
        val capabilities: List<String> = emptyList()
    )

    /**
     * Name update fields for an individual account holder.
     *
     * @property firstName Updated legal first name.
     * @property middleName Updated middle name or initial.
     * @property lastName Updated legal last name.
     * @property suffix Updated name suffix.
     */
    data class UpdateAccountInfo(
        @SerializedName("first_name") val firstName: String? = null,
        @SerializedName("middle_name") val middleName: String? = null,
        @SerializedName("last_name") val lastName: String? = null,
        val suffix: String? = null
    )

    /**
     * Updated profile data for an individual account holder.
     *
     * @property name Updated name components.
     * @property email Updated contact email address.
     * @property phoneNumber Updated contact phone number.
     * @property phoneCountryCode ISO country calling code for the updated phone number.
     * @property address Updated residential address.
     * @property birthdate Updated date of birth in YYYY-MM-DD format.
     * @property ssn Updated full Social Security Number.
     * @property ssnLast4 Updated last four digits of the Social Security Number.
     * @property profileURL Updated profile image URL.
     */
    data class UpdateIndividualAccount(
        val name: UpdateAccountInfo? = null,
        val email: String? = null,
        @SerializedName("phone_number") val phoneNumber: String? = null,
        @SerializedName("phone_country_code") val phoneCountryCode: String? = null,
        val address: FrameObjects.BillingAddress? = null,
        val birthdate: String? = null,
        val ssn: String? = null,
        @SerializedName("ssn_last4") val ssnLast4: String? = null,
        @SerializedName("profile_url") val profileURL: String? = null
    )

    /**
     * Updated profile data for a business account.
     *
     * @property legalBusinessName Updated registered legal business name.
     * @property doingBusinessAs Updated trade or DBA name.
     * @property businessType Updated business structure type.
     * @property email Updated business contact email.
     * @property website Updated business website URL.
     * @property description Updated business description.
     * @property einLastFour Updated last four digits of the EIN.
     * @property mcc Updated Merchant Category Code.
     * @property naics Updated NAICS industry code.
     * @property address Updated business address.
     * @property phone Updated business phone number.
     */
    data class UpdateBusinessAccount(
        @SerializedName("legal_business_name") val legalBusinessName: String? = null,
        @SerializedName("doing_business_as") val doingBusinessAs: String? = null,
        @SerializedName("business_type") val businessType: String? = null,
        val email: String? = null,
        val website: String? = null,
        val description: String? = null,
        @SerializedName("ein_last_four") val einLastFour: String? = null,
        val mcc: String? = null,
        val naics: String? = null,
        val address: FrameObjects.BillingAddress? = null,
        val phone: AccountObjects.AccountPhoneNumber? = null
    )

    /**
     * Profile container used when updating an account.
     *
     * @property business Updated business profile details.
     * @property individual Updated individual profile details.
     */
    data class UpdateAccountProfile(
        val business: UpdateBusinessAccount? = null,
        val individual: UpdateIndividualAccount? = null
    )

    /**
     * Request body for updating an existing account.
     *
     * @property type Updated account type.
     * @property externalId Updated merchant-assigned external identifier.
     * @property termsOfService Updated terms-of-service acceptance record.
     * @property metadata Updated arbitrary key-value pairs.
     * @property profile Updated business or individual profile data.
     */
    data class UpdateAccountRequest(
        val type: AccountObjects.AccountType? = null,
        @SerializedName("external_id") val externalId: String? = null,
        @SerializedName("terms_of_service") val termsOfService: AccountObjects.AccountTermsOfService? = null,
        val metadata: Map<String, String>? = null,
        val profile: UpdateAccountProfile? = null
    )

    /**
     * Request body for confirming a phone verification.
     *
     * @property code The one-time verification code sent to the account holder's phone.
     */
    data class ConfirmPhoneVerificationRequest(
        val code: String
    )
}
