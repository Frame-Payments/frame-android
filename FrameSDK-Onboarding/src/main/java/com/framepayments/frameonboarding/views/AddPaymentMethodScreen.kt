package com.framepayments.frameonboarding.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.reusable.BillingAddressDetailView
import com.framepayments.frameonboarding.reusable.PaymentCardForm
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor
import com.framepayments.frameonboarding.validation.OnboardingValidators
import com.framepayments.frameonboarding.viewmodels.BillingAddressFieldVM
import com.framepayments.frameonboarding.viewmodels.BillingAddressMode
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk_ui.EncryptedPaymentCardInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddPaymentMethodScreen(
    viewModel: FrameOnboardingViewModel,
    onBack: () -> Unit
) {
    val paymentCard by viewModel.paymentCardData.collectAsState()
    val card by viewModel.paymentCardDraft.collectAsState()
    val billing by viewModel.createdBillingAddress.collectAsState()

    val isPreview = LocalInspectionMode.current

    var evervaultReady by remember(isPreview) {
        mutableStateOf<Boolean?>(if (isPreview) false else null)
    }

    LaunchedEffect(isPreview) {
        if (isPreview) {
            viewModel.setAddPaymentUsesEvervaultCardUi(false)
        } else {
            val ok = FrameNetworking.ensureEvervaultReadyForCardInputs()
            evervaultReady = ok
            viewModel.setAddPaymentUsesEvervaultCardUi(ok)
        }
    }

    val billingVM = rememberSaveable(
        saver = BillingAddressFieldVM.Saver(BillingAddressMode.US_ONLY)
    ) { BillingAddressFieldVM(billing, BillingAddressMode.US_ONLY) }
    var cardError by rememberSaveable { mutableStateOf<String?>(null) }

    // Auto-clear card error when the user changes card details (iOS .onChange(of: cardData)).
    LaunchedEffect(paymentCard, card) {
        if (cardError != null) cardError = null
    }

    // Merge async billing prefill (e.g. from Plaid metadata, account profile) into the
    // per-screen VM without clobbering user-typed values.
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Payment Method") },
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
            Text(
                text = "Card Details",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (isPreview) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "EncryptedPaymentCardInput (preview placeholder)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                when (evervaultReady) {
                    null -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    true -> AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp),
                        factory = { EncryptedPaymentCardInput(it) },
                        update = { view ->
                            view.onCardDataChange = { viewModel.onPaymentCardDataChange(it) }
                        }
                    )
                    false -> PaymentCardForm(
                        cardNumber = card.cardNumber,
                        onCardNumberChange = { n -> viewModel.updatePaymentCardDraft { it.copy(cardNumber = n) } },
                        expiryMonth = card.expiryMonth,
                        expiryYear = card.expiryYear,
                        onExpiryChange = { m, y -> viewModel.updatePaymentCardDraft { it.copy(expiryMonth = m, expiryYear = y) } },
                        cvc = card.cvc,
                        onCvcChange = { c -> viewModel.updatePaymentCardDraft { it.copy(cvc = c) } }
                    )
                }
            }

            cardError?.let { msg ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            BillingAddressDetailView(
                viewModel = billingVM,
                headerTitle = "Billing Address"
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = card.useForPayouts,
                    onCheckedChange = { checked -> viewModel.updatePaymentCardDraft { it.copy(useForPayouts = checked) } }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Use this card for payouts if eligible",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = evervaultReady != null,
                onClick = {
                    val addressOK = billingVM.validate()
                    val cardOK = if (evervaultReady == true) {
                        OnboardingValidators.validateCard(paymentCard).also { cardError = it } == null
                    } else {
                        // Manual fallback form: validate via the existing form-completeness check.
                        val ok = viewModel.isPaymentMethodFormComplete(
                            paymentCard,
                            card,
                            billingVM.address.value,
                            onlyAddress = false,
                            useEvervaultCardInput = false
                        )
                        if (!ok) cardError = "Enter valid card details"
                        ok
                    }
                    if (addressOK && cardOK) {
                        viewModel.updateCreatedBillingAddress { billingVM.address.value }
                        viewModel.submitNewPaymentMethod()
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

@Preview(showBackground = true)
@Composable
private fun AddPaymentMethodScreenPreview() {
    MaterialTheme {
        AddPaymentMethodScreen(
            viewModel = FrameOnboardingViewModel(
                OnboardingConfig(skipInitNetwork = true)
            ),
            onBack = {}
        )
    }
}
