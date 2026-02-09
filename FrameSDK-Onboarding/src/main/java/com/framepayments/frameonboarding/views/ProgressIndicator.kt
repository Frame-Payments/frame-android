package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.classes.OnboardingStep

@Composable
internal fun ProgressIndicator(
    currentStep: OnboardingStep,
    modifier: Modifier = Modifier
) {
    val totalSteps = 5
    val currentStepNumber = when (currentStep) {
        OnboardingStep.VerificationWelcome,
        OnboardingStep.VerifyIdentification -> 1
        OnboardingStep.SelectPaymentMethod,
        OnboardingStep.AddPaymentMethod,
        OnboardingStep.VerifyYourCard -> 2
        OnboardingStep.SelectPayoutMethod,
        OnboardingStep.AddPayoutMethod -> 3
        OnboardingStep.UploadDocumentsList,
        OnboardingStep.CaptureFrontPhoto,
        OnboardingStep.ReviewFrontPhoto,
        OnboardingStep.CaptureBackPhoto,
        OnboardingStep.ReviewBackPhoto,
        OnboardingStep.CaptureSelfie,
        OnboardingStep.ReviewSelfie -> 4
        OnboardingStep.GeolocationVerification -> 5
    }
    
    val progress = currentStepNumber.toFloat() / totalSteps.toFloat()
    
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        )
    }
}
