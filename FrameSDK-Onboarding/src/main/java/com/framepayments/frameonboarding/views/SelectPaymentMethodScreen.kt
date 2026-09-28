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
internal fun SelectPaymentMethodScreen(
    savedMethods: List<PaymentMethodSummary>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onAddCard: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val canContinue = selectedId != null

    LaunchedEffect(Unit) {
        AccountEventEmitter.emit(
            AccountEventName.PAYMENT_METHOD_STEP_STARTED,
            AccountEventScreen.PAYMENT_METHOD
        )
    }

    Scaffold(
        containerColor = LocalFrameTheme.current.colors.surface,
        topBar = {
            TopAppBar(
                title = { Text("Select A Payment Method") },
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Choose a saved payment method or add a new one to continue",
                    style = LocalFrameTheme.current.fonts.bodySmall
                )

                Spacer(Modifier.height(20.dp))

                if (savedMethods.isNotEmpty()) {
                    Text("Saved Payment Methods", style = LocalFrameTheme.current.fonts.label)
                    Spacer(Modifier.height(8.dp))
                    savedMethods.forEach { pm ->
                        SavedPaymentMethodRow(
                            pm = pm,
                            selected = selectedId == pm.id,
                            onClick = {
                                onSelect(pm.id)
                                AccountEventEmitter.emit(
                                    AccountEventName.SAVED_PAYMENT_METHOD_SELECTED,
                                    AccountEventScreen.PAYMENT_METHOD
                                )
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Text("Add a payment method", style = LocalFrameTheme.current.fonts.label)
                Spacer(Modifier.height(8.dp))

                MethodOptionRow(
                    iconRes = R.drawable.ic_add_card,
                    title = "Add a card",
                    onClick = {
                        AccountEventEmitter.emit(
                            AccountEventName.ADD_PAYMENT_METHOD_STARTED,
                            AccountEventScreen.PAYMENT_METHOD
                        )
                        onAddCard()
                    }
                )
            }

            ContinueButton(
                enabled = canContinue,
                onClick = onContinue
            )
        }
    }
}

@Composable
private fun SavedPaymentMethodRow(
    pm: PaymentMethodSummary,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(LocalFrameTheme.current.radii.medium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = cardBrandIcon(pm.brand)),
                contentDescription = pm.brand,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(text = "${pm.brand}  •••• ${pm.last4}", style = LocalFrameTheme.current.fonts.body)
                Spacer(Modifier.height(2.dp))
                Text(text = "Exp. ${pm.exp}", style = LocalFrameTheme.current.fonts.bodySmall)
            }
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

@FrameThemePreviews
@Composable
private fun SelectPaymentMethodScreenEmptyPreview() {
    FrameTheme {
    SelectPaymentMethodScreen(
        savedMethods = emptyList(),
        selectedId = null,
        onSelect = {},
        onAddCard = {},
        onBack = {},
        onContinue = {}
    )
    }
}

@FrameThemePreviews
@Composable
private fun SelectPaymentMethodScreenWithMethodsPreview() {
    FrameTheme {
    SelectPaymentMethodScreen(
        savedMethods = listOf(
            PaymentMethodSummary(id = "pm_1", brand = "visa", last4 = "4242", exp = "12/26"),
            PaymentMethodSummary(id = "pm_2", brand = "mastercard", last4 = "5555", exp = "08/25")
        ),
        selectedId = "pm_1",
        onSelect = {},
        onAddCard = {},
        onBack = {},
        onContinue = {}
    )
    }
}
