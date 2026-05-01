package com.framepayments.frameonboarding.reusable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Text field with validation error display. 1:1 port of iOS ValidatedTextField.
 *
 * Three error display modes:
 * - default: error rendered below the field
 * - [inlineError] = true: error rendered to the right of the field in a Row
 * - [compactError] = true: error label suppressed (parent renders a header summary instead)
 *
 * Errors auto-clear via [onClearError] on every keystroke (matches iOS auto-clear behavior).
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
    onClearError: (() -> Unit)? = null
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

    if (inlineError) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(errorSpacing)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = handleChange,
                placeholder = { Text(prompt) },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                singleLine = true,
                isError = showError,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
            )
            if (showError) {
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = handleChange,
                placeholder = { Text(prompt) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showError,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
            )
            if (showError) {
                Spacer(Modifier.height(errorSpacing))
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
