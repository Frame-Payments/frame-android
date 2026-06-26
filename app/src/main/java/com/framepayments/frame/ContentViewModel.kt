package com.framepayments.frame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.framepayments.frameonboarding.plaid.PlaidLinkResult
import com.framepayments.frameonboarding.plaid.PlaidLinkService
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.accounts.AccountsAPI
import com.framepayments.framesdk.chargeintents.ChargeIntent
import com.framepayments.framesdk.chargeintents.ChargeIntentAPI
import com.framepayments.framesdk.customers.CustomersAPI
import com.framepayments.framesdk.onboardingsessions.OnboardingSessionRequests
import com.framepayments.framesdk.onboardingsessions.OnboardingSessionsAPI
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
import com.framepayments.framesdk.refunds.Refund
import com.framepayments.framesdk.refunds.RefundsAPI
import com.framepayments.framesdk.subscriptions.Subscription
import com.framepayments.framesdk.subscriptions.SubscriptionsAPI
import com.framepayments.framesdk.subscriptionphases.SubscriptionPhase
import com.framepayments.framesdk.subscriptionphases.SubscriptionPhasesAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContentUiState(
    val customers: List<FrameObjects.Customer> = emptyList(),
    val paymentMethods: List<FrameObjects.PaymentMethod> = emptyList(),
    val subscriptions: List<Subscription> = emptyList(),
    val chargeIntents: List<ChargeIntent> = emptyList(),
    val refunds: List<Refund> = emptyList(),
    val subscriptionPhases: List<SubscriptionPhase> = emptyList(),
)

data class PlaidMessage(val title: String, val body: String)

class ContentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ContentUiState())
    val uiState: StateFlow<ContentUiState> = _uiState.asStateFlow()

    private val _plaidService = MutableStateFlow<PlaidLinkService?>(null)
    val plaidService: StateFlow<PlaidLinkService?> = _plaidService.asStateFlow()

    private val _plaidMessage = MutableStateFlow<PlaidMessage?>(null)
    val plaidMessage: StateFlow<PlaidMessage?> = _plaidMessage.asStateFlow()

    /**
     * The onboarding-session token (`onb_sess_…`) minted for the demo flow, or `null` until one is
     * minted (or if minting failed). Passed to `OnboardingConfig.clientSecret` so the flow is scoped
     * to a single account.
     */
    private val _onboardingClientSecret = MutableStateFlow<String?>(null)
    val onboardingClientSecret: StateFlow<String?> = _onboardingClientSecret.asStateFlow()

    init {
        viewModelScope.launch {
            loadCustomers()
            loadPaymentMethods()
            loadSubscriptions()
            loadChargeIntents()
            loadRefunds()
            loadSubscriptionPhases()
        }
    }

    private suspend fun loadCustomers() {
        val (response, _) = CustomersAPI.getCustomers()
        _uiState.value = _uiState.value.copy(customers = response?.data ?: emptyList())
    }

    private suspend fun loadPaymentMethods() {
        val (response, _) = PaymentMethodsAPI.getPaymentMethods()
        _uiState.value = _uiState.value.copy(paymentMethods = response?.data ?: emptyList())
    }

    private suspend fun loadSubscriptions() {
        val (response, _) = SubscriptionsAPI.getSubscriptions(perPage = 50, page = 1)
        _uiState.value = _uiState.value.copy(subscriptions = response?.data ?: emptyList())
    }

    private suspend fun loadChargeIntents() {
        val (response, _) = ChargeIntentAPI.getAllChargeIntents(perPage = 50, page = 1)
        _uiState.value = _uiState.value.copy(chargeIntents = response?.data ?: emptyList())
    }

    private suspend fun loadRefunds() {
        val (response, _) = RefundsAPI.getRefunds(chargeId = null, chargeIntentId = null, perPage = 50, page = 1)
        _uiState.value = _uiState.value.copy(refunds = response?.data ?: emptyList())
    }

    /**
     * Demo/testing only: mints an onboarding-session token (`onb_sess_…`) for the first available
     * account so the example app can exercise the onboarding flow end-to-end.
     *
     * This is **not** the production path. Creating an onboarding session is a server-only operation
     * that requires your secret key (`sk_`). Production integrations mint the token from their
     * backend (`POST /v1/onboarding_sessions`) and pass it to `OnboardingConfig.clientSecret`. The
     * example app does it inline only because it is configured with an `sk_`.
     */
    @Suppress("DEPRECATION")
    fun mintOnboardingClientSecret() {
        viewModelScope.launch {
            val (accountsResponse, _) = AccountsAPI.getAccounts(perPage = 1, page = 1)
            val accountId = accountsResponse?.data?.firstOrNull()?.id
            if (accountId == null) {
                _onboardingClientSecret.value = null
                return@launch
            }
            val request = OnboardingSessionRequests.CreateOnboardingSessionRequest(
                accountId = accountId,
                steps = listOf(
                    OnboardingSessionRequests.OnboardingSessionStep.ID_VERIFICATION,
                    OnboardingSessionRequests.OnboardingSessionStep.GEO_COMPLIANCE,
                    OnboardingSessionRequests.OnboardingSessionStep.PAYMENT_METHOD,
                )
            )
            val (session, _) = OnboardingSessionsAPI.createOnboardingSession(request)
            _onboardingClientSecret.value = session?.clientSecret
        }
    }

    fun startPlaidLink() {
        if (_plaidService.value?.isConnecting?.value == true) return
        viewModelScope.launch {
            val (accountsResponse, accountsErr) = AccountsAPI.getAccounts(perPage = 1, page = 1)
            val account = accountsResponse?.data?.firstOrNull()
            val accountId = account?.id
            if (account == null || accountId == null) {
                _plaidMessage.value = PlaidMessage(
                    title = "Plaid",
                    body = "No accounts found (${accountsErr ?: "empty list"})"
                )
                return@launch
            }
            val service = PlaidLinkService(accountId)
            _plaidService.value = service
            service.fetchLinkToken()
            if (service.linkToken.value == null) {
                _plaidMessage.value = PlaidMessage(
                    title = "Plaid",
                    body = "Failed to get Plaid token (${service.result.value})"
                )
                _plaidService.value = null
            }
        }
    }

    fun handlePlaidSuccess(
        publicToken: String,
        plaidAccountId: String,
        institutionName: String?,
        subtype: String?
    ) {
        val service = _plaidService.value ?: return
        viewModelScope.launch {
            service.connectBankAccount(
                publicToken = publicToken,
                plaidAccountId = plaidAccountId,
                institutionName = institutionName,
                subtype = subtype
            )
            when (val outcome = service.result.value) {
                is PlaidLinkResult.Success -> {
                    val pm = outcome.paymentMethod
                    val mask = pm.ach?.lastFour?.let { "••$it" }.orEmpty()
                    _plaidMessage.value = PlaidMessage(
                        title = "Bank account connected",
                        body = buildString {
                            append("Payment method saved to the Frame account.\n\n")
                            append("ID: ${pm.id}")
                            if (mask.isNotEmpty()) append("\nAccount: $mask")
                            institutionName?.let { append("\nBank: $it") }
                        }
                    )
                    loadPaymentMethods()
                }
                is PlaidLinkResult.Failure -> {
                    _plaidMessage.value = PlaidMessage(
                        title = "Plaid connection failed",
                        body = outcome.error?.toString() ?: "Unknown error"
                    )
                }
                else -> Unit
            }
            service.clearResult()
            _plaidService.value = null
        }
    }

    fun onPlaidDismissed() {
        val service = _plaidService.value
        service?.onDismissed()
        service?.clearResult()
        _plaidService.value = null
    }

    fun clearPlaidMessage() {
        _plaidMessage.value = null
    }

    private suspend fun loadSubscriptionPhases() {
        val (subResponse, _) = SubscriptionsAPI.getSubscriptions(perPage = 1, page = 1)
        val firstSub = subResponse?.data?.firstOrNull() ?: return
        val firstSubId = firstSub.id ?: return
        val (phaseResponse, _) = SubscriptionPhasesAPI.getSubscriptionPhases(firstSubId)
        _uiState.value = _uiState.value.copy(subscriptionPhases = phaseResponse?.phases ?: emptyList())
    }
}
