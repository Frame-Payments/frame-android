package com.framepayments.framesdk_ui.reusable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Text field with validation error display. 1:1 port of iOS ValidatedTextField.
 */
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    prompt: String,
    error: String?,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    characterLimit: Int? = null,
    compactError: Boolean = false,
    inlineError: Boolean = false,
    errorSpacing: Dp = 4.dp,
    onClearError: (() -> Unit)? = null,
    autofillContentType: ContentType? = null
) {
    val handleChange: (String) -> Unit = { newValue ->
        val limited = if (characterLimit != null && newValue.length > characterLimit) {
            newValue.take(characterLimit)
        } else {
            newValue
        }
        onValueChange(limited)
        if (error != null) onClearError?.invoke()
    }

    val showError = error != null && !compactError
    val theme = LocalFrameTheme.current
    val fieldModifier = if (autofillContentType != null) {
        Modifier.semantics { contentType = autofillContentType }
    } else {
        Modifier
    }
    val colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = theme.colors.fieldFocusStroke,
        unfocusedBorderColor = theme.colors.surfaceStroke,
        errorBorderColor = theme.colors.error,
        focusedContainerColor = theme.colors.surface,
        unfocusedContainerColor = theme.colors.surface,
    )
    val fieldShape = RoundedCornerShape(theme.radii.medium)

    if (inlineError) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(errorSpacing)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = handleChange,
                placeholder = {
                    Text(
                        prompt,
                        style = theme.fonts.body,
                        color = theme.colors.textSecondary,
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .then(fieldModifier),
                singleLine = true,
                isError = showError,
                textStyle = theme.fonts.body,
                shape = fieldShape,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = colors
            )
            if (showError) {
                Text(
                    text = error!!,
                    style = theme.fonts.caption,
                    color = theme.colors.error,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = handleChange,
                placeholder = {
                    Text(
                        prompt,
                        style = theme.fonts.body,
                        color = theme.colors.textSecondary,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(fieldModifier),
                singleLine = true,
                isError = showError,
                textStyle = theme.fonts.body,
                shape = fieldShape,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = colors
            )
            if (showError) {
                Spacer(Modifier.height(errorSpacing))
                Text(
                    text = error!!,
                    style = theme.fonts.caption,
                    color = theme.colors.error,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
