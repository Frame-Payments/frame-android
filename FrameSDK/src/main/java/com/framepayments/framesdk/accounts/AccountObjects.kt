package com.framepayments.framesdk.accounts

import com.framepayments.framesdk.FrameObjects
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
        @SerializedName("accepted_at") val acceptedAt: Int? = null,
        @SerializedName("ip_address") val ipAddress: String? = null,
        @SerializedName("user_agent") val userAgent: String? = null
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
        val address: FrameObjects.BillingAddress? = null,
        val phone: AccountPhoneNumber? = null
    )

    data class IndividualAccount(
        @SerializedName("first_name") val firstName: String,
        @SerializedName("middle_name") val middleName: String? = null,
        @SerializedName("last_name") val lastName: String,
        val suffix: String? = null,
        val email: String,
        @SerializedName("ssn_last_four") val ssnLastFour: String? = null
    )

    data class Account(
        val id: String,
        @SerializedName("object") val accountObject: String,
        val type: AccountType,
        val status: AccountStatus,
        @SerializedName("external_id") val externalId: String? = null,
        val metadata: Map<String, String>? = null,
        val profile: AccountProfile? = null,
        @SerializedName("created_at") val createdAt: Int,
        @SerializedName("updated_at") val updatedAt: Int,
        val livemode: Boolean
    )
}
