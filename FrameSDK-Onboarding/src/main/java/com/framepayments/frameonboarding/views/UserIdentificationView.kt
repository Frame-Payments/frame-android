package com.framepayments.frameonboarding.views

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.reusable.BillingAddressForm
import com.framepayments.frameonboarding.reusable.CustomerInformationForm
import com.framepayments.frameonboarding.reusable.TermsOfServiceView
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
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

    // Information step local state (pure UI, submitted to VM on continue)
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var ssnLastFour by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("United States") }

    LaunchedEffect(subStep) {
        if (subStep != VerifyIdSubStep.InformationForm) return@LaunchedEffect
        val d = viewModel.onboardingData.value
        d.firstName?.takeIf { it.isNotBlank() }?.let { firstName = it }
        d.lastName?.takeIf { it.isNotBlank() }?.let { lastName = it }
        d.email?.takeIf { it.isNotBlank() }?.let { email = it }
        d.ssnLast4?.takeIf { it.isNotBlank() }?.let { ssnLastFour = it }
        d.addressLine1?.takeIf { it.isNotBlank() }?.let { addressLine1 = it }
        d.addressLine2?.takeIf { it.isNotBlank() }?.let { addressLine2 = it }
        d.city?.takeIf { it.isNotBlank() }?.let { city = it }
        d.stateCode?.takeIf { it.isNotBlank() }?.let { state = it }
        d.postalCode?.takeIf { it.isNotBlank() }?.let { postalCode = it }
        d.country?.takeIf { it.isNotBlank() }?.let { country = it }
    }

    val dobComplete = dobMonth.length == 2 && dobDay.length == 2 && dobYear.length == 4
    val canContinuePhone = phoneNumber.length >= 10 && (!requiresDateOfBirth || dobComplete)
    val canContinueInfo = firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() &&
        addressLine1.isNotEmpty() && city.isNotEmpty() && ssnLastFour.isNotEmpty() &&
        state.isNotEmpty() && postalCode.length > 4

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
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { viewModel.onPhoneNumberChanged(it) },
                                label = { Text("Phone Number") },
                                placeholder = { Text("Enter your phone number") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            if (requiresDateOfBirth) {
                                Spacer(Modifier.height(16.dp))
                                Text(text = "Date of Birth", style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = dobMonth,
                                        onValueChange = { viewModel.onDobMonthChanged(it) },
                                        label = { Text("MM") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = dobDay,
                                        onValueChange = { viewModel.onDobDayChanged(it) },
                                        label = { Text("DD") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = dobYear,
                                        onValueChange = { viewModel.onDobYearChanged(it) },
                                        label = { Text("YYYY") },
                                        modifier = Modifier.weight(2f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }
                        }

                        VerifyIdSubStep.InformationForm -> {
                            CustomerInformationForm(
                                firstName = firstName,
                                onFirstNameChange = { firstName = it },
                                lastName = lastName,
                                onLastNameChange = { lastName = it },
                                email = email,
                                onEmailChange = { email = it },
                                ssnLastFour = ssnLastFour,
                                onSsnChange = { ssnLastFour = it },
                                showHeader = false
                            )

                            Spacer(Modifier.height(24.dp))

                            BillingAddressForm(
                                addressLine1 = addressLine1,
                                onAddressLine1Change = { addressLine1 = it },
                                addressLine2 = addressLine2,
                                onAddressLine2Change = { addressLine2 = it },
                                city = city,
                                onCityChange = { city = it },
                                state = state,
                                onStateChange = { state = it },
                                zipCode = postalCode,
                                onZipCodeChange = { postalCode = it },
                                showHeader = false
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
                        enabled = when (subStep) {
                            VerifyIdSubStep.PhoneAuth -> canContinuePhone
                            else -> canContinueInfo
                        },
                        onClick = {
                            when (subStep) {
                                VerifyIdSubStep.PhoneAuth -> viewModel.submitPhoneAuth(requiresDateOfBirth)
                                else -> {
                                    viewModel.submitPersonalInfo(
                                        firstName = firstName,
                                        lastName = lastName,
                                        email = email,
                                        dobOverride = null,
                                        ssnLastFour = ssnLastFour,
                                        addressLine1 = addressLine1,
                                        addressLine2 = addressLine2.ifEmpty { null },
                                        city = city,
                                        stateCode = state,
                                        postalCode = postalCode,
                                        country = country
                                    )
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
}

@Preview(showBackground = true)
@Composable
private fun OnboardingContainerViewPreview() {
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
