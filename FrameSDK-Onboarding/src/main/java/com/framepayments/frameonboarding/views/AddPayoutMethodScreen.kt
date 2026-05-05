package com.framepayments.frameonboarding.views

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.reusable.BankAccountDetailView
import com.framepayments.frameonboarding.reusable.BillingAddressDetailView
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor
import com.framepayments.frameonboarding.viewmodels.BankAccountFieldVM
import com.framepayments.frameonboarding.viewmodels.BillingAddressFieldVM
import com.framepayments.frameonboarding.viewmodels.BillingAddressMode
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
import com.plaid.link.FastOpenPlaidLink
import com.plaid.link.Plaid
import com.plaid.link.PlaidHandler
import com.plaid.link.configuration.LinkTokenConfiguration
import com.plaid.link.result.LinkExit
import com.plaid.link.result.LinkSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddPayoutMethodScreen(
    viewModel: FrameOnboardingViewModel,
    onBack: () -> Unit
) {
    val bank by viewModel.bankAccountDraft.collectAsState()
    val billing by viewModel.createdBillingAddress.collectAsState()
    val plaidToken by viewModel.plaidLinkToken.collectAsState()
    val isConnecting by viewModel.isConnectingPlaidBank.collectAsState()

    var showManualForm by rememberSaveable { mutableStateOf(false) }

    val bankVM = rememberSaveable(saver = BankAccountFieldVM.Saver) {
        BankAccountFieldVM(bank)
    }
    val billingVM = rememberSaveable(
        saver = BillingAddressFieldVM.Saver(BillingAddressMode.US_ONLY)
    ) { BillingAddressFieldVM(billing, BillingAddressMode.US_ONLY) }

    // Merge async backend updates (e.g. Plaid metadata populates accountTypeLabel /
    // billing address) into the per-screen VMs without clobbering user-typed values.
    LaunchedEffect(bank) {
        bankVM.updateDraft { current ->
            current.copy(
                routingNumber = current.routingNumber.ifBlank { bank.routingNumber },
                accountNumber = current.accountNumber.ifBlank { bank.accountNumber },
                accountTypeLabel = if (current.accountTypeLabel.isBlank() ||
                    current.accountTypeLabel == "Checking"
                ) bank.accountTypeLabel else current.accountTypeLabel
            )
        }
    }
    LaunchedEffect(billing) {
        billingVM.updateAddress { current ->
            current.copy(
                addressLine1 = current.addressLine1?.takeIf { it.isNotBlank() } ?: billing.addressLine1,
                addressLine2 = current.addressLine2 ?: billing.addressLine2,
                city = current.city?.takeIf { it.isNotBlank() } ?: billing.city,
                state = current.state?.takeIf { it.isNotBlank() } ?: billing.state,
                postalCode = current.postalCode.ifBlank { billing.postalCode }
            )
        }
    }

    val application = LocalContext.current.applicationContext as Application

    val plaidLauncher = rememberLauncherForActivityResult(FastOpenPlaidLink()) { result ->
        when (result) {
            is LinkSuccess -> {
                val account = result.metadata.accounts.firstOrNull()
                viewModel.handlePlaidSuccess(
                    publicToken = result.publicToken,
                    plaidAccountId = account?.id ?: "",
                    institutionName = result.metadata.institution?.name,
                    subtype = account?.subtype?.json
                )
            }
            is LinkExit -> {
                viewModel.onPlaidDismissed()
                result.error?.let { android.util.Log.w("Plaid", "Plaid exited: ${it.displayMessage}") }
            }
            else -> {
                viewModel.onPlaidDismissed()
            }
        }
    }

    LaunchedEffect(plaidToken) {
        plaidToken?.let { token ->
            viewModel.clearPlaidLinkToken()
            val configuration = LinkTokenConfiguration.Builder().token(token).build()
            val handler: PlaidHandler = Plaid.create(application, configuration)
            plaidLauncher.launch(handler)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Bank Account") },
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
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isConnecting,
                onClick = { viewModel.fetchPlaidLinkToken() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = FramePrimaryColor,
                    contentColor = FrameOnPrimaryColor,
                    disabledContainerColor = FramePrimaryColor.copy(alpha = 0.35f),
                    disabledContentColor = FrameOnPrimaryColor.copy(alpha = 0.7f)
                )
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        color = FrameOnPrimaryColor,
                        modifier = Modifier.height(20.dp).padding(end = 8.dp)
                    )
                }
                Text("Connect Bank Account")
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showManualForm = true }
            ) {
                Text("Enter manually")
            }

            AnimatedVisibility(visible = showManualForm) {
                Column {
                    Spacer(Modifier.height(16.dp))

                    BankAccountDetailView(viewModel = bankVM)

                    Spacer(Modifier.height(24.dp))

                    BillingAddressDetailView(
                        viewModel = billingVM,
                        headerTitle = "Billing Address"
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true,
                        onClick = {
                            val bankOK = bankVM.validate()
                            val addressOK = billingVM.validate()
                            if (bankOK && addressOK) {
                                viewModel.updateBankAccountDraft { bankVM.draft.value }
                                viewModel.updateCreatedBillingAddress { billingVM.address.value }
                                viewModel.submitNewPayoutMethod()
                            }
                        },
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
