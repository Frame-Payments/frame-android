package com.framepayments.frameonboarding.views

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.classes.OnboardingResult
import com.framepayments.frameonboarding.classes.OnboardingStep
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk_ui.theme.FrameTheme
import com.framepayments.framesdk_ui.theme.FrameThemePreviews

/**
 * Root composable for the Frame onboarding flow.
 *
 * Instantiates a [FrameOnboardingViewModel], renders a progress indicator, and routes the
 * customer through the capability-driven step sequence defined by [OnboardingConfig].  The
 * active [FrameTheme] is provided to all child composables automatically.
 *
 * @param config Onboarding configuration specifying the account, required capabilities, and
 *   optional theme overrides.
 * @param onResult Callback invoked with the terminal [OnboardingResult] when the flow
 *   completes, is cancelled, or fails.
 */
@Composable
fun OnboardingContainerView(
    config: OnboardingConfig,
    onResult: (OnboardingResult) -> Unit
) {
    val viewModel = remember { FrameOnboardingViewModel(config) }
    val snackbarHostState = remember { SnackbarHostState() }
    val result by viewModel.result.collectAsState()
    val userError by viewModel.userErrorMessage.collectAsState()
    val onboardingData by viewModel.onboardingData.collectAsState()
    val savedPaymentMethods by viewModel.savedPaymentMethods.collectAsState()
    val savedPayoutMethods by viewModel.savedPayoutMethods.collectAsState()
    val context = LocalContext.current

    // Authenticate every onboarding request with the onboarding-session token while this flow is
    // on screen, scoping it to a single account. Only flows that began a session end one, so a
    // legacy (clientSecret == null) flow leaves the configured keys untouched.
    DisposableEffect(config.clientSecret) {
        val clientSecret = config.clientSecret
        if (clientSecret != null) {
            FrameNetworking.beginOnboardingSession(clientSecret)
        } else {
            Log.w(
                "FrameSDK",
                "⚠️ Frame: onboarding launched without OnboardingConfig.clientSecret. Requests will fall back to " +
                    "the configured pk_/sk_ keys, which are not scoped to a single account. Mint an onboarding-session " +
                    "token from your backend (POST /v1/onboarding_sessions) and pass it as OnboardingConfig.clientSecret."
            )
        }
        onDispose {
            if (clientSecret != null) {
                // Clear only the token this flow set; if a newer onboarding flow has already begun
                // its own session, this stale disposal must not wipe it out.
                FrameNetworking.endOnboardingSession(clientSecret)
            }
        }
    }

    LaunchedEffect(result) {
        when (val r = result) {
            is OnboardingResult.Completed -> onResult(r)
            is OnboardingResult.Cancelled -> onResult(r)
            else -> Unit
        }
    }

    LaunchedEffect(userError) {
        val msg = userError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearUserErrorMessage()
    }

    LaunchedEffect(config.accountId) {
        if (config.accountId != null) {
            viewModel.launchCheckExistingAccount(updateCapabilities = true)
        }
    }

    LaunchedEffect(viewModel.navigationState.currentStep, onboardingData.selectedPaymentMethodId) {
        if (
            viewModel.navigationState.currentStep == OnboardingStep.VerifyYourCard &&
            config.requiredCapabilities.contains(Capabilities.CARD_VERIFICATION)
        ) {
            // TODO: Re-enable 3DS when card verification flow is ready.
            // viewModel.initialize3DS()
            viewModel.moveNext()
        }
    }

    FrameTheme(theme = config.theme ?: FrameTheme.default()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                ProgressIndicator(
                    currentStep = viewModel.navigationState.currentStep,
                    flowSegments = viewModel.flowSegments,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.weight(1f)) {
                    OnboardingScreenRouter(
                        viewModel = viewModel,
                        config = config,
                        savedPaymentMethods = savedPaymentMethods,
                        savedPayoutMethods = savedPayoutMethods,
                        onboardingData = onboardingData,
                        context = context
                    )
                }
            }
        }
    }
}

@FrameThemePreviews
@Composable
private fun OnboardingContainerViewPreview() {
    OnboardingContainerView(
        config = OnboardingConfig(
            requiredCapabilities = listOf(
                Capabilities.KYC,
                Capabilities.KYC_PREFILL,
                Capabilities.CARD_VERIFICATION,
                Capabilities.BANK_ACCOUNT_VERIFICATION,
                Capabilities.GEO_COMPLIANCE,
                Capabilities.AGE_VERIFICATION,
                Capabilities.PHONE_VERIFICATION
            ),
            skipInitNetwork = true
        ),
        onResult = {}
    )
}
