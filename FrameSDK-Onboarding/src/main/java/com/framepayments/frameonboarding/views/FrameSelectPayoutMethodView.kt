package com.framepayments.frameonboarding.views

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameResult
import com.framepayments.framesdk_ui.reusable.refreshesSonarSession
import com.framepayments.framesdk_ui.theme.FrameTheme

/**
 * A standalone "choose a payout account" screen that can be presented outside the onboarding flow.
 *
 * Lists the account's saved ACH payout methods and lets the applicant add a new one.
 *
 * Continue elects the chosen method as the account's primary payout destination, as does adding
 * a new bank; [FrameResult.Completed] is reported only after that.
 *
 * @param accountId The Frame account ID whose payout method is being selected.
 * @param clientSecret The onboarding-session token (`onb_sess_…`) minted by your server
 *   (`POST /v1/onboarding_sessions`) and handed to your app. While this screen is presented every
 *   request authenticates with this token, scoping it to a single account. Pass null only for
 *   legacy integrations that still authenticate with a secret key.
 * @param onResult Called with a [FrameResult] when the screen finishes or is cancelled. On
 *   [FrameResult.Completed] the id is the elected payout method.
 */
@Composable
fun FrameSelectPayoutMethodView(
    accountId: String,
    clientSecret: String? = null,
    onResult: (FrameResult) -> Unit = {}
) {
    // Keyed on accountId/clientSecret: a keyless remember would keep the first VM instance (and
    // its captured OnboardingConfig) across a recomposition that passes a different account,
    // silently continuing to operate on the old one.
    val viewModel = remember(accountId, clientSecret) {
        FrameOnboardingViewModel(OnboardingConfig(accountId = accountId, clientSecret = clientSecret))
    }
    DisposableEffect(viewModel) { onDispose { viewModel.close() } }
    val snackbarHostState = remember { SnackbarHostState() }
    val onboardingData by viewModel.onboardingData.collectAsState()
    val savedPayoutMethods by viewModel.savedPayoutMethods.collectAsState()
    val userError by viewModel.userErrorMessage.collectAsState()
    val primaryPayoutMethodId by viewModel.primaryPayoutMethodId.collectAsState()
    val isPerformingAction by viewModel.isPerformingAction.collectAsState()
    // Guards against emitting Cancelled on dismiss once a selection has succeeded.
    var didFinish by remember { mutableStateOf(false) }
    var showAddPayout by remember { mutableStateOf(false) }
    // The selection when Add opened; only a different id afterwards means a bank was added.
    var selectedIdWhenAddOpened by remember { mutableStateOf<String?>(null) }

    DisposableEffect(viewModel, clientSecret) {
        clientSecret?.let { FrameNetworking.beginOnboardingSession(it) }
        // Seeds saved payout methods; onboarding gets this from its container.
        viewModel.launchCheckExistingAccount(updateCapabilities = false)
        viewModel.loadSavedPaymentMethods()
        onDispose {
            clientSecret?.let { FrameNetworking.endOnboardingSession(it) }
        }
    }

    LaunchedEffect(userError) {
        val msg = userError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearUserErrorMessage()
    }

    LaunchedEffect(showAddPayout, onboardingData.selectedPayoutMethodId) {
        val id = onboardingData.selectedPayoutMethodId
        if (showAddPayout && id != null && id != selectedIdWhenAddOpened && !didFinish) {
            didFinish = true
            onResult(FrameResult.Completed(id))
        }
    }

    fun finish(result: FrameResult) {
        if (didFinish) return
        didFinish = true
        onResult(result)
    }

    BackHandler { finish(FrameResult.Cancelled) }

    FrameTheme {
        Scaffold(
            modifier = Modifier.refreshesSonarSession(accountId = accountId),
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            if (showAddPayout) {
                AddPayoutMethodScreen(
                    viewModel = viewModel,
                    onBack = { showAddPayout = false }
                )
            } else {
                SelectPayoutMethodScreen(
                    savedMethods = savedPayoutMethods,
                    selectedId = onboardingData.selectedPayoutMethodId,
                    primaryId = primaryPayoutMethodId,
                    isLoading = isPerformingAction,
                    // Selecting only selects — the flow completes on Continue, so the applicant
                    // can change their mind before committing.
                    onSelect = { id -> viewModel.onPayoutMethodSelected(id) },
                    onAddPayout = {
                        selectedIdWhenAddOpened = onboardingData.selectedPayoutMethodId
                        showAddPayout = true
                    },
                    onBack = { finish(FrameResult.Cancelled) },
                    onContinue = {
                        viewModel.electSelectedPayoutMethod { id -> finish(FrameResult.Completed(id)) }
                    }
                )
            }
        }
    }
}
