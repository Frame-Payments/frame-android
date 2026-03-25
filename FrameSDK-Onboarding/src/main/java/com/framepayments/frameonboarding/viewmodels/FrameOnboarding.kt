package com.framepayments.frameonboarding.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.classes.OnboardingData
import com.framepayments.frameonboarding.classes.OnboardingResult
import com.framepayments.frameonboarding.classes.OnboardingState
import com.framepayments.frameonboarding.classes.OnboardingStep
import com.framepayments.frameonboarding.classes.PaymentMethodDetails
import com.framepayments.frameonboarding.classes.PaymentMethodSummary
import com.framepayments.frameonboarding.classes.PayoutMethodDetails
import com.framepayments.frameonboarding.classes.computeFlowSegments
import com.framepayments.frameonboarding.classes.computeOrderedSteps
import com.framepayments.frameonboarding.networking.phoneotpverification.PhoneOTPVerificationAPI
import com.framepayments.frameonboarding.prove.ProveAuthService
import com.framepayments.framesdk.FileUpload
import com.framepayments.framesdk.FileUploadFieldName
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.accounts.AccountObjects
import com.framepayments.framesdk.accounts.AccountRequests
import com.framepayments.framesdk.accounts.AccountsAPI
import com.framepayments.framesdk.capabilities.CapabilitiesAPI
import com.framepayments.framesdk.capabilities.CapabilityRequests
import com.framepayments.framesdk.customeridentity.CustomerIdentityAPI
import com.framepayments.framesdk.customeridentity.CustomerIdentityRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
import com.framepayments.framesdk.termsofservice.TermsOfServiceAPI
import com.framepayments.framesdk.threedsecure.ThreeDSecureRequests
import com.framepayments.framesdk.threedsecure.ThreeDSecureVerificationsAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal enum class VerifyIdSubStep { PhoneAuth, VerifyPhone, InformationForm }

internal class FrameOnboardingViewModel(private val config: OnboardingConfig) : ViewModel() {

    val orderedSteps = computeOrderedSteps(config.requiredCapabilities)
    val flowSegments = computeFlowSegments(config.requiredCapabilities)
    val navigationState = OnboardingState(orderedSteps.first())

    // Onboarding data
    private val _onboardingData = MutableStateFlow(OnboardingData())
    val onboardingData: StateFlow<OnboardingData> = _onboardingData.asStateFlow()

    // Resolved account ID (may start null if config has none, set after account creation)
    private val _resolvedAccountId = MutableStateFlow(config.accountId ?: config.customerId)
    val resolvedAccountId: StateFlow<String?> = _resolvedAccountId.asStateFlow()

    // Payment methods loaded for the account
    private val _savedPaymentMethods = MutableStateFlow<List<PaymentMethodSummary>>(emptyList())
    val savedPaymentMethods: StateFlow<List<PaymentMethodSummary>> = _savedPaymentMethods.asStateFlow()

    private val _savedPayoutMethods = MutableStateFlow<List<PaymentMethodSummary>>(emptyList())
    val savedPayoutMethods: StateFlow<List<PaymentMethodSummary>> = _savedPayoutMethods.asStateFlow()

    // Result to emit to the host
    private val _result = MutableStateFlow<OnboardingResult?>(null)
    val result: StateFlow<OnboardingResult?> = _result.asStateFlow()

    // Phone OTP step state
    private val _tosToken = MutableStateFlow<String?>(null)
    val tosToken: StateFlow<String?> = _tosToken.asStateFlow()

    private val _verifyIdSubStep = MutableStateFlow(VerifyIdSubStep.PhoneAuth)
    val verifyIdSubStep: StateFlow<VerifyIdSubStep> = _verifyIdSubStep.asStateFlow()

    private val _pendingVerificationId = MutableStateFlow<String?>(null)
    private val _pendingProveAuthToken = MutableStateFlow<String?>(null)
    val pendingProveAuthToken: StateFlow<String?> = _pendingProveAuthToken.asStateFlow()

    // 3DS state
    private val _threeDSVerificationId = MutableStateFlow<String?>(null)
    private var customerIdentityId: String? = null

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
        if (config.requiredCapabilities.contains(Capabilities.GEO_COMPLIANCE)) {
            viewModelScope.launch {
                val (response, _) = TermsOfServiceAPI.createToken()
                _tosToken.value = response?.token
            }
        }
        val accountId = config.accountId ?: config.customerId
        if (accountId != null) {
            loadPaymentMethods(accountId)
        }
    }

    // region Navigation

    fun moveNext() {
        val i = orderedSteps.indexOf(navigationState.currentStep)
        if (i < 0 || i >= orderedSteps.size - 1) {
            _result.value = OnboardingResult.Completed(
                paymentMethodId = _onboardingData.value.selectedPaymentMethodId,
                onboardingSessionId = config.sessionId
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

    // region Phone OTP flow

    fun submitPhoneAuth(requiresDateOfBirth: Boolean) {
        viewModelScope.launch {
            val acctId = _resolvedAccountId.value ?: run {
                val accountRequest = AccountRequests.CreateAccountRequest(
                    type = AccountObjects.AccountType.INDIVIDUAL,
                    termsOfService = _tosToken.value?.let {
                        AccountObjects.AccountTermsOfService(token = it)
                    },
                    profile = AccountRequests.CreateAccountProfile(
                        individual = AccountRequests.CreateIndividualAccount(
                            name = AccountRequests.CreateAccountInfo(
                                firstName = "",
                                lastName = ""
                            ),
                            email = "",
                            phone = AccountObjects.AccountPhoneNumber(
                                number = _phoneNumber.value,
                                countryCode = "+1"
                            ),
                            dob = dateOfBirth.ifEmpty { null }
                        )
                    )
                )
                val (account, _) = AccountsAPI.createAccount(accountRequest)
                _resolvedAccountId.value = account?.id
                _onboardingData.value = _onboardingData.value.copy(resolvedAccountId = account?.id)
                account?.id
            } ?: return@launch

            val (result, _) = PhoneOTPVerificationAPI.createVerification(
                accountId = acctId,
                phoneNumber = _phoneNumber.value,
                dateOfBirth = dateOfBirth
            )
            _pendingVerificationId.value = result?.id
            _pendingProveAuthToken.value = result?.proveAuthToken
            _verifyIdSubStep.value = if (result?.id != null) {
                VerifyIdSubStep.VerifyPhone
            } else {
                VerifyIdSubStep.InformationForm
            }
        }
    }

    fun resendVerificationCode() {
        viewModelScope.launch {
            val acctId = _resolvedAccountId.value ?: return@launch
            PhoneOTPVerificationAPI.createVerification(
                accountId = acctId,
                phoneNumber = _phoneNumber.value,
                dateOfBirth = dateOfBirth
            )
        }
    }

    fun startProveAuth(context: Context) {
        viewModelScope.launch {
            val acctId = _resolvedAccountId.value ?: return@launch
            val verificationId = _pendingVerificationId.value ?: return@launch
            val authToken = _pendingProveAuthToken.value ?: return@launch
            val service = ProveAuthService(
                context = context,
                accountId = acctId,
                verificationId = verificationId,
                confirmHandler = { _, vid ->
                    PhoneOTPVerificationAPI.confirmVerification(acctId, vid, code = null)
                }
            )
            try {
                service.authenticateWith(authToken)
            } catch (_: Exception) { }
            _verifyIdSubStep.value = VerifyIdSubStep.InformationForm
        }
    }

    fun confirmVerificationCode(code: String) {
        viewModelScope.launch {
            val acctId = _resolvedAccountId.value
            val verificationId = _pendingVerificationId.value
            if (acctId != null && verificationId != null) {
                PhoneOTPVerificationAPI.confirmVerification(acctId, verificationId, code)
            }
            _verifyIdSubStep.value = VerifyIdSubStep.InformationForm
        }
    }

    fun goBackFromVerifyPhone() {
        _verifyIdSubStep.value = VerifyIdSubStep.PhoneAuth
    }

    // endregion

    // region Personal info + account/identity creation

    fun submitPersonalInfo(
        firstName: String,
        lastName: String,
        email: String,
        dobOverride: String?,
        ssn: String,
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
            ssn = ssn,
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

            // Update the existing account with full profile now that we have all info
            val acctId = _resolvedAccountId.value ?: run {
                val accountRequest = AccountRequests.CreateAccountRequest(
                    type = AccountObjects.AccountType.INDIVIDUAL,
                    termsOfService = _tosToken.value?.let {
                        AccountObjects.AccountTermsOfService(token = it)
                    },
                    profile = AccountRequests.CreateAccountProfile(
                        individual = AccountRequests.CreateIndividualAccount(
                            name = AccountRequests.CreateAccountInfo(
                                firstName = firstName,
                                lastName = lastName
                            ),
                            email = email,
                            phone = AccountObjects.AccountPhoneNumber(
                                number = _phoneNumber.value,
                                countryCode = "+1"
                            ),
                            address = billingAddress,
                            dob = dob.ifEmpty { null },
                            ssn = ssn.ifEmpty { null }
                        )
                    )
                )
                val (account, _) = AccountsAPI.createAccount(accountRequest)
                _resolvedAccountId.value = account?.id
                _onboardingData.value = _onboardingData.value.copy(resolvedAccountId = account?.id)
                account?.id
            } ?: return@launch

            // Create customer identity
            val identityRequest = CustomerIdentityRequests.CreateCustomerIdentityRequest(
                firstName = firstName,
                lastName = lastName,
                dateOfBirth = dob,
                email = email,
                phoneNumber = _phoneNumber.value,
                ssn = ssn,
                address = billingAddress
            )
            val (identity, _) = CustomerIdentityAPI.createCustomerIdentity(identityRequest)
            if (identity != null) {
                customerIdentityId = identity.id
                _onboardingData.value = _onboardingData.value.copy(customerIdentityId = identity.id)
            }

            if (config.requiredCapabilities.isNotEmpty()) {
                CapabilitiesAPI.requestCapabilities(
                    acctId,
                    CapabilityRequests.RequestCapabilitiesRequest(
                        capabilities = config.requiredCapabilities.map { it.apiValue }
                    )
                )
            }
        }

        moveNext()
    }

    // endregion

    // region Payment methods

    fun onPaymentMethodSelected(id: String) {
        _onboardingData.value = _onboardingData.value.copy(selectedPaymentMethodId = id)
    }

    fun onPayoutMethodSelected(id: String) {
        _onboardingData.value = _onboardingData.value.copy(selectedPayoutMethodId = id)
    }

    fun submitNewPaymentMethod(paymentDetails: PaymentMethodDetails) {
        _onboardingData.value = _onboardingData.value.copy(newPaymentMethod = paymentDetails)
        viewModelScope.launch {
            val acctId = _resolvedAccountId.value ?: return@launch
            val billingAddress = FrameObjects.BillingAddress(
                city = paymentDetails.city,
                country = "US",
                state = paymentDetails.state,
                postalCode = paymentDetails.zipCode,
                addressLine1 = paymentDetails.addressLine1,
                addressLine2 = paymentDetails.addressLine2
            )
            val pmRequest = PaymentMethodRequests.CreateCardPaymentMethodRequest(
                cardNumber = paymentDetails.cardNumber,
                expMonth = paymentDetails.expiryMonth,
                expYear = paymentDetails.expiryYear,
                cvc = paymentDetails.cvc,
                customer = acctId,
                billing = billingAddress
            )
            val (paymentMethod, _) = PaymentMethodsAPI.createCardPaymentMethod(pmRequest)
            if (paymentMethod != null) {
                _onboardingData.value = _onboardingData.value.copy(selectedPaymentMethodId = paymentMethod.id)
                _savedPaymentMethods.value = _savedPaymentMethods.value + PaymentMethodSummary(
                    id = paymentMethod.id,
                    brand = paymentMethod.card?.brand?.uppercase() ?: "",
                    last4 = paymentMethod.card?.lastFourDigits ?: "",
                    exp = "${paymentMethod.card?.expirationMonth}/${paymentMethod.card?.expirationYear?.takeLast(2)}"
                )
            }
        }
        moveNext()
    }

    fun submitNewPayoutMethod(payoutDetails: PayoutMethodDetails) {
        _onboardingData.value = _onboardingData.value.copy(newPayoutMethod = payoutDetails)
        viewModelScope.launch {
            val acctId = _resolvedAccountId.value ?: return@launch
            val achRequest = PaymentMethodRequests.CreateACHPaymentMethodRequest(
                accountType = if (payoutDetails.accountType.lowercase() == "savings")
                    FrameObjects.PaymentAccountType.SAVINGS
                else
                    FrameObjects.PaymentAccountType.CHECKING,
                accountNumber = payoutDetails.accountNumber,
                routingNumber = payoutDetails.routingNumber,
                customer = acctId,
                billing = FrameObjects.BillingAddress(
                    addressLine1 = payoutDetails.addressLine1,
                    addressLine2 = payoutDetails.addressLine2,
                    city = payoutDetails.city,
                    state = payoutDetails.state,
                    postalCode = payoutDetails.zipCode,
                    country = "US"
                )
            )
            val (payoutMethod, _) = PaymentMethodsAPI.createACHPaymentMethod(achRequest)
            if (payoutMethod != null) {
                _onboardingData.value = _onboardingData.value.copy(selectedPayoutMethodId = payoutMethod.id)
            }
        }
        moveNext()
    }

    // endregion

    // region 3DS

    fun initialize3DS() {
        viewModelScope.launch {
            val paymentMethodId = _onboardingData.value.selectedPaymentMethodId ?: return@launch
            val request = ThreeDSecureRequests.CreateThreeDSecureVerification(paymentMethodId = paymentMethodId)
            val (verification, networkError, _) = ThreeDSecureVerificationsAPI.create3DSecureVerification(request)
            if (networkError != null) {
                _result.value = OnboardingResult.Error("Failed to initialize card verification. Please try again.")
                return@launch
            }
            _threeDSVerificationId.value = verification?.id
        }
    }

    fun resend3DS() {
        viewModelScope.launch {
            _threeDSVerificationId.value?.let { id ->
                ThreeDSecureVerificationsAPI.resend3DSecureVerification(id)
            }
        }
    }

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

    fun submitDocuments(context: Context) {
        viewModelScope.launch {
            val identityId = customerIdentityId ?: return@launch
            val data = _onboardingData.value
            val frontUri = data.frontPhotoUri ?: return@launch
            val backUri = data.backPhotoUri ?: return@launch
            val selfieUri = data.selfieUri ?: return@launch

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

            val frontBitmap = uriToBitmap(frontUri) ?: return@launch
            val backBitmap = uriToBitmap(backUri) ?: return@launch
            val selfieBitmap = uriToBitmap(selfieUri) ?: return@launch

            CustomerIdentityAPI.uploadIdentityDocuments(
                identityId,
                listOf(
                    FileUpload(frontBitmap, FileUploadFieldName.FRONT),
                    FileUpload(backBitmap, FileUploadFieldName.BACK),
                    FileUpload(selfieBitmap, FileUploadFieldName.SELFIE)
                )
            )
            CustomerIdentityAPI.submitForVerification(identityId)
            moveNext()
        }
    }

    // endregion

    private fun loadPaymentMethods(accountId: String) {
        viewModelScope.launch {
            val (list, _) = PaymentMethodsAPI.getPaymentMethodsWithCustomer(accountId)
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
