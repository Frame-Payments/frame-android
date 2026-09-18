package com.framepayments.frameonboarding.reusable

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk_ui.theme.LocalFrameTheme

/**
 * Pairs an optional bold header label with a tappable dropdown selector row, used throughout
 * the onboarding payment elements to let users open a picker sheet for a given field.
 *
 * @param headerText Label displayed above the dropdown row, when [showHeaderText] is true.
 * @param dropDownText The currently selected value shown inside the dropdown row.
 * @param onTap Called when the row is tapped, so the caller can show its picker sheet.
 * @param showHeaderText When true, the bold header label is rendered above the dropdown row.
 * @param showDropdownBorder When true, a rounded-rectangle stroke border is drawn around the dropdown row.
 */
@Composable
fun DropDownWithHeaderView(
    headerText: String,
    dropDownText: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    showHeaderText: Boolean = true,
    showDropdownBorder: Boolean = true
) {
    val theme = LocalFrameTheme.current

    Column(modifier = modifier) {
        if (showHeaderText) {
            Text(
                text = headerText,
                style = theme.fonts.label,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        val rowModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .let {
                if (showDropdownBorder) {
                    it.border(1.dp, theme.colors.surfaceStroke, RoundedCornerShape(theme.radii.medium))
                } else {
                    it
                }
            }
            .clickable(onClick = onTap)

        Row(
            modifier = rowModifier.padding(vertical = if (showDropdownBorder) 12.dp else 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = dropDownText, style = theme.fonts.label, fontWeight = FontWeight.Medium, color = theme.colors.textPrimary)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = theme.colors.textPrimary)
        }
    }
}
