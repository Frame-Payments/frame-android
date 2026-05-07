package com.framepayments.frameonboarding.reusable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk_ui.theme.LocalFrameTheme

/**
 * Primary action button shared by checkout and onboarding. 1:1 port of iOS `ContinueButton`.
 * Pick [ContinueButtonStyle.SECONDARY] for an outlined, brand-colored variant.
 *
 * While [isLoading] is true the label is replaced with a circular spinner and presses are
 * blocked; while !`enabled` the button is disabled and a stroke is drawn around it (matches iOS).
 */
enum class ContinueButtonStyle { PRIMARY, SECONDARY }

@Composable
fun ContinueButton(
    text: String = "Continue",
    style: ContinueButtonStyle = ContinueButtonStyle.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = LocalFrameTheme.current
    val fillColor = when (style) {
        ContinueButtonStyle.PRIMARY -> theme.colors.primaryButton
        ContinueButtonStyle.SECONDARY -> theme.colors.secondaryButton
    }
    val textColor = when (style) {
        ContinueButtonStyle.PRIMARY -> theme.colors.primaryButtonText
        ContinueButtonStyle.SECONDARY -> theme.colors.secondaryButtonText
    }
    val border = when {
        !enabled && !isLoading -> BorderStroke(1.dp, theme.colors.disabledButtonStroke)
        enabled && style == ContinueButtonStyle.SECONDARY ->
            BorderStroke(1.dp, theme.colors.secondaryButtonText)
        else -> null
    }

    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled && !isLoading,
        onClick = onClick,
        shape = RoundedCornerShape(theme.radii.medium),
        border = border,
        colors = ButtonDefaults.buttonColors(
            containerColor = fillColor,
            contentColor = textColor,
            disabledContainerColor = theme.colors.disabledButton,
            disabledContentColor = theme.colors.disabledButtonText
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = textColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = theme.fonts.button
            )
        }
    }
}
