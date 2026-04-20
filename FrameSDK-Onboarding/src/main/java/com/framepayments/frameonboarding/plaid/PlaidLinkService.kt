package com.framepayments.frameonboarding.plaid

import com.framepayments.framesdk.accounts.AccountsAPI
import com.framepayments.framesdk.paymentmethods.PaymentMethodRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PlaidLinkResult {
    data class Success(val paymentMethod: FrameObjects.PaymentMethod) : PlaidLinkResult()
    data class Failure(val error: NetworkingError?) : PlaidLinkResult()
    data object Dismissed : PlaidLinkResult()
}

/**
 * Reusable service that manages the Plaid Link token lifecycle and bank account connection.
 * Follows the same pattern as ProveAuthService — owns the state, callers react to it.
 *
 * Usage:
 *  1. Call [fetchLinkToken] to begin. Observe [linkToken] — when non-null, launch Plaid Link UI.
 *  2. After launch, call [clearLinkToken] so the LaunchedEffect won't re-fire.
 *  3. On Plaid success, call [connectBankAccount]. On dismiss/cancel, call [onDismissed].
 *  4. Observe [isConnecting] for loading state and [result] for final outcome.
 */
class PlaidLinkService(val accountId: String) {

    private val _linkToken = MutableStateFlow<String?>(null)
    val linkToken: StateFlow<String?> = _linkToken.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _result = MutableStateFlow<PlaidLinkResult?>(null)
    val result: StateFlow<PlaidLinkResult?> = _result.asStateFlow()

    suspend fun fetchLinkToken() {
        if (_isConnecting.value || _linkToken.value != null) return
        _isConnecting.value = true
        val (response, err) = AccountsAPI.getPlaidLinkToken(accountId)
        _isConnecting.value = false
        if (response?.linkToken != null) {
            _linkToken.value = response.linkToken
        } else {
            android.util.Log.w("PlaidLinkService", "Failed to fetch link_token: $err")
            _result.value = PlaidLinkResult.Failure(err)
        }
    }

    fun clearLinkToken() {
        _linkToken.value = null
    }

    suspend fun connectBankAccount(
        publicToken: String,
        plaidAccountId: String,
        institutionName: String?,
        subtype: String?
    ) {
        _isConnecting.value = true
        val request = PaymentMethodRequests.ConnectPlaidBankAccountRequest(
            account = accountId,
            publicToken = publicToken,
            accountId = plaidAccountId,
            institutionName = institutionName,
            subtype = subtype
        )
        val (paymentMethod, err) = PaymentMethodsAPI.connectPlaidBankAccount(request)
        _isConnecting.value = false
        _result.value = if (paymentMethod != null) {
            PlaidLinkResult.Success(paymentMethod)
        } else {
            PlaidLinkResult.Failure(err)
        }
    }

    fun onDismissed() {
        _isConnecting.value = false
        _result.value = PlaidLinkResult.Dismissed
    }

    fun clearResult() {
        _result.value = null
    }
}
