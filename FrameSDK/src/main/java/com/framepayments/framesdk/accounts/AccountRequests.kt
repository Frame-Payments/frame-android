package com.framepayments.framesdk.accounts

import com.framepayments.framesdk.FrameObjects
import com.google.gson.annotations.SerializedName

object AccountRequests {
    data class CreateAccountInfo(
        @SerializedName("first_name") val firstName: String,
        @SerializedName("middle_name") val middleName: String? = null,
        @SerializedName("last_name") val lastName: String,
        val suffix: String? = null
    )

    data class CreateIndividualAccount(
        val name: CreateAccountInfo,
        val email: String,
        val phone: AccountObjects.AccountPhoneNumber,
        val address: FrameObjects.BillingAddress? = null,
        val dob: String? = null,
        val ssn: String? = null
    )

    data class CreateAccountProfile(
        val business: AccountObjects.BusinessAccount? = null,
        val individual: CreateIndividualAccount? = null
    )

    data class CreateAccountRequest(
        val type: AccountObjects.AccountType,
        @SerializedName("external_id") val externalId: String? = null,
        @SerializedName("terms_of_service") val termsOfService: AccountObjects.AccountTermsOfService? = null,
        val metadata: Map<String, String>? = null,
        val profile: CreateAccountProfile
    )

    data class UpdateAccountInfo(
        @SerializedName("first_name") val firstName: String? = null,
        @SerializedName("middle_name") val middleName: String? = null,
        @SerializedName("last_name") val lastName: String? = null,
        val suffix: String? = null
    )

    data class UpdateIndividualAccount(
        val name: UpdateAccountInfo? = null,
        val email: String? = null,
        val phone: AccountObjects.AccountPhoneNumber? = null,
        val address: FrameObjects.BillingAddress? = null,
        val dob: String? = null,
        val ssn: String? = null
    )

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

    data class UpdateAccountProfile(
        val business: UpdateBusinessAccount? = null,
        val individual: UpdateIndividualAccount? = null
    )

    data class UpdateAccountRequest(
        val type: AccountObjects.AccountType? = null,
        @SerializedName("external_id") val externalId: String? = null,
        @SerializedName("terms_of_service") val termsOfService: AccountObjects.AccountTermsOfService? = null,
        val metadata: Map<String, String>? = null,
        val profile: CreateAccountProfile? = null
    )
}
