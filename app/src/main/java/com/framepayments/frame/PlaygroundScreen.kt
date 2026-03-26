package com.framepayments.frame

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.views.OnboardingContainerView
import com.framepayments.frameonboarding.classes.OnboardingConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaygroundScreen(
    viewModel: ContentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showOnboarding by remember { mutableStateOf(false) }
    var showCustomers by remember { mutableStateOf(false) }
    var showPaymentMethods by remember { mutableStateOf(false) }
    var showSubscriptions by remember { mutableStateOf(false) }
    var showChargeIntents by remember { mutableStateOf(false) }
    var showRefunds by remember { mutableStateOf(false) }
    var showSubscriptionPhases by remember { mutableStateOf(false) }

    if (showOnboarding) {
        Box(modifier = Modifier.fillMaxSize()) {
            OnboardingContainerView(
                config = OnboardingConfig(
                    requiredCapabilities = listOf(
                        Capabilities.KYC_PREFILL,
                        Capabilities.CARD_VERIFICATION,
                        Capabilities.BANK_ACCOUNT_VERIFICATION,
                        Capabilities.AGE_VERIFICATION,
                        Capabilities.GEO_COMPLIANCE,
                        Capabilities.PHONE_VERIFICATION
                    )
                ),
                onResult = { showOnboarding = false }
            )
        }
        return
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        // Do not use fillMaxSize() with verticalScroll() on the same node — it can measure
        // unbounded height (Int.MAX_VALUE) and crash Scaffold layout.
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Frame Payments\nSDK Playground",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap a button below to view your Frame data after you have entered your API key!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            PlaygroundButton(text = "Show Onboarding Flow") { showOnboarding = true }
            PlaygroundButton(text = "Checkout") {
                val intent = Intent(context, CartTestActivity::class.java)
                context.startActivity(intent)
            }
            PlaygroundButton(
                text = "View All Customers",
                enabled = uiState.customers.isNotEmpty(),
                onClick = { showCustomers = true }
            )
            PlaygroundButton(
                text = "View All Payment Methods",
                enabled = uiState.paymentMethods.isNotEmpty(),
                onClick = { showPaymentMethods = true }
            )
            PlaygroundButton(
                text = "View All Subscriptions",
                enabled = uiState.subscriptions.isNotEmpty(),
                onClick = { showSubscriptions = true }
            )
            PlaygroundButton(
                text = "View All Charge Intents",
                enabled = uiState.chargeIntents.isNotEmpty(),
                onClick = { showChargeIntents = true }
            )
            PlaygroundButton(
                text = "View All Refunds",
                enabled = uiState.refunds.isNotEmpty(),
                onClick = { showRefunds = true }
            )
            PlaygroundButton(
                text = "View All Subscription Phases",
                enabled = uiState.subscriptionPhases.isNotEmpty(),
                onClick = { showSubscriptionPhases = true }
            )
        }
    }

    if (showCustomers) {
        ListBottomSheet(
            title = "Customers",
            onDismiss = { showCustomers = false },
            items = uiState.customers.map { c ->
                "Name: ${c.name}\nEmail: ${c.email ?: ""}\nPhone: ${c.phone ?: "Not Found"}"
            }
        )
    }
    if (showPaymentMethods) {
        ListBottomSheet(
            title = "Payment Methods",
            onDismiss = { showPaymentMethods = false },
            items = uiState.paymentMethods.map { pm ->
                "Payment Method ID: ${pm.id}\nCustomer ID: ${pm.customerId ?: ""}"
            }
        )
    }
    if (showSubscriptions) {
        ListBottomSheet(
            title = "Subscriptions",
            onDismiss = { showSubscriptions = false },
            items = uiState.subscriptions.map { s ->
                "Subscription ID: ${s.id}\nCustomer ID: ${s.customer ?: ""}"
            }
        )
    }
    if (showChargeIntents) {
        ListBottomSheet(
            title = "Charge Intents",
            onDismiss = { showChargeIntents = false },
            items = uiState.chargeIntents.map { ci ->
                "Charge Intent ID: ${ci.id}\nCustomer ID: ${ci.customer?.id ?: ""}\nPayment Method Id: ${ci.paymentMethod?.id ?: ""}"
            }
        )
    }
    if (showRefunds) {
        ListBottomSheet(
            title = "Refunds",
            onDismiss = { showRefunds = false },
            items = uiState.refunds.map { r ->
                "Refund ID: ${r.id}\nCharge Intent ID: ${r.chargeIntent ?: ""}"
            }
        )
    }
    if (showSubscriptionPhases) {
        ListBottomSheet(
            title = "Subscription Phases",
            onDismiss = { showSubscriptionPhases = false },
            items = uiState.subscriptionPhases.map { sp ->
                "Subscription Phase ID: ${sp.id}\nPricing Type: ${sp.pricingType ?: ""}"
            }
        )
    }
}

@Composable
private fun PlaygroundButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = ButtonDefaults.buttonColors().containerColor.copy(alpha = 0.3f),
            disabledContentColor = ButtonDefaults.buttonColors().contentColor.copy(alpha = 0.7f)
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.titleSmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    items: List<String>
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            items.forEach { text ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
