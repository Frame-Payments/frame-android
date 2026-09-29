package com.framepayments.framesdk_ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Immutable design token bundle that drives all Frame SDK UI components.
 *
 * Construct a custom instance and pass it to the [FrameTheme] composable or to
 * [com.framepayments.framesdk_ui.FrameCartView.setTheme] /
 * [com.framepayments.framesdk_ui.FrameCheckoutView.setTheme] to apply merchant branding.
 *
 * @property colors Color token set controlling palette across all SDK surfaces.
 * @property fonts Typography token set controlling text styles across all SDK surfaces.
 * @property radii Corner-radius token set controlling shape across all SDK surfaces.
 * @property spacing Vertical spacing token set for section headers and form blocks.
 */
@Immutable
data class FrameTheme(
    val colors: FrameColors,
    val fonts: FrameFonts,
    val radii: FrameRadii,
    val spacing: FrameSpacing = FrameSpacing(),
) {
    /** Factory methods for constructing default [FrameTheme] instances. */
    companion object {
        /**
         * Default theme — pulls colors via [colorResource] (so dark mode "just works" via
         * `values-night/colors.xml`) and fonts via [FrameFonts.defaults], which apply the
         * weight overrides needed for visual parity with iOS.
         */
        @Composable
        fun default(): FrameTheme = FrameTheme(
            colors = FrameColors.defaults(),
            fonts = FrameFonts.defaults(),
            radii = FrameRadii(),
            spacing = FrameSpacing(),
        )

        /**
         * Non-Composable equivalent of [default] for View-based hosts that want to call
         * [com.framepayments.framesdk_ui.FrameCartView.setTheme] /
         * [com.framepayments.framesdk_ui.FrameCheckoutView.setTheme] without spinning up
         * a Compose tree just to construct a theme.
         */
        fun default(context: Context): FrameTheme = FrameTheme(
            colors = FrameColors.defaults(context),
            fonts = FrameFonts.defaultsForViews(),
            radii = FrameRadii(),
            spacing = FrameSpacing(),
        )
    }
}

/**
 * Composition local providing the active [FrameTheme]. Components inside the SDK read this
 * via `LocalFrameTheme.current`.
 *
 * No default value: SDK Composables MUST be hosted inside a `FrameTheme { ... }` wrapper or
 * invoked through `OnboardingContainerView`, both of which install a theme via
 * `CompositionLocalProvider`. Failing loudly here matches the iOS contract (where a theme
 * is always available via `@Environment(\.frameTheme)` with a static `.default`) and avoids
 * the silent-customization-bug class where a hardcoded fallback drifts from the real
 * defaults. For `@Preview`, wrap the previewed content in `FrameTheme { ... }`.
 */
val LocalFrameTheme = staticCompositionLocalOf<FrameTheme> {
    error(
        "FrameTheme not provided. Wrap SDK UI in FrameTheme { ... } " +
            "(or use OnboardingContainerView, which wraps automatically)."
    )
}

/**
 * Installs [theme] into the composition tree so all nested SDK components read it via
 * [LocalFrameTheme], and mirrors its colors/fonts into Material 3 so Scaffold, TopAppBar,
 * and default Text/Button styles stay white-background + Soehne instead of Material lilac.
 *
 * @param theme Theme to provide to child composables (default: [FrameTheme.default]).
 * @param content Composable content that receives the theme.
 */
@Composable
fun FrameTheme(
    theme: FrameTheme = FrameTheme.default(),
    content: @Composable () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = remember(theme.colors, isDark) {
        frameColorScheme(theme.colors, isDark)
    }
    val typography = remember(theme.fonts) {
        Typography(
            displayLarge = theme.fonts.title,
            displayMedium = theme.fonts.title,
            displaySmall = theme.fonts.heading,
            headlineLarge = theme.fonts.title,
            headlineMedium = theme.fonts.heading,
            headlineSmall = theme.fonts.headline,
            titleLarge = theme.fonts.headline,
            titleMedium = theme.fonts.label,
            titleSmall = theme.fonts.label,
            bodyLarge = theme.fonts.body,
            bodyMedium = theme.fonts.bodySmall,
            bodySmall = theme.fonts.caption,
            labelLarge = theme.fonts.button,
            labelMedium = theme.fonts.label,
            labelSmall = theme.fonts.caption,
        )
    }
    CompositionLocalProvider(LocalFrameTheme provides theme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}

private fun frameColorScheme(colors: FrameColors, isDark: Boolean) =
    if (isDark) {
        darkColorScheme(
            primary = colors.primaryButton,
            onPrimary = colors.primaryButtonText,
            primaryContainer = colors.surface,
            onPrimaryContainer = colors.textPrimary,
            secondary = colors.primaryButton,
            onSecondary = colors.primaryButtonText,
            tertiary = colors.primaryButton,
            background = colors.surface,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surface,
            onSurfaceVariant = colors.textSecondary,
            surfaceContainer = colors.surface,
            surfaceContainerHigh = colors.surface,
            surfaceContainerHighest = colors.surface,
            surfaceContainerLow = colors.surface,
            surfaceContainerLowest = colors.surface,
            outline = colors.surfaceStroke,
            outlineVariant = colors.surfaceStroke,
            error = colors.error,
            onError = Color.White,
        )
    } else {
        lightColorScheme(
            primary = colors.primaryButton,
            onPrimary = colors.primaryButtonText,
            primaryContainer = colors.surface,
            onPrimaryContainer = colors.textPrimary,
            secondary = colors.primaryButton,
            onSecondary = colors.primaryButtonText,
            tertiary = colors.primaryButton,
            background = colors.surface,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surface,
            onSurfaceVariant = colors.textSecondary,
            surfaceContainer = colors.surface,
            surfaceContainerHigh = colors.surface,
            surfaceContainerHighest = colors.surface,
            surfaceContainerLow = colors.surface,
            surfaceContainerLowest = colors.surface,
            outline = colors.surfaceStroke,
            outlineVariant = colors.surfaceStroke,
            error = colors.error,
            onError = Color.White,
        )
    }
