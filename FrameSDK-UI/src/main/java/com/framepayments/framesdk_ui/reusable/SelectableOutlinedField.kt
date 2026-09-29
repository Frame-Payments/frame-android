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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
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
    val a11yLabel = buildList {
        add(prompt)
        if (showValue) add(value)
        if (error != null) add(error)
    }.joinToString(", ")
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
                // Visual only — a11y lives on the clickable overlay so prompt/value/error + button
                // action are one node (mergeDescendants does not pull clickable children in).
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clearAndSetSemantics { },
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
                    .clickable(
                        onClick = onClick,
                        role = Role.Button,
                        onClickLabel = prompt,
                    )
                    .semantics {
                        contentDescription = a11yLabel
                        if (error != null) error(error)
                    }
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
