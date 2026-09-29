package com.framepayments.frameonboarding.views

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.R
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.reusable.BankAccountDetailView
import com.framepayments.frameonboarding.reusable.MethodOptionRow
import com.framepayments.framesdk_ui.reusable.ContinueButton
import com.framepayments.frameonboarding.viewmodels.BankAccountFieldVM
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
import com.plaid.link.FastOpenPlaidLink
import com.plaid.link.Plaid
import com.plaid.link.PlaidHandler
import com.plaid.link.configuration.LinkTokenConfiguration
import com.plaid.link.result.LinkExit
import com.plaid.link.result.LinkSuccess
import com.framepayments.framesdk_ui.theme.FrameTheme
import com.framepayments.framesdk_ui.theme.FrameThemePreviews
import com.framepayments.framesdk_ui.theme.LocalFrameTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddPayoutMethodScreen(
    viewModel: FrameOnboardingViewModel,
    onBack: () -> Unit
) {
    val bank by viewModel.bankAccountDraft.collectAsState()
    val plaidToken by viewModel.plaidLinkToken.collectAsState()
    val isConnecting by viewModel.isConnectingPlaidBank.collectAsState()

    var showManualForm by rememberSaveable { mutableStateOf(false) }

    val bankVM = rememberSaveable(saver = BankAccountFieldVM.Saver) {
        BankAccountFieldVM(bank)
    }

    // Merge async backend updates into the per-screen VM without clobbering user-typed values.
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

    val application = LocalContext.current.applicationContext as Application
    val theme = LocalFrameTheme.current

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
                val errorMsg = result.error?.displayMessage
                    ?: result.error?.errorCode?.toString()
                viewModel.onPlaidDismissed(errorMessage = errorMsg)
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
        containerColor = LocalFrameTheme.current.colors.surface,
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
            MethodOptionRow(
                iconRes = R.drawable.ic_connect_bank,
                title = "Connect a bank account",
                onClick = { viewModel.fetchPlaidLinkToken() },
                enabled = !isConnecting
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.colors.surfaceStroke, RoundedCornerShape(theme.radii.medium))
                    .clickable { showManualForm = !showManualForm }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enter bank details manually",
                    style = theme.fonts.bodySmall,
                    color = theme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (showManualForm) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.AutoMirrored.Filled.KeyboardArrowRight
                    },
                    contentDescription = null,
                    tint = theme.colors.textSecondary
                )
            }

            AnimatedVisibility(visible = showManualForm) {
                Column {
                    Spacer(Modifier.height(16.dp))

                    BankAccountDetailView(viewModel = bankVM)

                    Spacer(Modifier.height(24.dp))

                    ContinueButton(
                        text = "Save bank",
                        isLoading = isConnecting,
                        onClick = {
                            if (bankVM.validate()) {
                                viewModel.updateBankAccountDraft { bankVM.draft.value }
                                viewModel.submitNewPayoutMethod()
                            }
                        }
                    )
                }
            }
        }
    }
}

@FrameThemePreviews
@Composable
private fun AddPayoutMethodScreenPreview() {
    FrameTheme {
    AddPayoutMethodScreen(
        viewModel = FrameOnboardingViewModel(OnboardingConfig()),
        onBack = {}
    )
    }
}
