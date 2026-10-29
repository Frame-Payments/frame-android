package com.framepayments.framesdk_ui

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk_ui.reusable.ContinueButton
import com.framepayments.framesdk_ui.reusable.FrameAmountKeypad
import com.framepayments.framesdk_ui.reusable.refreshesSonarSession
import com.framepayments.framesdk_ui.snackbar.SnackbarBridge
import com.framepayments.framesdk_ui.theme.FrameTheme
import com.framepayments.framesdk_ui.theme.FrameThemePreviews
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import com.framepayments.framesdk_ui.viewmodels.FrameDepositViewModel

/**
 * Amount entry for a deposit. Continue reports cents to the host.
 *
 * The host mints `chk_sess_` because the session locks the amount, then presents checkout.
 * Back and close call [onCancel] once.
 *
 * @param accountId Account that will receive the deposit.
 * @param minAmount Optional minimum in cents. Continue stays disabled below it.
 * @param maxAmount Optional maximum in cents. The keypad ignores digits that would exceed it.
 * @param isContinuing When true, Continue shows a spinner while the host mints a session.
 * @param onContinue Called with the typed cents.
 * @param onCancel Called once when the user closes the screen or presses back.
 */
@Composable
fun FrameDepositView(
    accountId: String,
    minAmount: Int? = null,
    maxAmount: Int? = null,
    isContinuing: Boolean = false,
    onContinue: (amountCents: Int) -> Unit,
    onCancel: () -> Unit,
) {
    var savedCents by rememberSaveable { mutableIntStateOf(0) }
    val viewModel = remember(minAmount, maxAmount) {
        FrameDepositViewModel(minAmount = minAmount, maxAmount = maxAmount).also {
            it.replaceTypedCents(savedCents)
        }
    }
    SideEffect { savedCents = viewModel.typedCents }
    val snackbarHostState = remember { SnackbarHostState() }
    var didCancel by remember { mutableStateOf(false) }

    fun cancel() {
        if (didCancel) return
        didCancel = true
        onCancel()
    }

    BackHandler(enabled = !didCancel) { cancel() }

    LaunchedEffect(accountId) {
        if (accountId.isNotEmpty()) {
            FrameNetworking.setAccountIdIfUnset(accountId)
        }
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
                        text = "Deposit",
                        style = fonts.heading,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { cancel() }) {
                        Image(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.close),
                            modifier = Modifier
                                .size(30.dp)
                                .padding(horizontal = 4.dp),
                        )
                    }
                }
                HorizontalDivider(color = colors.surfaceStroke)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = CurrencyFormatter.convertCentsToCurrencyString(viewModel.typedCents),
                        style = fonts.depositAmount,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = spacing.sectionGap),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sectionGap),
                ) {
                    FrameDepositViewModel.quickAmounts.forEach { cents ->
                        QuickAmountChip(
                            cents = cents,
                            selected = viewModel.typedCents == cents,
                            enabled = !isContinuing && viewModel.isQuickAmountAvailable(cents),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.applyQuickAmount(cents) },
                        )
                    }
                }
                FrameAmountKeypad(
                    cents = viewModel.typedCents,
                    onCentsChange = { cents ->
                        if (!isContinuing) viewModel.replaceTypedCents(cents)
                    },
                    maxAmount = viewModel.maxAmount,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                ContinueButton(
                    text = "Continue",
                    enabled = viewModel.isAmountInRange,
                    isLoading = isContinuing,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    onClick = { onContinue(viewModel.typedCents) },
                )
            }
        }
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
            .semantics { contentDescription = "Deposit $${cents / 100}" }
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
private fun FrameDepositViewPreview() {
    FrameTheme {
        FrameDepositView(accountId = "", onContinue = {}, onCancel = {})
    }
}
