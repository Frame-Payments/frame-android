package com.framepayments.frameonboarding.views

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.framepayments.frameonboarding.reusable.SpinnerDatePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.accountevents.AccountEventEmitter
import com.framepayments.framesdk.accountevents.AccountEventName
import com.framepayments.framesdk.accountevents.AccountEventScreen
import com.framepayments.framesdk.configurations.LegalConfiguration
import com.framepayments.framesdk.customeridentity.CustomerIdentityRequests
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.framesdk_ui.reusable.BillingAddressDetailView
import com.framepayments.framesdk_ui.reusable.ContinueButton
import com.framepayments.frameonboarding.reusable.CustomerInformationView
import com.framepayments.frameonboarding.reusable.PhoneCountryPickerSheet
import com.framepayments.framesdk_ui.reusable.PhoneNumberTextField
import com.framepayments.frameonboarding.reusable.TermsOfServiceView
import com.framepayments.framesdk_ui.viewmodels.BillingAddressFieldVM
import com.framepayments.framesdk_ui.viewmodels.BillingAddressMode
import com.framepayments.frameonboarding.viewmodels.CustomerInformationFieldVM
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
import com.framepayments.frameonboarding.viewmodels.OnboardingField
import com.framepayments.frameonboarding.viewmodels.VerifyIdSubStep
import com.framepayments.frameonboarding.viewmodels.VerifyPhoneUi
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import com.framepayments.framesdk_ui.theme.FrameTheme
import com.framepayments.framesdk_ui.theme.FrameThemePreviews
import com.withpersona.sdk2.inquiry.Inquiry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UserIdentificationView(
    viewModel: FrameOnboardingViewModel,
    requiresDateOfBirth: Boolean = false,
    showTermsOfService: Boolean = false,
    canGoBack: Boolean = true,
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
    val onboardingData by viewModel.onboardingData.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()
    val personaInquiryToLaunch by viewModel.personaInquiryToLaunch.collectAsState()
    val isVerifyingGovId by viewModel.isVerifyingGovId.collectAsState()
    val context = LocalContext.current

    // Show the no-SSN government-ID path only when KYC is required (the same gate as the SSN
    // field). Keyed off what the host originally asked for, not the shrinking live list — the
    // latter drops a capability the moment it's granted, hiding the field mid-flow. Hidden under a
    // step-up or when the host required idv, where Continue runs Persona itself.
    val hostRequestedIdv = viewModel.originallyRequiredCapabilities.contains(Capabilities.IDV)
    val showGovIdVerification = !viewModel.governmentIdRequired && (
        viewModel.originallyRequiredCapabilities.contains(Capabilities.KYC) ||
            viewModel.originallyRequiredCapabilities.contains(Capabilities.KYC_PREFILL)
        )
    // Host-requested IDV / identity-document step-up skips SSN so Continue can launch Persona;
    // corrected-KYC overrides and keeps the SSN field required.
    val skipsSsnEntry = !onboardingData.correctedKycDetailsRequired && (
        onboardingData.skipsSsnEntry || viewModel.governmentIdRequired || hostRequestedIdv
    )

    // Persona result launcher. Lifecycle-owned here (the VM can't launch an ActivityResult); the
    // callback forwards the (best-effort) client outcome to the VM, which confirms with the server.
    val personaLauncher = rememberLauncherForActivityResult(Inquiry.Contract(context)) { response ->
        viewModel.onPersonaInquiryResult(response)
    }

    // When /idv/session returns an inquiry, launch the Persona SDK against it and confirm server-side.
    LaunchedEffect(personaInquiryToLaunch) {
        personaInquiryToLaunch?.let { inquiry ->
            viewModel.launchPersonaInquiry(inquiry, personaLauncher)
        }
    }

    LaunchedEffect(showTermsOfService, termsToken) {
        if (showTermsOfService && termsToken == null) {
            viewModel.generateTermsOfServiceToken()
        }
    }

    LaunchedEffect(subStep) {
        if (subStep == VerifyIdSubStep.InformationForm) {
            AccountEventEmitter.emit(
                AccountEventName.PROFILE_STEP_STARTED,
                AccountEventScreen.PERSONAL_INFORMATION
            )
        }
    }

    var showPhoneCountryPicker by rememberSaveable { mutableStateOf(false) }

    // Per-screen view models for the InformationForm step (iOS @StateObject parity).
    // rememberSaveable so typed values survive rotation / process recreation.
    val customerInfoVM = rememberSaveable(saver = CustomerInformationFieldVM.Saver) {
        CustomerInformationFieldVM(
            initialIdentity = identityFromOnboarding(viewModel),
            initialPhoneCountry = viewModel.phoneCountry.value
        )
    }
    val personalAddressVM = rememberSaveable(
        saver = BillingAddressFieldVM.Saver(BillingAddressMode.INTERNATIONAL)
    ) {
        BillingAddressFieldVM(
            initial = addressFromOnboarding(viewModel),
            mode = BillingAddressMode.INTERNATIONAL
        )
    }

    // Merge async account-profile data into the per-screen VMs without clobbering
    // anything the user has already typed. Keyed on onboardingData so prefill arriving
    // mid-screen still propagates; iOS uses .onChange(of: createdCustomerIdentity).
    LaunchedEffect(onboardingData) {
        customerInfoVM.updateIdentity { current ->
            current.copy(
                firstName = current.firstName.ifBlank { onboardingData.firstName.orEmpty() },
                lastName = current.lastName.ifBlank { onboardingData.lastName.orEmpty() },
                email = current.email.ifBlank { onboardingData.email.orEmpty() },
                phoneNumber = current.phoneNumber.ifBlank { onboardingData.phoneNumber.orEmpty() },
                dateOfBirth = current.dateOfBirth.ifBlank { onboardingData.dateOfBirth.orEmpty() },
                ssn = current.ssn.ifBlank { onboardingData.ssnLast4.orEmpty() }
            )
        }
        personalAddressVM.updateAddress { current ->
            current.copy(
                addressLine1 = current.addressLine1?.takeIf { it.isNotBlank() } ?: onboardingData.addressLine1,
                addressLine2 = current.addressLine2 ?: onboardingData.addressLine2,
                city = current.city?.takeIf { it.isNotBlank() } ?: onboardingData.city,
                state = current.state?.takeIf { it.isNotBlank() } ?: onboardingData.stateCode,
                postalCode = current.postalCode?.takeIf { it.isNotBlank() } ?: onboardingData.postalCode,
                country = current.country?.takeIf { it.isNotBlank() }
                    ?: onboardingData.country
                    ?: "US"
            )
        }
        if (customerInfoVM.phoneCountry.value.alpha2 != viewModel.phoneCountry.value.alpha2) {
            customerInfoVM.setPhoneCountry(viewModel.phoneCountry.value)
        }
    }

    val theme = LocalFrameTheme.current
    val pageTitle = when (subStep) {
        VerifyIdSubStep.PhoneAuth -> "Verify your phone number with a code"
        VerifyIdSubStep.VerifyPhone -> "Enter your verification code"
        VerifyIdSubStep.InformationForm -> "Verify your personal info"
    }
    val showNavBack = canGoBack || subStep != VerifyIdSubStep.PhoneAuth

    Scaffold(
        containerColor = theme.colors.surface,
        topBar = {
            // Back only — page title lives in the scroll content so it shares the same
            // horizontal inset as the form (matches iOS PageHeaderView).
            if (showNavBack) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        TextButton(
                            onClick = {
                                when (subStep) {
                                    VerifyIdSubStep.PhoneAuth -> onBack()
                                    VerifyIdSubStep.VerifyPhone,
                                    VerifyIdSubStep.InformationForm -> viewModel.goBackFromVerifyPhone()
                                }
                            }
                        ) { Text("Back") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = theme.colors.surface
                    )
                )
            }
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
                                headerTitle = pageTitle,
                                bodyAnnotated = otpSubtitleAnnotated(
                                    dialCode = phoneCountry.dialCode,
                                    phoneNumber = phoneNumber
                                ),
                                codeExpirationHint = "Your code expires in 10 minutes",
                                digitCount = 6,
                                showResendCode = true,
                                showChangePhoneNumber = true,
                                embedInParentScaffold = true,
                                emitsPhoneCodeEntry = verifyPhoneUi != VerifyPhoneUi.OtpForProve,
                                onBack = { viewModel.goBackFromVerifyPhone() },
                                onResendCode = { viewModel.resendVerificationCode() },
                                onChangePhoneNumber = { viewModel.goBackFromVerifyPhone() },
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
                // Match iOS PageHeaderView: 20dp horizontal for title + body; even gaps between
                // title → subtitle → first field (iOS uses 20 under the subtitle).
                val contentInset = 20.dp
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = contentInset)
                        .padding(top = theme.spacing.sectionTop, bottom = 24.dp)
                        .fillMaxWidth()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = pageTitle,
                        style = theme.fonts.heading,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    when (subStep) {
                        VerifyIdSubStep.PhoneAuth -> {
                            Spacer(Modifier.height(theme.spacing.sectionGap))
                            Text(
                                text = "We'll text you a 6-digit code to confirm it's you.",
                                style = theme.fonts.bodySmall,
                                color = theme.colors.textSecondary
                            )
                            Spacer(Modifier.height(20.dp))

                            // Phone number header row with error
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Phone number", style = theme.fonts.label)
                                fieldErrors[OnboardingField.AUTH_PHONE]?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = theme.fonts.caption,
                                        color = theme.colors.error
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { showPhoneCountryPicker = true },
                                    shape = RoundedCornerShape(LocalFrameTheme.current.radii.medium),
                                    border = BorderStroke(1.dp, LocalFrameTheme.current.colors.surfaceStroke),
                                    modifier = Modifier
                                        .height(64.dp)
                                        .width(120.dp)
                                ) {
                                    Text(phoneCountry.flag)
                                    Spacer(Modifier.width(4.dp))
                                    Text(phoneCountry.dialCode, style = LocalFrameTheme.current.fonts.bodySmall)
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
                                    error = fieldErrors[OnboardingField.AUTH_PHONE],
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
                                    Text("Date of Birth", style = LocalFrameTheme.current.fonts.label)
                                    val firstDobError = fieldErrors[OnboardingField.AUTH_BIRTH_MONTH]
                                        ?: fieldErrors[OnboardingField.AUTH_BIRTH_DAY]
                                        ?: fieldErrors[OnboardingField.AUTH_BIRTH_YEAR]
                                    firstDobError?.let { msg ->
                                        Text(
                                            text = msg,
                                            style = LocalFrameTheme.current.fonts.caption,
                                            color = LocalFrameTheme.current.colors.error
                                        )
                                    }
                                }

                                var showDobPicker by rememberSaveable { mutableStateOf(false) }
                                var selectedDobMillis by rememberSaveable {
                                    mutableStateOf(
                                        millisFromDobParts(dobYear, dobMonth, dobDay)
                                            ?: defaultAdultDobMillis()
                                    )
                                }
                                LaunchedEffect(dobYear, dobMonth, dobDay) {
                                    millisFromDobParts(dobYear, dobMonth, dobDay)?.let {
                                        if (it != selectedDobMillis) selectedDobMillis = it
                                    }
                                }
                                LaunchedEffect(Unit) {
                                    if (dobYear.isEmpty() || dobMonth.isEmpty() || dobDay.isEmpty()) {
                                        applyDobMillisToViewModel(viewModel, selectedDobMillis)
                                    }
                                }
                                val displayDate = remember(selectedDobMillis) {
                                    formatDisplayDate(selectedDobMillis)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 50.dp)
                                        .border(
                                            1.dp,
                                            LocalFrameTheme.current.colors.surfaceStroke,
                                            RoundedCornerShape(LocalFrameTheme.current.radii.medium)
                                        )
                                        .clickable { showDobPicker = true }
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = displayDate,
                                        style = LocalFrameTheme.current.fonts.body,
                                        color = LocalFrameTheme.current.colors.textPrimary
                                    )
                                }
                                if (showDobPicker) {
                                    SpinnerDatePickerDialog(
                                        initialMillis = selectedDobMillis,
                                        onDismiss = { showDobPicker = false },
                                        onDateSelected = { millis ->
                                            selectedDobMillis = millis
                                            applyDobMillisToViewModel(viewModel, millis)
                                            clearAuthDobErrors(viewModel)
                                            showDobPicker = false
                                        }
                                    )
                                }
                            }
                        }

                        VerifyIdSubStep.InformationForm -> {
                            Spacer(Modifier.height(theme.spacing.sectionGap))
                            PersonalInfoIntro()
                            Spacer(Modifier.height(16.dp))
                            CustomerInformationView(
                                viewModel = customerInfoVM,
                                headerTitle = "Legal name",
                                showHeader = true,
                                showGovIdVerification = showGovIdVerification,
                                identityVerifiedViaGovId = onboardingData.identityVerifiedViaGovId &&
                                    !onboardingData.correctedKycDetailsRequired,
                                showSsnField = !skipsSsnEntry,
                                isVerifyingGovId = isVerifyingGovId,
                                onVerifyWithoutSsn = { viewModel.verifyIdentityWithoutSsn() },
                                onUseSsnInstead = { viewModel.resetIdentityVerification() },
                                allowSsnInstead = !viewModel.governmentIdRequired
                            )

                            Spacer(Modifier.height(24.dp))

                            BillingAddressDetailView(
                                viewModel = personalAddressVM,
                                headerTitle = "Home address"
                            )
                        }

                        else -> Unit
                    }

                    val termsVisible = showTermsOfService && subStep == VerifyIdSubStep.PhoneAuth
                    if (termsVisible) {
                        LaunchedEffect(Unit) {
                            AccountEventEmitter.emit(
                                AccountEventName.TERMS_OF_SERVICE_SHOWN,
                                AccountEventScreen.TERMS_OF_SERVICE
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        TermsOfServiceView()
                    }
                    Spacer(Modifier.height(24.dp))
                    val isPerformingAction by viewModel.isPerformingAction.collectAsState()
                    ContinueButton(
                        // A step-up hands Continue off to Persona, which outlives the submit action.
                        isLoading = isPerformingAction || isVerifyingGovId,
                        onClick = {
                            when (subStep) {
                                VerifyIdSubStep.PhoneAuth -> {
                                    if (viewModel.validateAllPhoneAuth()) {
                                        if (termsVisible) {
                                            AccountEventEmitter.emit(
                                                AccountEventName.TERMS_OF_SERVICE_ACCEPTED,
                                                AccountEventScreen.TERMS_OF_SERVICE
                                            )
                                        }
                                        viewModel.submitPhoneAuth(requiresDateOfBirth)
                                    }
                                }
                                else -> {
                                    val infoOK = customerInfoVM.validate(ssnOptional = skipsSsnEntry)
                                    val addressOK = personalAddressVM.validate()
                                    if (!infoOK || !addressOK) {
                                        AccountEventEmitter.emit(
                                            AccountEventName.PROFILE_VALIDATION_FAILED,
                                            AccountEventScreen.PERSONAL_INFORMATION,
                                            detail = "info valid: $infoOK, address valid: $addressOK"
                                        )
                                    }
                                    if (infoOK && addressOK) {
                                        val id = customerInfoVM.identity.value
                                        val addr = personalAddressVM.address.value
                                        viewModel.onPhoneCountryChanged(customerInfoVM.phoneCountry.value)
                                        viewModel.onPhoneNumberChanged(id.phoneNumber)
                                        viewModel.submitPersonalInfo(
                                            firstName = id.firstName,
                                            lastName = id.lastName,
                                            email = id.email,
                                            dobOverride = id.dateOfBirth.takeIf { it.isNotBlank() },
                                            // Omit SSN on the government-ID path; upsert treats "" as null.
                                            ssnLastFour = if (skipsSsnEntry) "" else id.ssn,
                                            addressLine1 = addr.addressLine1.orEmpty(),
                                            addressLine2 = addr.addressLine2,
                                            city = addr.city.orEmpty(),
                                            stateCode = addr.state.orEmpty(),
                                            postalCode = addr.postalCode.orEmpty(),
                                            country = addr.country ?: "US"
                                        )
                                    }
                                }
                            }
                        }
                    )
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

private fun clearAuthDobErrors(vm: FrameOnboardingViewModel) {
    vm.clearError(OnboardingField.AUTH_BIRTH_MONTH)
    vm.clearError(OnboardingField.AUTH_BIRTH_DAY)
    vm.clearError(OnboardingField.AUTH_BIRTH_YEAR)
}

private fun defaultAdultDobMillis(): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.add(Calendar.YEAR, -18)
    return startOfUtcDayMillis(cal.timeInMillis)
}

private fun millisFromDobParts(year: String, month: String, day: String): Long? {
    val y = year.toIntOrNull() ?: return null
    val m = month.toIntOrNull() ?: return null
    val d = day.toIntOrNull() ?: return null
    if (year.length != 4 || month.isEmpty() || day.isEmpty()) return null
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.clear()
    cal.set(Calendar.YEAR, y)
    cal.set(Calendar.MONTH, m - 1)
    cal.set(Calendar.DAY_OF_MONTH, d)
    return cal.timeInMillis
}

private fun applyDobMillisToViewModel(viewModel: FrameOnboardingViewModel, millis: Long) {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = millis
    }
    viewModel.onDobYearChanged(cal.get(Calendar.YEAR).toString())
    viewModel.onDobMonthChanged(String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1))
    viewModel.onDobDayChanged(String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH)))
}

private fun formatDisplayDate(millis: Long): String {
    val fmt = SimpleDateFormat("MMM d, yyyy", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return fmt.format(Date(millis))
}

private fun startOfUtcDayMillis(millis: Long): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = millis
    }
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
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

@Composable
private fun PersonalInfoIntro() {
    val theme = LocalFrameTheme.current
    val uriHandler = LocalUriHandler.current
    val privacyUrl = LegalConfiguration.privacyUrl
    val annotated = buildAnnotatedString {
        append(
            "This information is collected to verify your identity, keep your account safe, and help meet legal regulatory requirements. For more information, review Frame's "
        )
        pushStringAnnotation(tag = "URL", annotation = privacyUrl)
        withStyle(
            SpanStyle(
                color = theme.colors.textPrimary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Privacy Policy")
        }
        pop()
        append(".")
    }
    ClickableText(
        text = annotated,
        style = theme.fonts.bodySmall.copy(color = theme.colors.textSecondary),
        onClick = { offset ->
            annotated.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()
                ?.let { uriHandler.openUri(it.item) }
        }
    )
}

@Composable
private fun otpSubtitleAnnotated(dialCode: String, phoneNumber: String): AnnotatedString {
    val theme = LocalFrameTheme.current
    // Keep AsYouTypeFormatter grouping (e.g. "(200) 100-1695"); only trim ends.
    val formatted = phoneNumber.trim()
    val display = listOf(dialCode, formatted)
        .filter { it.isNotBlank() }
        .joinToString(" ")
    return buildAnnotatedString {
        append("We texted a 6-digit code to ")
        withStyle(
            SpanStyle(
                color = theme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        ) {
            append(display)
        }
    }
}

@FrameThemePreviews
@Composable
private fun UserIdentificationViewPreview() {
    FrameTheme {
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
}
