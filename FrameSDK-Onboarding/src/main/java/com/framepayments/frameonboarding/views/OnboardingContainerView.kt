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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.classes.OnboardingFlowSegment
import com.framepayments.frameonboarding.classes.OnboardingResult
import com.framepayments.frameonboarding.classes.OnboardingStep
import com.framepayments.frameonboarding.classes.toFlowSegment
import com.framepayments.frameonboarding.classes.OnboardingOutcome
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.accountevents.AccountEventDetail
import com.framepayments.framesdk.accountevents.AccountEventEmitter
import com.framepayments.framesdk.accountevents.AccountEventName
import com.framepayments.framesdk.accountevents.AccountEventScreen
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
    DisposableEffect(viewModel) { onDispose { viewModel.close() } }
    val snackbarHostState = remember { SnackbarHostState() }
    val result by viewModel.result.collectAsState()
    val userError by viewModel.userErrorMessage.collectAsState()
    val onboardingData by viewModel.onboardingData.collectAsState()
    val savedPaymentMethods by viewModel.savedPaymentMethods.collectAsState()
    val savedPayoutMethods by viewModel.savedPayoutMethods.collectAsState()

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

    LaunchedEffect(Unit) {
        AccountEventEmitter.emit(AccountEventName.ONBOARDING_STARTED, AccountEventScreen.ONBOARDING)
    }

    var previousStep by remember { mutableStateOf(viewModel.navigationState.currentStep) }
    LaunchedEffect(viewModel.navigationState.currentStep) {
        val step = viewModel.navigationState.currentStep
        // Only a forward move completes a step; going back re-views one without completing it.
        val movedForward = viewModel.orderedSteps.indexOf(step) > viewModel.orderedSteps.indexOf(previousStep)
        if (step != previousStep) {
            if (movedForward) {
                AccountEventEmitter.emit(
                    AccountEventName.ONBOARDING_STEP_COMPLETED,
                    previousStep.accountEventScreen(),
                    detail = previousStep.toString()
                )
            }
            previousStep = step
        }
        AccountEventEmitter.emit(
            AccountEventName.ONBOARDING_STEP_VIEWED,
            step.accountEventScreen(),
            detail = step.toString()
        )
    }

    LaunchedEffect(result) {
        when (val r = result) {
            is OnboardingResult.Completed -> {
                AccountEventEmitter.emit(
                    AccountEventName.ONBOARDING_COMPLETED,
                    AccountEventScreen.ONBOARDING,
                    detail = AccountEventDetail.ONBOARDING_COMPLETED_APPROVED
                )
                onResult(r)
            }
            is OnboardingResult.FinishedUnverified -> {
                when (val outcome = r.outcome) {
                    is OnboardingOutcome.Declined -> AccountEventEmitter.emit(
                        AccountEventName.ONBOARDING_DECLINED,
                        AccountEventScreen.ONBOARDING,
                        detail = outcome.message ?: "declined"
                    )
                    is OnboardingOutcome.ActionRequired -> AccountEventEmitter.emit(
                        AccountEventName.ONBOARDING_ACTION_REQUIRED,
                        AccountEventScreen.ONBOARDING,
                        detail = outcome.message ?: "action required"
                    )
                    else -> AccountEventEmitter.emit(
                        AccountEventName.ONBOARDING_NEEDS_REVIEW,
                        AccountEventScreen.ONBOARDING
                    )
                }
                onResult(r)
            }
            is OnboardingResult.Cancelled -> {
                val step = viewModel.navigationState.currentStep
                AccountEventEmitter.emit(
                    AccountEventName.ONBOARDING_CANCELLED,
                    step.accountEventScreen(),
                    detail = "last step reached: $step"
                )
                onResult(r)
            }
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
            viewModel.initialize3DS()
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
                    onClose = viewModel::cancel,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.weight(1f)) {
                    OnboardingScreenRouter(
                        viewModel = viewModel,
                        config = config,
                        savedPaymentMethods = savedPaymentMethods,
                        savedPayoutMethods = savedPayoutMethods,
                        onboardingData = onboardingData
                    )
                }
            }
        }
    }
}

/** Mirrors iOS `OnboardingFlow.accountEventScreenName`, which maps at segment granularity. */
private fun OnboardingStep.accountEventScreen(): AccountEventScreen = when (toFlowSegment()) {
    OnboardingFlowSegment.PERSONAL_INFORMATION -> AccountEventScreen.PERSONAL_INFORMATION
    OnboardingFlowSegment.CONFIRM_PAYMENT_METHOD -> AccountEventScreen.PAYMENT_METHOD
    OnboardingFlowSegment.CONFIRM_PAYOUT_METHOD -> AccountEventScreen.PAYOUT_METHOD
    OnboardingFlowSegment.VERIFICATION_SUBMITTED -> AccountEventScreen.ONBOARDING
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
