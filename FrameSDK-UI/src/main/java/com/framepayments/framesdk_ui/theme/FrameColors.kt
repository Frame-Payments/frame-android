package com.framepayments.framesdk_ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.framepayments.framesdk_ui.R

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
    val onboardingHeaderBackground: Color,
    val onboardingProgressFilledOnBrand: Color,
    val onboardingProgressEmptyOnBrand: Color,
) {
    companion object {
        @Composable
        fun defaults(): FrameColors = FrameColors(
            primaryButton = colorResource(R.color.frame_primary_button),
            primaryButtonText = colorResource(R.color.frame_primary_button_text),
            secondaryButton = colorResource(R.color.frame_secondary_button),
            secondaryButtonText = colorResource(R.color.frame_secondary_button_text),
            disabledButton = colorResource(R.color.frame_disabled_button),
            disabledButtonStroke = colorResource(R.color.frame_disabled_button_stroke),
            disabledButtonText = colorResource(R.color.frame_disabled_button_text),
            surface = colorResource(R.color.frame_surface),
            surfaceStroke = colorResource(R.color.frame_surface_stroke),
            textPrimary = colorResource(R.color.frame_text_primary),
            textSecondary = colorResource(R.color.frame_text_secondary),
            error = colorResource(R.color.frame_error),
            onboardingHeaderBackground = colorResource(R.color.frame_onboarding_header_background),
            onboardingProgressFilledOnBrand = colorResource(R.color.frame_onboarding_progress_filled_on_brand),
            onboardingProgressEmptyOnBrand = colorResource(R.color.frame_onboarding_progress_empty_on_brand),
        )
    }
}
