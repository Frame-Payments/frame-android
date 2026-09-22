package com.framepayments.frame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.framepayments.frameonboarding.plaid.PlaidLinkResult
import com.framepayments.frameonboarding.plaid.PlaidLinkService
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.accounts.AccountObjects
import com.framepayments.framesdk.accounts.AccountRequests
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

/**
 * State of the demo onboarding-session mint flow. The example app cannot launch onboarding until a
 * token is minted, so the UI gates on this: [Loading] shows a spinner, [Ready] launches the flow,
 * and [Error] shows an actionable retry instead of spinning forever when minting fails.
 */
sealed class OnboardingMintState {
    /** No mint in progress; onboarding not launched. */
    object Idle : OnboardingMintState()

    /** A mint is in flight; show a spinner. */
    object Loading : OnboardingMintState()

    /**
     * A token was minted; [clientSecret] is the `onb_sess_…` to launch the flow with, scoped to
     * [accountId]. Onboarding must be launched with this same [accountId], or it creates a new
     * account the session was never scoped to and every request after that gets PII-gated.
     */
    data class Ready(val clientSecret: String, val accountId: String) : OnboardingMintState()

    /** Minting failed; [message] explains why so the UI can offer a retry. */
    data class Error(val message: String) : OnboardingMintState()
}

class ContentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ContentUiState())
    val uiState: StateFlow<ContentUiState> = _uiState.asStateFlow()

    private val _plaidService = MutableStateFlow<PlaidLinkService?>(null)
    val plaidService: StateFlow<PlaidLinkService?> = _plaidService.asStateFlow()

    private val _plaidMessage = MutableStateFlow<PlaidMessage?>(null)
    val plaidMessage: StateFlow<PlaidMessage?> = _plaidMessage.asStateFlow()

    /**
     * State of the demo onboarding-session mint flow. The minted token (`onb_sess_…`) is exposed via
     * [OnboardingMintState.Ready] and passed to `OnboardingConfig.clientSecret`, scoping the flow to a
     * single account. Starts [OnboardingMintState.Idle].
     */
    private val _onboardingMintState = MutableStateFlow<OnboardingMintState>(OnboardingMintState.Idle)
    val onboardingMintState: StateFlow<OnboardingMintState> = _onboardingMintState.asStateFlow()

    /**
     * The account every demo entry point acts on, mirroring the iOS example app's single
     * `viewModel.accountId`: onboarding, the standalone entry-point demos (add payment method,
     * add payout method, select payout method), Plaid, and Google Pay all read and write this
     * one value instead of each resolving their own account independently.
     *
     * Seeded from [FrameNetworking.accountId] — the accountId passed to
     * `initializeWithAPIKey`, if the host configured one — and otherwise starts blank, in which
     * case onboarding a new applicant fills it in.
     */
    private val _accountId = MutableStateFlow(FrameNetworking.accountId.orEmpty())
    val accountId: StateFlow<String> = _accountId.asStateFlow()

    fun setAccountId(accountId: String) {
        _accountId.value = accountId
    }

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
     * Demo/testing only: mints an onboarding-session token (`onb_sess_…`) so the example app can
     * exercise the onboarding flow end-to-end. Mirrors the iOS example app: a valid [accountId]
     * resumes that account, otherwise (blank, or not a real account) a new individual account is
     * created first — never a random pre-existing one.
     *
     * This is **not** the production path. Creating an onboarding session is a server-only operation
     * that requires your secret key (`sk_`). Production integrations mint the token from their
     * backend (`POST /v1/onboarding_sessions`) and pass it to `OnboardingConfig.clientSecret`. The
     * example app does it inline only because it is configured with an `sk_`.
     */
    @Suppress("DEPRECATION")
    fun mintOnboardingClientSecret(accountIdInput: String?) {
        _onboardingMintState.value = OnboardingMintState.Loading
        viewModelScope.launch {
            val resolvedAccountId = accountIdInput?.takeIf { it.isNotBlank() } ?: run {
                val (account, err) = createEmptyIndividualAccount()
                account?.id ?: run {
                    _onboardingMintState.value = OnboardingMintState.Error(
                        err?.let { "Couldn't create an account to onboard: $it" }
                            ?: "Account creation did not return an account id."
                    )
                    return@launch
                }
            }
            val request = OnboardingSessionRequests.CreateOnboardingSessionRequest(
                accountId = resolvedAccountId,
                steps = listOf(
                    OnboardingSessionRequests.OnboardingSessionStep.ID_VERIFICATION,
                    OnboardingSessionRequests.OnboardingSessionStep.GEO_COMPLIANCE,
                    OnboardingSessionRequests.OnboardingSessionStep.PAYMENT_METHOD,
                )
            )
            val (session, sessionError) = OnboardingSessionsAPI.createOnboardingSession(request)
            val clientSecret = session?.clientSecret
            _onboardingMintState.value = if (clientSecret != null) {
                _accountId.value = resolvedAccountId
                OnboardingMintState.Ready(clientSecret, resolvedAccountId)
            } else {
                OnboardingMintState.Error(
                    sessionError?.let { "Couldn't mint an onboarding session: $it" }
                        ?: "Onboarding session response did not include a client secret."
                )
            }
        }
    }

    /** Creates a blank individual account for the onboarding demo to fill in from scratch. */
    private suspend fun createEmptyIndividualAccount(): Pair<AccountObjects.Account?, NetworkingError?> {
        val request = AccountRequests.CreateAccountRequest(
            type = AccountObjects.AccountType.INDIVIDUAL,
            profile = AccountRequests.CreateAccountProfile(
                individual = AccountRequests.CreateIndividualAccount(email = "newaccount@example.com")
            )
        )
        return AccountsAPI.createAccount(request)
    }

    /** Resets the mint flow to [OnboardingMintState.Idle] so the next launch mints a fresh token. */
    fun clearOnboardingClientSecret() {
        _onboardingMintState.value = OnboardingMintState.Idle
    }

    fun startPlaidLink() {
        if (_plaidService.value?.isConnecting?.value == true) return
        val accountId = _accountId.value.takeIf { it.isNotBlank() } ?: run {
            _plaidMessage.value = PlaidMessage(
                title = "Plaid",
                body = "No account set. Onboard or enter an account ID first."
            )
            return
        }
        viewModelScope.launch {
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
