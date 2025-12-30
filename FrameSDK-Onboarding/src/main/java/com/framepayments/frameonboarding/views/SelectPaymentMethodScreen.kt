package com.framepayments.frameonboarding.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.R
import com.framepayments.frameonboarding.classes.PaymentMethodSummary
import com.framepayments.frameonboarding.reusable.cardBrandIcon
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select a Payment Method") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
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
                    text = "Choose a saved payment method or add a new one to continue.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(20.dp))

                Text("Saved Payment Methods", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))

                savedMethods.forEach { pm ->
                    SavedPaymentMethodRow(
                        pm = pm,
                        selected = pm.id == selectedId,
                        onClick = { onSelect(pm.id) }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                Spacer(Modifier.height(18.dp))

                Text("Add Payment Method", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))

                AddPaymentMethodRow(
                    title = "Debit/Credit Card",
                    onClick = onAddCard
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canContinue,
                onClick = onContinue,
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
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(
                    id = cardBrandIcon(pm.brand)
                ),
                contentDescription = pm.brand,
                modifier = Modifier
                    .size(40.dp)
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column(Modifier.weight(1f)) {
                Text(text = "${pm.brand}  •••• ${pm.last4}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(2.dp))
                Text(text = "Exp. ${pm.exp}", style = MaterialTheme.typography.bodySmall)
            }

            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun AddPaymentMethodRow(
    title: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "＋", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.weight(1f))
            Text(text = "›", style = MaterialTheme.typography.titleLarge)
        }
    }
}