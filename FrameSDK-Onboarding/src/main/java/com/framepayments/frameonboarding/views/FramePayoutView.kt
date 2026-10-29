package com.framepayments.frameonboarding.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.R
import com.framepayments.frameonboarding.reusable.MethodOptionRow
import com.framepayments.frameonboarding.viewmodels.FramePayoutViewModel
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameResult
import com.framepayments.framesdk.payoutsessions.FramePayoutClientSecret
import com.framepayments.framesdk.payoutsessions.PayoutSessionsAPI
import com.framepayments.framesdk.transfersv2.TransferV2Requests
import com.framepayments.framesdk.transfersv2.TransfersV2API
import com.framepayments.framesdk_ui.reusable.ContinueButton
import com.framepayments.framesdk_ui.reusable.FrameAmountKeypad
import com.framepayments.framesdk_ui.reusable.FramePaymentMethodRow
import com.framepayments.framesdk_ui.reusable.refreshesSonarSession
import com.framepayments.framesdk_ui.snackbar.FrameSnackbarController
import com.framepayments.framesdk_ui.snackbar.SnackbarBridge
import com.framepayments.framesdk_ui.theme.FrameTheme
import com.framepayments.framesdk_ui.theme.FrameThemePreviews
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.framepayments.framesdk_ui.R as FrameUiR

/**
 * Amount-and-bank screen for paying out to a saved ACH account.
 *
 * Present this composable. The keypad is capped at the payout session amount. Continue
 * creates `POST /v2/transfers` with the session token and [onResult] receives that transfer id.
 * Dismissing this screen returns [FrameResult.Cancelled]. Closing the bank picker returns here.
 *
 * Mint `pyt_sess_` on the host with `POST /v1/payout_sessions` and the withdrawable balance
 * as the amount cap. Do not mint `sk_` inside the SDK. A checkout session can only create a
 * payment; an onboarding session cannot create a transfer.
 *
 * The elected primary bank is prefilled from `payout_payment_method_id`. A single saved bank
 * is selected either way.
 *
 * @param accountId The Frame account the payout is drawn from.
 * @param clientSecret Payout-session token (`pyt_sess_…`) from `POST /v1/payout_sessions`.
 *   Lists banks, opens the add-bank flow, and creates the transfer. The session amount is the
 *   keypad cap.
 * @param onResult Called exactly once with a [FrameResult]. [FrameResult.Completed] carries the
 *   created transfer id.
 *
 * @sample
 * ```kotlin
 * FramePayoutView(
 *     accountId = accountId,
 *     clientSecret = FramePayoutClientSecret(
 *         clientSecret = session.clientSecret,
 *         expiresAt = session.expiresAt,
 *         amountCents = availableCents,
 *     ),
 *     onResult = { result -> },
 * )
 * ```
 */
@Composable
fun FramePayoutView(
    accountId: String,
    clientSecret: FramePayoutClientSecret,
    onResult: (FrameResult) -> Unit,
) {
    val viewModel = payoutViewModel(accountId, clientSecret.amountCents)
    SideEffect { viewModel.onResult = onResult }
    val snackbarHostState = remember { SnackbarHostState() }
    var showPayoutMethods by remember { mutableStateOf(false) }

    fun cancel() {
        if (viewModel.isCreating) return
        viewModel.finish(FrameResult.Cancelled)
    }

    BackHandler(enabled = !showPayoutMethods && !viewModel.didFinish) { cancel() }

    LaunchedEffect(accountId) {
        if (accountId.isNotEmpty()) {
            FrameNetworking.setAccountIdIfUnset(accountId)
        }
    }

    fun toast(message: String) {
        FrameSnackbarController.emit(message)
    }

    suspend fun loadMethods() {
        viewModel.isLoadingMethods = true
        try {
            val (methods, listError) = PayoutSessionsAPI.loadPaymentMethods(accountId, clientSecret)
            val (primaryId, _) = PayoutSessionsAPI.loadAccount(accountId, clientSecret)
            if (viewModel.didFinish) return
            if (listError != null) {
                toast(listError.toastMessage())
                if (viewModel.didLoadMethods) return
            }
            viewModel.applyLoadedMethods(methods.orEmpty(), primaryId)
        } finally {
            viewModel.isLoadingMethods = false
        }
    }

    fun presentBankPicker() {
        if (viewModel.isCreating || viewModel.isLoadingMethods) return
        viewModel.viewModelScope.launch {
            val token = PayoutSessionsAPI.authorizationToken(accountId, clientSecret)
            if (token == null) {
                toast("Could not load payout methods. Please try again.")
                return@launch
            }
            showPayoutMethods = true
        }
    }

    fun submit() {
        val methodId = viewModel.selectedPaymentMethod?.id ?: return
        if (!viewModel.canConfirm || viewModel.isCreating) return
        val cents = viewModel.typedCents
        val idempotencyKey = viewModel.rememberIdempotencyKey(cents, methodId)
        viewModel.isCreating = true
        viewModel.viewModelScope.launch {
            try {
                withContext(NonCancellable) {
                    val token = PayoutSessionsAPI.authorizationToken(accountId, clientSecret)
                    if (token == null) {
                        if (!viewModel.didFinish) toast("Could not send this payout. Please try again.")
                        return@withContext
                    }
                    val request = TransferV2Requests.CreateTransferRequest(
                        amount = TransferV2Requests.MoneyAmount(
                            value = cents,
                            currency = clientSecret.amountCurrency,
                        ),
                        destination = TransferV2Requests.EndpointSlot(paymentMethodId = methodId),
                    )
                    val (transfer, error) = TransfersV2API.createPayoutTransfer(
                        request,
                        payoutClientSecret = token,
                        idempotencyKey = idempotencyKey,
                        accountId = accountId,
                    )
                    if (error != null) {
                        if (!viewModel.didFinish) toast(error.toastMessage())
                        viewModel.rotateIdempotencyKeyIfNeeded(error)
                        return@withContext
                    }
                    val transferId = transfer?.id?.takeIf { it.isNotEmpty() }
                    if (transferId == null) {
                        if (!viewModel.didFinish) toast("Could not send this payout. Please try again.")
                        return@withContext
                    }
                    withContext(Dispatchers.Main.immediate) {
                        viewModel.finish(FrameResult.Completed(transferId))
                    }
                }
            } finally {
                viewModel.isCreating = false
            }
        }
    }

    LaunchedEffect(accountId) { loadMethods() }

    if (showPayoutMethods) {
        FrameSelectPayoutMethodView(
            accountId = accountId,
            clientSecret = clientSecret.clientSecret,
        ) { result ->
            if (result is FrameResult.Completed) {
                viewModel.selectElected(result.id)
            }
            showPayoutMethods = false
            viewModel.isLoadingMethods = true
            viewModel.viewModelScope.launch { loadMethods() }
        }
        return
    }

    FrameTheme {
        val frameTheme = LocalFrameTheme.current
        val colors = frameTheme.colors
        val fonts = frameTheme.fonts
        val spacing = frameTheme.spacing
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .refreshesSonarSession(accountId = accountId.takeIf { it.isNotEmpty() }),
            containerColor = colors.surface,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            SnackbarBridge(snackbarHostState)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .navigationBarsPadding()
                    .background(colors.surface),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.sectionTop),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Payout",
                        style = fonts.heading,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { cancel() }) {
                        Image(
                            painter = painterResource(FrameUiR.drawable.ic_close),
                            contentDescription = stringResource(FrameUiR.string.close),
                            modifier = Modifier
                                .size(30.dp)
                                .padding(horizontal = 4.dp),
                        )
                    }
                }
                HorizontalDivider(color = colors.surfaceStroke)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = viewModel.amountText,
                        style = fonts.depositAmount,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Available ${viewModel.availableText}",
                        style = fonts.caption,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = spacing.sectionGap),
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = spacing.sectionGap),
                ) {
                    Text(
                        text = "Pay out to",
                        style = fonts.headline,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Box(modifier = Modifier.padding(top = spacing.sectionGap)) {
                        when {
                            !viewModel.didLoadMethods -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 56.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = colors.textPrimary,
                                        strokeWidth = 2.dp,
                                    )
                                }
                            }
                            viewModel.selectedPaymentMethod != null -> {
                                FramePaymentMethodRow(
                                    paymentMethod = viewModel.selectedPaymentMethod!!,
                                    isSelected = true,
                                    onTap = { presentBankPicker() },
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                            viewModel.payoutMethods.isEmpty() -> {
                                MethodOptionRow(
                                    iconRes = R.drawable.ic_connect_bank,
                                    title = "Connect a bank account",
                                    onClick = { presentBankPicker() },
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    enabled = !viewModel.isCreating && !viewModel.isLoadingMethods,
                                )
                            }
                            else -> {
                                MethodOptionRow(
                                    iconRes = R.drawable.ic_connect_bank,
                                    title = "Choose a bank account",
                                    onClick = { presentBankPicker() },
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    enabled = !viewModel.isCreating && !viewModel.isLoadingMethods,
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = spacing.sectionGap),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sectionGap),
                ) {
                    FramePayoutViewModel.quickAmounts.forEach { cents ->
                        QuickAmountChip(
                            cents = cents,
                            selected = viewModel.typedCents == cents,
                            enabled = !viewModel.isCreating && viewModel.isQuickAmountAvailable(cents),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.applyQuickAmount(cents) },
                        )
                    }
                }
                FrameAmountKeypad(
                    cents = viewModel.typedCents,
                    onCentsChange = { cents ->
                        if (!viewModel.isCreating) viewModel.replaceTypedCents(cents)
                    },
                    maxAmount = viewModel.availableAmount,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                ContinueButton(
                    text = "Payout ${viewModel.amountText}",
                    enabled = viewModel.canConfirm,
                    isLoading = viewModel.isCreating,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    onClick = { submit() },
                )
            }
        }
    }
}

@Composable
private fun payoutViewModel(accountId: String, amountCents: Int): FramePayoutViewModel {
    val owner = LocalViewModelStoreOwner.current
    val factory = remember(accountId, amountCents) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FramePayoutViewModel(availableAmount = amountCents) as T
            }
        }
    }
    return if (owner != null) {
        viewModel(
            key = "frame-payout:$accountId:$amountCents",
            factory = factory,
            viewModelStoreOwner = owner,
        )
    } else {
        remember(accountId, amountCents) { FramePayoutViewModel(availableAmount = amountCents) }
    }
}

@Composable
private fun QuickAmountChip(
    cents: Int,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val theme = LocalFrameTheme.current
    val shape = RoundedCornerShape(theme.radii.medium)
    val fill = when {
        !enabled -> theme.colors.disabledButton
        selected -> theme.colors.primaryButton
        else -> theme.colors.surface
    }
    val text = when {
        !enabled -> theme.colors.disabledButtonText
        selected -> theme.colors.primaryButtonText
        else -> theme.colors.textPrimary
    }
    val stroke = when {
        !enabled -> theme.colors.disabledButtonStroke
        selected -> theme.colors.primaryButton
        else -> theme.colors.surfaceStroke
    }
    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(fill)
            .border(1.dp, stroke, shape)
            .semantics { contentDescription = "Payout $${cents / 100}" }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$${cents / 100}",
            style = theme.fonts.button,
            color = text,
        )
    }
}

@FrameThemePreviews
@Composable
private fun FramePayoutViewPreview() {
    FrameTheme {
        FramePayoutView(
            accountId = "",
            clientSecret = FramePayoutClientSecret(
                clientSecret = "pyt_sess_preview",
                expiresAt = System.currentTimeMillis() / 1000 + 1_800,
                amountCents = 5_000,
            ),
            onResult = {},
        )
    }
}
