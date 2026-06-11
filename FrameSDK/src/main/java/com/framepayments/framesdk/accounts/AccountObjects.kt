package com.framepayments.framesdk.accounts

import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.capabilities.CapabilityObjects
import com.google.gson.annotations.SerializedName

/** Namespace for account domain model types. */
object AccountObjects {

    /** Classifies whether an account belongs to an individual or a business. */
    enum class AccountType {
        /** A personal account held by an individual. */
        @SerializedName("individual") INDIVIDUAL,

        /** A business account held by a company or organization. */
        @SerializedName("business") BUSINESS
    }

    /** Lifecycle status of an account. */
    enum class AccountStatus {
        /** Account has been created but not yet approved. */
        @SerializedName("pending") PENDING,
        /** Account is in good standing and can transact. */
        @SerializedName("active") ACTIVE,
        /** Account has been temporarily restricted from transacting. */
        @SerializedName("restricted") RESTRICTED,
        /** Account has been permanently disabled. */
        @SerializedName("disabled") DISABLED
    }

    /**
     * Terms-of-service acceptance record for an account.
     *
     * @property token The terms-of-service token that was accepted.
     * @property acceptedAt ISO-8601 timestamp of acceptance.
     * @property ipAddress IP address from which the terms were accepted.
     */
    data class AccountTermsOfService(
        val token: String? = null,
        @SerializedName("accepted_at") val acceptedAt: String? = null,
        @SerializedName("ip_address") val ipAddress: String? = null
    )

    /**
     * Profile information for an account, containing either business or individual data.
     *
     * @property business Business profile details, present when the account type is [AccountType.BUSINESS].
     * @property individual Individual profile details, present when the account type is [AccountType.INDIVIDUAL].
     */
    data class AccountProfile(
        val business: BusinessAccount? = null,
        val individual: IndividualAccount? = null
    )

    /**
     * A phone number associated with an account.
     *
     * @property number The phone number digits.
     * @property countryCode The ISO country calling code (e.g., "1" for the US).
     */
    data class AccountPhoneNumber(
        val number: String?,
        @SerializedName("country_code") val countryCode: String?
    )

    /**
     * Business profile data for a business account.
     *
     * @property legalBusinessName The registered legal name of the business.
     * @property doingBusinessAs Optional trade name or DBA name.
     * @property businessType The type or structure of the business (e.g., LLC, sole proprietorship).
     * @property email Business contact email address.
     * @property website Business website URL.
     * @property description Brief description of the business.
     * @property einLastFour Last four digits of the Employer Identification Number.
     * @property mcc Merchant Category Code.
     * @property naics North American Industry Classification System code.
     * @property address Business physical address.
     * @property phone Business phone number.
     */
    data class BusinessAccount(
        @SerializedName("legal_business_name") val legalBusinessName: String?,
        @SerializedName("doing_business_as") val doingBusinessAs: String? = null,
        @SerializedName("business_type") val businessType: String?,
        val email: String?,
        val website: String? = null,
        val description: String? = null,
        @SerializedName("ein_last_four") val einLastFour: String? = null,
        val mcc: String? = null,
        val naics: String? = null,
        val address: AccountBillingAddress? = null,
        val phone: AccountPhoneNumber? = null
    )

    /**
     * A billing or mailing address associated with an account.
     *
     * @property city City name.
     * @property country ISO 3166-1 alpha-2 country code.
     * @property state State or province.
     * @property postalCode ZIP or postal code.
     * @property addressLine1 Primary street address.
     * @property addressLine2 Secondary address line (apartment, suite, etc.).
     */
    data class AccountBillingAddress(
        val city: String? = null,
        val country: String? = null,
        val state: String? = null,
        @SerializedName("postal_code") val postalCode: String?,
        @SerializedName("line_1") val addressLine1: String? = null,
        @SerializedName("line_2") val addressLine2: String? = null
    )

    /**
     * The name components of an individual account holder.
     *
     * @property firstName Legal first name.
     * @property middleName Middle name or initial.
     * @property lastName Legal last name.
     * @property suffix Name suffix (e.g., Jr., III).
     */
    data class IndividualAccountName(
        @SerializedName("first_name") val firstName: String? = null,
        @SerializedName("middle_name") val middleName: String? = null,
        @SerializedName("last_name") val lastName: String? = null,
        val suffix: String? = null
    )

    /**
     * Individual profile data for a personal account.
     *
     * @property name The account holder's name components.
     * @property email Contact email address.
     * @property ssnLastFour Last four digits of the Social Security Number.
     * @property phoneNumber Contact phone number.
     * @property phoneCountryCode ISO country calling code for the phone number.
     * @property address Residential address.
     * @property birthdate Date of birth in YYYY-MM-DD format.
     * @property ssn Full Social Security Number; omitted from most responses.
     */
    data class IndividualAccount(
        val name: IndividualAccountName? = null,
        val email: String? = null,
        @SerializedName("ssn_last_four") val ssnLastFour: String? = null,
        @SerializedName("phone_number") val phoneNumber: String? = null,
        @SerializedName("phone_country_code") val phoneCountryCode: String? = null,
        val address: AccountBillingAddress? = null,
        val birthdate: String? = null,
        val ssn: String? = null
    )

    /**
     * A single onboarding or compliance step required for an account.
     *
     * @property key Unique identifier for this step.
     * @property status Completion status of the step.
     * @property label Human-readable label for the step.
     * @property fields Fields associated with this step.
     * @property currentlyDue Fields that are currently required to advance past this step.
     */
    data class AccountStep(
        val key: String?,
        val status: String?,
        val label: String?,
        val fields: List<String>?,
        @SerializedName("currently_due") val currentlyDue: List<String>?
    )

    /**
     * Represents a Frame account belonging to a merchant or their sub-merchant.
     *
     * @property id Unique account identifier.
     * @property accountObject The API object type string (always `"account"`).
     * @property type Whether the account is [AccountType.INDIVIDUAL] or [AccountType.BUSINESS].
     * @property status The current lifecycle status of the account.
     * @property externalId Merchant-assigned external identifier.
     * @property metadata Arbitrary key-value pairs set by the merchant.
     * @property termsOfService Terms-of-service acceptance record.
     * @property profile Business or individual profile data.
     * @property capabilities List of payment capabilities enabled for this account.
     * @property steps Outstanding onboarding or compliance steps.
     * @property created Unix timestamp of account creation.
     * @property updated Unix timestamp of the last account update.
     * @property livemode `true` when the account exists in the live environment.
     */
    data class Account(
        val id: String?,
        @SerializedName("object") val accountObject: String?,
        val type: AccountType?,
        val status: AccountStatus?,
        @SerializedName("external_id") val externalId: String? = null,
        val metadata: Map<String, String>? = null,
        @SerializedName("terms_of_service") val termsOfService: AccountTermsOfService? = null,
        val profile: AccountProfile? = null,
        val capabilities: List<CapabilityObjects.Capability>? = null,
        val steps: List<AccountStep>? = null,
        val created: Int?,
        val updated: Int?,
        val livemode: Boolean?
    )

    /**
     * Represents a phone number verification attempt for an account.
     *
     * @property id Unique verification identifier.
     * @property verificationObject The API object type string (always `"phone_verification"`).
     * @property accountId ID of the account this verification belongs to.
     * @property status Current status of the verification attempt.
     * @property created Unix timestamp of creation.
     * @property updated Unix timestamp of the last update.
     * @property livemode `true` when the verification exists in the live environment.
     */
    data class PhoneVerification(
        val id: String?,
        @SerializedName("object") val verificationObject: String?,
        @SerializedName("account_id") val accountId: String?,
        val status: String?,
        val created: Int?,
        val updated: Int?,
        val livemode: Boolean?
    )
}
