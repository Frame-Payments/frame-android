package com.framepayments.frameonboarding.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.classes.OnboardingData
import com.framepayments.frameonboarding.classes.OnboardingStep
import com.framepayments.frameonboarding.classes.PaymentMethodSummary
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel

@Composable
internal fun OnboardingScreenRouter(
    viewModel: FrameOnboardingViewModel,
    config: OnboardingConfig,
    savedPaymentMethods: List<PaymentMethodSummary>,
    savedPayoutMethods: List<PaymentMethodSummary>,
    onboardingData: OnboardingData
) {
    when (viewModel.navigationState.currentStep) {
        OnboardingStep.VerificationWelcome -> {
            OnboardingIntroView(
                onContinue = { viewModel.moveNext() }
            )
        }

        OnboardingStep.VerifyIdentification -> {
            UserIdentificationView(
                viewModel = viewModel,
                requiresDateOfBirth = config.requiredCapabilities.contains(Capabilities.KYC_PREFILL),
                showTermsOfService = true,
                onBack = { viewModel.moveBack() }
            )
        }

        OnboardingStep.GeolocationVerification -> {
            GeolocationVerificationScreen(
                accountId = onboardingData.resolvedAccountId,
                onContinue = { viewModel.moveNext() },
                onDisableVpn = { viewModel.moveNext() }
            )
        }

        OnboardingStep.SelectPaymentMethod -> {
            LaunchedEffect(Unit) { viewModel.loadSavedPaymentMethods() }
            SelectPaymentMethodScreen(
                savedMethods = savedPaymentMethods,
                selectedId = onboardingData.selectedPaymentMethodId,
                onSelect = { viewModel.onPaymentMethodSelected(it) },
                onAddCard = { viewModel.moveNext() },
                onBack = { viewModel.moveBack() },
                onContinue = {
                    if (onboardingData.selectedPaymentMethodId != null) {
                        viewModel.moveToNextSegment()
                    }
                }
            )
        }

        OnboardingStep.AddPaymentMethod -> {
            AddPaymentMethodScreen(
                viewModel = viewModel,
                onBack = { viewModel.moveBack() }
            )
        }

        OnboardingStep.VerifyYourCard -> {
            VerifyCardScreen(
                showResendCode = true,
                onBack = { viewModel.moveBack() },
                onResendCode = { viewModel.resend3DS() },
                onContinue = { viewModel.moveNext() }
            )
        }

        OnboardingStep.SelectPayoutMethod -> {
            LaunchedEffect(Unit) { viewModel.loadSavedPaymentMethods() }
            val primaryPayoutMethodId by viewModel.primaryPayoutMethodId.collectAsState()
            val isPerformingAction by viewModel.isPerformingAction.collectAsState()
            SelectPayoutMethodScreen(
                savedMethods = savedPayoutMethods,
                selectedId = onboardingData.selectedPayoutMethodId,
                primaryId = primaryPayoutMethodId,
                isLoading = isPerformingAction,
                onSelect = { viewModel.onPayoutMethodSelected(it) },
                onAddPayout = { viewModel.moveNext() },
                onBack = { viewModel.moveBack() },
                onContinue = { viewModel.electSelectedPayoutMethod { viewModel.moveToNextSegment() } }
            )
        }

        OnboardingStep.AddPayoutMethod -> {
            AddPayoutMethodScreen(
                viewModel = viewModel,
                onBack = { viewModel.moveBack() }
            )
        }

        OnboardingStep.VerificationSubmitted -> {
            val finalOutcome by viewModel.finalOutcome.collectAsState()
            val isResolvingOutcome by viewModel.isResolvingOutcome.collectAsState()
            LaunchedEffect(Unit) { viewModel.resolveFinalOutcomeIfNeeded() }
            VerificationSubmittedScreen(
                onDone = { viewModel.moveNext() },
                outcome = finalOutcome,
                isResolving = isResolvingOutcome
            )
        }
    }
}
