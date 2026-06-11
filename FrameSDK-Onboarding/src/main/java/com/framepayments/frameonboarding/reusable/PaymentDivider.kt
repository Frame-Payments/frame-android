package com.framepayments.frameonboarding.reusable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk_ui.theme.LocalFrameTheme

/**
 * Horizontal "Or" divider used to separate wallet payment options (Google Pay) from the card form.
 *
 * @param modifier Layout modifier.
 * @param label Text displayed between the two divider lines (default: "Or").
 */
@Composable
fun PaymentDivider(
    modifier: Modifier = Modifier,
    label: String = "Or"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val theme = LocalFrameTheme.current
        HorizontalDivider(modifier = Modifier.weight(1f), color = theme.colors.surfaceStroke)
        Text(label, style = theme.fonts.bodySmall, color = theme.colors.textSecondary)
        HorizontalDivider(modifier = Modifier.weight(1f), color = theme.colors.surfaceStroke)
    }
}
