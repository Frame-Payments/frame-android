package com.framepayments.framesdk.accounts

import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.managers.SiftManager

/** Provides coroutine and callback-based operations for managing accounts. */
object AccountsAPI {
    // MARK: Methods using coroutines

    /**
     * Creates a new account and emits a Sift login event on success.
     *
     * @param request The account creation payload.
     * @return A pair of the created [AccountObjects.Account] and any [NetworkingError].
     */
    suspend fun createAccount(request: AccountRequests.CreateAccountRequest): Pair<AccountObjects.Account?, NetworkingError?> {
        val endpoint = AccountEndpoints.CreateAccount
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret)
        val decodedResponse = data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }
        decodedResponse?.let {
            val email = it.profile?.individual?.email ?: it.profile?.business?.email ?: ""
            SiftManager.collectLoginEvent(customerId = it.id ?: "", email = email)
        }
        return Pair(decodedResponse, error)
    }

    /**
     * Updates an existing account.
     *
     * @param accountId The ID of the account to update.
     * @param request The fields to update.
     * @return A pair of the updated [AccountObjects.Account] and any [NetworkingError].
     */
    suspend fun updateAccount(accountId: String, request: AccountRequests.UpdateAccountRequest): Pair<AccountObjects.Account?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = AccountEndpoints.UpdateAccount(accountId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }, error)
    }

    /**
     * Retrieves a paginated list of accounts, optionally filtered.
     *
     * @param status Filter by account status.
     * @param type Filter by account type.
     * @param externalId Filter by the merchant-assigned external ID.
     * @param includeDisabled When `true`, includes disabled accounts in the results.
     * @param page Page number to retrieve.
     * @param perPage Number of results per page.
     * @return A pair of the [AccountResponses.ListAccountsResponse] and any [NetworkingError].
     */
    suspend fun getAccounts(
        status: AccountObjects.AccountStatus? = null,
        type: AccountObjects.AccountType? = null,
        externalId: String? = null,
        includeDisabled: Boolean = false,
        page: Int? = null,
        perPage: Int? = null
    ): Pair<AccountResponses.ListAccountsResponse?, NetworkingError?> {
        val endpoint = AccountEndpoints.GetAccounts(status = status, type = type, externalId = externalId, includeDisabled = includeDisabled, page = page, perPage = perPage)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<AccountResponses.ListAccountsResponse>(it) }, error)
    }

    /**
     * Retrieves a single account by ID and emits a Sift login event on success.
     *
     * @param accountId The ID of the account to retrieve.
     * @param forTesting When `true`, suppresses the Sift login event.
     * @return A pair of the [AccountObjects.Account] and any [NetworkingError].
     */
    suspend fun getAccountWith(accountId: String, forTesting: Boolean = false): Pair<AccountObjects.Account?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = AccountEndpoints.GetAccountWith(accountId)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        val decodedResponse = data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }
        if (!forTesting) {
            decodedResponse?.let {
                val email = it.profile?.individual?.email ?: it.profile?.business?.email ?: ""
                SiftManager.collectLoginEvent(customerId = it.id ?: "", email = email)
            }
        }
        return Pair(decodedResponse, error)
    }

    /**
     * Deletes an account by ID.
     *
     * @param accountId The ID of the account to delete.
     * @return A pair of the deleted [AccountObjects.Account] and any [NetworkingError].
     */
    suspend fun deleteAccountWith(accountId: String): Pair<AccountObjects.Account?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = AccountEndpoints.DeleteAccountWith(accountId)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }, error)
    }

    /**
     * Searches for accounts matching the given criteria.
     *
     * @param email Filter by email address.
     * @param externalId Filter by merchant-assigned external ID.
     * @param type Filter by account type.
     * @param status Filter by account status.
     * @param createdBefore ISO-8601 timestamp; return accounts created before this time.
     * @param createdAfter ISO-8601 timestamp; return accounts created after this time.
     * @return A pair of the [AccountResponses.SearchAccountsResponse] and any [NetworkingError].
     */
    suspend fun searchAccounts(
        email: String? = null,
        externalId: String? = null,
        type: AccountObjects.AccountType? = null,
        status: AccountObjects.AccountStatus? = null,
        createdBefore: String? = null,
        createdAfter: String? = null
    ): Pair<AccountResponses.SearchAccountsResponse?, NetworkingError?> {
        val endpoint = AccountEndpoints.SearchAccounts(
            email = email, externalId = externalId, type = type,
            status = status, createdBefore = createdBefore, createdAfter = createdAfter
        )
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<AccountResponses.SearchAccountsResponse>(it) }, error)
    }

    /**
     * Restricts an account, preventing it from processing payments.
     *
     * @param accountId The ID of the account to restrict.
     * @return A pair of the updated [AccountObjects.Account] and any [NetworkingError].
     */
    suspend fun restrictAccount(accountId: String): Pair<AccountObjects.Account?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = AccountEndpoints.RestrictAccount(accountId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null), FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }, error)
    }

    /**
     * Removes a restriction from an account.
     *
     * @param accountId The ID of the account to unrestrict.
     * @return A pair of the updated [AccountObjects.Account] and any [NetworkingError].
     */
    suspend fun unrestrictAccount(accountId: String): Pair<AccountObjects.Account?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = AccountEndpoints.UnrestrictAccount(accountId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null), FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }, error)
    }

    /**
     * Initiates a phone verification for an account.
     *
     * @param accountId The ID of the account whose phone number to verify.
     * @return A pair of the [AccountObjects.PhoneVerification] and any [NetworkingError].
     */
    suspend fun createPhoneVerification(accountId: String): Pair<AccountObjects.PhoneVerification?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = AccountEndpoints.CreatePhoneVerification(accountId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null), FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<AccountObjects.PhoneVerification>(it) }, error)
    }

    /**
     * Confirms a phone verification using the code sent to the account holder.
     *
     * @param accountId The ID of the account.
     * @param verificationId The ID of the pending phone verification.
     * @param request The confirmation payload containing the one-time code.
     * @return A pair of the confirmed [AccountObjects.PhoneVerification] and any [NetworkingError].
     */
    suspend fun confirmPhoneVerification(accountId: String, verificationId: String, request: AccountRequests.ConfirmPhoneVerificationRequest): Pair<AccountObjects.PhoneVerification?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = AccountEndpoints.ConfirmPhoneVerification(accountId, verificationId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<AccountObjects.PhoneVerification>(it) }, error)
    }

    /**
     * Retrieves a Plaid Link token for the given account to initiate a bank-account connection flow.
     *
     * @param accountId The ID of the account for which to generate the token.
     * @return A pair of the [AccountResponses.PlaidLinkTokenResponse] and any [NetworkingError].
     */
    suspend fun getPlaidLinkToken(accountId: String): Pair<AccountResponses.PlaidLinkTokenResponse?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = AccountEndpoints.GetPlaidLinkToken(accountId, androidPackageName = "com.framepayments.frame")
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<AccountResponses.PlaidLinkTokenResponse>(it) }, error)
    }

    // MARK: Methods using callbacks

    /**
     * Creates a new account and emits a Sift login event on success.
     *
     * @param request The account creation payload.
     * @param completionHandler Called with the created [AccountObjects.Account] or a [NetworkingError].
     */
    fun createAccount(request: AccountRequests.CreateAccountRequest, completionHandler: (AccountObjects.Account?, NetworkingError?) -> Unit) {
        val endpoint = AccountEndpoints.CreateAccount
        FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret) { data, error ->
            val decodedResponse = data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }
            decodedResponse?.let {
                val email = it.profile?.individual?.email ?: it.profile?.business?.email ?: ""
                SiftManager.collectLoginEvent(customerId = it.id ?: "", email = email)
            }
            completionHandler(decodedResponse, error)
        }
    }

    /**
     * Updates an existing account.
     *
     * @param accountId The ID of the account to update.
     * @param request The fields to update.
     * @param completionHandler Called with the updated [AccountObjects.Account] or a [NetworkingError].
     */
    fun updateAccount(accountId: String, request: AccountRequests.UpdateAccountRequest, completionHandler: (AccountObjects.Account?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = AccountEndpoints.UpdateAccount(accountId)
        FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }, error)
        }
    }

    /**
     * Retrieves a paginated list of accounts, optionally filtered.
     *
     * @param status Filter by account status.
     * @param type Filter by account type.
     * @param externalId Filter by the merchant-assigned external ID.
     * @param includeDisabled When `true`, includes disabled accounts in the results.
     * @param page Page number to retrieve.
     * @param perPage Number of results per page.
     * @param completionHandler Called with the [AccountResponses.ListAccountsResponse] or a [NetworkingError].
     */
    fun getAccounts(
        status: AccountObjects.AccountStatus? = null,
        type: AccountObjects.AccountType? = null,
        externalId: String? = null,
        includeDisabled: Boolean = false,
        page: Int? = null,
        perPage: Int? = null,
        completionHandler: (AccountResponses.ListAccountsResponse?, NetworkingError?) -> Unit
    ) {
        val endpoint = AccountEndpoints.GetAccounts(status = status, type = type, externalId = externalId, includeDisabled = includeDisabled, page = page, perPage = perPage)
        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<AccountResponses.ListAccountsResponse>(it) }, error)
        }
    }

    /**
     * Retrieves a single account by ID and emits a Sift login event on success.
     *
     * @param accountId The ID of the account to retrieve.
     * @param completionHandler Called with the [AccountObjects.Account] or a [NetworkingError].
     */
    fun getAccountWith(accountId: String, completionHandler: (AccountObjects.Account?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = AccountEndpoints.GetAccountWith(accountId)
        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            val decodedResponse = data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }
            decodedResponse?.let {
                val email = it.profile?.individual?.email ?: it.profile?.business?.email ?: ""
                SiftManager.collectLoginEvent(customerId = it.id ?: "", email = email)
            }
            completionHandler(decodedResponse, error)
        }
    }

    /**
     * Deletes an account by ID.
     *
     * @param accountId The ID of the account to delete.
     * @param completionHandler Called with the deleted [AccountObjects.Account] or a [NetworkingError].
     */
    fun deleteAccountWith(accountId: String, completionHandler: (AccountObjects.Account?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = AccountEndpoints.DeleteAccountWith(accountId)
        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }, error)
        }
    }

    /**
     * Searches for accounts matching the given criteria.
     *
     * @param email Filter by email address.
     * @param externalId Filter by merchant-assigned external ID.
     * @param type Filter by account type.
     * @param status Filter by account status.
     * @param createdBefore ISO-8601 timestamp; return accounts created before this time.
     * @param createdAfter ISO-8601 timestamp; return accounts created after this time.
     * @param completionHandler Called with the [AccountResponses.SearchAccountsResponse] or a [NetworkingError].
     */
    fun searchAccounts(
        email: String? = null,
        externalId: String? = null,
        type: AccountObjects.AccountType? = null,
        status: AccountObjects.AccountStatus? = null,
        createdBefore: String? = null,
        createdAfter: String? = null,
        completionHandler: (AccountResponses.SearchAccountsResponse?, NetworkingError?) -> Unit
    ) {
        val endpoint = AccountEndpoints.SearchAccounts(
            email = email, externalId = externalId, type = type,
            status = status, createdBefore = createdBefore, createdAfter = createdAfter
        )
        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<AccountResponses.SearchAccountsResponse>(it) }, error)
        }
    }

    /**
     * Restricts an account, preventing it from processing payments.
     *
     * @param accountId The ID of the account to restrict.
     * @param completionHandler Called with the updated [AccountObjects.Account] or a [NetworkingError].
     */
    fun restrictAccount(accountId: String, completionHandler: (AccountObjects.Account?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = AccountEndpoints.RestrictAccount(accountId)
        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null), FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }, error)
        }
    }

    /**
     * Removes a restriction from an account.
     *
     * @param accountId The ID of the account to unrestrict.
     * @param completionHandler Called with the updated [AccountObjects.Account] or a [NetworkingError].
     */
    fun unrestrictAccount(accountId: String, completionHandler: (AccountObjects.Account?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = AccountEndpoints.UnrestrictAccount(accountId)
        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null), FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<AccountObjects.Account>(it) }, error)
        }
    }

    /**
     * Initiates a phone verification for an account.
     *
     * @param accountId The ID of the account whose phone number to verify.
     * @param completionHandler Called with the [AccountObjects.PhoneVerification] or a [NetworkingError].
     */
    fun createPhoneVerification(accountId: String, completionHandler: (AccountObjects.PhoneVerification?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = AccountEndpoints.CreatePhoneVerification(accountId)
        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null), FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<AccountObjects.PhoneVerification>(it) }, error)
        }
    }

    /**
     * Confirms a phone verification using the code sent to the account holder.
     *
     * @param accountId The ID of the account.
     * @param verificationId The ID of the pending phone verification.
     * @param request The confirmation payload containing the one-time code.
     * @param completionHandler Called with the confirmed [AccountObjects.PhoneVerification] or a [NetworkingError].
     */
    fun confirmPhoneVerification(accountId: String, verificationId: String, request: AccountRequests.ConfirmPhoneVerificationRequest, completionHandler: (AccountObjects.PhoneVerification?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = AccountEndpoints.ConfirmPhoneVerification(accountId, verificationId)
        FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<AccountObjects.PhoneVerification>(it) }, error)
        }
    }

    /**
     * Retrieves a Plaid Link token for the given account to initiate a bank-account connection flow.
     *
     * @param accountId The ID of the account for which to generate the token.
     * @param completionHandler Called with the [AccountResponses.PlaidLinkTokenResponse] or a [NetworkingError].
     */
    fun getPlaidLinkToken(accountId: String, completionHandler: (AccountResponses.PlaidLinkTokenResponse?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = AccountEndpoints.GetPlaidLinkToken(accountId, androidPackageName = "com.framepayments.frame")
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<AccountResponses.PlaidLinkTokenResponse>(it) }, error)
        }
    }
}
