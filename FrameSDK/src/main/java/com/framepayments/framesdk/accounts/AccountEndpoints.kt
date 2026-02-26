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
        val includeDisabled: Boolean = false
    ) : AccountEndpoints()
    data class GetAccountWith(val accountId: String) : AccountEndpoints()
    data class DeleteAccountWith(val accountId: String) : AccountEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is CreateAccount, is GetAccounts -> "/v1/accounts"
            is UpdateAccount -> "/v1/accounts/${this.accountId}"
            is GetAccountWith -> "/v1/accounts/${this.accountId}"
            is DeleteAccountWith -> "/v1/accounts/${this.accountId}"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateAccount -> "POST"
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
                items
            }
            else -> null
        }
}
