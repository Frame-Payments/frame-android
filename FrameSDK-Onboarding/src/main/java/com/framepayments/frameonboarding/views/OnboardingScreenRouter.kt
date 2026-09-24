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
            val accountReady by viewModel.isExistingAccountReady.collectAsState()
            OnboardingIntroView(
                // Block Continue while a pre-existing account is still loading so the flow
                // cannot yank the applicant back once capabilities resolve.
                continueEnabled = accountReady,
                onContinue = { viewModel.moveNext() }
            )
        }

        OnboardingStep.VerifyIdentification -> {
            UserIdentificationView(
                viewModel = viewModel,
            requiresDateOfBirth = viewModel.originallyRequiredCapabilities.contains(Capabilities.KYC_PREFILL),
                // Always show TOS on Android. iOS gates on geo_compliance; Android keeps the
                // broader surface so every create/update path can attach an acceptance token
                // (decision: Android is the correct one for now — M18).
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
                onAddCard = {
                    viewModel.clearOnlyAddressVerification()
                    viewModel.moveNext()
                },
                onBack = { viewModel.moveBack() },
                onContinue = {
                    if (onboardingData.selectedPaymentMethodId != null) {
                        if (viewModel.continueWithSelectedPaymentMethod()) {
                            viewModel.moveToNextSegment()
                        }
                    }
                }
            )
        }

        OnboardingStep.AddPaymentMethod -> {
            AddPaymentMethodScreen(
                viewModel = viewModel,
                onBack = {
                    viewModel.clearOnlyAddressVerification()
                    viewModel.moveBack()
                }
            )
        }

        // Kept for sealed-class exhaustiveness; removed from the ordered flow (M5). iOS no
        // longer presents this orphan 3DS OTP screen.
        OnboardingStep.VerifyYourCard -> {
            LaunchedEffect(Unit) { viewModel.moveNext() }
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
                onAddPayout = { if (!isPerformingAction) viewModel.moveNext() },
                onBack = { if (!isPerformingAction) viewModel.moveBack() },
                onContinue = {
                    viewModel.electSelectedPayoutMethod {
                        // The applicant may have left (e.g. closed the flow) while the election ran.
                        if (viewModel.navigationState.currentStep == OnboardingStep.SelectPayoutMethod) {
                            viewModel.moveToNextSegment()
                        }
                    }
                }
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
