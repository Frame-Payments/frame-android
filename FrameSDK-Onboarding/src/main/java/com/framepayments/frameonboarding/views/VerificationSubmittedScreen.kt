package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.framepayments.frameonboarding.classes.OnboardingOutcome
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import com.framepayments.framesdk_ui.theme.FrameTheme
import com.framepayments.framesdk_ui.theme.FrameThemePreviews

/**
 * The final screen of the onboarding flow. Renders from the resolved [OnboardingOutcome]
 * rather than asserting success — only [OnboardingOutcome.Approved] gets the congratulatory copy.
 *
 * @param outcome How onboarding ended. Null while still being resolved.
 * @param isResolving Whether the outcome is still being fetched.
 */
@Composable
internal fun VerificationSubmittedScreen(
    onDone: () -> Unit,
    outcome: OnboardingOutcome? = null,
    isResolving: Boolean = false
) {
    val resolved = outcome ?: OnboardingOutcome.PendingReview
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            if (isResolving) {
                CircularProgressIndicator(color = LocalFrameTheme.current.colors.primaryButton)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Checking your verification…",
                    style = LocalFrameTheme.current.fonts.bodySmall,
                    textAlign = TextAlign.Center,
                    color = LocalFrameTheme.current.colors.textSecondary
                )
            } else {
                Icon(
                    imageVector = if (resolved is OnboardingOutcome.Approved) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp),
                    tint = LocalFrameTheme.current.colors.primaryButton
                )
                Text(
                    text = when (resolved) {
                        is OnboardingOutcome.Approved -> "Verification Submitted"
                        is OnboardingOutcome.PendingReview -> "Verification In Review"
                        is OnboardingOutcome.Declined -> "Verification Unsuccessful"
                        is OnboardingOutcome.ActionRequired -> "More Information Needed"
                    },
                    style = LocalFrameTheme.current.fonts.headline,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    // Prefers the server-authored message so every Frame surface says the same words.
                    text = when (resolved) {
                        is OnboardingOutcome.Approved ->
                            "Congratulations! You've submitted your identity verification check. You're ready to proceed."
                        is OnboardingOutcome.PendingReview ->
                            "We're reviewing your information. This usually doesn't take long, and we'll be in touch once it's complete."
                        // No retry affordance: a terminal decline cannot be changed by trying again.
                        is OnboardingOutcome.Declined -> resolved.message
                            ?: "We weren't able to verify your identity. Please contact support if you think this is a mistake."
                        is OnboardingOutcome.ActionRequired -> resolved.message
                            ?: "We need a bit more information from you before we can finish verifying your identity. Please contact support."
                    },
                    style = LocalFrameTheme.current.fonts.bodySmall,
                    textAlign = TextAlign.Center,
                    color = LocalFrameTheme.current.colors.textSecondary
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onDone,
            enabled = !isResolving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LocalFrameTheme.current.colors.primaryButton,
                contentColor = LocalFrameTheme.current.colors.primaryButtonText
            )
        ) {
            Text(if (resolved.isSuccess) "Done" else "Close")
        }
    }
}

@FrameThemePreviews
@Composable
private fun VerificationSubmittedScreenPreview() {
    FrameTheme {
        VerificationSubmittedScreen(onDone = {}, outcome = OnboardingOutcome.Approved)
    }
}

@FrameThemePreviews
@Composable
private fun VerificationSubmittedScreenDeclinedPreview() {
    FrameTheme {
        VerificationSubmittedScreen(onDone = {}, outcome = OnboardingOutcome.Declined(message = null))
    }
}
