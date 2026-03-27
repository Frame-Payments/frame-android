package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.reusable.BankAccountForm
import com.framepayments.frameonboarding.reusable.BillingAddressForm
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddPayoutMethodScreen(
    viewModel: FrameOnboardingViewModel,
    onBack: () -> Unit
) {
    val bank by viewModel.bankAccountDraft.collectAsState()
    val billing by viewModel.createdBillingAddress.collectAsState()

    val canContinue = remember(bank, billing) {
        viewModel.isPayoutMethodFormComplete(bank, billing)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Bank Account (ACH)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            BankAccountForm(
                routingNumber = bank.routingNumber,
                onRoutingNumberChange = { v -> viewModel.updateBankAccountDraft { it.copy(routingNumber = v) } },
                accountNumber = bank.accountNumber,
                onAccountNumberChange = { v -> viewModel.updateBankAccountDraft { it.copy(accountNumber = v) } },
                accountType = bank.accountTypeLabel,
                onAccountTypeChange = { v -> viewModel.updateBankAccountDraft { it.copy(accountTypeLabel = v) } }
            )

            Spacer(Modifier.height(24.dp))

            BillingAddressForm(
                addressLine1 = billing.addressLine1.orEmpty(),
                onAddressLine1Change = { v -> viewModel.updateCreatedBillingAddress { it.copy(addressLine1 = v) } },
                addressLine2 = billing.addressLine2.orEmpty(),
                onAddressLine2Change = { v ->
                    viewModel.updateCreatedBillingAddress { it.copy(addressLine2 = v.ifBlank { null }) }
                },
                city = billing.city.orEmpty(),
                onCityChange = { v -> viewModel.updateCreatedBillingAddress { it.copy(city = v) } },
                state = billing.state.orEmpty(),
                onStateChange = { v -> viewModel.updateCreatedBillingAddress { it.copy(state = v) } },
                zipCode = billing.postalCode,
                onZipCodeChange = { v -> viewModel.updateCreatedBillingAddress { it.copy(postalCode = v) } },
                headerTitle = "Account Holder Address"
            )

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canContinue,
                onClick = { viewModel.submitNewPayoutMethod() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = FramePrimaryColor,
                    contentColor = FrameOnPrimaryColor,
                    disabledContainerColor = FramePrimaryColor.copy(alpha = 0.35f),
                    disabledContentColor = FrameOnPrimaryColor.copy(alpha = 0.7f)
                )
            ) {
                Text("Add Bank Account")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPayoutMethodScreenPreview() {
    AddPayoutMethodScreen(
        viewModel = FrameOnboardingViewModel(OnboardingConfig()),
        onBack = {}
    )
}
