package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.classes.OnboardingFlowSegment
import com.framepayments.frameonboarding.classes.OnboardingStep
import com.framepayments.frameonboarding.classes.toFlowSegment

@Composable
internal fun ProgressIndicator(
    currentStep: OnboardingStep,
    flowSegments: List<OnboardingFlowSegment>,
    modifier: Modifier = Modifier
) {
    val segments = if (flowSegments.isEmpty()) listOf(OnboardingFlowSegment.PERSONAL_INFORMATION) else flowSegments
    val currentSegment = currentStep.toFlowSegment()
    val currentIndex = segments.indexOf(currentSegment).coerceAtLeast(0) + 1
    val progress = currentIndex.toFloat() / segments.size.toFloat()

    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun ProgressIndicatorPreview() {
    ProgressIndicator(
        currentStep = OnboardingStep.SelectPaymentMethod,
        flowSegments = listOf(
            OnboardingFlowSegment.PERSONAL_INFORMATION,
            OnboardingFlowSegment.CONFIRM_PAYMENT_METHOD,
            OnboardingFlowSegment.UPLOAD_DOCUMENTS,
            OnboardingFlowSegment.VERIFICATION_SUBMITTED
        )
    )
}
