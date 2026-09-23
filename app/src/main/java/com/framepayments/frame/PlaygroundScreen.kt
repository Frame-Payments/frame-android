package com.framepayments.frame

import android.app.Application
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingResult
import com.framepayments.frameonboarding.views.FrameAddPaymentMethodView
import com.framepayments.frameonboarding.views.FrameAddPayoutMethodView
import com.framepayments.frameonboarding.views.FrameSelectPayoutMethodView
import com.framepayments.frameonboarding.views.OnboardingContainerView
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.framesdk.FrameResult
import com.framepayments.framesdk_ui.buttons.FrameGooglePayButton
import com.plaid.link.FastOpenPlaidLink
import com.plaid.link.Plaid
import com.plaid.link.PlaidHandler
import com.plaid.link.configuration.LinkTokenConfiguration
import com.plaid.link.result.LinkExit
import com.plaid.link.result.LinkSuccess

/** Which standalone entry-point view to launch, acting on [ContentViewModel.accountId]. */
private enum class StandaloneView {
    ADD_PAYMENT_METHOD, ADD_PAYOUT_METHOD, SELECT_PAYOUT_METHOD
}

private data class DemoResultMessage(val title: String, val body: String)

/** Renders a [FrameResult] as a title/body pair for [DemoResultMessage], matching the FrameExample-iOS pattern. */
private fun FrameResult.toDemoMessage(title: String): DemoResultMessage = when (this) {
    is FrameResult.Completed -> DemoResultMessage(title, "Completed: $id")
    is FrameResult.Cancelled -> DemoResultMessage(title, "Cancelled")
    is FrameResult.Failed -> DemoResultMessage(title, "Failed: ${error.message}")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaygroundScreen(
    viewModel: ContentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val onboardingMintState by viewModel.onboardingMintState.collectAsState()
    val accountId by viewModel.accountId.collectAsState()
    val plaidService by viewModel.plaidService.collectAsState()
    val plaidMessage by viewModel.plaidMessage.collectAsState()
    val plaidToken by remember(plaidService) {
        plaidService?.linkToken ?: kotlinx.coroutines.flow.MutableStateFlow(null)
    }.collectAsState()
    val isConnectingPlaid by remember(plaidService) {
        plaidService?.isConnecting ?: kotlinx.coroutines.flow.MutableStateFlow(false)
    }.collectAsState()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    var showOnboarding by remember { mutableStateOf(false) }
    var showCustomers by remember { mutableStateOf(false) }
    var showPaymentMethods by remember { mutableStateOf(false) }
    var showSubscriptions by remember { mutableStateOf(false) }
    var showChargeIntents by remember { mutableStateOf(false) }
    var showRefunds by remember { mutableStateOf(false) }
    var showSubscriptionPhases by remember { mutableStateOf(false) }
    // Which standalone entry-point demo to launch, acting on viewModel.accountId.
    var pendingStandaloneView by remember { mutableStateOf<StandaloneView?>(null) }
    var demoResultMessage by remember { mutableStateOf<DemoResultMessage?>(null) }

    val plaidLauncher = rememberLauncherForActivityResult(FastOpenPlaidLink()) { result ->
        when (result) {
            is LinkSuccess -> {
                val plaidAccount = result.metadata.accounts.firstOrNull()
                viewModel.handlePlaidSuccess(
                    publicToken = result.publicToken,
                    plaidAccountId = plaidAccount?.id ?: "",
                    institutionName = result.metadata.institution?.name,
                    subtype = plaidAccount?.subtype?.json
                )
            }
            is LinkExit -> {
                result.error?.let { err ->
                    android.util.Log.w(
                        "Plaid",
                        "Exit error: code=${err.errorCode}, message='${err.displayMessage}'"
                    )
                }
                viewModel.onPlaidDismissed()
            }
            else -> viewModel.onPlaidDismissed()
        }
    }

    LaunchedEffect(plaidToken) {
        plaidToken?.let { token ->
            plaidService?.clearLinkToken()
            val configuration = LinkTokenConfiguration.Builder()
                .token(token)
                .build()
            val handler: PlaidHandler = Plaid.create(application, configuration)
            plaidLauncher.launch(handler)
        }
    }

    if (showOnboarding) {
        val dismissOnboarding = {
            showOnboarding = false
            // Clear the minted token so the next launch mints a fresh one.
            viewModel.clearOnboardingClientSecret()
        }
        val onboardingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = dismissOnboarding,
            sheetState = onboardingSheetState
        ) {
            // Fills the sheet so the multi-step flow gets the height it expects; without it the
            // sheet wraps its content and each step resizes the sheet as the user advances.
            Box(modifier = Modifier.fillMaxSize()) {
                // Wait for the onboarding-session token to be minted before launching the flow.
                // Rendering OnboardingContainerView with a null clientSecret would start onboarding
                // requests (e.g. the ToS token) before beginOnboardingSession runs, leaving those
                // early requests unscoped to the account. Gating on the minted token guarantees the
                // session is active before the first call.
                when (val mintState = onboardingMintState) {
                    is OnboardingMintState.Loading, OnboardingMintState.Idle -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is OnboardingMintState.Error -> {
                        // Minting failed — show why and let the user retry or back out, instead of
                        // spinning forever on the gate above.
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Couldn't start onboarding",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = mintState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = { viewModel.mintOnboardingClientSecret(accountId) }) {
                                    Text("Retry")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = dismissOnboarding) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                    is OnboardingMintState.Ready -> {
                        // Demo: show how a host app overrides the SDK theme. The override is shared
                        // with CartTestActivity / CheckoutActivity so onboarding, cart, and checkout
                        // all render with the same custom branding while running the playground.
                        val customTheme = rememberDemoTheme()
                        OnboardingContainerView(
                            config = OnboardingConfig(
                                // The clientSecret is scoped to the account it was minted for (see
                                // mintOnboardingClientSecret) — accountId must be passed too, or onboarding
                                // creates a brand-new account the session was never scoped to, and every
                                // request after that gets PII-gated (profile withheld, prefill silently fails).
                                accountId = mintState.accountId,
                                // The onb_sess_… token minted from the configured sk_ (demo/testing only). In
                                // production your backend mints this (POST /v1/onboarding_sessions) and passes
                                // it in as the clientSecret, scoping every onboarding request to one account.
                                clientSecret = mintState.clientSecret,
                                requiredCapabilities = listOf(
                                    Capabilities.KYC_PREFILL,
                                    Capabilities.AGE_VERIFICATION,
                                    Capabilities.PHONE_VERIFICATION
                                ),
                                theme = customTheme
                            ),
                            onResult = { result ->
                                dismissOnboarding()
                                when (result) {
                                    is OnboardingResult.Completed -> {
                                        // Matches FrameExample-iOS's onResult handler: the account onboarding
                                        // just resolved becomes the account every other demo acts on next.
                                        result.accountId?.let(viewModel::setAccountId)
                                        demoResultMessage = DemoResultMessage("Onboarding", "Completed: ${result.paymentMethodId}")
                                    }
                                    is OnboardingResult.FinishedUnverified -> {
                                        // The flow ran to the end but the applicant isn't verified — still worth
                                        // surfacing rather than treating it the same as a full success. The
                                        // account still exists and is still what follow-up demos should use.
                                        result.accountId?.let(viewModel::setAccountId)
                                        demoResultMessage = DemoResultMessage("Onboarding", "Finished unverified: ${result.outcome}")
                                    }
                                    is OnboardingResult.Cancelled -> Unit
                                    is OnboardingResult.Failed ->
                                        demoResultMessage = DemoResultMessage("Onboarding", "Failed: ${result.message}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    val standaloneView = pendingStandaloneView
    if (standaloneView != null) {
        val standaloneSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { pendingStandaloneView = null },
            sheetState = standaloneSheetState
        ) {
            // Fills the sheet so these flows get the height they expect; without it the sheet
            // wraps its content and resizes as the user advances.
            Box(modifier = Modifier.fillMaxSize()) {
                // Matches FrameExample-iOS: these views act on viewModel.accountId directly, with no
                // separate session mint — FrameAddPaymentMethodView/etc. bind their own session
                // internally, and a null clientSecret is the documented default for a standalone launch.
                if (accountId.isBlank()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No account set",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Onboard an applicant first, or pass an accountId to initializeWithAPIKey.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            TextButton(onClick = { pendingStandaloneView = null }) {
                                Text("Cancel")
                            }
                        }
                    }
                } else {
                    val finish = { message: DemoResultMessage ->
                        pendingStandaloneView = null
                        demoResultMessage = message
                    }
                    when (standaloneView) {
                        StandaloneView.ADD_PAYMENT_METHOD -> FrameAddPaymentMethodView(
                            accountId = accountId,
                            onResult = { finish(it.toDemoMessage("Add Payment Method")) }
                        )
                        StandaloneView.ADD_PAYOUT_METHOD -> FrameAddPayoutMethodView(
                            accountId = accountId,
                            onResult = { finish(it.toDemoMessage("Add Payout Method")) }
                        )
                        StandaloneView.SELECT_PAYOUT_METHOD -> FrameSelectPayoutMethodView(
                            accountId = accountId,
                            onResult = { finish(it.toDemoMessage("Select Payout Method")) }
                        )
                    }
                }
            }
        }
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

            if (accountId.isNotBlank()) {
                // Matches FrameExample-iOS's Apple Pay button placement: the wallet button leads,
                // ahead of the rest of the demo actions. Visibility of the button itself is gated
                // by Google Pay device/config readiness. Keyed on accountId so configure() (which
                // re-checks readiness over the network) only re-runs when the account actually
                // changes, not on every unrelated recomposition of this screen.
                key(accountId) {
                    AndroidView(
                        factory = { ctx ->
                            FrameGooglePayButton(ctx).apply {
                                configure(
                                    amountCents = 35000,
                                    owner = FrameGooglePayButton.Owner.Account(accountId),
                                    onResult = { result ->
                                        when (result) {
                                            is FrameGooglePayButton.Result.Success ->
                                                demoResultMessage = DemoResultMessage("Google Pay", "Completed: ${result.id}")
                                            is FrameGooglePayButton.Result.Failure ->
                                                demoResultMessage = DemoResultMessage("Google Pay", "Failed: ${result.message}")
                                            is FrameGooglePayButton.Result.Cancelled -> Unit
                                            is FrameGooglePayButton.Result.PaymentMethodCreated -> Unit
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            PlaygroundButton(text = "Show Onboarding Flow") {
                // Demo/testing only: mint an onboarding-session token (onb_sess_…) from the
                // configured sk_ before launching. In production your backend mints this token and
                // hands it to the app as the clientSecret — see ContentViewModel. Onboards the
                // account passed to initializeWithAPIKey when one was configured there; otherwise
                // creates a new applicant, matching FrameExample-iOS — never a random pre-existing
                // account.
                viewModel.mintOnboardingClientSecret(accountId)
                showOnboarding = true
            }
            PlaygroundButton(
                text = if (isConnectingPlaid) "Connecting to Plaid…" else "Test Plaid Link",
                enabled = !isConnectingPlaid,
                onClick = { viewModel.startPlaidLink() }
            )
            PlaygroundButton(text = "Checkout") {
                val intent = Intent(context, CartTestActivity::class.java).apply {
                    if (accountId.isNotBlank()) putExtra("accountId", accountId)
                }
                context.startActivity(intent)
            }
            PlaygroundButton(text = "Add Payment Method (standalone)") {
                pendingStandaloneView = StandaloneView.ADD_PAYMENT_METHOD
            }
            PlaygroundButton(text = "Add Payout Method (standalone)") {
                pendingStandaloneView = StandaloneView.ADD_PAYOUT_METHOD
            }
            PlaygroundButton(text = "Select Payout Method (standalone)") {
                pendingStandaloneView = StandaloneView.SELECT_PAYOUT_METHOD
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

    plaidMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearPlaidMessage() },
            title = { Text(message.title) },
            text = { Text(message.body) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearPlaidMessage() }) { Text("OK") }
            }
        )
    }

    demoResultMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { demoResultMessage = null },
            title = { Text(message.title) },
            text = { Text(message.body) },
            confirmButton = {
                TextButton(onClick = { demoResultMessage = null }) { Text("OK") }
            }
        )
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
