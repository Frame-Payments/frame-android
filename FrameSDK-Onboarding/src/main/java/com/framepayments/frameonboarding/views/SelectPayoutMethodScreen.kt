package com.framepayments.frameonboarding.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.R
import com.framepayments.frameonboarding.classes.PaymentMethodSummary
import com.framepayments.frameonboarding.reusable.MethodOptionRow
import com.framepayments.framesdk.accountevents.AccountEventDetail
import com.framepayments.framesdk.accountevents.AccountEventEmitter
import com.framepayments.framesdk.accountevents.AccountEventName
import com.framepayments.framesdk.accountevents.AccountEventScreen
import com.framepayments.framesdk_ui.reusable.ContinueButton
import com.framepayments.framesdk_ui.reusable.cardBrandIcon
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import com.framepayments.framesdk_ui.theme.FrameTheme
import com.framepayments.framesdk_ui.theme.FrameThemePreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectPayoutMethodScreen(
    savedMethods: List<PaymentMethodSummary>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onAddPayout: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    primaryId: String? = null,
    isLoading: Boolean = false
) {
    val canContinue = selectedId != null

    LaunchedEffect(Unit) {
        AccountEventEmitter.emit(
            AccountEventName.PAYOUT_METHOD_STEP_STARTED,
            AccountEventScreen.PAYOUT_METHOD
        )
    }

    Scaffold(
        containerColor = LocalFrameTheme.current.colors.surface,
        topBar = {
            TopAppBar(
                title = { Text("Select A Payout Method") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isLoading) {
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Choose a saved payout method or add a new one to continue",
                    style = LocalFrameTheme.current.fonts.bodySmall
                )

                Spacer(Modifier.height(20.dp))

                if (savedMethods.isNotEmpty()) {
                    Text("Saved Payout Methods", style = LocalFrameTheme.current.fonts.label)
                    Spacer(Modifier.height(8.dp))
                    savedMethods.forEach { pm ->
                        SavedPayoutMethodRow(
                            pm = pm,
                            selected = selectedId == pm.id,
                            isPrimary = primaryId == pm.id,
                            enabled = !isLoading,
                            onClick = {
                                onSelect(pm.id)
                                AccountEventEmitter.emit(
                                    AccountEventName.SAVED_PAYOUT_METHOD_SELECTED,
                                    AccountEventScreen.PAYOUT_METHOD
                                )
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Text("Payout methods", style = LocalFrameTheme.current.fonts.label)
                Spacer(Modifier.height(8.dp))

                MethodOptionRow(
                    iconRes = R.drawable.ic_connect_bank,
                    title = "Connect a bank account",
                    onClick = {
                        AccountEventEmitter.emit(
                            AccountEventName.ADD_PAYOUT_METHOD_STARTED,
                            AccountEventScreen.PAYOUT_METHOD,
                            detail = AccountEventDetail.PAYOUT_METHOD_ADD_STARTED_MANUAL_OR_PLAID
                        )
                        onAddPayout()
                    },
                    enabled = !isLoading
                )
            }

            ContinueButton(
                enabled = canContinue,
                isLoading = isLoading,
                onClick = onContinue
            )
        }
    }
}

@Composable
private fun SavedPayoutMethodRow(
    pm: PaymentMethodSummary,
    selected: Boolean,
    isPrimary: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(LocalFrameTheme.current.radii.medium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = cardBrandIcon(pm.brand)),
                contentDescription = pm.brand,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(text = "•••• ${pm.last4}", style = LocalFrameTheme.current.fonts.body)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (isPrimary) "Account · Primary" else "Account",
                    style = LocalFrameTheme.current.fonts.bodySmall
                )
            }
            RadioButton(selected = selected, onClick = onClick, enabled = enabled)
        }
    }
}

@FrameThemePreviews
@Composable
private fun SelectPayoutMethodScreenEmptyPreview() {
    FrameTheme {
    SelectPayoutMethodScreen(
        savedMethods = emptyList(),
        selectedId = null,
        onSelect = {},
        onAddPayout = {},
        onBack = {},
        onContinue = {}
    )
    }
}

@FrameThemePreviews
@Composable
private fun SelectPayoutMethodScreenWithMethodsPreview() {
    FrameTheme {
    SelectPayoutMethodScreen(
        savedMethods = listOf(
            PaymentMethodSummary(id = "ba_1", brand = "bank", last4 = "6789", exp = ""),
        ),
        selectedId = "ba_1",
        onSelect = {},
        onAddPayout = {},
        onBack = {},
        onContinue = {}
    )
    }
}
