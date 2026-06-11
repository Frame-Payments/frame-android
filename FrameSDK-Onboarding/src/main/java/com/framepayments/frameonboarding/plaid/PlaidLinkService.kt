package com.framepayments.frameonboarding.plaid

import com.framepayments.framesdk.accounts.AccountsAPI
import com.framepayments.framesdk.paymentmethods.PaymentMethodRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Outcome of a Plaid Link bank account connection flow. */
sealed class PlaidLinkResult {
    /** The bank account was successfully connected and a PaymentMethod was created. */
    data class Success(
        /** The PaymentMethod created for the connected bank account. */
        val paymentMethod: FrameObjects.PaymentMethod
    ) : PlaidLinkResult()
    /** The connection failed due to a networking or API error. */
    data class Failure(
        /** The underlying error, or null if the cause is unknown. */
        val error: NetworkingError?
    ) : PlaidLinkResult()
    /** The customer dismissed the Plaid Link UI without completing. */
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
class PlaidLinkService(
    /** The Frame account ID that will own the connected bank account. */
    val accountId: String
) {

    private val _linkToken = MutableStateFlow<String?>(null)
    val linkToken: StateFlow<String?> = _linkToken.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _result = MutableStateFlow<PlaidLinkResult?>(null)
    val result: StateFlow<PlaidLinkResult?> = _result.asStateFlow()

    /** Fetches a Plaid Link token from the Frame API and stores it in [linkToken]. No-op if already loading or a token exists. */
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

    /** Clears the stored link token so a [LaunchedEffect] won't re-fire after the Plaid UI is launched. */
    fun clearLinkToken() {
        _linkToken.value = null
    }

    /** Exchanges the Plaid public token for a Frame PaymentMethod and stores the result in [result]. */
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

    /** Records a [PlaidLinkResult.Dismissed] result when the customer cancels the Plaid Link UI. */
    fun onDismissed() {
        _isConnecting.value = false
        _result.value = PlaidLinkResult.Dismissed
    }

    /** Clears [result] so the UI can reset state after handling the outcome. */
    fun clearResult() {
        _result.value = null
    }
}
