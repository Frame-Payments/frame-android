package com.framepayments.frameonboarding.reusable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.validation.DateOfBirthFormatter
import com.framepayments.frameonboarding.viewmodels.CustomerInformationFieldVM

private val isoDobRegex = Regex("""^(\d{4})-(\d{1,2})-(\d{1,2})$""")

/**
 * Customer information form (first/last name, email, phone, DOB, SSN) bound to a
 * [CustomerInformationFieldVM]. 1:1 port of iOS CustomerInformationView.
 *
 * Fields use inline error display except the three DOB fields, which run in compact mode
 * with a single header-row error summary (matches iOS firstDateOfBirthError pattern).
 */
@Composable
fun CustomerInformationView(
    viewModel: CustomerInformationFieldVM,
    headerTitle: String = "Customer Information",
    showHeader: Boolean = true
) {
    val identity by viewModel.identity.collectAsState()
    val phoneCountry by viewModel.phoneCountry.collectAsState()
    val errors by viewModel.errors.collectAsState()
    val firstDobError by viewModel.firstDateOfBirthError.collectAsState()

    // Three local DOB components, synced bidirectionally to identity.dateOfBirth.
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }

    // Hydrate from stored ISO when it changes externally (e.g. async account profile fetch).
    LaunchedEffect(identity.dateOfBirth) {
        val match = isoDobRegex.matchEntire(identity.dateOfBirth) ?: return@LaunchedEffect
        val (y, m, d) = match.destructured
        // Avoid clobbering user's in-progress typing — only overwrite if the local component
        // would format to a different ISO than what's stored.
        val currentIso = DateOfBirthFormatter.format(birthYear, birthMonth, birthDay)
        if (currentIso != identity.dateOfBirth) {
            birthYear = y
            birthMonth = m.padStart(2, '0').takeLast(2)
            birthDay = d.padStart(2, '0').takeLast(2)
        }
    }

    val syncDob: () -> Unit = {
        viewModel.updateIdentity {
            it.copy(dateOfBirth = DateOfBirthFormatter.format(birthYear, birthMonth, birthDay))
        }
    }

    Column {
        if (showHeader) {
            Text(
                text = headerTitle,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ValidatedTextField(
                value = identity.firstName,
                onValueChange = { v -> viewModel.updateIdentity { it.copy(firstName = v) } },
                prompt = "First Name",
                error = errors[CustomerInformationFieldVM.Field.FIRST_NAME],
                inlineError = true,
                onClearError = { viewModel.clearError(CustomerInformationFieldVM.Field.FIRST_NAME) },
                modifier = Modifier.weight(1f)
            )
            ValidatedTextField(
                value = identity.lastName,
                onValueChange = { v -> viewModel.updateIdentity { it.copy(lastName = v) } },
                prompt = "Last Name",
                error = errors[CustomerInformationFieldVM.Field.LAST_NAME],
                inlineError = true,
                onClearError = { viewModel.clearError(CustomerInformationFieldVM.Field.LAST_NAME) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        ValidatedTextField(
            value = identity.email,
            onValueChange = { v -> viewModel.updateIdentity { it.copy(email = v) } },
            prompt = "Email Address",
            error = errors[CustomerInformationFieldVM.Field.EMAIL],
            keyboardType = KeyboardType.Email,
            inlineError = true,
            onClearError = { viewModel.clearError(CustomerInformationFieldVM.Field.EMAIL) }
        )

        Spacer(Modifier.height(16.dp))

        // Phone error rendered as a header row above the field (compact mode).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Phone Number", style = MaterialTheme.typography.labelMedium)
            errors[CustomerInformationFieldVM.Field.PHONE]?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        PhoneNumberTextField(
            value = identity.phoneNumber,
            onValueChange = { v -> viewModel.updateIdentity { it.copy(phoneNumber = v) } },
            prompt = "Phone Number",
            regionCode = phoneCountry.alpha2,
            error = errors[CustomerInformationFieldVM.Field.PHONE],
            compactError = true,
            onClearError = { viewModel.clearError(CustomerInformationFieldVM.Field.PHONE) }
        )

        Spacer(Modifier.height(16.dp))

        // Birthday header row with compact summary error.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Birthday", style = MaterialTheme.typography.labelMedium)
            firstDobError?.let { msg ->
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
                value = birthMonth,
                onValueChange = { v ->
                    birthMonth = v.filter(Char::isDigit).take(2)
                    syncDob()
                },
                prompt = "MM",
                error = errors[CustomerInformationFieldVM.Field.BIRTH_MONTH],
                keyboardType = KeyboardType.Number,
                characterLimit = 2,
                compactError = true,
                onClearError = { viewModel.clearDateOfBirthErrors() },
                modifier = Modifier.weight(1f)
            )
            ValidatedTextField(
                value = birthDay,
                onValueChange = { v ->
                    birthDay = v.filter(Char::isDigit).take(2)
                    syncDob()
                },
                prompt = "DD",
                error = errors[CustomerInformationFieldVM.Field.BIRTH_DAY],
                keyboardType = KeyboardType.Number,
                characterLimit = 2,
                compactError = true,
                onClearError = { viewModel.clearDateOfBirthErrors() },
                modifier = Modifier.weight(1f)
            )
            ValidatedTextField(
                value = birthYear,
                onValueChange = { v ->
                    birthYear = v.filter(Char::isDigit).take(4)
                    syncDob()
                },
                prompt = "YYYY",
                error = errors[CustomerInformationFieldVM.Field.BIRTH_YEAR],
                keyboardType = KeyboardType.Number,
                characterLimit = 4,
                compactError = true,
                onClearError = { viewModel.clearDateOfBirthErrors() },
                modifier = Modifier.weight(2f)
            )
        }

        Spacer(Modifier.height(16.dp))

        ValidatedTextField(
            value = identity.ssn,
            onValueChange = { v ->
                val filtered = v.filter(Char::isDigit).take(4)
                viewModel.updateIdentity { it.copy(ssn = filtered) }
            },
            prompt = "SSN (last 4 digits)",
            error = errors[CustomerInformationFieldVM.Field.SSN],
            keyboardType = KeyboardType.Number,
            characterLimit = 4,
            inlineError = true,
            onClearError = { viewModel.clearError(CustomerInformationFieldVM.Field.SSN) }
        )
    }
}
