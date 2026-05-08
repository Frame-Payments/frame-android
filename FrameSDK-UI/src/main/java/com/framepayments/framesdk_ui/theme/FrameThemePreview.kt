package com.framepayments.framesdk_ui.theme

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multi-preview annotation that renders a Composable in both light and dark mode.
 * Apply this in place of `@Preview` on theme-sensitive Composables so dark-mode
 * regressions surface in the IDE preview pane.
 *
 * Usage:
 * ```
 * @FrameThemePreviews
 * @Composable
 * private fun MyComponentPreview() {
 *     FrameTheme { MyComponent() }
 * }
 * ```
 */
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class FrameThemePreviews
