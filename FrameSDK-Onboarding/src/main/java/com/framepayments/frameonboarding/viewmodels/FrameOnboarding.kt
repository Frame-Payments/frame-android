package com.framepayments.frameonboarding.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.classes.BankAccountDraft
import com.framepayments.frameonboarding.classes.OnboardingData
import com.framepayments.frameonboarding.classes.OnboardingFlowSegment
import com.framepayments.frameonboarding.classes.OnboardingResult
import com.framepayments.frameonboarding.classes.OnboardingState
import com.framepayments.frameonboarding.classes.OnboardingStep
import com.evervault.sdk.input.model.card.PaymentCardData
import com.framepayments.frameonboarding.classes.PaymentCardDraft
import com.framepayments.frameonboarding.classes.PaymentMethodSummary
import com.framepayments.frameonboarding.classes.computeFlowSegments
import com.framepayments.frameonboarding.classes.computeOrderedSteps
import com.framepayments.frameonboarding.classes.toFlowSegment
import com.framepayments.frameonboarding.networking.phoneotpverification.PhoneOTPVerificationAPI
import com.framepayments.frameonboarding.prove.ProveAuthService
import com.framepayments.framesdk.FileUpload
import com.framepayments.framesdk.FileUploadFieldName
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.accounts.AccountObjects
import com.framepayments.framesdk.accounts.AccountRequests
import com.framepayments.framesdk.accounts.AccountsAPI
import com.framepayments.framesdk.capabilities.CapabilitiesAPI
import com.framepayments.framesdk.capabilities.CapabilityRequests
import com.framepayments.framesdk.customeridentity.CustomerIdentityAPI
import com.framepayments.framesdk.customeridentity.CustomerIdentityRequests
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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.time.Instant

internal enum class VerifyIdSubStep { PhoneAuth, VerifyPhone, InformationForm }

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

    /** API string values for [_requiredCapabilities] (e.g. `"kyc_prefill"`). */
    private fun requiredCapabilityApiStrings(): List<String> =
        _requiredCapabilities.value.map { it.apiValue }

    private var _orderedSteps: List<OnboardingStep> = computeOrderedSteps(_requiredCapabilities.value)
    private var _flowSegments: List<OnboardingFlowSegment> = computeFlowSegments(_requiredCapabilities.value)

    val orderedSteps: List<OnboardingStep> get() = _orderedSteps
    val flowSegments: List<OnboardingFlowSegment> get() = _flowSegments

    val navigationState = OnboardingState(_orderedSteps.first())

    // Onboarding data
    private val _onboardingData = MutableStateFlow(OnboardingData())
    val onboardingData: StateFlow<OnboardingData> = _onboardingData.asStateFlow()

    // Resolved account ID (may start null if config has none, set after account creation)
    private val _resolvedAccountId = MutableStateFlow(config.accountId)
    val resolvedAccountId: StateFlow<String?> = _resolvedAccountId.asStateFlow()

    // Payment methods loaded for the account
    private val _savedPaymentMethods = MutableStateFlow<List<PaymentMethodSummary>>(emptyList())
    val savedPaymentMethods: StateFlow<List<PaymentMethodSummary>> = _savedPaymentMethods.asStateFlow()

    private val _savedPayoutMethods = MutableStateFlow<List<PaymentMethodSummary>>(emptyList())
    val savedPayoutMethods: StateFlow<List<PaymentMethodSummary>> = _savedPayoutMethods.asStateFlow()

    private val _plaidLinkToken = MutableStateFlow<String?>(null)
    val plaidLinkToken: StateFlow<String?> = _plaidLinkToken.asStateFlow()

    private val _isConnectingPlaidBank = MutableStateFlow(false)
    val isConnectingPlaidBank: StateFlow<Boolean> = _isConnectingPlaidBank.asStateFlow()

    // Result to emit to the host (Completed / Cancelled only — errors stay in-flow via [userErrorMessage])
    private val _result = MutableStateFlow<OnboardingResult?>(null)
    val result: StateFlow<OnboardingResult?> = _result.asStateFlow()

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
        return when (err) {
            is NetworkingError.ServerError ->
                err.errorDescription.ifBlank { "Request failed (${err.statusCode})." }
            NetworkingError.DecodingFailed -> "Invalid response from server. Please try again."
            NetworkingError.InvalidURL -> "Configuration error. Please try again later."
            NetworkingError.UnknownError -> "Something went wrong. Please try again."
        }
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
    private var proveOtpDeferred: CompletableDeferred<String>? = null
    private var proveAuthLaunchStarted: Boolean = false

    /** True while fetching GET account after phone verification succeeds (before personal info step). */
    private val _awaitingAccountProfileRefresh = MutableStateFlow(false)
    val awaitingAccountProfileRefresh: StateFlow<Boolean> = _awaitingAccountProfileRefresh.asStateFlow()

    private val _termsOfServiceToken = MutableStateFlow<String?>(null)
    /** Mirrors iOS `termsOfServiceToken`. */
    val termsOfServiceToken: StateFlow<String?> = _termsOfServiceToken.asStateFlow()

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
            return if (y.length == 4 && m.length == 2 && d.length == 2) "$y-$m-$d" else ""
        }

    val dobComplete: Boolean
        get() = _dobMonth.value.length == 2 && _dobDay.value.length == 2 && _dobYear.value.length == 4

    init {
        if (_tosTokenDeferred != null) {
            viewModelScope.launch {
                _termsOfServiceToken.value = _tosTokenDeferred.await()
            }
        }
        if (config.accountId != null && !config.skipInitNetwork) {
            loadPaymentMethods(config.accountId)
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

    fun updateOnboardingFlow() {
        val caps = _requiredCapabilities.value
        _orderedSteps = computeOrderedSteps(caps)
        _flowSegments = computeFlowSegments(caps)
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
            if (cap.currentlyDue.isNullOrEmpty()) {
                mutable.removeAll { it == enumCap }
            }
        }
        _requiredCapabilities.value = mutable
        updateOnboardingFlow()
    }

    suspend fun checkExistingAccount(updateCapabilities: Boolean = false, depth: Int = 0) {
        val accountId = _resolvedAccountId.value ?: return
        val (account, _) = AccountsAPI.getAccountWith(accountId, forTesting = false)
        account?.id?.let { aid ->
            _resolvedAccountId.value = aid
            _onboardingData.update { it.copy(resolvedAccountId = aid) }
        }
        val individual = account?.profile?.individual ?: return
        refreshAccountProfileIntoOnboarding(accountId)
        if (!updateCapabilities) return
        val caps = account.capabilities ?: return
        val requiredNames = requiredCapabilityApiStrings().toSet()
        val accountNames = caps.map { it.name }.toSet()
        val hasSuperset = requiredNames.all { accountNames.contains(it) }
        if (!hasSuperset) {
            if (depth >= 3) return
            CapabilitiesAPI.requestCapabilities(
                accountId,
                CapabilityRequests.RequestCapabilitiesRequest(capabilities = requiredCapabilityApiStrings())
            )
            checkExistingAccount(updateCapabilities = true, depth = depth + 1)
            return
        }
        updateCapabilitiesBasedOnCompletion(caps)
    }

    fun launchCheckExistingAccount(updateCapabilities: Boolean) {
        viewModelScope.launch {
            checkExistingAccount(updateCapabilities = updateCapabilities)
        }
    }

    // region Navigation

    fun moveNext() {
        val i = orderedSteps.indexOf(navigationState.currentStep)
        if (i < 0 || i >= orderedSteps.size - 1) {
            _result.value = OnboardingResult.Completed(
                paymentMethodId = _onboardingData.value.selectedPaymentMethodId
            )
        } else {
            navigationState.goTo(orderedSteps[i + 1])
        }
    }

    fun moveBack() {
        val i = orderedSteps.indexOf(navigationState.currentStep)
        if (i > 0) navigationState.goTo(orderedSteps[i - 1])
    }

    // endregion

    // region Phone + DOB inputs

    fun onPhoneNumberChanged(value: String) {
        _phoneNumber.value = value.filter(Char::isDigit).take(10)
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

    private suspend fun refreshAccountProfileIntoOnboarding(accountId: String) {
        val (account, _) = AccountsAPI.getAccountWith(accountId, forTesting = false)
        val individual = account?.profile?.individual
        if (individual == null) {
            _onboardingData.update { it.copy(resolvedAccountId = account?.id ?: accountId) }
            return
        }
        val addr = individual.address
        val phoneDigits = individual.phoneNumber?.filter(Char::isDigit)?.takeLast(10)
        if (!phoneDigits.isNullOrEmpty()) {
            _phoneNumber.value = phoneDigits
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
        viewModelScope.launch {
            val acctId = _resolvedAccountId.value ?: run {
                val accountRequest = AccountRequests.CreateAccountRequest(
                    type = AccountObjects.AccountType.INDIVIDUAL,
                    termsOfService = termsOfServiceForCreate(),
                    profile = AccountRequests.CreateAccountProfile(
                        individual = AccountRequests.CreateIndividualAccount(
                            phone = AccountObjects.AccountPhoneNumber(
                                number = _phoneNumber.value,
                                countryCode = "+1"
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
                    _resolvedAccountId.value = id
                    _onboardingData.value = _onboardingData.value.copy(resolvedAccountId = id)
                    id
                }
            } ?: return@launch

            val (result, verifyErr) = PhoneOTPVerificationAPI.createVerification(
                accountId = acctId,
                phoneNumber = _phoneNumber.value,
                dateOfBirth = dateOfBirth
            )
            _pendingVerificationId.value = result?.id
            _pendingProveAuthToken.value = result?.proveAuthToken
            proveAuthLaunchStarted = false
            if (result?.id != null) {
                _verifyPhoneUi.value = if (result.proveAuthToken != null) {
                    VerifyPhoneUi.LoadingProve
                } else {
                    VerifyPhoneUi.OtpFrameApi
                }
                _verifyIdSubStep.value = VerifyIdSubStep.VerifyPhone
            } else {
                reportUserError(userMessageForNetworkError(verifyErr))
            }
        }
    }

    fun resendVerificationCode() {
        viewModelScope.launch {
            val acctId = _resolvedAccountId.value ?: return@launch
            val (_, err) = PhoneOTPVerificationAPI.createVerification(
                accountId = acctId,
                phoneNumber = _phoneNumber.value,
                dateOfBirth = dateOfBirth
            )
            if (err != null) reportUserError(userMessageForNetworkError(err))
        }
    }

    fun startProveAuth(context: Context) {
        if (proveAuthLaunchStarted) return
        proveAuthLaunchStarted = true
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
                val deferred = CompletableDeferred<String>()
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
            try {
                val success = service.authenticateWith(authToken)
                if (success) {
                    _pendingVerificationId.value = null
                    _pendingProveAuthToken.value = null
                    finalizePhoneVerificationAndShowPersonalInfo(acctId)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                synchronized(proveOtpLock) {
                    proveOtpDeferred?.cancel(CancellationException("Prove auth failed"))
                    proveOtpDeferred = null
                }
                _verifyPhoneUi.value = VerifyPhoneUi.OtpFrameApi
            } finally {
                proveAuthLaunchStarted = false
            }
        }
    }

    fun submitOtpToProveSdk(code: String) {
        synchronized(proveOtpLock) {
            proveOtpDeferred?.complete(code)
            proveOtpDeferred = null
        }
    }

    fun confirmVerificationCode(code: String) {
        viewModelScope.launch {
            val acctId = _resolvedAccountId.value ?: return@launch
            val verificationId = _pendingVerificationId.value ?: return@launch
            val (_, err) = PhoneOTPVerificationAPI.confirmVerification(acctId, verificationId, code)
            if (err == null) {
                _pendingVerificationId.value = null
                _pendingProveAuthToken.value = null
                finalizePhoneVerificationAndShowPersonalInfo(acctId)
            } else {
                reportUserError(userMessageForNetworkError(err))
            }
        }
    }

    fun goBackFromVerifyPhone() {
        synchronized(proveOtpLock) {
            proveOtpDeferred?.cancel(CancellationException("user navigated back"))
            proveOtpDeferred = null
        }
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
    ): String? {
        return _resolvedAccountId.value?.let { existing ->
            val updateIndividual = AccountRequests.UpdateIndividualAccount(
                name = AccountRequests.UpdateAccountInfo(
                    firstName = firstName,
                    middleName = null,
                    lastName = lastName
                ),
                email = email,
                phoneNumber = _phoneNumber.value,
                phoneCountryCode = "+1",
                address = billingAddress,
                birthdate = dob,
                ssnLast4 = ssnLastFour.ifEmpty { null }
            )
            val (_, err) = AccountsAPI.updateAccount(
                existing,
                AccountRequests.UpdateAccountRequest(
                    profile = AccountRequests.UpdateAccountProfile(individual = updateIndividual)
                )
            )
            if (err != null) {
                reportUserError(userMessageForNetworkError(err))
                null
            } else {
                existing
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
                            countryCode = "+1"
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
                reportUserError(userMessageForNetworkError(err))
                null
            } else {
                _resolvedAccountId.value = newId
                _onboardingData.value = _onboardingData.value.copy(resolvedAccountId = newId)
                newId
            }
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

        viewModelScope.launch {
            val billingAddress = FrameObjects.BillingAddress(
                city = city,
                country = country,
                state = stateCode,
                postalCode = postalCode,
                addressLine1 = addressLine1,
                addressLine2 = addressLine2
            )
            upsertIndividualAccountForPersonalInfo(firstName, lastName, email, dob, ssnLastFour, billingAddress)
                ?: return@launch
//            if (!createCustomerIdentityForPersonalInfo(firstName, lastName, dob, email, ssnLastFour, billingAddress)) {
//                return@launch
//            }
            moveNext()
        }
    }

    fun createIndividualAccount() {
        viewModelScope.launch {
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
                            countryCode = "+1"
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
                _resolvedAccountId.value = id
                _onboardingData.update { o -> o.copy(resolvedAccountId = id) }
            } else {
                reportUserError(userMessageForNetworkError(err))
            }
        }
    }

    fun updateExistingIndividualAccount() {
        viewModelScope.launch {
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
                phoneNumber = _phoneNumber.value,
                phoneCountryCode = "+1",
                address = billingAddress,
                birthdate = dob,
                ssnLast4 = d.ssnLast4?.ifEmpty { null }
            )
            val (_, updateErr) = AccountsAPI.updateAccount(
                existing,
                AccountRequests.UpdateAccountRequest(
                    profile = AccountRequests.UpdateAccountProfile(individual = updateIndividual)
                )
            )
            if (updateErr != null) reportUserError(userMessageForNetworkError(updateErr))
        }
    }

    fun createCustomerIdentity() {
        viewModelScope.launch {
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
        }
    }

    // endregion

    // region Payment methods

    fun onPaymentMethodSelected(id: String) {
        _onboardingData.value = _onboardingData.value.copy(selectedPaymentMethodId = id)
    }

    fun onPayoutMethodSelected(id: String) {
        _onboardingData.value = _onboardingData.value.copy(selectedPayoutMethodId = id)
    }

    fun submitNewPaymentMethod() {
        if (!checkIfCustomerCanContinueWithPaymentMethod(onlyAddress = false)) return
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
        val useForPayouts = cardDraft.useForPayouts
        val payoutIdForBilling = _onboardingData.value.selectedPayoutMethodId

        viewModelScope.launch {
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
            if (paymentMethod != null) {
                _onboardingData.value = _onboardingData.value.copy(selectedPaymentMethodId = paymentMethod.id)
                _savedPaymentMethods.value += PaymentMethodSummary(
                                    id = paymentMethod.id,
                                    brand = paymentMethod.card?.brand?.uppercase() ?: "",
                                    last4 = paymentMethod.card?.lastFourDigits ?: "",
                                    exp = "${paymentMethod.card?.expirationMonth}/${paymentMethod.card?.expirationYear?.takeLast(2)}"
                                )
                if (useForPayouts && payoutIdForBilling != null) {
                    val (_, billingErr) = PaymentMethodsAPI.updatePaymentMethodWith(
                        payoutIdForBilling,
                        PaymentMethodRequests.UpdatePaymentMethodRequest(billing = billingAddress)
                    )
                    if (billingErr != null) {
                        reportUserError(userMessageForNetworkError(billingErr))
                        return@launch
                    }
                }
                clearAccountDetails()
                moveNext()
            } else {
                reportUserError(userMessageForNetworkError(pmErr))
            }
        }
    }

    fun submitNewPayoutMethod() {
        if (!checkIfCustomerCanContinueWithPayoutMethod()) return
        val draft = _bankAccountDraft.value
        val b = _createdBillingAddress.value
        viewModelScope.launch {
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
            if (payoutMethod != null) {
                _onboardingData.value = _onboardingData.value.copy(selectedPayoutMethodId = payoutMethod.id)
                _savedPayoutMethods.value += PaymentMethodSummary(
                                    id = payoutMethod.id,
                                    brand = "BANK",
                                    last4 = payoutMethod.ach?.lastFour ?: "",
                                    exp = ""
                                )
                clearAccountDetails()
                moveNext()
            } else {
                reportUserError(userMessageForNetworkError(achErr))
            }
        }
    }

    fun clearPlaidLinkToken() {
        _plaidLinkToken.value = null
    }

    fun fetchPlaidLinkToken() {
        val accountId = _resolvedAccountId.value ?: return
        if (_isConnectingPlaidBank.value || _plaidLinkToken.value != null) return
        _isConnectingPlaidBank.value = true
        viewModelScope.launch {
            val (response, err) = AccountsAPI.getPlaidLinkToken(accountId)
            if (response?.linkToken != null) {
                _plaidLinkToken.value = response.linkToken
            } else {
                _isConnectingPlaidBank.value = false
                reportUserError(userMessageForNetworkError(err))
            }
        }
    }

    fun handlePlaidSuccess(publicToken: String, plaidAccountId: String, institutionName: String?, subtype: String?) {
        val accountId = _resolvedAccountId.value ?: return
        viewModelScope.launch {
            _isConnectingPlaidBank.value = true
            val request = com.framepayments.framesdk.paymentmethods.PaymentMethodRequests.ConnectPlaidBankAccountRequest(
                account = accountId,
                publicToken = publicToken,
                accountId = plaidAccountId,
                institutionName = institutionName,
                subtype = subtype
            )
            val (payoutMethod, err) = PaymentMethodsAPI.connectPlaidBankAccount(request)
            _isConnectingPlaidBank.value = false
            val ach = payoutMethod?.ach
            if (payoutMethod != null && ach != null) {
                _onboardingData.update { it.copy(selectedPayoutMethodId = payoutMethod.id) }
                _savedPayoutMethods.value += PaymentMethodSummary(
                    id = payoutMethod.id,
                    brand = "BANK",
                    last4 = ach.lastFour ?: "",
                    exp = ""
                )
                clearAccountDetails()
                moveNext()
            } else {
                reportUserError(userMessageForNetworkError(err))
            }
        }
    }

    // endregion

    // region 3DS

    fun initialize3DS() {
        viewModelScope.launch {
            val paymentMethodId = _onboardingData.value.selectedPaymentMethodId ?: return@launch
            val request = ThreeDSecureRequests.CreateThreeDSecureVerification(paymentMethodId = paymentMethodId)
            val (verification, verificationError, networkError) =
                ThreeDSecureVerificationsAPI.create3DSecureVerification(request)
            when {
                verification != null && verification.id.isNotEmpty() -> {
                    _paymentMethodVerification.value = verification
                }
                verificationError?.error?.existingIntentId != null -> {
                    val intentId = verificationError.error.existingIntentId ?: return@launch
                    retrieve3DSChallengeInternal(intentId)
                }
                networkError != null ->
                    reportUserError("Failed to initialize card verification. Please try again.")
            }
        }
    }

    fun start3DSecureProcess() = initialize3DS()

    private suspend fun retrieve3DSChallengeInternal(verificationId: String) {
        val (retrieved, _) = ThreeDSecureVerificationsAPI.retrieve3DSecureVerification(verificationId)
        if (retrieved != null && retrieved.id.isNotEmpty()) {
            _paymentMethodVerification.value = retrieved
        }
    }

    fun retrieve3DSChallenge(verificationId: String) {
        viewModelScope.launch { retrieve3DSChallengeInternal(verificationId) }
    }

    fun resend3DS() {
        viewModelScope.launch {
            val id = _paymentMethodVerification.value?.id ?: return@launch
            val (verification, _) = ThreeDSecureVerificationsAPI.resend3DSecureVerification(id)
            if (verification != null && verification.id.isNotEmpty()) {
                _paymentMethodVerification.value = verification
            }
        }
    }

    fun resend3DSChallenge() = resend3DS()

    // endregion

    // region Document upload

    fun onFrontPhotoSelected(uri: Uri?) {
        _onboardingData.value = _onboardingData.value.copy(frontPhotoUri = uri)
    }

    fun onBackPhotoSelected(uri: Uri?) {
        _onboardingData.value = _onboardingData.value.copy(backPhotoUri = uri)
    }

    fun onSelfieSelected(uri: Uri?) {
        _onboardingData.value = _onboardingData.value.copy(selfieUri = uri)
    }

    fun uploadIdentificationDocuments(context: Context) {
        viewModelScope.launch { performUploadIdentificationDocuments(context) }
    }

    fun uploadIdentificationDocumentsThenContinue(context: Context) {
        viewModelScope.launch {
            if (performUploadIdentificationDocuments(context)) {
                moveNext()
            }
        }
    }

    private suspend fun performUploadIdentificationDocuments(context: Context): Boolean {
        val identityId = effectiveCustomerIdentityId() ?: run {
            reportUserError("Your profile isn't ready for document upload. Please try again.")
            return false
        }
        val data = _onboardingData.value
        val frontUri = data.frontPhotoUri ?: return false
        val backUri = data.backPhotoUri ?: return false
        val selfieUri = data.selfieUri ?: return false

        fun uriToBitmap(uri: Uri): android.graphics.Bitmap? = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                android.graphics.ImageDecoder.decodeBitmap(
                    android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                )
            } else {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) { null }

        val frontBitmap = uriToBitmap(frontUri)
        val backBitmap = uriToBitmap(backUri)
        val selfieBitmap = uriToBitmap(selfieUri)
        if (frontBitmap == null || backBitmap == null || selfieBitmap == null) {
            reportUserError("Couldn't read one or more photos. Please try again.")
            return false
        }

        val uploads = listOf(
            FileUpload(frontBitmap, FileUploadFieldName.FRONT),
            FileUpload(backBitmap, FileUploadFieldName.BACK),
            FileUpload(selfieBitmap, FileUploadFieldName.SELFIE)
        )
        frontBitmap.recycle()
        backBitmap.recycle()
        selfieBitmap.recycle()

        val (updated, err) = CustomerIdentityAPI.uploadIdentityDocuments(identityId, uploads)
        if (updated != null) {
            _customerIdentity.value = updated
            return true
        }
        reportUserError(userMessageForNetworkError(err))
        return false
    }

    fun submitCustomerIdentityForVerification() {
        viewModelScope.launch {
            val id = effectiveCustomerIdentityId() ?: return@launch
            val (updated, _) = CustomerIdentityAPI.submitForVerification(id)
            updated?.let { _customerIdentity.value = it }
        }
    }

    // endregion

    fun generateTermsOfServiceToken() {
        viewModelScope.launch {
            val (r, _) = TermsOfServiceAPI.createToken()
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
            !billing.state.isNullOrBlank() && billing.postalCode.length > 4
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
            !billing.state.isNullOrBlank() && billing.postalCode.length == 5
        if (!addrOk) return false
        return bank.routingNumber.length >= 9 && bank.accountNumber.isNotEmpty() && bank.accountTypeLabel.isNotEmpty()
    }

    fun checkIfCustomerCanContinueWithPayoutMethod(): Boolean =
        isPayoutMethodFormComplete(_bankAccountDraft.value, _createdBillingAddress.value)

    fun checkIfCustomerCanContinueWithDocs(): Boolean {
        val d = _onboardingData.value
        return d.frontPhotoUri != null && d.backPhotoUri != null && d.selfieUri != null
    }

    @Suppress("unused")
    fun createNewBusinessAccount() {}

    private fun loadPaymentMethods(accountId: String) {
        viewModelScope.launch {
            val (list, _) = PaymentMethodsAPI.getPaymentMethodsWithAccount(accountId)
            _savedPaymentMethods.value = list
                ?.mapNotNull { pm ->
                    pm.card?.let { c ->
                        PaymentMethodSummary(
                            id = pm.id,
                            brand = c.brand.uppercase(),
                            last4 = c.lastFourDigits,
                            exp = "${c.expirationMonth}/${c.expirationYear.takeLast(2)}"
                        )
                    }
                }
                ?: emptyList()
            _savedPayoutMethods.value = list
                ?.filter { it.ach != null }
                ?.map { pm ->
                    PaymentMethodSummary(
                        id = pm.id,
                        brand = "BANK",
                        last4 = pm.ach?.lastFour ?: "",
                        exp = ""
                    )
                }
                ?: emptyList()
        }
    }
}
