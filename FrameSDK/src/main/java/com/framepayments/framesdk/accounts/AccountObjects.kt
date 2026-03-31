package com.framepayments.framesdk.accounts

import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.capabilities.CapabilityObjects
import com.google.gson.annotations.SerializedName

object AccountObjects {
    enum class AccountType {
        @SerializedName("individual") INDIVIDUAL,
        @SerializedName("business") BUSINESS
    }

    enum class AccountStatus {
        @SerializedName("pending") PENDING,
        @SerializedName("active") ACTIVE,
        @SerializedName("restricted") RESTRICTED,
        @SerializedName("disabled") DISABLED
    }

    data class AccountTermsOfService(
        val token: String? = null,
        @SerializedName("accepted_at") val acceptedAt: String? = null,
        @SerializedName("ip_address") val ipAddress: String? = null
    )

    data class AccountProfile(
        val business: BusinessAccount? = null,
        val individual: IndividualAccount? = null
    )

    data class AccountPhoneNumber(
        val number: String,
        @SerializedName("country_code") val countryCode: String
    )

    data class BusinessAccount(
        @SerializedName("legal_business_name") val legalBusinessName: String,
        @SerializedName("doing_business_as") val doingBusinessAs: String? = null,
        @SerializedName("business_type") val businessType: String,
        val email: String,
        val website: String? = null,
        val description: String? = null,
        @SerializedName("ein_last_four") val einLastFour: String? = null,
        val mcc: String? = null,
        val naics: String? = null,
        val address: AccountBillingAddress? = null,
        val phone: AccountPhoneNumber? = null
    )

    data class AccountBillingAddress(
        val city: String? = null,
        val country: String? = null,
        @SerializedName("state_or_province") val state: String? = null,
        @SerializedName("postal_code") val postalCode: String,
        @SerializedName("line_1") val addressLine1: String? = null,
        @SerializedName("line_2") val addressLine2: String? = null
    )

    data class IndividualAccountName(
        @SerializedName("first_name") val firstName: String? = null,
        @SerializedName("middle_name") val middleName: String? = null,
        @SerializedName("last_name") val lastName: String? = null,
        val suffix: String? = null
    )
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

    data class AccountStep(
        val key: String,
        val status: String,
        val label: String,
        val fields: List<String>,
        @SerializedName("currently_due") val currentlyDue: List<String>
    )

    data class Account(
        val id: String,
        @SerializedName("object") val accountObject: String,
        val type: AccountType,
        val status: AccountStatus,
        @SerializedName("external_id") val externalId: String? = null,
        val metadata: Map<String, String>? = null,
        val profile: AccountProfile? = null,
        val capabilities: List<CapabilityObjects.Capability>? = null,
        val steps: List<AccountStep>? = null,
        val created: Int,
        val updated: Int,
        val livemode: Boolean
    )

    data class PhoneVerification(
        val id: String,
        @SerializedName("object") val verificationObject: String,
        @SerializedName("account_id") val accountId: String,
        val status: String,
        val created: Int,
        val updated: Int,
        val livemode: Boolean
    )
}
