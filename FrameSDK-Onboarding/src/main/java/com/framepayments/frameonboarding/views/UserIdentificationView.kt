package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.reusable.BillingAddressForm
import com.framepayments.frameonboarding.reusable.CustomerInformationForm
import com.framepayments.frameonboarding.reusable.TermsOfServiceView
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
import com.framepayments.frameonboarding.viewmodels.VerifyIdSubStep

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
    val context = LocalContext.current

    // Information step local state (pure UI, submitted to VM on continue)
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var ssn by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    val country by remember { mutableStateOf("United States") }

    val dobComplete = dobMonth.length == 2 && dobDay.length == 2 && dobYear.length == 4
    val canContinuePhone = phoneNumber.length >= 10 && (!requiresDateOfBirth || dobComplete)
    val canContinueInfo = firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() &&
        addressLine1.isNotEmpty() && city.isNotEmpty() &&
        state.isNotEmpty() && postalCode.length == 5

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (subStep) {
                            VerifyIdSubStep.PhoneAuth -> if (requiresDateOfBirth) "Enter Your Phone Number & DOB" else "Enter Your Phone Number"
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
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
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

                    VerifyIdSubStep.VerifyPhone -> {
                        if (proveAuthToken != null) {
                            LaunchedEffect(Unit) {
                                viewModel.startProveAuth(context)
                            }
                        } else {
                            VerifyCardScreen(
                                headerTitle = "Enter Verification Code",
                                bodyText = "We've sent a verification code to your phone. Enter it below.",
                                digitCount = 6,
                                showResendCode = false,
                                onBack = { viewModel.goBackFromVerifyPhone() },
                                onResendCode = { viewModel.resendVerificationCode() },
                                onContinue = { code -> viewModel.confirmVerificationCode(code) }
                            )
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
                            ssn = ssn,
                            onSsnChange = { ssn = it },
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
                }
            }

            if (subStep != VerifyIdSubStep.VerifyPhone) {
                if (showTermsOfService && subStep == VerifyIdSubStep.PhoneAuth) {
                    TermsOfServiceView()
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = when (subStep) {
                        VerifyIdSubStep.PhoneAuth -> canContinuePhone
                        VerifyIdSubStep.InformationForm -> canContinueInfo
                        VerifyIdSubStep.VerifyPhone -> false
                    },
                    onClick = {
                        when (subStep) {
                            VerifyIdSubStep.PhoneAuth -> viewModel.submitPhoneAuth(requiresDateOfBirth)
                            VerifyIdSubStep.InformationForm -> {
                                viewModel.submitPersonalInfo(
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = email,
                                    dobOverride = null,
                                    ssn = ssn,
                                    addressLine1 = addressLine1,
                                    addressLine2 = addressLine2.ifEmpty { null },
                                    city = city,
                                    stateCode = state,
                                    postalCode = postalCode,
                                    country = country
                                )
                            }
                            VerifyIdSubStep.VerifyPhone -> Unit
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
