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
import com.framepayments.frameonboarding.reusable.ContinueButton
import com.framepayments.frameonboarding.reusable.cardBrandIcon
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectPayoutMethodScreen(
    savedMethods: List<PaymentMethodSummary>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onAddPayout: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val canContinue = selectedId != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select A Payout Method") },
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
                            onClick = { onSelect(pm.id) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Text("Add Payout Method", style = LocalFrameTheme.current.fonts.label)
                Spacer(Modifier.height(8.dp))

                AddPayoutMethodRow(
                    title = "Bank Account (ACH)",
                    subtitle = "Add Bank Account",
                    onClick = onAddPayout
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
private fun SavedPayoutMethodRow(
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
                Text(text = "Account", style = LocalFrameTheme.current.fonts.bodySmall)
            }
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

@Composable
private fun AddPayoutMethodRow(
    title: String,
    subtitle: String,
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
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = LocalFrameTheme.current.colors.primaryButton,
                shape = RoundedCornerShape(LocalFrameTheme.current.radii.small)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = LocalFrameTheme.current.colors.primaryButtonText
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = title, style = LocalFrameTheme.current.fonts.body)
                Text(
                    text = subtitle,
                    style = LocalFrameTheme.current.fonts.bodySmall,
                    color = LocalFrameTheme.current.colors.textPrimary.copy(alpha = 0.6f)
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
private fun SelectPayoutMethodScreenEmptyPreview() {
    SelectPayoutMethodScreen(
        savedMethods = emptyList(),
        selectedId = null,
        onSelect = {},
        onAddPayout = {},
        onBack = {},
        onContinue = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectPayoutMethodScreenWithMethodsPreview() {
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
