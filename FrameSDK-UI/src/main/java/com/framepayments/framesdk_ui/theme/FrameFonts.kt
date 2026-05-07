package com.framepayments.framesdk_ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Immutable
data class FrameFonts(
    val title: TextStyle,
    val heading: TextStyle,
    val headline: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val button: TextStyle,
) {
    companion object {
        /**
         * Defaults map to Material 3 typography slots, with weight overrides applied so the
         * visual hierarchy matches iOS, where `theme.fonts.title` / `.heading` / `.headline`
         * carry implicit semibold/bold weight via SwiftUI's system fonts. Material 3's
         * default weights are Regular (400) for headline and title slots, which renders
         * underweight against iOS without these overrides.
         */
        @Composable
        @ReadOnlyComposable
        fun defaults(typography: Typography = MaterialTheme.typography): FrameFonts = FrameFonts(
            title = typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            heading = typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            headline = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            body = typography.bodyLarge,
            bodySmall = typography.bodyMedium,
            label = typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            caption = typography.labelSmall,
            button = typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}
