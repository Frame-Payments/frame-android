package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.classes.OnboardingResult
import com.framepayments.frameonboarding.classes.OnboardingStep
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel

@Composable
fun OnboardingContainerView(
    config: OnboardingConfig,
    onResult: (OnboardingResult) -> Unit
) {
    val viewModel = remember { FrameOnboardingViewModel(config) }
    val result by viewModel.result.collectAsState()
    val onboardingData by viewModel.onboardingData.collectAsState()
    val savedPaymentMethods by viewModel.savedPaymentMethods.collectAsState()
    val savedPayoutMethods by viewModel.savedPayoutMethods.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(result) {
        result?.let { onResult(it) }
    }

    LaunchedEffect(viewModel.navigationState.currentStep, onboardingData.selectedPaymentMethodId) {
        if (
            viewModel.navigationState.currentStep == OnboardingStep.VerifyYourCard &&
            config.requiredCapabilities.contains(Capabilities.CARD_VERIFICATION)
        ) {
            viewModel.initialize3DS()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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

@Preview(showBackground = true)
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
            )
        ),
        onResult = {}
    )
}
