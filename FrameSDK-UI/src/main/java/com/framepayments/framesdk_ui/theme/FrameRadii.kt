package com.framepayments.framesdk_ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Corner-radius token set for the Frame SDK UI.
 *
 * Pass a customized instance to [FrameTheme] to adjust the rounding of all SDK surfaces.
 *
 * @property small Radius applied to small UI elements such as chips and badges (default: 8 dp).
 * @property medium Radius applied to cards, input fields, and buttons (default: 10 dp).
 * @property large Radius applied to large containers and bottom sheets (default: 16 dp).
 */
@Immutable
data class FrameRadii(
    val small: Dp = 8.dp,
    val medium: Dp = 10.dp,
    val large: Dp = 16.dp,
)
