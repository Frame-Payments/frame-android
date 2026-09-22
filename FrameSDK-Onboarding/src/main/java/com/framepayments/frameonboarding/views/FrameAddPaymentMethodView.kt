package com.framepayments.frameonboarding.views

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
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameResult
import com.framepayments.framesdk_ui.theme.FrameTheme

/**
 * A standalone "add a payment method" screen that can be presented outside the onboarding flow.
 *
 * Use this when a merchant wants to prompt an existing user to add a card at an arbitrary point
 * in their app, rather than as a step inside [com.framepayments.frameonboarding.views.OnboardingContainerView].
 * The screen presents the same card, billing-address, and Google Pay inputs used during onboarding.
 *
 * @param accountId The Frame account ID the new payment method is attached to.
 * @param clientSecret The onboarding-session token (`onb_sess_…`) minted by your server
 *   (`POST /v1/onboarding_sessions`) and handed to your app. While this screen is presented every
 *   request authenticates with this token, scoping it to a single account. Pass null only for
 *   legacy integrations that still authenticate with a secret key.
 * @param onResult Called with a [FrameResult] when the screen finishes or is cancelled.
 */
@Composable
fun FrameAddPaymentMethodView(
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
    val snackbarHostState = remember { SnackbarHostState() }
    val onboardingData by viewModel.onboardingData.collectAsState()
    val userError by viewModel.userErrorMessage.collectAsState()
    // Guards against emitting Cancelled on dismiss when a method was already added.
    var didFinish by remember { mutableStateOf(false) }

    DisposableEffect(clientSecret) {
        clientSecret?.let { FrameNetworking.beginOnboardingSession(it) }
        onDispose {
            clientSecret?.let { FrameNetworking.endOnboardingSession(it) }
        }
    }

    LaunchedEffect(onboardingData.selectedPaymentMethodId) {
        val id = onboardingData.selectedPaymentMethodId
        if (id != null && !didFinish) {
            didFinish = true
            onResult(FrameResult.Completed(id))
        }
    }

    LaunchedEffect(userError) {
        val msg = userError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearUserErrorMessage()
    }

    FrameTheme {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            AddPaymentMethodScreen(
                viewModel = viewModel,
                onBack = {
                    if (!didFinish) {
                        didFinish = true
                        onResult(FrameResult.Cancelled)
                    }
                }
            )
        }
    }
}
