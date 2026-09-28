package com.framepayments.framesdk_ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Vertical spacing tokens for titles, sheet headers, and form sections.
 *
 * @property sectionTop Padding above and below section headers (e.g. checkout titles).
 * @property sectionGap Gap between stacked sections.
 * @property formBlock Vertical gap between sibling form fields.
 */
@Immutable
data class FrameSpacing(
    val sectionTop: Dp = 16.dp,
    val sectionGap: Dp = 12.dp,
    val formBlock: Dp = 16.dp,
)
