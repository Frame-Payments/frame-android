package com.framepayments.frameonboarding.reusable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.validation.DateOfBirthFormatter
import com.framepayments.frameonboarding.viewmodels.CustomerInformationFieldVM
import com.framepayments.framesdk_ui.reusable.PhoneNumberTextField
import com.framepayments.framesdk_ui.reusable.ValidatedTextField
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val isoDobRegex = Regex("""^(\d{4})-(\d{1,2})-(\d{1,2})$""")

/**
 * Customer information form (first/last name, email, phone, DOB, SSN) bound to a
 * [CustomerInformationFieldVM]. 1:1 port of iOS CustomerInformationView.
 */
@Composable
fun CustomerInformationView(
    viewModel: CustomerInformationFieldVM,
    headerTitle: String = "Legal name",
    showHeader: Boolean = true,
    /**
     * When true, shows the "No Social Security number?" government-ID path beneath the
     * SSN row. Gate this on the same KYC capability that requires the SSN field. Defaults to false
     * so previews and other callers are unaffected.
     */
    showGovIdVerification: Boolean = false,
    /** When true, the SSN input and the button are hidden and a "Verified with government ID." line is shown. */
    identityVerifiedViaGovId: Boolean = false,
    /** When false (a government-ID step-up is pending), neither the SSN input nor the button is shown. */
    showSsnField: Boolean = true,
    /** When true, the government-ID button shows a spinner and is disabled (verification in flight). */
    isVerifyingGovId: Boolean = false,
    /** Invoked when the customer taps the no-SSN government-ID link. */
    onVerifyWithoutSsn: () -> Unit = {},
    /** Invoked when the customer taps "Use SSN instead" to undo government-ID verification. */
    onUseSsnInstead: () -> Unit = {},
    /** When false (a government ID is required), "Use SSN instead" is hidden. */
    allowSsnInstead: Boolean = true
) {
    val identity by viewModel.identity.collectAsState()
    val phoneCountry by viewModel.phoneCountry.collectAsState()
    val errors by viewModel.errors.collectAsState()
    val theme = LocalFrameTheme.current
    val firstDobError by remember {
        derivedStateOf {
            errors[CustomerInformationFieldVM.Field.BIRTH_MONTH]
                ?: errors[CustomerInformationFieldVM.Field.BIRTH_DAY]
                ?: errors[CustomerInformationFieldVM.Field.BIRTH_YEAR]
        }
    }

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var selectedDobMillis by rememberSaveable {
        mutableStateOf(defaultAdultDobMillis())
    }

    // Hydrate from stored ISO when it changes externally (e.g. async account profile fetch).
    LaunchedEffect(identity.dateOfBirth) {
        parseIsoToUtcMillis(identity.dateOfBirth)?.let { millis ->
            if (millis != selectedDobMillis) {
                selectedDobMillis = millis
            }
        }
    }

    // Seed the displayed default into the VM when no DOB is stored yet.
    LaunchedEffect(Unit) {
        if (identity.dateOfBirth.isBlank()) {
            applyDobMillis(viewModel, selectedDobMillis)
        }
    }

    val displayDate = remember(selectedDobMillis) {
        formatDisplayDate(selectedDobMillis)
    }

    Column {
        if (showHeader) {
            Text(
                text = headerTitle,
                style = theme.fonts.label,
                color = theme.colors.textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        ValidatedTextField(
            value = identity.firstName,
            onValueChange = { v -> viewModel.updateIdentity { it.copy(firstName = v) } },
            prompt = "First name",
            error = errors[CustomerInformationFieldVM.Field.FIRST_NAME],
            inlineError = true,
            onClearError = { viewModel.clearError(CustomerInformationFieldVM.Field.FIRST_NAME) },
            autofillContentType = ContentType.PersonFirstName,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        ValidatedTextField(
            value = identity.lastName,
            onValueChange = { v -> viewModel.updateIdentity { it.copy(lastName = v) } },
            prompt = "Last name",
            error = errors[CustomerInformationFieldVM.Field.LAST_NAME],
            inlineError = true,
            onClearError = { viewModel.clearError(CustomerInformationFieldVM.Field.LAST_NAME) },
            autofillContentType = ContentType.PersonLastName,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Enter your name exactly as it is recorded with government agencies (e.g., IRS).",
            style = theme.fonts.caption,
            color = theme.colors.textSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Email address",
            style = theme.fonts.label,
            color = theme.colors.textPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        ValidatedTextField(
            value = identity.email,
            onValueChange = { v -> viewModel.updateIdentity { it.copy(email = v) } },
            prompt = "Email address",
            error = errors[CustomerInformationFieldVM.Field.EMAIL],
            keyboardType = KeyboardType.Email,
            inlineError = true,
            onClearError = { viewModel.clearError(CustomerInformationFieldVM.Field.EMAIL) },
            autofillContentType = ContentType.EmailAddress
        )

        Spacer(Modifier.height(16.dp))

        // Phone error rendered as a header row above the field (compact mode).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Phone number", style = theme.fonts.label, color = theme.colors.textPrimary)
            errors[CustomerInformationFieldVM.Field.PHONE]?.let { msg ->
                Text(
                    text = msg,
                    style = theme.fonts.caption,
                    color = theme.colors.error
                )
            }
        }
        PhoneNumberTextField(
            value = identity.phoneNumber,
            onValueChange = { v -> viewModel.updateIdentity { it.copy(phoneNumber = v) } },
            prompt = "Phone number",
            regionCode = phoneCountry.alpha2,
            error = errors[CustomerInformationFieldVM.Field.PHONE],
            compactError = true,
            onClearError = { viewModel.clearError(CustomerInformationFieldVM.Field.PHONE) }
        )

        Spacer(Modifier.height(16.dp))

        // Date of birth header row with compact summary error.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Date of birth", style = theme.fonts.label, color = theme.colors.textPrimary)
            firstDobError?.let { msg ->
                Text(
                    text = msg,
                    style = theme.fonts.caption,
                    color = theme.colors.error
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .border(1.dp, theme.colors.surfaceStroke, RoundedCornerShape(theme.radii.medium))
                .clickable { showDatePicker = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = displayDate,
                style = theme.fonts.body,
                color = theme.colors.textPrimary
            )
        }

        if (showDatePicker) {
            SpinnerDatePickerDialog(
                initialMillis = selectedDobMillis,
                onDismiss = { showDatePicker = false },
                onDateSelected = { millis ->
                    selectedDobMillis = millis
                    applyDobMillis(viewModel, millis)
                    showDatePicker = false
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        if (identityVerifiedViaGovId) {
            // Verified with a government ID: replace both the SSN input and the button with a
            // confirmation line. SSN is optional (and omitted from submit) on this path.
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Verified with government ID.",
                    style = theme.fonts.bodySmall,
                    color = theme.colors.textPrimary
                )
                if (allowSsnInstead) {
                    TextButton(onClick = onUseSsnInstead) {
                        Text(
                            text = "Use SSN instead",
                            style = theme.fonts.caption,
                            color = theme.colors.textSecondary
                        )
                    }
                }
            }
        } else if (showSsnField) {
            Text(
                text = "Last 4 digits of Social Security number",
                style = theme.fonts.label,
                color = theme.colors.textSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            var ssnFocused by remember { mutableStateOf(false) }
            val ssnBorderColor = if (ssnFocused) {
                theme.colors.fieldFocusStroke
            } else {
                theme.colors.surfaceStroke
            }
            val ssnBorderWidth = if (ssnFocused) 1.5.dp else 1.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(
                        BorderStroke(ssnBorderWidth, ssnBorderColor),
                        RoundedCornerShape(theme.radii.medium)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "••• - •• -",
                    style = theme.fonts.body,
                    color = theme.colors.textSecondary,
                    modifier = Modifier.padding(start = 16.dp, end = 12.dp)
                )
                VerticalDivider(
                    thickness = ssnBorderWidth,
                    color = ssnBorderColor,
                    modifier = Modifier.fillMaxHeight()
                )
                OutlinedTextField(
                    value = identity.ssn,
                    onValueChange = { v ->
                        val filtered = v.filter(Char::isDigit).take(4)
                        viewModel.updateIdentity { it.copy(ssn = filtered) }
                        if (errors[CustomerInformationFieldVM.Field.SSN] != null) {
                            viewModel.clearError(CustomerInformationFieldVM.Field.SSN)
                        }
                    },
                    placeholder = {
                        Text("0000", style = theme.fonts.body, color = theme.colors.textSecondary)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { ssnFocused = it.isFocused },
                    singleLine = true,
                    textStyle = theme.fonts.body,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errors[CustomerInformationFieldVM.Field.SSN] != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        focusedContainerColor = theme.colors.surface,
                        unfocusedContainerColor = theme.colors.surface,
                        cursorColor = theme.colors.textPrimary
                    )
                )
            }
            errors[CustomerInformationFieldVM.Field.SSN]?.let { msg ->
                Text(
                    text = msg,
                    style = theme.fonts.caption,
                    color = theme.colors.error,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            if (showGovIdVerification) {
                Text(
                    text = "No Social Security number? Verify with an ID document",
                    style = theme.fonts.caption,
                    color = theme.colors.textSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isVerifyingGovId, onClick = onVerifyWithoutSsn)
                        .padding(top = 4.dp),
                )
            }
        }
    }
}

private fun defaultAdultDobMillis(): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.add(Calendar.YEAR, -18)
    return startOfUtcDayMillis(cal.timeInMillis)
}

private fun applyDobMillis(viewModel: CustomerInformationFieldVM, millis: Long) {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = millis
    }
    val year = cal.get(Calendar.YEAR).toString()
    val month = String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1)
    val day = String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH))
    viewModel.updateIdentity {
        it.copy(dateOfBirth = DateOfBirthFormatter.format(year, month, day))
    }
    viewModel.clearDateOfBirthErrors()
}

private fun parseIsoToUtcMillis(iso: String): Long? {
    val match = isoDobRegex.matchEntire(iso) ?: return null
    val (y, m, d) = match.destructured
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.clear()
    cal.set(Calendar.YEAR, y.toIntOrNull() ?: return null)
    cal.set(Calendar.MONTH, (m.toIntOrNull() ?: return null) - 1)
    cal.set(Calendar.DAY_OF_MONTH, d.toIntOrNull() ?: return null)
    return cal.timeInMillis
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
