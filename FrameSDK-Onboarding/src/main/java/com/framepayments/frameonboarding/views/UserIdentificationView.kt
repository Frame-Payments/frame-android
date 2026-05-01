package com.framepayments.frameonboarding.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.customeridentity.CustomerIdentityRequests
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.reusable.BillingAddressDetailView
import com.framepayments.frameonboarding.reusable.CustomerInformationView
import com.framepayments.frameonboarding.reusable.PhoneCountryPickerSheet
import com.framepayments.frameonboarding.reusable.PhoneNumberTextField
import com.framepayments.frameonboarding.reusable.TermsOfServiceView
import com.framepayments.frameonboarding.reusable.ValidatedTextField
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor
import com.framepayments.frameonboarding.viewmodels.BillingAddressFieldVM
import com.framepayments.frameonboarding.viewmodels.BillingAddressMode
import com.framepayments.frameonboarding.viewmodels.CustomerInformationFieldVM
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
import com.framepayments.frameonboarding.viewmodels.OnboardingField
import com.framepayments.frameonboarding.viewmodels.VerifyIdSubStep
import com.framepayments.frameonboarding.viewmodels.VerifyPhoneUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UserIdentificationView(
    viewModel: FrameOnboardingViewModel,
    requiresDateOfBirth: Boolean = false,
    showTermsOfService: Boolean = false,
    onBack: () -> Unit
) {
    val subStep by viewModel.verifyIdSubStep.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val dobMonth by viewModel.dobMonth.collectAsState()
    val dobDay by viewModel.dobDay.collectAsState()
    val dobYear by viewModel.dobYear.collectAsState()
    val phoneCountry by viewModel.phoneCountry.collectAsState()
    val proveAuthToken by viewModel.pendingProveAuthToken.collectAsState()
    val verifyPhoneUi by viewModel.verifyPhoneUi.collectAsState()
    val pendingPhoneVerificationId by viewModel.pendingPhoneVerificationId.collectAsState()
    val awaitingAccountRefresh by viewModel.awaitingAccountProfileRefresh.collectAsState()
    val termsToken by viewModel.termsOfServiceToken.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(showTermsOfService, termsToken) {
        if (showTermsOfService && termsToken == null) {
            viewModel.generateTermsOfServiceToken()
        }
    }

    var showPhoneCountryPicker by remember { mutableStateOf(false) }

    // Per-screen view models for the InformationForm step (iOS @StateObject parity).
    val customerInfoVM = remember {
        CustomerInformationFieldVM(
            initialIdentity = identityFromOnboarding(viewModel),
            initialPhoneCountry = viewModel.phoneCountry.value
        )
    }
    val personalAddressVM = remember {
        BillingAddressFieldVM(
            initial = addressFromOnboarding(viewModel),
            mode = BillingAddressMode.INTERNATIONAL
        )
    }

    // Hydrate the per-screen VMs when the InformationForm substep activates and async
    // account-profile data has populated viewModel.onboardingData.
    LaunchedEffect(subStep) {
        if (subStep != VerifyIdSubStep.InformationForm) return@LaunchedEffect
        customerInfoVM.updateIdentity { identityFromOnboarding(viewModel) }
        personalAddressVM.updateAddress { addressFromOnboarding(viewModel) }
        customerInfoVM.setPhoneCountry(viewModel.phoneCountry.value)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (subStep) {
                            VerifyIdSubStep.PhoneAuth -> if (requiresDateOfBirth) "Phone Number & DOB" else "Enter Your Phone Number"
                            VerifyIdSubStep.VerifyPhone -> "Enter Verification Code"
                            VerifyIdSubStep.InformationForm -> "Personal Information"
                        }
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = {
                            when (subStep) {
                                VerifyIdSubStep.PhoneAuth -> onBack()
                                VerifyIdSubStep.VerifyPhone, VerifyIdSubStep.InformationForm -> viewModel.goBackFromVerifyPhone()
                            }
                        }
                    ) { Text("Back") }
                }
            )
        }
    ) { padding ->
        when (subStep) {
            VerifyIdSubStep.VerifyPhone -> {
                LaunchedEffect(pendingPhoneVerificationId, subStep, proveAuthToken) {
                    if (proveAuthToken == null) return@LaunchedEffect
                    if (viewModel.verifyPhoneUi.value != VerifyPhoneUi.LoadingProve) return@LaunchedEffect
                    viewModel.startProveAuth(context.applicationContext)
                }
                when {
                    verifyPhoneUi == VerifyPhoneUi.LoadingProve || awaitingAccountRefresh -> {
                        Box(
                            modifier = Modifier
                                .padding(padding)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .padding(padding)
                                .fillMaxSize()
                                .imePadding()
                        ) {
                            VerifyCardScreen(
                                headerTitle = "Enter Verification Code",
                                bodyText = "We've sent a verification code to your phone. Enter it below.",
                                digitCount = 6,
                                showResendCode = false,
                                embedInParentScaffold = true,
                                onBack = { viewModel.goBackFromVerifyPhone() },
                                onResendCode = { viewModel.resendVerificationCode() },
                                onContinue = { code ->
                                    when (verifyPhoneUi) {
                                        VerifyPhoneUi.OtpForProve -> viewModel.submitOtpToProveSdk(code)
                                        else -> viewModel.confirmVerificationCode(code)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(24.dp)
                        .fillMaxWidth()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (subStep) {
                        VerifyIdSubStep.PhoneAuth -> {
                            Text(
                                text = "We'll send you a code — it helps us keep your account secure.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(20.dp))

                            // Phone number header row with error
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Phone Number", style = MaterialTheme.typography.labelMedium)
                                viewModel.errorFor(OnboardingField.AUTH_PHONE)?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                OutlinedButton(
                                    onClick = { showPhoneCountryPicker = true },
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .height(64.dp)
                                        .width(120.dp)
                                ) {
                                    Text(phoneCountry.flag)
                                    Spacer(Modifier.width(4.dp))
                                    Text(phoneCountry.dialCode, style = MaterialTheme.typography.bodyMedium)
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Pick country"
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                PhoneNumberTextField(
                                    value = phoneNumber,
                                    onValueChange = { viewModel.onPhoneNumberChanged(it) },
                                    prompt = "Enter your phone number",
                                    regionCode = phoneCountry.alpha2,
                                    error = viewModel.errorFor(OnboardingField.AUTH_PHONE),
                                    compactError = true,
                                    onClearError = { viewModel.clearError(OnboardingField.AUTH_PHONE) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (requiresDateOfBirth) {
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Date of Birth", style = MaterialTheme.typography.labelMedium)
                                    firstAuthDobError(viewModel)?.let { msg ->
                                        Text(
                                            text = msg,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ValidatedTextField(
                                        value = dobMonth,
                                        onValueChange = { viewModel.onDobMonthChanged(it) },
                                        prompt = "MM",
                                        error = viewModel.errorFor(OnboardingField.AUTH_BIRTH_MONTH),
                                        keyboardType = KeyboardType.Number,
                                        characterLimit = 2,
                                        compactError = true,
                                        onClearError = { clearAuthDobErrors(viewModel) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    ValidatedTextField(
                                        value = dobDay,
                                        onValueChange = { viewModel.onDobDayChanged(it) },
                                        prompt = "DD",
                                        error = viewModel.errorFor(OnboardingField.AUTH_BIRTH_DAY),
                                        keyboardType = KeyboardType.Number,
                                        characterLimit = 2,
                                        compactError = true,
                                        onClearError = { clearAuthDobErrors(viewModel) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    ValidatedTextField(
                                        value = dobYear,
                                        onValueChange = { viewModel.onDobYearChanged(it) },
                                        prompt = "YYYY",
                                        error = viewModel.errorFor(OnboardingField.AUTH_BIRTH_YEAR),
                                        keyboardType = KeyboardType.Number,
                                        characterLimit = 4,
                                        compactError = true,
                                        onClearError = { clearAuthDobErrors(viewModel) },
                                        modifier = Modifier.weight(2f)
                                    )
                                }
                            }
                        }

                        VerifyIdSubStep.InformationForm -> {
                            CustomerInformationView(
                                viewModel = customerInfoVM,
                                showHeader = false
                            )

                            Spacer(Modifier.height(24.dp))

                            BillingAddressDetailView(
                                viewModel = personalAddressVM,
                                headerTitle = "Current Address"
                            )
                        }

                        else -> Unit
                    }

                    if (showTermsOfService && subStep == VerifyIdSubStep.PhoneAuth) {
                        Spacer(Modifier.height(24.dp))
                        TermsOfServiceView()
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true,
                        onClick = {
                            when (subStep) {
                                VerifyIdSubStep.PhoneAuth -> {
                                    if (viewModel.validateAllPhoneAuth()) {
                                        viewModel.submitPhoneAuth(requiresDateOfBirth)
                                    }
                                }
                                else -> {
                                    val infoOK = customerInfoVM.validate()
                                    val addressOK = personalAddressVM.validate()
                                    if (infoOK && addressOK) {
                                        val id = customerInfoVM.identity.value
                                        val addr = personalAddressVM.address.value
                                        viewModel.onPhoneCountryChanged(customerInfoVM.phoneCountry.value)
                                        viewModel.submitPersonalInfo(
                                            firstName = id.firstName,
                                            lastName = id.lastName,
                                            email = id.email,
                                            dobOverride = id.dateOfBirth.takeIf { it.isNotBlank() },
                                            ssnLastFour = id.ssn,
                                            addressLine1 = addr.addressLine1.orEmpty(),
                                            addressLine2 = addr.addressLine2,
                                            city = addr.city.orEmpty(),
                                            stateCode = addr.state.orEmpty(),
                                            postalCode = addr.postalCode,
                                            country = addr.country ?: "US"
                                        )
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FramePrimaryColor,
                            contentColor = FrameOnPrimaryColor,
                            disabledContainerColor = FramePrimaryColor.copy(alpha = 0.35f),
                            disabledContentColor = FrameOnPrimaryColor.copy(alpha = 0.7f)
                        )
                    ) {
                        Text("Continue")
                    }
                }
            }
        }
    }

    if (showPhoneCountryPicker) {
        PhoneCountryPickerSheet(
            selected = phoneCountry,
            onSelected = { sel ->
                viewModel.onPhoneCountryChanged(sel)
                showPhoneCountryPicker = false
            },
            onDismiss = { showPhoneCountryPicker = false }
        )
    }
}

private fun firstAuthDobError(vm: FrameOnboardingViewModel): String? =
    vm.errorFor(OnboardingField.AUTH_BIRTH_MONTH)
        ?: vm.errorFor(OnboardingField.AUTH_BIRTH_DAY)
        ?: vm.errorFor(OnboardingField.AUTH_BIRTH_YEAR)

private fun clearAuthDobErrors(vm: FrameOnboardingViewModel) {
    vm.clearError(OnboardingField.AUTH_BIRTH_MONTH)
    vm.clearError(OnboardingField.AUTH_BIRTH_DAY)
    vm.clearError(OnboardingField.AUTH_BIRTH_YEAR)
}

private fun identityFromOnboarding(
    vm: FrameOnboardingViewModel
): CustomerIdentityRequests.CreateCustomerIdentityRequest {
    val d = vm.onboardingData.value
    return CustomerIdentityRequests.CreateCustomerIdentityRequest(
        address = addressFromOnboarding(vm),
        firstName = d.firstName.orEmpty(),
        lastName = d.lastName.orEmpty(),
        dateOfBirth = d.dateOfBirth.orEmpty(),
        phoneNumber = d.phoneNumber.orEmpty(),
        email = d.email.orEmpty(),
        ssn = d.ssnLast4.orEmpty()
    )
}

private fun addressFromOnboarding(vm: FrameOnboardingViewModel): FrameObjects.BillingAddress {
    val d = vm.onboardingData.value
    return FrameObjects.BillingAddress(
        city = d.city,
        country = d.country ?: "US",
        state = d.stateCode,
        postalCode = d.postalCode.orEmpty(),
        addressLine1 = d.addressLine1,
        addressLine2 = d.addressLine2
    )
}

@Preview(showBackground = true)
@Composable
private fun UserIdentificationViewPreview() {
    UserIdentificationView(
        viewModel = FrameOnboardingViewModel(
            config = OnboardingConfig(requiredCapabilities = listOf(
                Capabilities.KYC,
                Capabilities.KYC_PREFILL,
                Capabilities.CARD_VERIFICATION,
                Capabilities.BANK_ACCOUNT_VERIFICATION,
                Capabilities.GEO_COMPLIANCE,
                Capabilities.AGE_VERIFICATION,
                Capabilities.PHONE_VERIFICATION
            ))
        ),
        requiresDateOfBirth = true,
        showTermsOfService = true,
        onBack = { }
    )
}
