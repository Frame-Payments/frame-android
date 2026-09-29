package com.framepayments.frame

import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.framepayments.frameonboarding.networking.idv.IdvAPI
import com.framepayments.frameonboarding.persona.PersonaInquiry
import com.framepayments.frameonboarding.persona.PersonaVerificationResult
import com.framepayments.frameonboarding.persona.PersonaVerificationService
import com.framepayments.frameonboarding.plaid.PlaidLinkResult
import com.framepayments.frameonboarding.plaid.PlaidLinkService
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.accounts.AccountObjects
import com.framepayments.framesdk.accounts.AccountRequests
import com.framepayments.framesdk.accounts.AccountsAPI
import com.framepayments.framesdk.onboardingsessions.OnboardingSessionRequests
import com.framepayments.framesdk.onboardingsessions.OnboardingSessionsAPI
import com.withpersona.sdk2.inquiry.Inquiry
import com.withpersona.sdk2.inquiry.InquiryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DemoAlertMessage(val title: String, val body: String)

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

    private val _plaidService = MutableStateFlow<PlaidLinkService?>(null)
    val plaidService: StateFlow<PlaidLinkService?> = _plaidService.asStateFlow()

    private val _demoAlert = MutableStateFlow<DemoAlertMessage?>(null)
    val demoAlert: StateFlow<DemoAlertMessage?> = _demoAlert.asStateFlow()

    private val personaService = PersonaVerificationService()
    private var idvClientSecret: String? = null

    private val _personaInquiryToLaunch = MutableStateFlow<PersonaInquiry?>(null)
    /** Set when `POST /idv/session` returns an inquiry; the playground launches Persona against it. */
    val personaInquiryToLaunch: StateFlow<PersonaInquiry?> = _personaInquiryToLaunch.asStateFlow()

    private val _isVerifyingIdentity = MutableStateFlow(false)
    val isVerifyingIdentity: StateFlow<Boolean> = _isVerifyingIdentity.asStateFlow()

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
     * add payout method, select payout method), Plaid, Persona IDV, and Google Pay all read and
     * write this one value instead of each resolving their own account independently.
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
            when (val result = mintSession(
                accountIdInput = accountIdInput,
                steps = listOf(
                    OnboardingSessionRequests.OnboardingSessionStep.ID_VERIFICATION,
                    OnboardingSessionRequests.OnboardingSessionStep.GEO_COMPLIANCE,
                    OnboardingSessionRequests.OnboardingSessionStep.PAYMENT_METHOD,
                ),
            )) {
                is MintResult.Ok -> {
                    _accountId.value = result.session.accountId
                    _onboardingMintState.value = OnboardingMintState.Ready(
                        result.session.clientSecret,
                        result.session.accountId,
                    )
                }
                is MintResult.Err -> {
                    _onboardingMintState.value = OnboardingMintState.Error(result.message)
                }
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

    private data class MintedSession(val clientSecret: String, val accountId: String)

    private sealed class MintResult {
        data class Ok(val session: MintedSession) : MintResult()
        data class Err(val message: String) : MintResult()
    }

    @Suppress("DEPRECATION")
    private suspend fun mintSession(
        accountIdInput: String?,
        steps: List<OnboardingSessionRequests.OnboardingSessionStep>,
    ): MintResult {
        val resolvedAccountId = accountIdInput?.takeIf { it.isNotBlank() } ?: run {
            val (account, err) = createEmptyIndividualAccount()
            account?.id ?: return MintResult.Err(
                err?.let { "Couldn't create an account to onboard: $it" }
                    ?: "Account creation did not return an account id."
            )
        }
        val request = OnboardingSessionRequests.CreateOnboardingSessionRequest(
            accountId = resolvedAccountId,
            steps = steps,
        )
        val (session, sessionError) = OnboardingSessionsAPI.createOnboardingSession(request)
        val clientSecret = session?.clientSecret
            ?: return MintResult.Err(
                sessionError?.let { "Couldn't mint an onboarding session: $it" }
                    ?: "Onboarding session response did not include a client secret."
            )
        return MintResult.Ok(MintedSession(clientSecret, resolvedAccountId))
    }

    /** Resets the mint flow to [OnboardingMintState.Idle] so the next launch mints a fresh token. */
    fun clearOnboardingClientSecret() {
        _onboardingMintState.value = OnboardingMintState.Idle
    }

    fun startPlaidLink() {
        if (_plaidService.value?.isConnecting?.value == true) return
        val accountId = _accountId.value.takeIf { it.isNotBlank() } ?: run {
            _demoAlert.value = DemoAlertMessage(
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
                _demoAlert.value = DemoAlertMessage(
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
                    _demoAlert.value = DemoAlertMessage(
                        title = "Bank account connected",
                        body = buildString {
                            append("Payment method saved to the Frame account.\n\n")
                            append("ID: ${pm.id}")
                            if (mask.isNotEmpty()) append("\nAccount: $mask")
                            institutionName?.let { append("\nBank: $it") }
                        }
                    )
                }
                is PlaidLinkResult.Failure -> {
                    _demoAlert.value = DemoAlertMessage(
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

    /**
     * Demo/testing only: mints an onboarding session, creates a Persona inquiry via `/idv/session`,
     * and publishes it on [personaInquiryToLaunch] so the playground can open the Persona SDK.
     * Requires a configured account id (same gate as Plaid).
     */
    fun startIdentityVerification() {
        if (_isVerifyingIdentity.value) return
        val accountId = _accountId.value.takeIf { it.isNotBlank() } ?: run {
            _demoAlert.value = DemoAlertMessage(
                title = "Identity Verification",
                body = "No account set. Onboard or enter an account ID first."
            )
            return
        }
        _isVerifyingIdentity.value = true
        viewModelScope.launch {
            when (val minted = mintSession(
                accountIdInput = accountId,
                steps = listOf(OnboardingSessionRequests.OnboardingSessionStep.ID_VERIFICATION),
            )) {
                is MintResult.Err -> {
                    _isVerifyingIdentity.value = false
                    _demoAlert.value = DemoAlertMessage(
                        title = "Identity Verification",
                        body = minted.message,
                    )
                    return@launch
                }
                is MintResult.Ok -> {
                    idvClientSecret = minted.session.clientSecret
                    _accountId.value = minted.session.accountId

                    val (session, err) = IdvAPI.createSession(minted.session.clientSecret)
                    val inquiryId = session?.inquiryId?.takeIf { it.isNotBlank() }
                    if (inquiryId == null) {
                        _isVerifyingIdentity.value = false
                        _demoAlert.value = DemoAlertMessage(
                            title = "Identity Verification",
                            body = err?.toString() ?: "Couldn't create a Persona inquiry."
                        )
                        return@launch
                    }

                    // Already-approved inquiries are terminal — confirm and skip the Persona UI.
                    val (existing, existingErr) = IdvAPI.completeInquiry(
                        minted.session.clientSecret,
                        inquiryId,
                    )
                    if (existingErr == null && existing?.verified == true) {
                        _isVerifyingIdentity.value = false
                        _demoAlert.value = DemoAlertMessage(
                            title = "Identity Verification",
                            body = "Already verified.\n\nInquiry: $inquiryId"
                        )
                        return@launch
                    }

                    _personaInquiryToLaunch.value = PersonaInquiry(inquiryId, session?.sessionToken)
                }
            }
        }
    }

    /** Forwards the Persona ActivityResult callback into [personaService]. */
    fun onPersonaInquiryResult(response: InquiryResponse) {
        personaService.onInquiryResult(response)
    }

    /**
     * Launches Persona for the pending inquiry via the lifecycle-owned [launcher], then confirms
     * with `POST /idv/complete` (server is source of truth).
     */
    fun launchPendingPersonaInquiry(launcher: ActivityResultLauncher<Inquiry>) {
        val inquiry = _personaInquiryToLaunch.value ?: return
        _personaInquiryToLaunch.value = null
        val clientSecret = idvClientSecret ?: run {
            _isVerifyingIdentity.value = false
            _demoAlert.value = DemoAlertMessage(
                title = "Identity Verification",
                body = "Verification is unavailable for this session."
            )
            return
        }
        viewModelScope.launch {
            try {
                when (val outcome = personaService.awaitResult(
                    inquiry.inquiryId,
                    inquiry.sessionToken,
                    launcher,
                )) {
                    is PersonaVerificationResult.Completed -> {
                        val (complete, err) = IdvAPI.completeInquiry(clientSecret, outcome.inquiryId)
                        _demoAlert.value = when {
                            complete?.verified == true -> DemoAlertMessage(
                                title = "Identity Verification",
                                body = "Verified.\n\nInquiry: ${outcome.inquiryId}"
                            )
                            err != null -> DemoAlertMessage(
                                title = "Identity Verification",
                                body = "Complete failed: $err"
                            )
                            else -> DemoAlertMessage(
                                title = "Identity Verification",
                                body = buildString {
                                    append("Not verified.")
                                    complete?.status?.let { append("\nStatus: $it") }
                                    complete?.failureType?.let { append("\nFailure: $it") }
                                    append("\nInquiry: ${outcome.inquiryId}")
                                }
                            )
                        }
                    }
                    is PersonaVerificationResult.Cancelled -> {
                        _demoAlert.value = DemoAlertMessage(
                            title = "Identity Verification",
                            body = "Cancelled."
                        )
                    }
                    is PersonaVerificationResult.Failure -> {
                        _demoAlert.value = DemoAlertMessage(
                            title = "Identity Verification",
                            body = "Persona failed${outcome.message?.let { ": $it" } ?: "."}"
                        )
                    }
                }
            } finally {
                _isVerifyingIdentity.value = false
                idvClientSecret = null
                personaService.clearResult()
            }
        }
    }

    fun clearDemoAlert() {
        _demoAlert.value = null
    }
}
