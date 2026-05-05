package com.framepayments.frameonboarding.reusable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor

/// Primary action button used across every onboarding/payment screen. While `isLoading`,
/// the button label is replaced with a small CircularProgressIndicator and the button is
/// disabled to prevent redundant submissions.
@Composable
fun ContinueButton(
    text: String = "Continue",
    enabled: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        enabled = enabled && !isLoading,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = FramePrimaryColor,
            contentColor = FrameOnPrimaryColor,
            disabledContainerColor = FramePrimaryColor.copy(alpha = 0.35f),
            disabledContentColor = FrameOnPrimaryColor.copy(alpha = 0.7f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = FrameOnPrimaryColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(text)
        }
    }
}
