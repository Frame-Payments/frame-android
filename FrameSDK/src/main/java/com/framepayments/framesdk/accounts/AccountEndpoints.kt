package com.framepayments.framesdk.accounts

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class AccountEndpoints : FrameNetworkingEndpoints {
    object CreateAccount : AccountEndpoints()
    data class UpdateAccount(val accountId: String) : AccountEndpoints()
    data class GetAccounts(
        val status: AccountObjects.AccountStatus? = null,
        val type: AccountObjects.AccountType? = null,
        val externalId: String? = null,
        val includeDisabled: Boolean = false,
        val page: Int? = null,
        val perPage: Int? = null
    ) : AccountEndpoints()
    data class GetAccountWith(val accountId: String) : AccountEndpoints()
    data class DeleteAccountWith(val accountId: String) : AccountEndpoints()
    data class SearchAccounts(
        val email: String? = null,
        val externalId: String? = null,
        val type: AccountObjects.AccountType? = null,
        val status: AccountObjects.AccountStatus? = null,
        val createdBefore: String? = null,
        val createdAfter: String? = null
    ) : AccountEndpoints()
    data class RestrictAccount(val accountId: String) : AccountEndpoints()
    data class UnrestrictAccount(val accountId: String) : AccountEndpoints()
    data class GetPlaidLinkToken(val accountId: String, val androidPackageName: String) : AccountEndpoints()
    data class CreatePhoneVerification(val accountId: String) : AccountEndpoints()
    data class ConfirmPhoneVerification(val accountId: String, val verificationId: String) : AccountEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is CreateAccount, is GetAccounts -> "/v1/accounts"
            is UpdateAccount -> "/v1/accounts/${this.accountId}"
            is GetAccountWith -> "/v1/accounts/${this.accountId}"
            is DeleteAccountWith -> "/v1/accounts/${this.accountId}"
            is SearchAccounts -> "/v1/accounts/search"
            is RestrictAccount -> "/v1/accounts/${this.accountId}/restrict"
            is UnrestrictAccount -> "/v1/accounts/${this.accountId}/unrestrict"
            is GetPlaidLinkToken -> "/v1/accounts/${this.accountId}/plaid_link_token"
            is CreatePhoneVerification -> "/v1/accounts/${this.accountId}/phone_verifications"
            is ConfirmPhoneVerification -> "/v1/accounts/${this.accountId}/phone_verifications/${this.verificationId}/confirm"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateAccount, is RestrictAccount, is UnrestrictAccount,
            is CreatePhoneVerification, is ConfirmPhoneVerification -> "POST"
            is UpdateAccount -> "PATCH"
            is DeleteAccountWith -> "DELETE"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>?
        get() = when (this) {
            is GetAccounts -> {
                val items = mutableListOf<QueryItem>()
                status?.let { items.add(QueryItem("status", it.name.lowercase())) }
                type?.let { items.add(QueryItem("type", it.name.lowercase())) }
                externalId?.let { items.add(QueryItem("external_id", it)) }
                items.add(QueryItem("include_disabled", includeDisabled.toString()))
                page?.let { items.add(QueryItem("page", it.toString())) }
                perPage?.let { items.add(QueryItem("per_page", it.toString())) }
                items
            }
            is SearchAccounts -> {
                val items = mutableListOf<QueryItem>()
                email?.let { items.add(QueryItem("email", it)) }
                externalId?.let { items.add(QueryItem("external_id", it)) }
                type?.let { items.add(QueryItem("type", it.name.lowercase())) }
                status?.let { items.add(QueryItem("status", it.name.lowercase())) }
                createdBefore?.let { items.add(QueryItem("created_before", it)) }
                createdAfter?.let { items.add(QueryItem("created_after", it)) }
                items
            }
            is GetPlaidLinkToken -> listOf(QueryItem("android_package_name", androidPackageName))
            else -> null
        }
}
