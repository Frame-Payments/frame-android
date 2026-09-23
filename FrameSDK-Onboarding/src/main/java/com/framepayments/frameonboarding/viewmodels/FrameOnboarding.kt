package com.framepayments.frameonboarding.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.classes.BankAccountDraft
import com.framepayments.frameonboarding.classes.OnboardingData
import com.framepayments.frameonboarding.classes.PhoneCountrySelection
import com.framepayments.framesdk_ui.validation.Validators
import com.framepayments.frameonboarding.classes.OnboardingFlowSegment
import com.framepayments.frameonboarding.classes.OnboardingOutcome
import com.framepayments.frameonboarding.classes.OnboardingResult
import com.framepayments.frameonboarding.classes.OnboardingState
import com.framepayments.frameonboarding.classes.OnboardingStep
import com.evervault.sdk.input.model.card.PaymentCardData
import com.framepayments.frameonboarding.classes.PaymentCardDraft
import com.framepayments.frameonboarding.classes.PaymentMethodSummary
import com.framepayments.frameonboarding.classes.capabilitiesWithDependencies
import com.framepayments.frameonboarding.classes.computeFlowSegments
import com.framepayments.frameonboarding.classes.computeOrderedSteps
import com.framepayments.frameonboarding.classes.toFlowSegment
import com.framepayments.frameonboarding.networking.idv.IdvAPI
import com.framepayments.frameonboarding.networking.idv.IdvCompletionRouting
import com.framepayments.frameonboarding.networking.phoneotpverification.PhoneOTPVerificationAPI
import com.framepayments.frameonboarding.persona.PersonaInquiry
import com.framepayments.frameonboarding.persona.PersonaVerificationResult
import com.framepayments.frameonboarding.persona.PersonaVerificationService
import com.framepayments.frameonboarding.plaid.PlaidLinkResult
import com.framepayments.frameonboarding.plaid.PlaidLinkService
import com.framepayments.frameonboarding.prove.ProveAuthService
import com.framepayments.frameonboarding.prove.ProveAuthServiceError
import com.framepayments.framesdk.AddressSubregions
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.accountevents.AccountEventDetail
import com.framepayments.framesdk.accountevents.AccountEventEmitter
import com.framepayments.framesdk.accountevents.AccountEventName
import com.framepayments.framesdk.accountevents.AccountEventScreen
import com.framepayments.framesdk.accounts.AccountObjects
import com.framepayments.framesdk.accounts.AccountRequests
import com.framepayments.framesdk.accounts.AccountsAPI
import com.framepayments.framesdk.capabilities.CapabilitiesAPI
import com.framepayments.framesdk.capabilities.CapabilityRequests
import com.framepayments.framesdk.capabilities.CapabilityObjects.actionableRequirements
import com.framepayments.framesdk.capabilities.CapabilityObjects.capabilityStatus
import com.framepayments.framesdk.capabilities.CapabilityObjects.hasActionableRequirements
import com.framepayments.framesdk.capabilities.CapabilityObjects.isOutstanding
import com.framepayments.framesdk.customeridentity.CustomerIdentityAPI
import com.framepayments.framesdk.customeridentity.CustomerIdentityRequests
import com.framepayments.framesdk.onboardingsessions.OnboardingSessionRequests
import com.framepayments.framesdk.onboardingsessions.OnboardingSessionsAPI
import com.framepayments.framesdk.paymentmethods.PaymentMethodRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
import com.framepayments.framesdk.managers.SiftManager
import com.framepayments.framesdk.termsofservice.TermsOfServiceAPI
import com.framepayments.framesdk.threedsecure.ThreeDSecureRequests
import com.framepayments.framesdk.threedsecure.ThreeDSecureVerificationsAPI
import com.framepayments.framesdk.capabilities.CapabilityObjects
import com.framepayments.framesdk.customeridentity.CustomerIdentity
import com.framepayments.framesdk.threedsecure.ThreeDSecureVerification
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.time.Instant

internal enum class VerifyIdSubStep { PhoneAuth, VerifyPhone, InformationForm }

internal enum class OnboardingFieldGroup { PHONE_AUTH }

internal enum class OnboardingField(val group: OnboardingFieldGroup) {
    AUTH_PHONE(OnboardingFieldGroup.PHONE_AUTH),
    AUTH_BIRTH_MONTH(OnboardingFieldGroup.PHONE_AUTH),
    AUTH_BIRTH_DAY(OnboardingFieldGroup.PHONE_AUTH),
    AUTH_BIRTH_YEAR(OnboardingFieldGroup.PHONE_AUTH),
}

/** UI state for the phone verification step (Prove vs manual Frame confirm). */
internal sealed class VerifyPhoneUi {
    /** Prove SDK is running; show a spinner. */
    data object LoadingProve : VerifyPhoneUi()

    /** Prove OTP fallback: user-entered code is passed into the Prove SDK. */
    data object OtpForProve : VerifyPhoneUi()

    /** Confirm via Frame API with OTP (no Prove token or Prove flow failed). */
    data object OtpFrameApi : VerifyPhoneUi()
}

internal class FrameOnboardingViewModel(private val config: OnboardingConfig) : ViewModel() {

    private val _requiredCapabilities = MutableStateFlow(config.requiredCapabilities.toList())

    /** Mirrors iOS `OnboardingContainerViewModel.requiredCapabilities` (shrinks as capabilities complete). */
    val requiredCapabilities: StateFlow<List<Capabilities>> = _requiredCapabilities.asStateFlow()

    /**
     * What the host asked for, fixed for the life of the flow. Distinct from
     * [requiredCapabilities], which shrinks as capabilities are granted: a UI gate or validator
     * keyed off the shrinking list silently switches itself off mid-flow — that is what stopped
     * the date-of-birth field being collected once `kyc_prefill` was satisfied. Mirrors iOS
     * `originallyRequiredCapabilities`.
     */
    val originallyRequiredCapabilities: List<Capabilities> = config.requiredCapabilities.toList()

    /** Persona runs on Continue: the host required `idv`, or the backend stepped the account up. */
    val governmentIdRequired: Boolean
        get() = originallyRequiredCapabilities.contains(Capabilities.IDV) ||
            _onboardingData.value.identityDocumentRequired

    /** API string values for [_requiredCapabilities] (e.g. `"kyc_prefill"`). */
    private fun requiredCapabilityApiStrings(): List<String> =
        _requiredCapabilities.value.map { it.apiValue }

    private var _orderedSteps: List<OnboardingStep> = run {
        var steps = computeOrderedSteps(_requiredCapabilities.value, originallyRequiredCapabilities)
        if (!config.showIntroScreen) steps = steps.filter { it != OnboardingStep.VerificationWelcome }
        if (!config.showCompletionScreen) steps = steps.filter { it != OnboardingStep.VerificationSubmitted }
        steps
    }
    private var _flowSegments: List<OnboardingFlowSegment> = computeFlowSegments(
        _requiredCapabilities.value,
        originallyRequiredCapabilities
    )

    val orderedSteps: List<OnboardingStep> get() = _orderedSteps
    val flowSegments: List<OnboardingFlowSegment> get() = _flowSegments

    val navigationState = OnboardingState(_orderedSteps.first())

    private val _isExistingAccountReady = MutableStateFlow(
        config.accountId == null || config.skipInitNetwork
    )
    /** False until a pre-existing account has been fetched, so intro Continue cannot race the load. */
    val isExistingAccountReady: StateFlow<Boolean> = _isExistingAccountReady.asStateFlow()

    private val _onlyAddressVerification = MutableStateFlow(false)
    /** Saved-card path: collect billing address only (do not create a new card). */
    val onlyAddressVerification: StateFlow<Boolean> = _onlyAddressVerification.asStateFlow()

    // Onboarding data
    private val _onboardingData = MutableStateFlow(OnboardingData())
    val onboardingData: StateFlow<OnboardingData> = _onboardingData.asStateFlow()

    // Resolved account ID (may start null if config has none, set after account creation)
    private val _resolvedAccountId = MutableStateFlow(config.accountId)
    val resolvedAccountId: StateFlow<String?> = _resolvedAccountId.asStateFlow()

    init {
        // A host that launches onboarding with an existing account named it before the SDK
        // could have; publish it so events emitted before account creation still attribute.
        FrameNetworking.setAccountIdIfUnset(config.accountId)
    }

    /**
     * Records the account this flow resolved, publishing it to the SDK so account events
     * emitted from here on are attributed. Onboarding creates the account mid-flow, so
     * without this a host that launched without one emits nothing for the entire run.
     */
    private fun setResolvedAccountId(accountId: String) {
        _resolvedAccountId.value = accountId
        FrameNetworking.setAccountIdIfUnset(accountId)
    }

    // Onboarding-session secret (`onb_sess_…`) minted locally when the host did not supply a
    // config.clientSecret. Retained so endpoints that carry client_secret in the body (e.g. IDV) can
    // authenticate on the publishable-key path. FrameNetworking uses it for auth headers but does not
    // expose it, so we keep our own copy — along with the account it was minted for, since
    // FrameNetworking.hasActiveOnboardingSession is a process-global flag that says nothing about
    // which account the live token belongs to.
    private var mintedOnboardingSessionSecret: String? = null
    private var mintedOnboardingSessionAccountId: String? = null

    /**
     * The onboarding-session secret the IDV endpoints authenticate with: the host-supplied
     * [OnboardingConfig.clientSecret] when present, otherwise the locally minted `onb_sess_…`.
     *
     * Every step of the government-ID flow must resolve the secret the same way — Frame-iOS keeps
     * this in one place by letting `FrameNetworking` resolve auth centrally, so its
     * `IdentityVerificationAPI` takes no secret at all. Android threads it explicitly (the server
     * reads `client_secret` from the request body here), so this accessor is the single source of
     * truth instead. Reading `config.clientSecret` directly in one step and this chain in another
     * strands the flow half-completed on the publishable-key path.
     */
    private val idvClientSecret: String?
        get() = config.clientSecret ?: mintedOnboardingSessionSecret?.takeIf {
            mintedOnboardingSessionAccountId == _resolvedAccountId.value
        }

    // Payment methods loaded for the account
    private val _savedPaymentMethods = MutableStateFlow<List<PaymentMethodSummary>>(emptyList())
    val savedPaymentMethods: StateFlow<List<PaymentMethodSummary>> = _savedPaymentMethods.asStateFlow()

    private val _savedPayoutMethods = MutableStateFlow<List<PaymentMethodSummary>>(emptyList())
    val savedPayoutMethods: StateFlow<List<PaymentMethodSummary>> = _savedPayoutMethods.asStateFlow()

    private val _primaryPayoutMethodId = MutableStateFlow<String?>(null)
    val primaryPayoutMethodId: StateFlow<String?> = _primaryPayoutMethodId.asStateFlow()

    private val _plaidLinkToken = MutableStateFlow<String?>(null)
    val plaidLinkToken: StateFlow<String?> = _plaidLinkToken.asStateFlow()

    // Government-ID (Persona) no-SSN verification. [personaInquiryToLaunch] is set to the
    // /idv/session response once it returns; the personal-info view observes it from a
    // LaunchedEffect and launches the Persona SDK (the ActivityResult launcher is lifecycle-owned by
    // the composable, so the VM can't launch it directly). Cleared via [clearPersonaInquiryToLaunch]
    // right after launch so the effect won't re-fire.
    private val personaService = PersonaVerificationService()

    private val _personaInquiryToLaunch = MutableStateFlow<PersonaInquiry?>(null)
    val personaInquiryToLaunch: StateFlow<PersonaInquiry?> = _personaInquiryToLaunch.asStateFlow()

    /** True while /idv/session is in flight or the Persona SDK / completion round-trip is running. */
    private val _isVerifyingGovId = MutableStateFlow(false)
    val isVerifyingGovId: StateFlow<Boolean> = _isVerifyingGovId.asStateFlow()

    // Set when Continue launched Persona for a step-up, so a verified result advances the flow.
    private var advanceAfterGovIdVerification = false

    // Whether the customer has verified via government ID is read from
    // [onboardingData].identityVerifiedViaGovId directly in the UI — no separate flow needed.

    /// Single re-entrancy + loading flag for any user-initiated network action. Drives the
    /// in-button spinner across every onboarding screen and prevents double-submits.
    private val _isPerformingAction = MutableStateFlow(false)
    val isPerformingAction: StateFlow<Boolean> = _isPerformingAction.asStateFlow()

    /// Backwards-compatible alias used by `AddPayoutMethodScreen` to drive the Plaid
    /// "Connect Bank Account" button. Now backed by the unified action flag.
    val isConnectingPlaidBank: StateFlow<Boolean> = _isPerformingAction.asStateFlow()

    /// Try to acquire the action guard. Returns true if the caller should proceed with the
    /// action (and is responsible for calling [endAction] when done). Returns false if another
    /// action is already in flight, in which case the caller should bail out immediately.
    private fun beginAction(): Boolean {
        if (_isPerformingAction.value) return false
        _isPerformingAction.value = true
        return true
    }

    private fun endAction() {
        _isPerformingAction.value = false
    }

    private fun buildPlaidService(accountId: String) = PlaidLinkService(accountId)

    // Result to emit to the host (Completed / Cancelled only — errors stay in-flow via [userErrorMessage])
    private val _result = MutableStateFlow<OnboardingResult?>(null)
    val result: StateFlow<OnboardingResult?> = _result.asStateFlow()

    /** How onboarding ended, for the final screen to render from. Null until resolved. */
    private val _finalOutcome = MutableStateFlow<OnboardingOutcome?>(null)
    val finalOutcome: StateFlow<OnboardingOutcome?> = _finalOutcome.asStateFlow()

    private val _isResolvingOutcome = MutableStateFlow(false)
    val isResolvingOutcome: StateFlow<Boolean> = _isResolvingOutcome.asStateFlow()

    /**
     * Resolves the outcome on arrival at the final screen. Capability status settles after the
     * applicant's last answer, so this resolves fresh rather than reusing earlier state.
     */
    fun resolveFinalOutcomeIfNeeded() {
        if (_finalOutcome.value != null || _isResolvingOutcome.value) return
        viewModelScope.launch {
            _isResolvingOutcome.value = true
            try {
                resolveFinalOutcome()?.let { _finalOutcome.value = it }
            } finally {
                _isResolvingOutcome.value = false
            }
        }
    }

    private val _userErrorMessage = MutableStateFlow<String?>(null)
    val userErrorMessage: StateFlow<String?> = _userErrorMessage.asStateFlow()

    fun clearUserErrorMessage() {
        _userErrorMessage.value = null
    }

    private fun reportUserError(message: String) {
        _userErrorMessage.value = message
    }

    private fun userMessageForNetworkError(err: NetworkingError?): String {
        if (err == null) return "Something went wrong. Please try again."
        return err.toastMessage()
    }

    /**
     * Binds the onboarding flow to the resolved account by minting an account-scoped onboarding
     * session (`onb_sess_…`) and beginning it, so subsequent requests (e.g. IDV) authenticate as the
     * session rather than falling back to the configured publishable/secret key.
     *
     * Mints with the publishable key (`pk_`), which `POST /v1/onboarding_sessions` accepts, so no
     * secret key leaves the device. Idempotent and safe to call after each account-creation path: it
     * does nothing when the host already supplied a `clientSecret` (a session is active) or when no
     * account exists yet.
     */
    private suspend fun beginOnboardingSessionIfNeeded() {
        if (config.clientSecret != null) return
        val accountId = _resolvedAccountId.value ?: return
        // FrameNetworking.hasActiveOnboardingSession is process-global and says nothing about which
        // account the live token belongs to, so skip only when this flow holds one for this account.
        if (mintedOnboardingSessionSecret != null && mintedOnboardingSessionAccountId == accountId) return

        val request = OnboardingSessionRequests.CreateOnboardingSessionRequest(accountId = accountId)
        val (session, error) = OnboardingSessionsAPI.createOnboardingSessionWithPublishableKey(request)
        if (error != null) reportUserError(userMessageForNetworkError(error))
        val clientSecret = session?.clientSecret ?: run {
            if (error != null) {
                AccountEventEmitter.emit(
                    AccountEventName.ONBOARDING_SESSION_START_FAILED,
                    AccountEventScreen.ONBOARDING,
                    detail = "$error"
                )
            }
            return
        }
        // The flow closed while the mint was in flight; installing the token now would leak it.
        if (isClosed) return
        mintedOnboardingSessionSecret = clientSecret
        mintedOnboardingSessionAccountId = accountId
        FrameNetworking.beginOnboardingSession(clientSecret)
    }

    private var isClosed = false

    /**
     * Ends a self-minted onboarding session and cancels in-flight work. Views build this VM with
     * `remember`, not a ViewModelStore, so `onCleared` never runs — they call this on dispose.
     */
    fun close() {
        isClosed = true
        mintedOnboardingSessionSecret?.let { FrameNetworking.endOnboardingSession(it) }
        viewModelScope.cancel()
    }

    // Phone OTP step state
    private val _tosTokenDeferred: Deferred<String?>? =
        if (config.skipInitNetwork) null
        else viewModelScope.async {
            val (response, _) = TermsOfServiceAPI.createToken()
            response?.token
        }

    private val _verifyIdSubStep = MutableStateFlow(VerifyIdSubStep.PhoneAuth)
    val verifyIdSubStep: StateFlow<VerifyIdSubStep> = _verifyIdSubStep.asStateFlow()

    private val _pendingVerificationId = MutableStateFlow<String?>(null)
    val pendingPhoneVerificationId: StateFlow<String?> = _pendingVerificationId.asStateFlow()

    private val _pendingProveAuthToken = MutableStateFlow<String?>(null)
    val pendingProveAuthToken: StateFlow<String?> = _pendingProveAuthToken.asStateFlow()

    private val _verifyPhoneUi = MutableStateFlow<VerifyPhoneUi?>(null)
    val verifyPhoneUi: StateFlow<VerifyPhoneUi?> = _verifyPhoneUi.asStateFlow()

    private val proveOtpLock = Any()
    private var proveOtpDeferred: CompletableDeferred<String?>? = null
    private var proveAuthLaunchStarted: Boolean = false
    private var activeProveAuthService: ProveAuthService? = null

    /** True while fetching GET account after phone verification succeeds (before personal info step). */
    private val _awaitingAccountProfileRefresh = MutableStateFlow(false)
    val awaitingAccountProfileRefresh: StateFlow<Boolean> = _awaitingAccountProfileRefresh.asStateFlow()

    private val _termsOfServiceToken = MutableStateFlow<String?>(null)
    /** Mirrors iOS `termsOfServiceToken`. */
    val termsOfServiceToken: StateFlow<String?> = _termsOfServiceToken.asStateFlow()

    private var existingAccountHasTOS: Boolean = false

    private val _paymentMethodVerification = MutableStateFlow<ThreeDSecureVerification?>(null)
    /** Mirrors iOS `paymentMethodVerification`. */
    val paymentMethodVerification: StateFlow<ThreeDSecureVerification?> = _paymentMethodVerification.asStateFlow()

    private val _customerIdentity = MutableStateFlow<CustomerIdentity?>(null)
    /** Mirrors iOS `customerIdentity`. */
    val customerIdentity: StateFlow<CustomerIdentity?> = _customerIdentity.asStateFlow()

    private val defaultCreatedBillingAddress = FrameObjects.BillingAddress(
        city = "",
        country = "US",
        state = "",
        postalCode = "",
        addressLine1 = "",
        addressLine2 = null
    )

    private val _paymentCardData = MutableStateFlow(PaymentCardData())
    /** Evervault [RowsPaymentCard] state from [com.framepayments.framesdk_ui.EncryptedPaymentCardInput]. */
    val paymentCardData: StateFlow<PaymentCardData> = _paymentCardData.asStateFlow()

    private val _paymentCardDraft = MutableStateFlow(PaymentCardDraft())
    /** Payout checkbox; plain card fields used when Evervault UI is unavailable. */
    val paymentCardDraft: StateFlow<PaymentCardDraft> = _paymentCardDraft.asStateFlow()

    private val _addPaymentUsesEvervaultCardUi = MutableStateFlow(true)
    val addPaymentUsesEvervaultCardUi: StateFlow<Boolean> = _addPaymentUsesEvervaultCardUi.asStateFlow()

    fun setAddPaymentUsesEvervaultCardUi(useEvervault: Boolean) {
        _addPaymentUsesEvervaultCardUi.value = useEvervault
    }

    private val _createdBillingAddress = MutableStateFlow(defaultCreatedBillingAddress)
    /** Billing for add-card / add-ACH; iOS `createdBillingAddress`. */
    val createdBillingAddress: StateFlow<FrameObjects.BillingAddress> = _createdBillingAddress.asStateFlow()

    private val _bankAccountDraft = MutableStateFlow(BankAccountDraft())
    /** ACH form state; iOS bank account draft. */
    val bankAccountDraft: StateFlow<BankAccountDraft> = _bankAccountDraft.asStateFlow()

    fun updatePaymentCardDraft(transform: (PaymentCardDraft) -> PaymentCardDraft) {
        _paymentCardDraft.update(transform)
    }

    fun onPaymentCardDataChange(data: PaymentCardData) {
        _paymentCardData.value = data
    }

    fun updateCreatedBillingAddress(transform: (FrameObjects.BillingAddress) -> FrameObjects.BillingAddress) {
        _createdBillingAddress.update(transform)
    }

    fun updateBankAccountDraft(transform: (BankAccountDraft) -> BankAccountDraft) {
        _bankAccountDraft.update(transform)
    }

    private fun effectiveCustomerIdentityId(): String? =
        _customerIdentity.value?.id ?: _onboardingData.value.customerIdentityId

    // Phone + DOB form inputs (owned here so they survive recomposition)
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _dobMonth = MutableStateFlow("")
    val dobMonth: StateFlow<String> = _dobMonth.asStateFlow()

    private val _dobDay = MutableStateFlow("")
    val dobDay: StateFlow<String> = _dobDay.asStateFlow()

    private val _dobYear = MutableStateFlow("")
    val dobYear: StateFlow<String> = _dobYear.asStateFlow()

    val dateOfBirth: String
        get() {
            val m = _dobMonth.value; val d = _dobDay.value; val y = _dobYear.value
            // Pad single-digit month/day so a user typing "5" for May still produces a
            // well-formed `YYYY-MM-DD` ISO string for the backend. `Validators`
            // is responsible for rejecting out-of-range values before this is read.
            return if (y.length == 4 && m.isNotEmpty() && d.isNotEmpty()) {
                "$y-${m.padStart(2, '0')}-${d.padStart(2, '0')}"
            } else ""
        }

    val dobComplete: Boolean
        get() = _dobMonth.value.isNotEmpty() && _dobDay.value.isNotEmpty() && _dobYear.value.length == 4

    /// E.164-shaped phone number for OTP / verification endpoints: dial code prefix + the
    /// user-typed digits with all formatter spaces/punctuation stripped. Mirrors iOS
    /// `OnboardingContainerViewModel.sendOTPVerification` which builds the same shape.
    private val phoneNumberForVerification: String
        get() = _phoneCountry.value.dialCode + _phoneNumber.value.filter(Char::isDigit)

    // region Form validation (1:1 with iOS OnboardingContainerViewModel partitioned errors)

    private val _phoneCountry = MutableStateFlow(PhoneCountrySelection.default)
    val phoneCountry: StateFlow<PhoneCountrySelection> = _phoneCountry.asStateFlow()

    fun onPhoneCountryChanged(selection: PhoneCountrySelection) {
        _phoneCountry.value = selection
    }

    private val _fieldErrors = MutableStateFlow<Map<OnboardingField, String>>(emptyMap())
    val fieldErrors: StateFlow<Map<OnboardingField, String>> = _fieldErrors.asStateFlow()

    fun errorFor(field: OnboardingField): String? = _fieldErrors.value[field]

    fun clearError(field: OnboardingField) {
        if (_fieldErrors.value.containsKey(field)) {
            _fieldErrors.value = _fieldErrors.value - field
        }
    }

    private fun applyValidation(
        group: OnboardingFieldGroup,
        errors: Map<OnboardingField, String>
    ): Boolean {
        val preserved = _fieldErrors.value.filterKeys { it.group != group }
        _fieldErrors.value = preserved + errors
        return errors.isEmpty()
    }

    /** Validate the phone-auth screen. Errors in the [OnboardingFieldGroup.PHONE_AUTH] group only. */
    fun validateAllPhoneAuth(): Boolean {
        val errors = mutableMapOf<OnboardingField, String>()
        Validators.validatePhoneE164(_phoneNumber.value, _phoneCountry.value.alpha2)
            ?.let { errors[OnboardingField.AUTH_PHONE] = it }
        // Matches the intro screen gate, which uses the host's original list: shrinking
        // requiredCapabilities would silently drop DOB validation once kyc_prefill is granted.
        if (originallyRequiredCapabilities.contains(Capabilities.KYC_PREFILL)) {
            Validators.validateDateOfBirth(
                year = _dobYear.value,
                month = _dobMonth.value,
                day = _dobDay.value
            )?.let { err ->
                errors[OnboardingField.AUTH_BIRTH_MONTH] = err
                errors[OnboardingField.AUTH_BIRTH_DAY] = err
                errors[OnboardingField.AUTH_BIRTH_YEAR] = err
            }
        }
        return applyValidation(OnboardingFieldGroup.PHONE_AUTH, errors)
    }

    // endregion

    init {
        if (_tosTokenDeferred != null) {
            viewModelScope.launch {
                _termsOfServiceToken.value = _tosTokenDeferred.await()
            }
        }
    }

    private suspend fun termsOfServiceForCreate(): AccountObjects.AccountTermsOfService {
        val token = _tosTokenDeferred?.await()
            ?: TermsOfServiceAPI.createToken().let { (r, _) -> r?.token }
        val ipAddress = withContext(Dispatchers.IO) { SiftManager.getIPAddress() }
        return AccountObjects.AccountTermsOfService(
            token = token,
            acceptedAt = Instant.now().toString(),
            ipAddress = ipAddress
        )
    }

    /**
     * Mirrors iOS: when updating an existing account, include the TOS only if the
     * account hasn't already accepted one. Returns null when [existingAccountHasTOS]
     * is true so the update request omits the field entirely.
     */
    private suspend fun termsOfServiceForUpdate(): AccountObjects.AccountTermsOfService? {
        if (existingAccountHasTOS) return null
        val ipAddress = withContext(Dispatchers.IO) { SiftManager.getIPAddress() }
        return AccountObjects.AccountTermsOfService(
            token = _termsOfServiceToken.value,
            acceptedAt = Instant.now().toString(),
            ipAddress = ipAddress
        )
    }

    fun updateOnboardingFlow() {
        val caps = _requiredCapabilities.value
        var steps = computeOrderedSteps(caps, originallyRequiredCapabilities)
        if (!config.showIntroScreen) steps = steps.filter { it != OnboardingStep.VerificationWelcome }
        if (!config.showCompletionScreen) steps = steps.filter { it != OnboardingStep.VerificationSubmitted }
        _orderedSteps = steps
        var segments = computeFlowSegments(caps, originallyRequiredCapabilities)
        // Keep progress capsules in sync with the screens that actually exist.
        if (!config.showCompletionScreen) {
            segments = segments.filter { it != OnboardingFlowSegment.VERIFICATION_SUBMITTED }
        }
        _flowSegments = segments
        if (steps.isEmpty()) {
            viewModelScope.launch { finishOnboarding() }
            return
        }
        if (navigationState.currentStep !in _orderedSteps) {
            navigationState.goTo(_orderedSteps.first())
        }
    }

    fun progressiveFlowSegments(): List<OnboardingFlowSegment> {
        val segs = _flowSegments
        if (segs.isEmpty()) return listOf(OnboardingFlowSegment.PERSONAL_INFORMATION)
        val currentSeg = navigationState.currentStep.toFlowSegment()
        val idx = segs.indexOf(currentSeg).coerceAtLeast(0)
        return segs.take(idx + 1)
    }

    private suspend fun updateCapabilitiesBasedOnCompletion(accountCaps: List<CapabilityObjects.Capability>) {
        val mutable = _requiredCapabilities.value.toMutableList()
        for (cap in accountCaps) {
            val enumCap = Capabilities.entries.find { it.apiValue == cap.name } ?: continue
            // idv declares no field keys, so it reports nothing due even before verification.
            if (enumCap == Capabilities.IDV && cap.capabilityStatus != CapabilityObjects.CapabilityStatus.ACTIVE) continue
            // Null currently_due means "not reported" — only an empty list means complete.
            // Treating null as empty was skipping still-outstanding capabilities.
            if (cap.currentlyDue?.isEmpty() == true) {
                mutable.removeAll { it == enumCap }
            }
        }
        _requiredCapabilities.value = mutable
        updateOnboardingFlow()
    }

    suspend fun checkExistingAccount(updateCapabilities: Boolean = false, depth: Int = 0) {
        val accountId = _resolvedAccountId.value ?: return
        // A host that launches onboarding with an existing accountId but no clientSecret has no
        // account-creation step to mint from, so bind a session here too — otherwise IDV and other
        // account-scoped requests fall back to the configured key. No-ops if a session is already active.
        beginOnboardingSessionIfNeeded()
        val (account, _) = AccountsAPI.getAccountWith(accountId, forTesting = false)
        account?.id?.let { aid ->
            setResolvedAccountId(aid)
            _onboardingData.update { it.copy(resolvedAccountId = aid) }
        }
        existingAccountHasTOS = account?.termsOfService?.acceptedAt != null
        account?.payoutPaymentMethodId?.let { _primaryPayoutMethodId.value = it }
        // Capabilities aren't PII-gated, so seed before the profile guard below.
        updateStepUpRequirements(account?.capabilities)
        // A completed government-ID verification leaves idv active; don't ask the applicant again.
        if (account?.capabilities?.any {
                it.name == Capabilities.IDV.apiValue && it.capabilityStatus == CapabilityObjects.CapabilityStatus.ACTIVE
            } == true
        ) {
            _onboardingData.update { it.copy(identityVerifiedViaGovId = true) }
        }
        if (account?.profile?.individual == null) return
        applyAccountProfileToOnboarding(accountId, account)
        if (!updateCapabilities) return
        val caps = account.capabilities ?: return
        val requiredNames = requiredCapabilityApiStrings().toSet()
        val accountNames = caps.map { it.name }.toSet()
        val hasSuperset = requiredNames.all { accountNames.contains(it) }
        if (!hasSuperset) {
            // One request + one recheck, matching iOS. Retrying in a loop can thrash the API
            // without ever making progress when the merchant has not enabled the capability.
            CapabilitiesAPI.requestCapabilities(
                accountId,
                CapabilityRequests.RequestCapabilitiesRequest(capabilities = requiredCapabilityApiStrings())
            )
            if (depth == 0) {
                checkExistingAccount(updateCapabilities = true, depth = 1)
            }
            return
        }
        updateCapabilitiesBasedOnCompletion(caps)
    }

    fun launchCheckExistingAccount(updateCapabilities: Boolean) {
        viewModelScope.launch {
            try {
                checkExistingAccount(updateCapabilities = updateCapabilities)
            } finally {
                _isExistingAccountReady.value = true
            }
        }
    }

    // region Navigation

    fun moveNext() {
        val i = orderedSteps.indexOf(navigationState.currentStep)
        if (i < 0 || i >= orderedSteps.size - 1) {
            viewModelScope.launch { finishOnboarding() }
        } else {
            navigationState.goTo(orderedSteps[i + 1])
        }
    }

    /**
     * Resolves the applicant's real outcome from a fresh account fetch.
     *
     * Deliberately not routed through [checkExistingAccount]: that returns early when the
     * server withholds `profile` (it is PII-gated, so a publishable-key host never sees it)
     * and again when capabilities aren't yet a superset, either of which would leave
     * capabilities unread and make a passing run resolve as unverified. Capabilities are not
     * PII-gated, so reading them directly always works. Mirrors iOS `resolveFinalOutcome()`.
     *
     * Returns null when the account lookup fails or there's no account to look up yet, rather
     * than a [OnboardingOutcome.PendingReview] sentinel — a failed lookup is not a verified
     * outcome, and callers must not cache it as one.
     */
    private suspend fun resolveFinalOutcome(): OnboardingOutcome? {
        val accountId = _resolvedAccountId.value ?: return null
        val (account, _) = AccountsAPI.getAccountWith(accountId, forTesting = false)
        val capabilities = account?.capabilities ?: return null
        return OnboardingOutcome.resolve(capabilities, config.requiredCapabilities.toList())
    }

    private suspend fun finishOnboarding() {
        // Reuse what the final screen resolved, so the host is never told something the applicant
        // was not shown. Retry the lookup if it hasn't resolved yet (or previously failed).
        val outcome = _finalOutcome.value ?: resolveFinalOutcome() ?: OnboardingOutcome.PendingReview
        val paymentMethodId = _onboardingData.value.selectedPaymentMethodId
        val accountId = _resolvedAccountId.value
        _result.value = if (outcome.isSuccess) {
            OnboardingResult.Completed(paymentMethodId = paymentMethodId, accountId = accountId)
        } else {
            OnboardingResult.FinishedUnverified(
                paymentMethodId = paymentMethodId,
                outcome = outcome,
                accountId = accountId
            )
        }
    }

    /** Skips the rest of the current segment, so Continue on a saved method bypasses its Add step. */
    fun moveToNextSegment() {
        val i = orderedSteps.indexOf(navigationState.currentStep)
        val segment = navigationState.currentStep.toFlowSegment()
        val next = orderedSteps.drop(i + 1).firstOrNull { it.toFlowSegment() != segment }
        if (i < 0 || next == null) {
            viewModelScope.launch { finishOnboarding() }
        } else {
            navigationState.goTo(next)
        }
    }

    fun moveBack() {
        val current = navigationState.currentStep
        // From an Add screen, return to that segment's Select screen rather than the previous
        // ordered step (which can be wrong after goTo/address-only or a rebuilt step list).
        when (current) {
            OnboardingStep.AddPaymentMethod -> {
                clearOnlyAddressVerification()
                val select = orderedSteps.firstOrNull { it == OnboardingStep.SelectPaymentMethod }
                if (select != null) {
                    navigationState.goTo(select)
                    return
                }
            }
            OnboardingStep.AddPayoutMethod -> {
                val select = orderedSteps.firstOrNull { it == OnboardingStep.SelectPayoutMethod }
                if (select != null) {
                    navigationState.goTo(select)
                    return
                }
            }
            else -> Unit
        }
        val i = orderedSteps.indexOf(current)
        if (i > 0) navigationState.goTo(orderedSteps[i - 1])
    }

    /**
     * Exits the flow before completion. Android has no swipe-to-dismiss equivalent for a
     * full-screen composable the way iOS's `.sheet` presentation does, so the container's header
     * exposes an explicit close button that calls this instead.
     */
    fun cancel() {
        _result.value = OnboardingResult.Cancelled
    }

    // endregion

    // region Phone + DOB inputs

    fun onPhoneNumberChanged(value: String) {
        // Store the value as-is; PhoneNumberTextField applies AsYouTypeFormatter
        // formatting. Stripping non-digits here would clobber the formatter.
        _phoneNumber.value = value
    }

    fun onDobMonthChanged(value: String) {
        _dobMonth.value = value.filter(Char::isDigit).take(2)
    }

    fun onDobDayChanged(value: String) {
        _dobDay.value = value.filter(Char::isDigit).take(2)
    }

    fun onDobYearChanged(value: String) {
        _dobYear.value = value.filter(Char::isDigit).take(4)
    }

    // endregion

    // region Account profile (prefill)

    private val isoDobRegex = Regex("""^(\d{4})-(\d{2})-(\d{2})$""")

    private fun applyIsoDobToPhoneAuthFields(isoDob: String?) {
        val d = isoDob ?: return
        val m = isoDobRegex.matchEntire(d) ?: return
        _dobYear.value = m.groupValues[1]
        _dobMonth.value = m.groupValues[2].padStart(2, '0').takeLast(2)
        _dobDay.value = m.groupValues[3].padStart(2, '0').takeLast(2)
    }

    /**
     * Fetches [accountId] with a session bound first (so `profile` isn't PII-gated) and applies
     * it to onboarding state via [applyAccountProfileToOnboarding].
     *
     * `profile` is PII-gated: the server withholds it unless the request carries a secret key or
     * a matching onboarding session. Without a session bound first, a publishable-key host reads
     * back a profile-less account and silently prefills nothing — which is what stopped Prove's
     * KYC-prefill data reaching the form.
     */
    private suspend fun refreshAccountProfileIntoOnboarding(accountId: String) {
        beginOnboardingSessionIfNeeded()
        val (account, _) = AccountsAPI.getAccountWith(accountId, forTesting = false)
        applyAccountProfileToOnboarding(accountId, account)
        // Phone verification can satisfy KYC-prefill and surface step-up flags; re-read them so
        // the SSN field hides (or shows) before the applicant reaches personal info.
        updateStepUpRequirements(account?.capabilities)
    }

    /**
     * Applies an already-fetched [account]'s profile to onboarding state. Split out of
     * [refreshAccountProfileIntoOnboarding] so [checkExistingAccount] can reuse the account it
     * already fetched instead of fetching it a second time, matching iOS's single-fetch shape.
     */
    private fun applyAccountProfileToOnboarding(accountId: String, account: AccountObjects.Account?) {
        val individual = account?.profile?.individual
        if (individual == null) {
            if (FrameNetworking.debugMode) {
                Log.w(
                    "FrameSDK",
                    "Account $accountId returned no profile, so there is nothing to prefill. The " +
                        "profile is PII-gated — check that an onboarding session is active for this account."
                )
            }
            _onboardingData.update { it.copy(resolvedAccountId = account?.id ?: accountId) }
            return
        }
        val addr = individual.address
        // The server returns a structured `phone` object; `phoneNumber` is a legacy flat fallback
        // for responses that predate it.
        val rawPhone = individual.phone?.number ?: individual.phoneNumber
        // Keep all digits — takeLast(10) dropped the country code and broke non-US numbers.
        val phoneDigits = rawPhone?.filter(Char::isDigit)?.takeIf { it.isNotEmpty() }
        if (!phoneDigits.isNullOrEmpty()) {
            _phoneNumber.value = phoneDigits
        }
        individual.phone?.countryCode?.let { code ->
            val dial = code.trim().let { if (it.startsWith("+")) it else "+$it" }
            PhoneCountrySelection.all.find { it.dialCode == dial }?.let {
                _phoneCountry.value = it
            }
        }
        val birthOrDob = individual.birthdate?.takeIf { it.isNotBlank() }
        applyIsoDobToPhoneAuthFields(birthOrDob)
        _onboardingData.update { cur ->
            cur.copy(
                resolvedAccountId = account.id,
                firstName = individual.name?.firstName?.takeIf { it.isNotBlank() } ?: cur.firstName,
                lastName = individual.name?.lastName?.takeIf { it.isNotBlank() } ?: cur.lastName,
                email = individual.email?.takeIf { it.isNotBlank() } ?: cur.email,
                phoneNumber = phoneDigits ?: cur.phoneNumber ?: _phoneNumber.value,
                dateOfBirth = birthOrDob ?: cur.dateOfBirth,
                ssnLast4 = individual.ssnLastFour?.takeIf { it.isNotBlank() } ?: cur.ssnLast4,
                addressLine1 = addr?.addressLine1?.takeIf { it.isNotBlank() } ?: cur.addressLine1,
                addressLine2 = addr?.addressLine2?.takeIf { !it.isNullOrBlank() } ?: cur.addressLine2,
                city = addr?.city?.takeIf { it.isNotBlank() } ?: cur.city,
                stateCode = addr?.state?.takeIf { it.isNotBlank() } ?: cur.stateCode,
                postalCode = addr?.postalCode?.takeIf { it.isNotBlank() } ?: cur.postalCode,
                country = addr?.country?.takeIf { it.isNotBlank() } ?: cur.country
            )
        }
    }

    private suspend fun finalizePhoneVerificationAndShowPersonalInfo(accountId: String) {
        _awaitingAccountProfileRefresh.value = true
        try {
            refreshAccountProfileIntoOnboarding(accountId)
        } finally {
            _awaitingAccountProfileRefresh.value = false
        }
        _verifyPhoneUi.value = null
        _verifyIdSubStep.value = VerifyIdSubStep.InformationForm
    }

    // endregion

    // region Phone OTP flow

    fun submitPhoneAuth(requiresDateOfBirth: Boolean) {
        if (!beginAction()) return
        AccountEventEmitter.emit(
            AccountEventName.PHONE_VERIFICATION_STARTED,
            AccountEventScreen.PHONE_VERIFICATION
        )
        viewModelScope.launch {
            try {
                val acctId = _resolvedAccountId.value ?: run {
                    val accountRequest = AccountRequests.CreateAccountRequest(
                        type = AccountObjects.AccountType.INDIVIDUAL,
                        termsOfService = termsOfServiceForCreate(),
                        profile = AccountRequests.CreateAccountProfile(
                            individual = AccountRequests.CreateIndividualAccount(
                                phone = AccountObjects.AccountPhoneNumber(
                                    number = _phoneNumber.value,
                                    countryCode = _phoneCountry.value.dialCode
                                ),
                                birthdate = dateOfBirth.ifEmpty { null }
                            )
                        ),
                        capabilities = requiredCapabilityApiStrings()
                    )
                    val (account, err) = AccountsAPI.createAccount(accountRequest)
                    val id = account?.id
                    if (id == null) {
                        reportUserError(userMessageForNetworkError(err))
                        null
                    } else {
                        setResolvedAccountId(id)
                        _onboardingData.value = _onboardingData.value.copy(resolvedAccountId = id)
                        beginOnboardingSessionIfNeeded()
                        id
                    }
                } ?: return@launch

                val (result, verifyErr) = PhoneOTPVerificationAPI.createVerification(
                    accountId = acctId,
                    phoneNumber = phoneNumberForVerification,
                    dateOfBirth = dateOfBirth
                )
                if (verifyErr != null) {
                    AccountEventEmitter.emit(
                        AccountEventName.PHONE_CODE_SEND_FAILED,
                        AccountEventScreen.PHONE_VERIFICATION,
                        detail = "$verifyErr"
                    )
                }
                _pendingVerificationId.value = result?.id
                _pendingProveAuthToken.value = result?.proveAuthToken
                proveAuthLaunchStarted = false
                if (result?.id != null) {
                    val isProve = result.proveAuthToken != null ||
                        result.provider.equals("prove", ignoreCase = true)
                    _verifyPhoneUi.value = if (isProve && result.proveAuthToken != null) {
                        AccountEventEmitter.emit(
                            AccountEventName.SILENT_PHONE_AUTH_STARTED,
                            AccountEventScreen.PHONE_VERIFICATION,
                            detail = AccountEventDetail.PROVE_PROVIDER
                        )
                        VerifyPhoneUi.LoadingProve
                    } else {
                        AccountEventEmitter.emit(
                            AccountEventName.PHONE_CODE_SENT,
                            AccountEventScreen.PHONE_VERIFICATION,
                            detail = result.provider?.let { "provider: $it" }
                        )
                        VerifyPhoneUi.OtpFrameApi
                    }
                    _verifyIdSubStep.value = VerifyIdSubStep.VerifyPhone
                } else {
                    reportUserError(userMessageForNetworkError(verifyErr))
                }
            } finally {
                endAction()
            }
        }
    }

    fun resendVerificationCode() {
        if (!beginAction()) return
        viewModelScope.launch {
            try {
                val acctId = _resolvedAccountId.value ?: return@launch
                val (resent, err) = PhoneOTPVerificationAPI.createVerification(
                    accountId = acctId,
                    phoneNumber = phoneNumberForVerification,
                    dateOfBirth = dateOfBirth
                )
                // Confirm must target the new verification; the old one no longer takes a code.
                resent?.id?.let { _pendingVerificationId.value = it }
                if (err != null) {
                    AccountEventEmitter.emit(
                        AccountEventName.PHONE_CODE_SEND_FAILED,
                        AccountEventScreen.PHONE_VERIFICATION,
                        detail = "$err"
                    )
                    reportUserError(userMessageForNetworkError(err))
                } else {
                    AccountEventEmitter.emit(
                        AccountEventName.PHONE_CODE_SENT,
                        AccountEventScreen.PHONE_VERIFICATION
                    )
                }
            } finally {
                endAction()
            }
        }
    }

    // A cancelled Prove code entry can surface as a Prove failure; this keeps it from triggering the fallback.
    private var proveOtpCancelledByUser = false

    fun startProveAuth(context: Context) {
        if (proveAuthLaunchStarted) return
        proveAuthLaunchStarted = true
        proveOtpCancelledByUser = false
        viewModelScope.launch {
            val acctId = _resolvedAccountId.value ?: run {
                proveAuthLaunchStarted = false
                return@launch
            }
            val verificationId = _pendingVerificationId.value ?: run {
                proveAuthLaunchStarted = false
                return@launch
            }
            val authToken = _pendingProveAuthToken.value ?: run {
                proveAuthLaunchStarted = false
                return@launch
            }
            val otpProvider: suspend () -> String? = {
                val deferred = CompletableDeferred<String?>()
                synchronized(proveOtpLock) {
                    proveOtpDeferred = deferred
                }
                _verifyPhoneUi.value = VerifyPhoneUi.OtpForProve
                try {
                    deferred.await()
                } finally {
                    synchronized(proveOtpLock) {
                        if (proveOtpDeferred === deferred) proveOtpDeferred = null
                    }
                }
            }
            val service = ProveAuthService(
                context = context,
                accountId = acctId,
                verificationId = verificationId,
                confirmHandler = { _, vid ->
                    val (resp, err) = PhoneOTPVerificationAPI.confirmVerification(acctId, vid, code = null)
                    if (err != null) throw err
                    if (resp == null) {
                        throw IllegalStateException("Phone verification confirm returned no data")
                    }
                },
                otpProvider = otpProvider
            )
            activeProveAuthService = service
            try {
                val success = service.authenticateWith(authToken)
                if (success) {
                    AccountEventEmitter.emit(
                        AccountEventName.SILENT_PHONE_AUTH_COMPLETED,
                        AccountEventScreen.PHONE_VERIFICATION,
                        detail = AccountEventDetail.PROVE_PROVIDER
                    )
                    _pendingVerificationId.value = null
                    _pendingProveAuthToken.value = null
                    finalizePhoneVerificationAndShowPersonalInfo(acctId)
                } else {
                    fallBackToTwilio(acctId, proveError = AccountEventDetail.PROVE_PROVIDER)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (err: ProveAuthServiceError.Cancelled) {
                // Applicant dismissed OTP; the Prove session is already released.
            } catch (err: Exception) {
                synchronized(proveOtpLock) {
                    proveOtpDeferred?.complete(null)
                    proveOtpDeferred = null
                }
                fallBackToTwilio(acctId, proveError = err)
            } finally {
                activeProveAuthService = null
                proveAuthLaunchStarted = false
            }
        }
    }

    /**
     * Recovers from a failed Prove attempt by creating a second verification, which the backend
     * routes to Twilio (it sends a retry on a number with an unverified Prove attempt there).
     * A retry that comes back on Prove, or not at all, has no code-entry path, so it ends the attempt.
     */
    private suspend fun fallBackToTwilio(accountId: String, proveError: Any) {
        if (proveOtpCancelledByUser) {
            proveOtpCancelledByUser = false
            return
        }
        AccountEventEmitter.emit(
            AccountEventName.SILENT_PHONE_AUTH_FALLBACK,
            AccountEventScreen.PHONE_VERIFICATION,
            detail = "$proveError"
        )
        val (retry, err) = PhoneOTPVerificationAPI.createVerification(
            accountId = accountId,
            phoneNumber = phoneNumberForVerification,
            dateOfBirth = dateOfBirth
        )
        if (err != null) {
            AccountEventEmitter.emit(
                AccountEventName.PHONE_CODE_SEND_FAILED,
                AccountEventScreen.PHONE_VERIFICATION,
                detail = "$err"
            )
        }
        val retryId = retry?.id
        if (retryId == null || retry.proveAuthToken != null ||
            retry.provider.equals("prove", ignoreCase = true)
        ) {
            AccountEventEmitter.emit(
                AccountEventName.SILENT_PHONE_AUTH_FAILED,
                AccountEventScreen.PHONE_VERIFICATION,
                detail = "terminal failure after fallback also failed: $proveError"
            )
            _pendingVerificationId.value = null
            _pendingProveAuthToken.value = null
            _verifyPhoneUi.value = null
            _verifyIdSubStep.value = VerifyIdSubStep.PhoneAuth
            reportUserError(
                err?.let(::userMessageForNetworkError) ?: "We couldn't verify your phone number. Please try again."
            )
            return
        }
        AccountEventEmitter.emit(AccountEventName.PHONE_CODE_SENT, AccountEventScreen.PHONE_VERIFICATION)
        _pendingVerificationId.value = retryId
        _pendingProveAuthToken.value = null
        _verifyPhoneUi.value = VerifyPhoneUi.OtpFrameApi
        reportUserError("Phone verification service failed. We've sent a second code — please try again.")
    }

    fun submitOtpToProveSdk(code: String) {
        synchronized(proveOtpLock) {
            proveOtpDeferred?.complete(code)
            proveOtpDeferred = null
        }
    }

    fun confirmVerificationCode(code: String) {
        if (!beginAction()) return
        viewModelScope.launch {
            try {
                val acctId = _resolvedAccountId.value ?: return@launch
                val verificationId = _pendingVerificationId.value ?: return@launch
                val (_, err) = PhoneOTPVerificationAPI.confirmVerification(acctId, verificationId, code)
                if (err == null) {
                    AccountEventEmitter.emit(
                        AccountEventName.PHONE_VERIFIED,
                        AccountEventScreen.PHONE_VERIFICATION
                    )
                    _pendingVerificationId.value = null
                    _pendingProveAuthToken.value = null
                    finalizePhoneVerificationAndShowPersonalInfo(acctId)
                } else {
                    AccountEventEmitter.emit(
                        AccountEventName.PHONE_CODE_INCORRECT,
                        AccountEventScreen.PHONE_VERIFICATION
                    )
                    reportUserError(userMessageForNetworkError(err))
                }
            } finally {
                endAction()
            }
        }
    }

    fun goBackFromVerifyPhone() {
        proveOtpCancelledByUser = true
        synchronized(proveOtpLock) {
            if (proveOtpDeferred != null) {
                AccountEventEmitter.emit(
                    AccountEventName.PHONE_CODE_ENTRY_CANCELLED,
                    AccountEventScreen.PHONE_VERIFICATION
                )
            }
            proveOtpDeferred?.complete(null)
            proveOtpDeferred = null
        }
        activeProveAuthService?.cancel()
        activeProveAuthService = null
        proveAuthLaunchStarted = false
        _verifyPhoneUi.value = null
        _verifyIdSubStep.value = VerifyIdSubStep.PhoneAuth
    }

    // endregion

    // region Personal info + account/identity creation

    private suspend fun upsertIndividualAccountForPersonalInfo(
        firstName: String,
        lastName: String,
        email: String,
        dob: String,
        ssnLastFour: String,
        billingAddress: FrameObjects.BillingAddress
    ): AccountObjects.Account? {
        return _resolvedAccountId.value?.let { existing ->
            val updateIndividual = AccountRequests.UpdateIndividualAccount(
                name = AccountRequests.UpdateAccountInfo(
                    firstName = firstName,
                    middleName = null,
                    lastName = lastName
                ),
                email = email,
                phone = AccountObjects.AccountPhoneNumber(
                    number = _phoneNumber.value,
                    countryCode = _phoneCountry.value.dialCode
                ),
                address = billingAddress,
                birthdate = dob,
                ssnLast4 = ssnLastFour.ifEmpty { null }
            )
            val (updated, err) = AccountsAPI.updateAccount(
                existing,
                AccountRequests.UpdateAccountRequest(
                    termsOfService = termsOfServiceForUpdate(),
                    profile = AccountRequests.UpdateAccountProfile(individual = updateIndividual)
                )
            )
            if (err != null) {
                AccountEventEmitter.emit(
                    AccountEventName.PROFILE_UPDATE_FAILED,
                    AccountEventScreen.PERSONAL_INFORMATION,
                    detail = "$err"
                )
                reportUserError(userMessageForNetworkError(err))
                null
            } else {
                AccountEventEmitter.emit(
                    AccountEventName.PROFILE_UPDATED,
                    AccountEventScreen.PERSONAL_INFORMATION
                )
                updateStepUpRequirements(updated?.capabilities)
                updated ?: AccountsAPI.getAccountWith(existing, forTesting = false).first
            }
        } ?: run {
            val accountRequest = AccountRequests.CreateAccountRequest(
                type = AccountObjects.AccountType.INDIVIDUAL,
                termsOfService = termsOfServiceForCreate(),
                profile = AccountRequests.CreateAccountProfile(
                    individual = AccountRequests.CreateIndividualAccount(
                        name = AccountObjects.IndividualAccountName(
                            firstName = firstName,
                            lastName = lastName
                        ),
                        email = email,
                        phone = AccountObjects.AccountPhoneNumber(
                            number = _phoneNumber.value,
                            countryCode = _phoneCountry.value.dialCode
                        ),
                        address = billingAddress,
                        birthdate = dob.ifEmpty { null },
                        ssnLast4 = ssnLastFour.ifEmpty { null }
                    )
                ),
                capabilities = requiredCapabilityApiStrings()
            )
            val (account, err) = AccountsAPI.createAccount(accountRequest)
            val newId = account?.id
            if (newId == null) {
                AccountEventEmitter.emit(
                    AccountEventName.PROFILE_UPDATE_FAILED,
                    AccountEventScreen.PERSONAL_INFORMATION,
                    detail = "$err"
                )
                reportUserError(userMessageForNetworkError(err))
                null
            } else {
                AccountEventEmitter.emit(
                    AccountEventName.PROFILE_UPDATED,
                    AccountEventScreen.PERSONAL_INFORMATION
                )
                setResolvedAccountId(newId)
                _onboardingData.value = _onboardingData.value.copy(resolvedAccountId = newId)
                updateStepUpRequirements(account.capabilities)
                beginOnboardingSessionIfNeeded()
                account
            }
        }
    }

    private fun updateStepUpRequirements(capabilities: List<CapabilityObjects.Capability>?) {
        capabilities ?: return
        val due = capabilities.flatMap { it.actionableRequirements }
        _onboardingData.update {
            it.copy(
                identityDocumentRequired = CapabilityObjects.CapabilityRequirementKey.IDENTITY_DOCUMENT in due,
                correctedKycDetailsRequired = CapabilityObjects.CapabilityRequirementKey.KYC in due
            )
        }
    }

    private suspend fun createCustomerIdentityForPersonalInfo(
        firstName: String,
        lastName: String,
        dob: String,
        email: String,
        ssn: String,
        billingAddress: FrameObjects.BillingAddress
    ): Boolean {
        val identityRequest = CustomerIdentityRequests.CreateCustomerIdentityRequest(
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dob,
            email = email,
            phoneNumber = _phoneNumber.value,
            ssn = ssn,
            address = billingAddress
        )
        val (identity, err) = CustomerIdentityAPI.createCustomerIdentity(identityRequest)
        if (identity != null) {
            _customerIdentity.value = identity
            _onboardingData.value = _onboardingData.value.copy(customerIdentityId = identity.id)
            return true
        }
        reportUserError(userMessageForNetworkError(err))
        return false
    }

    fun submitPersonalInfo(
        firstName: String,
        lastName: String,
        email: String,
        dobOverride: String?,
        ssnLastFour: String,
        addressLine1: String,
        addressLine2: String?,
        city: String,
        stateCode: String,
        postalCode: String,
        country: String
    ) {
        val dob = dobOverride ?: dateOfBirth
        _onboardingData.value = _onboardingData.value.copy(
            firstName = firstName,
            lastName = lastName,
            email = email,
            dateOfBirth = dob,
            ssnLast4 = ssnLastFour,
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            city = city,
            stateCode = stateCode,
            postalCode = postalCode,
            country = country,
            phoneNumber = _phoneNumber.value
        )

        if (!beginAction()) return
        viewModelScope.launch {
            try {
                val normalizedState = AddressSubregions.normalize(stateCode, country)
                val billingAddress = FrameObjects.BillingAddress(
                    city = city,
                    country = country,
                    state = normalizedState,
                    postalCode = postalCode,
                    addressLine1 = addressLine1,
                    addressLine2 = addressLine2
                )
                _onboardingData.update { it.copy(stateCode = normalizedState) }
                val account = upsertIndividualAccountForPersonalInfo(firstName, lastName, email, dob, ssnLastFour, billingAddress)
                    ?: return@launch
                // Nothing left the applicant can act on — don't advance into dead-end steps.
                blockedOutcome(account)?.let { outcome ->
                    concludeOnboarding(outcome)
                    return@launch
                }
                // Customer identity creation is deferred — handled by createCustomerIdentity()
                // separately in the flow once the account is established.
                val data = _onboardingData.value
                if (governmentIdRequired && !data.identityVerifiedViaGovId) {
                    verifyIdentityWithoutSsn(advanceOnVerified = true)
                } else {
                    moveNext()
                }
            } finally {
                endAction()
            }
        }
    }

    // region Government-ID (Persona) no-SSN verification

    /**
     * Ends onboarding early when the account has nothing left for the applicant to act on.
     * Truncates the flow to the terminal screen (or finishes immediately when the host opted out
     * of the completion screen), mirroring iOS `concludeOnboarding(with:)`.
     *
     * @return `true` when the terminal screen is showing; `false` when the host takes over.
     */
    @Suppress("UnusedReturnValue")
    private fun concludeOnboarding(outcome: OnboardingOutcome): Boolean {
        _finalOutcome.value = outcome
        val finalStep = if (config.showCompletionScreen) {
            OnboardingStep.VerificationSubmitted
        } else {
            navigationState.currentStep
        }
        _orderedSteps = listOf(finalStep)
        _flowSegments = listOf(finalStep.toFlowSegment())
        navigationState.goTo(finalStep)
        if (!config.showCompletionScreen) {
            viewModelScope.launch { finishOnboarding() }
            return false
        }
        return true
    }

    /**
     * The outcome to end on when this account has no road left, or `null` while one remains —
     * a dead end is a capability that still blocks onboarding but lists no work the applicant can do.
     */
    private fun blockedOutcome(account: AccountObjects.Account): OnboardingOutcome? {
        val capabilities = account.capabilities ?: return null
        // Only the host-required set (plus what it depends on) can block the flow — unrelated
        // outstanding capabilities on the account must not end onboarding early.
        val requiredNames = capabilitiesWithDependencies(originallyRequiredCapabilities)
            .map { it.apiValue }
            .toSet()
        val relevant = if (requiredNames.isEmpty()) {
            capabilities
        } else {
            capabilities.filter { requiredNames.contains(it.name) }
        }
        val outstanding = relevant.filter { it.isOutstanding }
        if (outstanding.isEmpty()) return null
        if (!outstanding.all { !it.hasActionableRequirements }) return null

        val outcome = OnboardingOutcome.resolve(capabilities, originallyRequiredCapabilities)
        return when (outcome) {
            is OnboardingOutcome.Declined, is OnboardingOutcome.ActionRequired -> {
                AccountEventEmitter.emit(
                    AccountEventName.ONBOARDING_BLOCKED,
                    AccountEventScreen.ONBOARDING,
                    detail = AccountEventDetail.ONBOARDING_BLOCKED_NOTHING_ACTIONABLE
                )
                _finalOutcome.value = outcome
                outcome
            }
            OnboardingOutcome.Approved, OnboardingOutcome.PendingReview -> null
        }
    }

    /**
     * Kicks off the no-SSN government-ID flow. Calls `POST /idv/session` to obtain a pre-created
     * Persona inquiry id, then publishes it via [personaInquiryToLaunch]. The personal-info view
     * observes that flow from a `LaunchedEffect` and drives the Persona SDK through
     * [launchPersonaInquiry], because the Persona `ActivityResultLauncher` is owned by the
     * composable's lifecycle and cannot be launched from the ViewModel directly.
     *
     * No-op if a verification is already in flight, if the customer is already verified, or if the
     * onboarding session has no `client_secret` — either a host-supplied one or a locally minted
     * `onb_sess_` secret (the IDV endpoints authenticate via it in the body).
     *
     * @param advanceOnVerified Move to the next step once verified — set when Continue runs a step-up.
     */
    fun verifyIdentityWithoutSsn(advanceOnVerified: Boolean = false) {
        if (_isVerifyingGovId.value) return
        if (_onboardingData.value.identityVerifiedViaGovId) return
        val clientSecret = idvClientSecret ?: run {
            reportUserError("Verification is unavailable for this session.")
            return
        }
        advanceAfterGovIdVerification = advanceOnVerified
        _isVerifyingGovId.value = true
        viewModelScope.launch {
            val (session, err) = IdvAPI.createSession(clientSecret)
            val inquiryId = session?.inquiryId?.takeIf { it.isNotBlank() }
            if (inquiryId == null) {
                _isVerifyingGovId.value = false
                reportUserError(userMessageForNetworkError(err))
                return@launch
            }
            // For a pre-existing account, createSession may return an already-approved inquiry, which
            // is terminal — the Persona SDK can't open a session on it and would fail. The server
            // reads inquiry status from Persona (works on terminal inquiries), so confirm first: if it
            // reports verified, mark the step done and skip the Persona launch entirely. Any other
            // outcome (not verified / error / null) falls through to the normal Persona flow.
            val (existing, existingErr) = IdvAPI.completeInquiry(clientSecret, inquiryId)
            if (existingErr == null && existing?.verified == true) {
                AccountEventEmitter.emit(
                    AccountEventName.STEP_UP_ALREADY_VERIFIED,
                    AccountEventScreen.IDENTITY_VERIFICATION,
                    detail = AccountEventDetail.STEP_UP_ALREADY_VERIFIED_SHORT_CIRCUIT
                )
                _isVerifyingGovId.value = false
                markVerifiedViaGovId(inquiryId)
                return@launch
            }
            // Hand the inquiry to the UI; the flag stays true until launch/completion resolves it.
            _personaInquiryToLaunch.value = PersonaInquiry(inquiryId, session?.sessionToken)
        }
    }

    private fun markVerifiedViaGovId(inquiryId: String) {
        _onboardingData.update {
            it.copy(identityVerifiedViaGovId = true, govIdInquiryId = inquiryId)
        }
        if (advanceAfterGovIdVerification) {
            advanceAfterGovIdVerification = false
            moveNext()
        }
    }

    /** Clears the pending inquiry id once the UI has launched the Persona SDK, so the effect won't re-fire. */
    fun clearPersonaInquiryToLaunch() {
        _personaInquiryToLaunch.value = null
    }

    /**
     * Clears the government-ID verified state so the applicant can re-run verification or enter an
     * SSN instead. Restores the SSN input and the "I don't have a social security number" button.
     *
     * Mirrors Frame-iOS's `resetIdentityVerification()`, which backs its "Use SSN instead" button.
     */
    fun resetIdentityVerification() {
        _onboardingData.update {
            it.copy(identityVerifiedViaGovId = false, govIdInquiryId = null)
        }
    }

    /**
     * Forwards the Persona `ActivityResult` callback from the host composable into the service so the
     * suspended [launchPersonaInquiry] round-trip can resume. Wire this into
     * `registerForActivityResult(Inquiry.Contract()) { onPersonaInquiryResult(it) }`.
     */
    fun onPersonaInquiryResult(response: com.withpersona.sdk2.inquiry.InquiryResponse) {
        personaService.onInquiryResult(response)
    }

    /**
     * Launches the Persona SDK for [inquiry] via a lifecycle-owned [launcher], awaits the
     * (best-effort) client outcome, then confirms with the server via `POST /idv/complete` — the
     * authoritative source of truth for flipping the UI to verified.
     *
     * @param inquiry Pre-created Persona inquiry from [verifyIdentityWithoutSsn].
     * @param launcher A launcher registered via `registerForActivityResult(Inquiry.Contract())`.
     */
    fun launchPersonaInquiry(
        inquiry: PersonaInquiry,
        launcher: androidx.activity.result.ActivityResultLauncher<com.withpersona.sdk2.inquiry.Inquiry>
    ) {
        clearPersonaInquiryToLaunch()
        // Must resolve the secret exactly as verifyIdentityWithoutSsn did — it already reached
        // /idv/session, so bailing here would strand the applicant with an inquiry that never opens.
        val clientSecret = idvClientSecret ?: run {
            _isVerifyingGovId.value = false
            reportUserError("Verification is unavailable for this session.")
            return
        }
        AccountEventEmitter.emit(
            AccountEventName.STEP_UP_STARTED,
            AccountEventScreen.IDENTITY_VERIFICATION,
            detail = AccountEventDetail.PERSONA_PROVIDER
        )
        viewModelScope.launch {
            try {
                val outcome = personaService.awaitResult(inquiry.inquiryId, inquiry.sessionToken, launcher)
                when (outcome) {
                    is PersonaVerificationResult.Completed -> {
                        // Server response — not the Persona callback — decides verification.
                        val (complete, err) = IdvAPI.completeInquiry(clientSecret, outcome.inquiryId)
                        if (complete?.verified == true) {
                            AccountEventEmitter.emit(
                                AccountEventName.STEP_UP_COMPLETED,
                                AccountEventScreen.IDENTITY_VERIFICATION
                            )
                            markVerifiedViaGovId(outcome.inquiryId)
                        } else if (err != null && !err.isTransport) {
                            AccountEventEmitter.emit(
                                AccountEventName.STEP_UP_UNAVAILABLE,
                                AccountEventScreen.IDENTITY_VERIFICATION,
                                detail = "${AccountEventDetail.STEP_UP_CATEGORY_TRANSIENT_PROVIDER_ERROR} — $err"
                            )
                            reportUserError(userMessageForNetworkError(err))
                        } else {
                            // Pending (JSON variant not live yet / transient) — leave unverified.
                            // category/status decide which event and message; a terminally
                            // declined applicant is told to contact support, not to retry a check
                            // that cannot succeed.
                            IdvCompletionRouting.emitStepUpFailure(complete)
                            reportUserError(IdvCompletionRouting.idvFailureMessage(complete))
                        }
                    }
                    is PersonaVerificationResult.Cancelled -> {
                        AccountEventEmitter.emit(
                            AccountEventName.STEP_UP_CANCELLED,
                            AccountEventScreen.IDENTITY_VERIFICATION,
                            detail = AccountEventDetail.STEP_UP_CANCELLED_BY_USER
                        )
                        // Required verification with a silent cancel leaves Continue looking dead.
                        reportUserError("Identity verification was cancelled.")
                    }
                    is PersonaVerificationResult.Failure -> {
                        AccountEventEmitter.emit(
                            AccountEventName.STEP_UP_FAILED,
                            AccountEventScreen.IDENTITY_VERIFICATION,
                            detail = AccountEventDetail.PERSONA_PROVIDER
                        )
                        // Never surface Persona's debugMessage to the applicant.
                        reportUserError(
                            "We couldn't verify your identity. Please try again or enter your Social Security Number."
                        )
                    }
                }
            } finally {
                _isVerifyingGovId.value = false
            }
        }
    }

    // endregion

    fun createIndividualAccount() {
        if (!beginAction()) return
        viewModelScope.launch {
            try {
            if (_resolvedAccountId.value != null) return@launch
            val d = _onboardingData.value
            val dob = d.dateOfBirth ?: dateOfBirth
            if (dob.isEmpty()) return@launch
            val billingAddress = FrameObjects.BillingAddress(
                city = d.city,
                country = d.country ?: "US",
                state = d.stateCode,
                postalCode = d.postalCode ?: "",
                addressLine1 = d.addressLine1,
                addressLine2 = d.addressLine2
            )
            val accountRequest = AccountRequests.CreateAccountRequest(
                type = AccountObjects.AccountType.INDIVIDUAL,
                termsOfService = termsOfServiceForCreate(),
                profile = AccountRequests.CreateAccountProfile(
                    individual = AccountRequests.CreateIndividualAccount(
                        name = AccountObjects.IndividualAccountName(
                            firstName = d.firstName ?: return@launch,
                            lastName = d.lastName ?: return@launch
                        ),
                        email = d.email ?: return@launch,
                        phone = AccountObjects.AccountPhoneNumber(
                            number = _phoneNumber.value,
                            countryCode = _phoneCountry.value.dialCode
                        ),
                        address = billingAddress,
                        birthdate = dob.ifEmpty { null },
                        ssnLast4 = d.ssnLast4?.ifEmpty { null }
                    )
                ),
                capabilities = requiredCapabilityApiStrings()
            )
            val (account, err) = AccountsAPI.createAccount(accountRequest)
            val id = account?.id
            if (id != null) {
                AccountEventEmitter.emit(
                    AccountEventName.PROFILE_UPDATED,
                    AccountEventScreen.PERSONAL_INFORMATION
                )
                setResolvedAccountId(id)
                _onboardingData.update { o -> o.copy(resolvedAccountId = id) }
                updateStepUpRequirements(account?.capabilities)
                beginOnboardingSessionIfNeeded()
            } else {
                AccountEventEmitter.emit(
                    AccountEventName.PROFILE_UPDATE_FAILED,
                    AccountEventScreen.PERSONAL_INFORMATION,
                    detail = "$err"
                )
                reportUserError(userMessageForNetworkError(err))
            }
            } finally {
                endAction()
            }
        }
    }

    fun updateExistingIndividualAccount() {
        if (!beginAction()) return
        viewModelScope.launch {
            try {
                val existing = _resolvedAccountId.value ?: return@launch
                val d = _onboardingData.value
                val dob = d.dateOfBirth ?: dateOfBirth
                val billingAddress = FrameObjects.BillingAddress(
                    city = d.city,
                    country = d.country ?: "US",
                    state = d.stateCode,
                    postalCode = d.postalCode ?: "",
                    addressLine1 = d.addressLine1,
                    addressLine2 = d.addressLine2
                )
                val updateIndividual = AccountRequests.UpdateIndividualAccount(
                    name = AccountRequests.UpdateAccountInfo(
                        firstName = d.firstName ?: return@launch,
                        middleName = null,
                        lastName = d.lastName ?: return@launch
                    ),
                    email = d.email ?: return@launch,
                    phone = AccountObjects.AccountPhoneNumber(
                        number = _phoneNumber.value,
                        countryCode = _phoneCountry.value.dialCode
                    ),
                    address = billingAddress,
                    birthdate = dob,
                    ssnLast4 = d.ssnLast4?.ifEmpty { null }
                )
                val (updated, updateErr) = AccountsAPI.updateAccount(
                    existing,
                    AccountRequests.UpdateAccountRequest(
                        termsOfService = termsOfServiceForUpdate(),
                        profile = AccountRequests.UpdateAccountProfile(individual = updateIndividual)
                    )
                )
                if (updateErr != null) {
                    AccountEventEmitter.emit(
                        AccountEventName.PROFILE_UPDATE_FAILED,
                        AccountEventScreen.PERSONAL_INFORMATION,
                        detail = "$updateErr"
                    )
                    reportUserError(userMessageForNetworkError(updateErr))
                } else {
                    AccountEventEmitter.emit(
                        AccountEventName.PROFILE_UPDATED,
                        AccountEventScreen.PERSONAL_INFORMATION
                    )
                    updateStepUpRequirements(updated?.capabilities)
                }
            } finally {
                endAction()
            }
        }
    }

    fun createCustomerIdentity() {
        if (!beginAction()) return
        viewModelScope.launch {
            try {
                val d = _onboardingData.value
                val dob = d.dateOfBirth ?: dateOfBirth
                if (dob.isEmpty()) return@launch
                val billingAddress = FrameObjects.BillingAddress(
                    city = d.city,
                    country = d.country ?: "US",
                    state = d.stateCode,
                    postalCode = d.postalCode ?: "",
                    addressLine1 = d.addressLine1,
                    addressLine2 = d.addressLine2
                )
                if (!createCustomerIdentityForPersonalInfo(
                        d.firstName ?: return@launch,
                        d.lastName ?: return@launch,
                        dob,
                        d.email ?: return@launch,
                        d.ssnLast4 ?: "",
                        billingAddress
                    )
                ) {
                    return@launch
                }
            } finally {
                endAction()
            }
        }
    }

    // endregion

    // region Payment methods

    fun onPaymentMethodSelected(id: String) {
        _onboardingData.value = _onboardingData.value.copy(selectedPaymentMethodId = id)
    }

    /**
     * Continue on a saved payment method. When address verification is required and the selected
     * card has no billing address, opens the address-only path instead of advancing.
     * Checks the selected **card** (`selectedPaymentMethodId`), not the payout method — iOS
     * historically read the wrong field here.
     *
     * @return `true` when the flow should advance to the next segment; `false` when the
     *   address-only screen was opened instead.
     */
    fun continueWithSelectedPaymentMethod(): Boolean {
        val selectedId = _onboardingData.value.selectedPaymentMethodId ?: return false
        val needsAddress = originallyRequiredCapabilities.contains(Capabilities.ADDRESS_VERIFICATION)
        val summary = _savedPaymentMethods.value.find { it.id == selectedId }
        if (needsAddress && summary != null && !summary.hasBillingAddress) {
            _onlyAddressVerification.value = true
            // Move into AddPaymentMethod which renders address-only when the flag is set.
            val addStep = orderedSteps.firstOrNull { it == OnboardingStep.AddPaymentMethod }
            if (addStep != null) {
                navigationState.goTo(addStep)
            } else {
                moveNext()
            }
            return false
        }
        _onlyAddressVerification.value = false
        return true
    }

    fun clearOnlyAddressVerification() {
        _onlyAddressVerification.value = false
    }

    /**
     * Updates the selected payment method's billing address (address-only verification path for
     * a saved card that is missing one).
     */
    fun updateSelectedPaymentMethodBillingAddress() {
        val paymentMethodId = _onboardingData.value.selectedPaymentMethodId ?: return
        if (!checkIfCustomerCanContinueWithPaymentMethod(onlyAddress = true)) {
            reportUserError("Please enter a complete billing address.")
            return
        }
        if (!beginAction()) return
        viewModelScope.launch {
            try {
                val b = _createdBillingAddress.value
                val billingAddress = FrameObjects.BillingAddress(
                    city = b.city ?: "",
                    country = b.country ?: "US",
                    state = b.state ?: "",
                    postalCode = b.postalCode,
                    addressLine1 = b.addressLine1 ?: "",
                    addressLine2 = b.addressLine2
                )
                val (_, err) = PaymentMethodsAPI.updatePaymentMethodWith(
                    paymentMethodId,
                    PaymentMethodRequests.UpdatePaymentMethodRequest(billing = billingAddress)
                )
                if (err != null) {
                    AccountEventEmitter.emit(
                        AccountEventName.BILLING_ADDRESS_UPDATE_FAILED,
                        AccountEventScreen.PAYMENT_METHOD,
                        detail = "$err"
                    )
                    reportUserError(userMessageForNetworkError(err))
                    return@launch
                }
                AccountEventEmitter.emit(
                    AccountEventName.BILLING_ADDRESS_UPDATED,
                    AccountEventScreen.PAYMENT_METHOD,
                    detail = AccountEventDetail.BILLING_ADDRESS_ONLY_VERIFICATION_PATH
                )
                _onlyAddressVerification.value = false
                clearAccountDetails()
                moveToNextSegment()
            } finally {
                endAction()
            }
        }
    }

    fun onPayoutMethodSelected(id: String) {
        _onboardingData.value = _onboardingData.value.copy(selectedPayoutMethodId = id)
    }

    /**
     * Continue on a saved payout method: elects it as the account's payout destination, then
     * calls [onElected]. Elected on Continue rather than on row tap, so browsing doesn't call the API.
     */
    fun electSelectedPayoutMethod(onElected: (String) -> Unit) {
        val id = _onboardingData.value.selectedPayoutMethodId ?: return
        if (!beginAction()) return
        viewModelScope.launch {
            try {
                if (electPayoutMethod(id)) onElected(id)
            } finally {
                endAction()
            }
        }
    }

    // Callers hold beginAction(); it is not reentrant, so this doesn't take it.
    private suspend fun electPayoutMethod(paymentMethodId: String): Boolean {
        val accountId = _resolvedAccountId.value ?: return false
        val (account, err) = AccountsAPI.electPayoutMethod(
            accountId,
            AccountRequests.ElectPayoutMethodRequest(paymentMethodId = paymentMethodId)
        )
        if (err != null || account == null) {
            AccountEventEmitter.emit(
                AccountEventName.PAYOUT_METHOD_ELECTION_FAILED,
                AccountEventScreen.PAYOUT_METHOD,
                detail = "$err"
            )
            reportUserError(userMessageForNetworkError(err))
            return false
        }
        AccountEventEmitter.emit(
            AccountEventName.PAYOUT_METHOD_ELECTED,
            AccountEventScreen.PAYOUT_METHOD,
            detail = AccountEventDetail.PAYOUT_METHOD_SET_AS_PRIMARY
        )
        _primaryPayoutMethodId.value = account.payoutPaymentMethodId ?: paymentMethodId
        return true
    }

    fun submitNewPaymentMethod() {
        if (_onlyAddressVerification.value) {
            updateSelectedPaymentMethodBillingAddress()
            return
        }
        if (!checkIfCustomerCanContinueWithPaymentMethod(onlyAddress = false)) {
            reportUserError("Please enter valid card details and a complete billing address.")
            return
        }
        val useEvervaultUi = _addPaymentUsesEvervaultCardUi.value
        val cardData = _paymentCardData.value
        val cardDraft = _paymentCardDraft.value
        val b = _createdBillingAddress.value

        val cardNumber: String
        val expMonth: String
        val expYear: String
        val cvc: String
        if (useEvervaultUi) {
            cardNumber = cardData.card.number.replace(" ", "")
            expMonth = cardData.card.expMonth
            expYear = cardData.card.expYear
            cvc = cardData.card.cvc
        } else {
            cardNumber = cardDraft.cardNumber.replace(" ", "")
            expMonth = cardDraft.expiryMonth
            expYear = cardDraft.expiryYear
            cvc = cardDraft.cvc
        }

        if (!beginAction()) return
        viewModelScope.launch {
            try {
                val acctId = _resolvedAccountId.value ?: return@launch
                val billingAddress = FrameObjects.BillingAddress(
                    city = b.city ?: "",
                    country = b.country ?: "US",
                    state = b.state ?: "",
                    postalCode = b.postalCode,
                    addressLine1 = b.addressLine1 ?: "",
                    addressLine2 = b.addressLine2
                )
                val pmRequest = PaymentMethodRequests.CreateCardPaymentMethodRequest(
                    cardNumber = cardNumber,
                    expMonth = expMonth,
                    expYear = expYear,
                    cvc = cvc,
                    customer = null,
                    account = acctId,
                    billing = billingAddress
                )
                val encryptPayload = useEvervaultUi && FrameNetworking.isEvervaultConfigured
                val (paymentMethod, pmErr) = PaymentMethodsAPI.createCardPaymentMethod(pmRequest, encryptData = encryptPayload)
                val paymentMethodId = paymentMethod?.id
                if (paymentMethod != null && paymentMethodId != null) {
                    AccountEventEmitter.emit(
                        AccountEventName.PAYMENT_METHOD_ADDED,
                        AccountEventScreen.PAYMENT_METHOD
                    )
                    _onboardingData.value = _onboardingData.value.copy(selectedPaymentMethodId = paymentMethodId)
                    _savedPaymentMethods.value += PaymentMethodSummary(
                                        id = paymentMethodId,
                                        brand = paymentMethod.card?.brand?.uppercase() ?: "",
                                        last4 = paymentMethod.card?.lastFourDigits ?: "",
                                        exp = "${paymentMethod.card?.expirationMonth}/${paymentMethod.card?.expirationYear?.takeLast(2)}",
                                        hasBillingAddress = true
                                    )
                    clearAccountDetails()
                    moveNext()
                } else {
                    AccountEventEmitter.emit(
                        AccountEventName.PAYMENT_METHOD_ADD_FAILED,
                        AccountEventScreen.PAYMENT_METHOD,
                        detail = "$pmErr"
                    )
                    reportUserError(userMessageForNetworkError(pmErr))
                }
            } finally {
                endAction()
            }
        }
    }

    fun submitNewPayoutMethod() {
        if (!checkIfCustomerCanContinueWithPayoutMethod()) return
        val draft = _bankAccountDraft.value
        val b = _createdBillingAddress.value
        if (!beginAction()) return
        viewModelScope.launch {
            try {
            val acctId = _resolvedAccountId.value ?: return@launch
            val achAccountType = if (draft.accountTypeLabel.lowercase() == "savings") {
                FrameObjects.PaymentAccountType.SAVINGS
            } else {
                FrameObjects.PaymentAccountType.CHECKING
            }
            val achRequest = PaymentMethodRequests.CreateACHPaymentMethodRequest(
                accountType = achAccountType,
                accountNumber = draft.accountNumber,
                routingNumber = draft.routingNumber,
                customer = null,
                account = acctId,
                billing = FrameObjects.BillingAddress(
                    addressLine1 = b.addressLine1 ?: "",
                    addressLine2 = b.addressLine2,
                    city = b.city ?: "",
                    state = b.state ?: "",
                    postalCode = b.postalCode,
                    country = b.country ?: "US"
                )
            )
            val (payoutMethod, achErr) = PaymentMethodsAPI.createACHPaymentMethod(achRequest)
            val payoutMethodId = payoutMethod?.id
            if (payoutMethod != null && payoutMethodId != null) {
                AccountEventEmitter.emit(
                    AccountEventName.PAYOUT_METHOD_ADDED,
                    AccountEventScreen.PAYOUT_METHOD,
                    detail = AccountEventDetail.PAYOUT_METHOD_MANUAL_ACH_PATH
                )
                _savedPayoutMethods.value += PaymentMethodSummary(
                                    id = payoutMethodId,
                                    brand = "BANK",
                                    last4 = payoutMethod.ach?.lastFour ?: "",
                                    exp = ""
                                )
                electPayoutMethod(payoutMethodId)
                // Selected only after the election: the standalone views report Completed on this.
                _onboardingData.value = _onboardingData.value.copy(selectedPayoutMethodId = payoutMethodId)
                clearAccountDetails()
                moveNext()
            } else {
                AccountEventEmitter.emit(
                    AccountEventName.PAYOUT_METHOD_ADD_FAILED,
                    AccountEventScreen.PAYOUT_METHOD,
                    detail = "$achErr"
                )
                reportUserError(userMessageForNetworkError(achErr))
            }
            } finally {
                endAction()
            }
        }
    }

    fun clearPlaidLinkToken() {
        _plaidLinkToken.value = null
    }

    fun onPlaidDismissed(errorMessage: String? = null) {
        if (errorMessage != null) {
            AccountEventEmitter.emit(
                AccountEventName.BANK_LINK_FAILED,
                AccountEventScreen.PAYOUT_METHOD,
                detail = errorMessage
            )
        } else {
            AccountEventEmitter.emit(
                AccountEventName.BANK_LINK_CANCELLED,
                AccountEventScreen.PAYOUT_METHOD,
                detail = AccountEventDetail.PLAID_USER_DISMISSED
            )
        }
        _isPerformingAction.value = false
    }

    fun fetchPlaidLinkToken() {
        val accountId = _resolvedAccountId.value ?: return
        if (_plaidLinkToken.value != null) return
        if (_isPerformingAction.value) return
        _isPerformingAction.value = true
        AccountEventEmitter.emit(
            AccountEventName.BANK_LINK_STARTED,
            AccountEventScreen.PAYOUT_METHOD,
            detail = AccountEventDetail.PLAID_PROVIDER
        )
        viewModelScope.launch {
            val service = buildPlaidService(accountId)
            service.fetchLinkToken()
            val token = service.linkToken.value
            if (token != null) {
                // Action stays active across the Plaid sheet; cleared by handlePlaidSuccess /
                // onPlaidDismissed once the user finishes or cancels the link flow.
                _plaidLinkToken.value = token
            } else {
                _isPerformingAction.value = false
                val err = (service.result.value as? PlaidLinkResult.Failure)?.error
                AccountEventEmitter.emit(
                    AccountEventName.BANK_LINK_FAILED,
                    AccountEventScreen.PAYOUT_METHOD,
                    detail = "$err"
                )
                reportUserError(userMessageForNetworkError(err))
            }
        }
    }

    fun handlePlaidSuccess(publicToken: String, plaidAccountId: String, institutionName: String?, subtype: String?) {
        val accountId = _resolvedAccountId.value ?: return
        if (plaidAccountId.isBlank()) {
            AccountEventEmitter.emit(
                AccountEventName.BANK_LINK_FAILED,
                AccountEventScreen.PAYOUT_METHOD,
                detail = "empty plaid account id"
            )
            reportUserError("We couldn't link that bank account. Please try again.")
            _isPerformingAction.value = false
            return
        }
        viewModelScope.launch {
            _isPerformingAction.value = true
            val service = buildPlaidService(accountId)
            service.connectBankAccount(publicToken, plaidAccountId, institutionName, subtype)
            _isPerformingAction.value = false
            when (val outcome = service.result.value) {
                is PlaidLinkResult.Success -> {
                    val payoutMethod = outcome.paymentMethod
                    val ach = payoutMethod.ach
                    @Suppress("USELESS_CAST")
                    val payoutMethodId = payoutMethod.id as String?
                    if (ach != null && payoutMethodId != null) {
                        AccountEventEmitter.emit(
                            AccountEventName.BANK_LINK_COMPLETED,
                            AccountEventScreen.PAYOUT_METHOD,
                            detail = AccountEventDetail.PLAID_PROVIDER
                        )
                        _savedPayoutMethods.value += PaymentMethodSummary(
                            id = payoutMethodId,
                            brand = "BANK",
                            last4 = ach.lastFour ?: "",
                            exp = ""
                        )
                        electPayoutMethod(payoutMethodId)
                        _onboardingData.update { it.copy(selectedPayoutMethodId = payoutMethodId) }
                        clearAccountDetails()
                        moveNext()
                    } else {
                        AccountEventEmitter.emit(
                            AccountEventName.BANK_LINK_FAILED,
                            AccountEventScreen.PAYOUT_METHOD,
                            detail = AccountEventDetail.PLAID_PROVIDER
                        )
                        reportUserError(userMessageForNetworkError(null))
                    }
                }
                is PlaidLinkResult.Failure -> {
                    AccountEventEmitter.emit(
                        AccountEventName.BANK_LINK_FAILED,
                        AccountEventScreen.PAYOUT_METHOD,
                        detail = "${outcome.error}"
                    )
                    reportUserError(userMessageForNetworkError(outcome.error))
                }
                else -> {}
            }
        }
    }

    // endregion

    // region 3DS

    fun initialize3DS() {
        if (!beginAction()) return
        viewModelScope.launch {
            try {
                val paymentMethodId = _onboardingData.value.selectedPaymentMethodId ?: return@launch
                val request = ThreeDSecureRequests.CreateThreeDSecureVerification(paymentMethodId = paymentMethodId)
                val (verification, verificationError, networkError) =
                    ThreeDSecureVerificationsAPI.create3DSecureVerification(request)
                when {
                    verification != null && !verification.id.isNullOrEmpty() -> {
                        _paymentMethodVerification.value = verification
                    }
                    verificationError?.error?.existingIntentId != null -> {
                        val intentId = verificationError.error?.existingIntentId ?: return@launch
                        retrieve3DSChallengeInternal(intentId)
                    }
                    networkError != null ->
                        reportUserError("Failed to initialize card verification. Please try again.")
                }
            } finally {
                endAction()
            }
        }
    }

    fun start3DSecureProcess() = initialize3DS()

    private suspend fun retrieve3DSChallengeInternal(verificationId: String) {
        val (retrieved, _) = ThreeDSecureVerificationsAPI.retrieve3DSecureVerification(verificationId)
        if (retrieved != null && !retrieved.id.isNullOrEmpty()) {
            _paymentMethodVerification.value = retrieved
        }
    }

    fun retrieve3DSChallenge(verificationId: String) {
        if (!beginAction()) return
        viewModelScope.launch {
            try {
                retrieve3DSChallengeInternal(verificationId)
            } finally {
                endAction()
            }
        }
    }

    fun resend3DS() {
        if (!beginAction()) return
        viewModelScope.launch {
            try {
                val id = _paymentMethodVerification.value?.id ?: return@launch
                val (verification, _) = ThreeDSecureVerificationsAPI.resend3DSecureVerification(id)
                if (verification != null && !verification.id.isNullOrEmpty()) {
                    _paymentMethodVerification.value = verification
                }
            } finally {
                endAction()
            }
        }
    }

    fun resend3DSChallenge() = resend3DS()

    // endregion

    fun submitCustomerIdentityForVerification() {
        if (!beginAction()) return
        viewModelScope.launch {
            try {
                val id = effectiveCustomerIdentityId() ?: return@launch
                val (updated, _) = CustomerIdentityAPI.submitForVerification(id)
                updated?.let { _customerIdentity.value = it }
            } finally {
                endAction()
            }
        }
    }

    /// Append a wallet-created payment method (Google Pay) to the in-memory list, select it, and
    /// advance — mirrors submitNewPaymentMethod()'s own explicit moveNext() after a successful
    /// add, rather than leaving the caller to call onBack()/onContinue() itself. In the onboarding
    /// step router that is what actually advances past this screen; in the standalone
    /// FrameAddPaymentMethodView it is a no-op result nothing reads, since that host instead
    /// reacts to onboardingData.selectedPaymentMethodId, set synchronously below.
    /// Used by `AddPaymentMethodScreen` after a successful `FrameGooglePayButton` AddToOwner flow.
    fun appendNewlyAddedPaymentMethod(paymentMethod: FrameObjects.PaymentMethod) {
        val paymentMethodId = paymentMethod.id ?: return
        _onboardingData.value = _onboardingData.value.copy(selectedPaymentMethodId = paymentMethodId)
        _savedPaymentMethods.value += PaymentMethodSummary(
            id = paymentMethodId,
            brand = paymentMethod.card?.brand?.uppercase() ?: "WALLET",
            last4 = paymentMethod.card?.lastFourDigits ?: "",
            exp = "${paymentMethod.card?.expirationMonth ?: ""}/${paymentMethod.card?.expirationYear?.takeLast(2) ?: ""}"
        )
        clearAccountDetails()
        moveNext()
    }

    // endregion

    fun generateTermsOfServiceToken() {
        viewModelScope.launch {
            val (r, err) = TermsOfServiceAPI.createToken()
            if (err != null) {
                AccountEventEmitter.emit(
                    AccountEventName.TERMS_OF_SERVICE_TOKEN_FAILED,
                    AccountEventScreen.TERMS_OF_SERVICE,
                    detail = "$err"
                )
            }
            _termsOfServiceToken.value = r?.token
        }
    }

    fun clearAccountDetails() {
        _paymentCardData.value = PaymentCardData()
        _paymentCardDraft.value = PaymentCardDraft()
        _addPaymentUsesEvervaultCardUi.value = true
        _createdBillingAddress.value = defaultCreatedBillingAddress
        _bankAccountDraft.value = BankAccountDraft()
    }

    /**
     * Validates add-card form from explicit snapshots.
     * Use from Compose with `remember(...)` keys that include [paymentCard], [cardDraft], and [useEvervaultCardInput].
     */
    fun isPaymentMethodFormComplete(
        paymentCard: PaymentCardData,
        cardDraft: PaymentCardDraft,
        billing: FrameObjects.BillingAddress,
        onlyAddress: Boolean = false,
        useEvervaultCardInput: Boolean = true
    ): Boolean {
        val addrOk = !billing.addressLine1.isNullOrBlank() && !billing.city.isNullOrBlank() &&
            !billing.state.isNullOrBlank() && (billing.postalCode?.length ?: 0) > 4
        if (!addrOk) return false
        if (onlyAddress) return true
        if (useEvervaultCardInput) return paymentCard.isValid
        val digits = cardDraft.cardNumber.replace(" ", "")
        val panOk = digits.length in 13..19
        val cvcOk = cardDraft.cvc.length in 3..4
        return panOk && cardDraft.expiryMonth.isNotEmpty() && cardDraft.expiryYear.isNotEmpty() && cvcOk
    }

    fun checkIfCustomerCanContinueWithPaymentMethod(onlyAddress: Boolean = false): Boolean =
        isPaymentMethodFormComplete(
            _paymentCardData.value,
            _paymentCardDraft.value,
            _createdBillingAddress.value,
            onlyAddress,
            _addPaymentUsesEvervaultCardUi.value
        )

    /** Use from Compose with `remember(bank, billing)` so Continue tracks form state while typing. */
    fun isPayoutMethodFormComplete(
        bank: BankAccountDraft,
        billing: FrameObjects.BillingAddress
    ): Boolean {
        val addrOk = !billing.addressLine1.isNullOrBlank() && !billing.city.isNullOrBlank() &&
            !billing.state.isNullOrBlank() && billing.postalCode?.length == 5
        if (!addrOk) return false
        return bank.routingNumber.length >= 9 && bank.accountNumber.isNotEmpty() && bank.accountTypeLabel.isNotEmpty()
    }

    fun checkIfCustomerCanContinueWithPayoutMethod(): Boolean =
        isPayoutMethodFormComplete(_bankAccountDraft.value, _createdBillingAddress.value)

    @Suppress("unused")
    fun createNewBusinessAccount() {}

    /**
     * Loads the account's saved cards and banks. Called when a select screen appears rather than
     * from `init`, which runs before the host's onboarding session is bound.
     */
    fun loadSavedPaymentMethods() {
        val accountId = _resolvedAccountId.value ?: return
        if (config.skipInitNetwork) return
        viewModelScope.launch {
            val (list, err) = PaymentMethodsAPI.getPaymentMethodsWithAccount(accountId)
            if (err != null) {
                AccountEventEmitter.emit(
                    AccountEventName.SAVED_PAYMENT_METHODS_LOAD_FAILED,
                    AccountEventScreen.PAYMENT_METHOD,
                    detail = "$err"
                )
            }
            _savedPaymentMethods.value = list
                ?.mapNotNull { pm ->
                    val pmId = pm.id ?: return@mapNotNull null
                    pm.card?.let { c ->
                        PaymentMethodSummary(
                            id = pmId,
                            brand = c.brand?.uppercase().orEmpty(),
                            last4 = c.lastFourDigits.orEmpty(),
                            exp = "${c.expirationMonth.orEmpty()}/${c.expirationYear?.takeLast(2).orEmpty()}",
                            hasBillingAddress = !pm.billing?.addressLine1.isNullOrBlank()
                        )
                    }
                }
                ?: emptyList()
            _savedPayoutMethods.value = list
                ?.filter { it.ach != null }
                ?.mapNotNull { pm ->
                    val pmId = pm.id ?: return@mapNotNull null
                    PaymentMethodSummary(
                        id = pmId,
                        brand = "BANK",
                        last4 = pm.ach?.lastFour ?: "",
                        exp = ""
                    )
                }
                ?: emptyList()
        }
    }
}
