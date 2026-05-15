package com.framepayments.framesdk_ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.framepayments.framesdk_ui.R
import androidx.core.content.ContextCompat

@Immutable
data class FrameColors(
    val primaryButton: Color,
    val primaryButtonText: Color,
    val secondaryButton: Color,
    val secondaryButtonText: Color,
    val disabledButton: Color,
    val disabledButtonStroke: Color,
    val disabledButtonText: Color,
    val surface: Color,
    val surfaceStroke: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val error: Color,
    val toastBackground: Color,
    val toastText: Color,
    val onboardingHeaderBackground: Color,
    val onboardingProgressFilledOnBrand: Color,
    val onboardingProgressEmptyOnBrand: Color,
) {
    companion object {
        fun defaults(context: Context): FrameColors {
            fun c(id: Int) = Color(ContextCompat.getColor(context, id))
            return FrameColors(
                primaryButton = c(R.color.frame_primary_button),
                primaryButtonText = c(R.color.frame_primary_button_text),
                secondaryButton = c(R.color.frame_secondary_button),
                secondaryButtonText = c(R.color.frame_secondary_button_text),
                disabledButton = c(R.color.frame_disabled_button),
                disabledButtonStroke = c(R.color.frame_disabled_button_stroke),
                disabledButtonText = c(R.color.frame_disabled_button_text),
                surface = c(R.color.frame_surface),
                surfaceStroke = c(R.color.frame_surface_stroke),
                textPrimary = c(R.color.frame_text_primary),
                textSecondary = c(R.color.frame_text_secondary),
                error = c(R.color.frame_error),
                toastBackground = c(R.color.frame_toast_background),
                toastText = c(R.color.frame_toast_text),
                onboardingHeaderBackground = c(R.color.frame_onboarding_header_background),
                onboardingProgressFilledOnBrand = c(R.color.frame_onboarding_progress_filled_on_brand),
                onboardingProgressEmptyOnBrand = c(R.color.frame_onboarding_progress_empty_on_brand),
            )
        }

        @Composable
        fun defaults(): FrameColors = defaults(LocalContext.current)
    }
}
