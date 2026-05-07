package com.framepayments.frameonboarding.views

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.classes.OnboardingFlowSegment
import com.framepayments.frameonboarding.classes.OnboardingStep
import com.framepayments.frameonboarding.classes.toFlowSegment
import com.framepayments.framesdk_ui.theme.LocalFrameTheme

/**
 * Onboarding header with per-segment progress capsules. Mirrors iOS `containerHeader`:
 * a brand-colored bar with one capsule per flow segment; capsules up to and including the
 * current segment are "filled". Capsule colors swap in dark mode to keep contrast on the
 * brand-colored background, matching iOS `progressIndicatorColor(filled:)` behavior.
 */
@Composable
internal fun ProgressIndicator(
    currentStep: OnboardingStep,
    flowSegments: List<OnboardingFlowSegment>,
    modifier: Modifier = Modifier
) {
    val theme = LocalFrameTheme.current
    val isDark = isSystemInDarkTheme()
    val segments = if (flowSegments.isEmpty()) listOf(OnboardingFlowSegment.PERSONAL_INFORMATION) else flowSegments
    val currentSegment = currentStep.toFlowSegment()
    val currentIndex = segments.indexOf(currentSegment).coerceAtLeast(0)

    fun capsuleColor(filled: Boolean): Color = when {
        isDark && filled -> theme.colors.onboardingProgressFilledOnBrand
        isDark && !filled -> theme.colors.onboardingProgressEmptyOnBrand
        filled -> theme.colors.primaryButton
        else -> theme.colors.surfaceStroke
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(theme.colors.onboardingHeaderBackground),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            segments.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .height(5.dp)
                        .weight(1f)
                        .background(
                            color = capsuleColor(filled = index <= currentIndex),
                            // 2.5.dp = half the 5.dp height, producing a true pill shape.
                            // Intentionally not theme.radii.* — capsule geometry is derived
                            // from height, not customizable corner radius.
                            shape = RoundedCornerShape(2.5.dp)
                        )
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            color = theme.colors.surfaceStroke
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressIndicatorPreview() {
    ProgressIndicator(
        currentStep = OnboardingStep.SelectPaymentMethod,
        flowSegments = listOf(
            OnboardingFlowSegment.PERSONAL_INFORMATION,
            OnboardingFlowSegment.CONFIRM_PAYMENT_METHOD,
            OnboardingFlowSegment.VERIFICATION_SUBMITTED
        )
    )
}
