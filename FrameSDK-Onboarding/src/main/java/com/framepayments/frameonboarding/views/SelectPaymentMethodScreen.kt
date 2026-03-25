package com.framepayments.frameonboarding.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(20.dp))

                if (savedMethods.isNotEmpty()) {
                    Text("Saved Payment Methods", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    savedMethods.forEach { pm ->
                        SavedPaymentMethodRow(
                            pm = pm,
                            selected = selectedId == pm.id,
                            onClick = { onSelect(pm.id) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Text("Add Payment Method", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))

                AddPaymentMethodRow(
                    title = "Debit/Credit Card",
                    subtitle = "Add New Payment Method",
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
                Text(text = "${pm.brand}  •••• ${pm.last4}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(2.dp))
                Text(text = "Exp. ${pm.exp}", style = MaterialTheme.typography.bodySmall)
            }
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

@Composable
private fun AddPaymentMethodRow(
    title: String,
    subtitle: String,
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
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = FramePrimaryColor,
                shape = MaterialTheme.shapes.small
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = FrameOnPrimaryColor
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectPaymentMethodScreenEmptyPreview() {
    SelectPaymentMethodScreen(
        savedMethods = emptyList(),
        selectedId = null,
        onSelect = {},
        onAddCard = {},
        onBack = {},
        onContinue = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectPaymentMethodScreenWithMethodsPreview() {
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
