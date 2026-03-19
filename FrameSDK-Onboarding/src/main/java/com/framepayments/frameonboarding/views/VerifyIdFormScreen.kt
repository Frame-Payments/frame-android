package com.framepayments.frameonboarding.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.classes.IdType
import com.framepayments.frameonboarding.networking.phoneotpverification.PhoneOTPVerificationAPI
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor
import kotlinx.coroutines.launch

private enum class UserIdentificationSteps {
    phoneAuth, verifyPhone, information
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VerifyIdFormScreen(
    accountId: String?,
    requiresDateOfBirth: Boolean = false,
    onBack: () -> Unit,
    onContinue: (issuingCountry: String, idType: IdType) -> Unit
) {
    val scope = rememberCoroutineScope()
    var identificationStep by remember { mutableStateOf(UserIdentificationSteps.phoneAuth) }

    var phoneNumber by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var pendingVerificationId by remember { mutableStateOf<String?>(null) }

    var selectedCountry by remember { mutableStateOf<String?>(null) }
    var selectedIdType by remember { mutableStateOf<IdType?>(null) }

    val canContinueInfo = selectedCountry != null && selectedIdType != null
    val canContinuePhone = phoneNumber.length >= 10 && (!requiresDateOfBirth || dateOfBirth.length == 10)

    val countryOptions = listOf(
        "United States",
        "Canada",
        "United Kingdom",
        "Australia",
        "Mexico"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (identificationStep) {
                            UserIdentificationSteps.phoneAuth -> if (requiresDateOfBirth) "Enter Your Phone Number & DOB" else "Enter Your Phone Number"
                            UserIdentificationSteps.verifyPhone -> "Enter Verification Code"
                            UserIdentificationSteps.information -> "Personal Information"
                        }
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = {
                            when (identificationStep) {
                                UserIdentificationSteps.phoneAuth -> onBack()
                                UserIdentificationSteps.verifyPhone -> identificationStep = UserIdentificationSteps.phoneAuth
                                UserIdentificationSteps.information -> identificationStep = UserIdentificationSteps.phoneAuth
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                when (identificationStep) {
                    UserIdentificationSteps.phoneAuth -> {
                        Text(
                            text = "We'll send you a code - it helps us keep your account secure.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(20.dp))
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it.filter(Char::isDigit).take(15) },
                            label = { Text("Phone Number") },
                            placeholder = { Text("Enter your phone number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        if (requiresDateOfBirth) {
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                value = dateOfBirth,
                                onValueChange = { dateOfBirth = it.take(10) },
                                label = { Text("Date of Birth") },
                                placeholder = { Text("YYYY-MM-DD") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    UserIdentificationSteps.verifyPhone -> {
                        VerifyCardScreen(
                            headerTitle = "Enter Verification Code",
                            bodyText = "We've sent a verification code to your phone. Enter it below.",
                            confirmButtonText = "Confirm",
                            digitCount = 6,
                            onBack = { identificationStep = UserIdentificationSteps.phoneAuth },
                            onResendCode = {
                                val resolvedAccountId = accountId ?: return@VerifyCardScreen
                                scope.launch {
                                    PhoneOTPVerificationAPI.createVerification(
                                        accountId = resolvedAccountId,
                                        phoneNumber = phoneNumber,
                                        dateOfBirth = dateOfBirth
                                    )
                                }
                            },
                            onContinue = {
                                val resolvedAccountId = accountId
                                val verificationId = pendingVerificationId
                                if (resolvedAccountId == null || verificationId == null) {
                                    identificationStep = UserIdentificationSteps.information
                                } else {
                                    scope.launch {
                                        PhoneOTPVerificationAPI.confirmVerification(resolvedAccountId, verificationId)
                                        identificationStep = UserIdentificationSteps.information
                                    }
                                }
                            }
                        )
                    }

                    UserIdentificationSteps.information -> {
                        Text(
                            text = "Select the country that issued your government ID and the type of ID you'll use to verify your identity.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(20.dp))

                        DropdownField(
                            label = "Issuing Country",
                            value = selectedCountry ?: "Selection",
                            options = countryOptions,
                            onSelected = { selectedCountry = it }
                        )

                        Spacer(Modifier.height(16.dp))

                        DropdownField(
                            label = "ID Type",
                            value = selectedIdType?.displayName ?: "Selection",
                            options = IdType.entries.map { it.displayName },
                            onSelected = { picked ->
                                selectedIdType = IdType.entries.first { it.displayName == picked }
                            }
                        )
                    }
                }
            }

            if (identificationStep != UserIdentificationSteps.verifyPhone) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = when (identificationStep) {
                        UserIdentificationSteps.phoneAuth -> canContinuePhone
                        UserIdentificationSteps.information -> canContinueInfo
                        UserIdentificationSteps.verifyPhone -> false
                    },
                    onClick = {
                        when (identificationStep) {
                            UserIdentificationSteps.phoneAuth -> {
                                val resolvedAccountId = accountId
                                if (resolvedAccountId == null) {
                                    identificationStep = UserIdentificationSteps.information
                                    return@Button
                                }
                                scope.launch {
                                    val (result, _) = PhoneOTPVerificationAPI.createVerification(
                                        accountId = resolvedAccountId,
                                        phoneNumber = phoneNumber,
                                        dateOfBirth = dateOfBirth
                                    )
                                    pendingVerificationId = result?.id
                                    identificationStep = if (pendingVerificationId != null) {
                                        UserIdentificationSteps.verifyPhone
                                    } else {
                                        UserIdentificationSteps.information
                                    }
                                }
                            }
                            UserIdentificationSteps.information -> {
                                val country = selectedCountry ?: return@Button
                                val idType = selectedIdType ?: return@Button
                                onContinue(country, idType)
                            }
                            UserIdentificationSteps.verifyPhone -> Unit
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

@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))

        Box {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            onSelected(item)
                            expanded = false
                        }
                    )
                }
            }

            // Overlay to make the full field tappable (excluding trailing icon area)
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .padding(end = 48.dp)
                    .clickable { expanded = true }
            )
        }
    }
}
