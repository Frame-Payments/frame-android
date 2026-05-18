package com.framepayments.frameonboarding.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.framepayments.frameonboarding.reusable.ContinueButton
import com.framepayments.frameonboarding.reusable.PaymentCardForm
import com.framepayments.frameonboarding.reusable.PaymentDivider
import com.framepayments.frameonboarding.validation.OnboardingValidators
import com.framepayments.frameonboarding.viewmodels.BillingAddressFieldVM
import com.framepayments.frameonboarding.viewmodels.BillingAddressMode
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk_ui.EncryptedPaymentCardInput
import com.framepayments.framesdk_ui.buttons.FrameGooglePayButton
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import com.framepayments.framesdk_ui.theme.FrameTheme
import com.framepayments.framesdk_ui.theme.FrameThemePreviews

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
                postalCode = current.postalCode?.takeIf { it.isNotBlank() } ?: billing.postalCode
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
        var googlePayReady by remember { mutableStateOf(false) }
        val resolvedAccountId by viewModel.resolvedAccountId.collectAsState()

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Google Pay wallet attach button. The button gates its own visibility internally
            // via Google's `isReadyToPay` check + Frame's wallet config + the merchant ID stored
            // on `FrameNetworking` (set once at SDK init). If the merchant ID isn't configured,
            // the button stays hidden — no host-app opt-in required here.
            if (!isPreview) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx -> FrameGooglePayButton(ctx) },
                    update = { btn ->
                        btn.configure(
                            mode = FrameGooglePayButton.Mode.AddToOwner(
                                customerId = null,
                                accountId = resolvedAccountId
                            ),
                            onResult = { result ->
                                when (result) {
                                    is FrameGooglePayButton.Result.PaymentMethodCreated -> {
                                        viewModel.appendNewlyAddedPaymentMethod(result.paymentMethod)
                                        onBack()
                                    }
                                    is FrameGooglePayButton.Result.Failure -> {
                                        // Transport failures already surfaced via FrameSnackbarController
                                        // inside the button; non-transport failures stay quiet here
                                        // since the user can fall through to card entry below.
                                    }
                                    else -> Unit
                                }
                            },
                            onReadinessChanged = { isReady -> googlePayReady = isReady }
                        )
                    }
                )
                if (googlePayReady) {
                    PaymentDivider()
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Card Details",
                    style = LocalFrameTheme.current.fonts.label
                )
                cardError?.let { msg ->
                    Text(
                        text = msg,
                        style = LocalFrameTheme.current.fonts.caption,
                        color = LocalFrameTheme.current.colors.error
                    )
                }
            }
            if (isPreview) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp)
                        .background(LocalFrameTheme.current.colors.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "EncryptedPaymentCardInput (preview placeholder)",
                        style = LocalFrameTheme.current.fonts.bodySmall,
                        color = LocalFrameTheme.current.colors.textSecondary
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
                        modifier = Modifier.fillMaxWidth(),
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
                    style = LocalFrameTheme.current.fonts.bodySmall
                )
            }

            Spacer(Modifier.height(24.dp))

            val isPerformingAction by viewModel.isPerformingAction.collectAsState()
            ContinueButton(
                enabled = evervaultReady != null,
                isLoading = isPerformingAction,
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
                }
            )
        }
    }
}

@FrameThemePreviews
@Composable
private fun AddPaymentMethodScreenPreview() {
    FrameTheme {
    MaterialTheme {
        AddPaymentMethodScreen(
            viewModel = FrameOnboardingViewModel(
                OnboardingConfig(skipInitNetwork = true)
            ),
            onBack = {}
        )
    }
    }
}
