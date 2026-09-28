package com.framepayments.framesdk_ui.reusable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk_ui.theme.LocalFrameTheme

/**
 * Read-only outlined field that matches [ValidatedTextField] chrome (placeholder, not floating
 * label) and opens a picker on tap — used for state/country selects.
 */
@Composable
fun SelectableOutlinedField(
    value: String,
    prompt: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
) {
    val theme = LocalFrameTheme.current
    val showValue = value.isNotBlank()
    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = if (showValue) value else "",
                onValueChange = {},
                readOnly = true,
                enabled = false,
                placeholder = {
                    Text(
                        text = prompt,
                        style = theme.fonts.body,
                        color = theme.colors.textSecondary,
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = theme.colors.textSecondary,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                singleLine = true,
                isError = error != null,
                textStyle = theme.fonts.body,
                shape = RoundedCornerShape(theme.radii.medium),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = theme.colors.textPrimary,
                    disabledBorderColor = if (error != null) {
                        theme.colors.error
                    } else {
                        theme.colors.surfaceStroke
                    },
                    disabledPlaceholderColor = theme.colors.textSecondary,
                    disabledTrailingIconColor = theme.colors.textSecondary,
                    disabledContainerColor = theme.colors.surface,
                    errorBorderColor = theme.colors.error,
                    errorContainerColor = theme.colors.surface,
                ),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = onClick)
            )
        }
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = error,
                style = theme.fonts.caption,
                color = theme.colors.error,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}
