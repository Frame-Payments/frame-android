package com.framepayments.framesdk_ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class FrameTheme(
    val colors: FrameColors,
    val fonts: FrameFonts,
    val radii: FrameRadii,
) {
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

@Composable
fun FrameTheme(
    theme: FrameTheme = FrameTheme.default(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalFrameTheme provides theme, content = content)
}
