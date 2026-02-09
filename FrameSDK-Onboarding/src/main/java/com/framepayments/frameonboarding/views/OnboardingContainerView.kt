package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.framepayments.frameonboarding.classes.*
import com.framepayments.frameonboarding.viewmodels.FrameOnboarding

@Composable
fun OnboardingContainerView(
    config: OnboardingConfig,
    onResult: (OnboardingResult) -> Unit
) {
    var onboardingData by remember { mutableStateOf(OnboardingData()) }
    val state = remember { OnboardingState() }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Progress indicator always visible at top
        ProgressIndicator(
            currentStep = state.currentStep,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Screen content based on current step
        Box(modifier = Modifier.weight(1f)) {
            FrameOnboarding(
                config = config,
                onboardingData = onboardingData,
                state = state,
                onUpdateData = { updatedData ->
                    onboardingData = updatedData
                },
                onResult = onResult
            )
        }
    }
}
