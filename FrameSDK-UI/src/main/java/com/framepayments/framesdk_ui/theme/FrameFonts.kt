package com.framepayments.framesdk_ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Typography token set for the Frame SDK UI.
 *
 * Each slot maps to a Material 3 typography role with weight overrides applied so the visual
 * hierarchy matches iOS. Pass a customized instance to [FrameTheme] to override fonts globally.
 *
 * @property title Large display text style used for screen titles.
 * @property heading Medium display text style used for section headings.
 * @property headline Prominent label text used for card headings and emphasized content.
 * @property body Default body copy text style.
 * @property bodySmall Smaller body copy used for secondary descriptions.
 * @property label Semibold label used for form field labels and list item titles.
 * @property caption Small text used for captions and footnotes.
 * @property button Semibold text used inside action buttons.
 */
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
    /** Factory methods for constructing default [FrameFonts] instances. */
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
        fun defaults(): FrameFonts = fromTypography(MaterialTheme.typography)

        /**
         * Non-Composable variant for View-based hosts. Uses Material 3's default
         * [Typography] (no theme-customized typography from a Compose tree).
         */
        fun defaultsForViews(): FrameFonts = fromTypography(Typography())

        private fun fromTypography(typography: Typography): FrameFonts = FrameFonts(
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
