package com.framepayments.framesdk.accounts

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.managers.SiftManager

object AccountsAPI {
    // MARK: Methods using coroutines
    suspend fun createAccount(request: AccountRequests.CreateAccountRequest): Pair<AccountObjects.Account?, NetworkingError?> {
        val endpoint = AccountEndpoints.CreateAccount
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        val decodedResponse = data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }
        decodedResponse?.let {
            val email = it.profile?.individual?.email ?: it.profile?.business?.email ?: ""
            SiftManager.collectLoginEvent(customerId = it.id, email = email)
        }
        return Pair(decodedResponse, error)
    }

    suspend fun updateAccount(accountId: String, request: AccountRequests.UpdateAccountRequest): Pair<AccountObjects.Account?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = AccountEndpoints.UpdateAccount(accountId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }, error)
    }

    suspend fun getAccounts(
        status: AccountObjects.AccountStatus? = null,
        type: AccountObjects.AccountType? = null,
        externalId: String? = null,
        includeDisabled: Boolean = false
    ): Pair<AccountResponses.ListAccountsResponse?, NetworkingError?> {
        val endpoint = AccountEndpoints.GetAccounts(status = status, type = type, externalId = externalId, includeDisabled = includeDisabled)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<AccountResponses.ListAccountsResponse>(it) }, error)
    }

    suspend fun getAccountWith(accountId: String, forTesting: Boolean = false): Pair<AccountObjects.Account?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = AccountEndpoints.GetAccountWith(accountId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        val decodedResponse = data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }
        if (!forTesting) {
            decodedResponse?.let {
                val email = it.profile?.individual?.email ?: it.profile?.business?.email ?: ""
                SiftManager.collectLoginEvent(customerId = it.id, email = email)
            }
        }
        return Pair(decodedResponse, error)
    }

    suspend fun deleteAccountWith(accountId: String): Pair<AccountObjects.Account?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = AccountEndpoints.DeleteAccountWith(accountId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }, error)
    }

    // MARK: Methods using callbacks
    fun createAccount(request: AccountRequests.CreateAccountRequest, completionHandler: (AccountObjects.Account?, NetworkingError?) -> Unit) {
        val endpoint = AccountEndpoints.CreateAccount
        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            val decodedResponse = data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }
            decodedResponse?.let {
                val email = it.profile?.individual?.email ?: it.profile?.business?.email ?: ""
                SiftManager.collectLoginEvent(customerId = it.id, email = email)
            }
            completionHandler(decodedResponse, error)
        }
    }

    fun updateAccount(accountId: String, request: AccountRequests.UpdateAccountRequest, completionHandler: (AccountObjects.Account?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = AccountEndpoints.UpdateAccount(accountId)
        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }, error)
        }
    }

    fun getAccounts(
        status: AccountObjects.AccountStatus? = null,
        type: AccountObjects.AccountType? = null,
        externalId: String? = null,
        includeDisabled: Boolean = false,
        completionHandler: (AccountResponses.ListAccountsResponse?, NetworkingError?) -> Unit
    ) {
        val endpoint = AccountEndpoints.GetAccounts(status = status, type = type, externalId = externalId, includeDisabled = includeDisabled)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<AccountResponses.ListAccountsResponse>(it) }, error)
        }
    }

    fun getAccountWith(accountId: String, completionHandler: (AccountObjects.Account?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = AccountEndpoints.GetAccountWith(accountId)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            val decodedResponse = data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }
            decodedResponse?.let {
                val email = it.profile?.individual?.email ?: it.profile?.business?.email ?: ""
                SiftManager.collectLoginEvent(customerId = it.id, email = email)
            }
            completionHandler(decodedResponse, error)
        }
    }

    fun deleteAccountWith(accountId: String, completionHandler: (AccountObjects.Account?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = AccountEndpoints.DeleteAccountWith(accountId)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }, error)
        }
    }
}
