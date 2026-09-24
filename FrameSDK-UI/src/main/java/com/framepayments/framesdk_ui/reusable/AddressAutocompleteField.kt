package com.framepayments.framesdk_ui.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.addresssearch.AddressAutocompleteController
import com.framepayments.framesdk.addresssearch.AddressSuggestion
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import kotlinx.coroutines.launch

/**
 * An address line 1 field that offers suggestions as the user types.
 *
 * The field is a plain [ValidatedTextField], so typing drives the suggestion search exactly as
 * it does without autocomplete. But a valid address must come from a picked suggestion: if the
 * field loses focus without one being selected, whatever was hand-typed is cleared. Suggestions
 * are drawn in an overlay below the field and appear only while it is focused and the lookup
 * returned something.
 *
 * @param prompt Placeholder text shown inside the field when it is empty.
 * @param value Address line 1 value.
 * @param onValueChange Called with the new value on every keystroke.
 * @param error Current validation error message, if any.
 * @param countryCode ISO 3166-1 alpha-2 code the suggestions are restricted to, so a form
 *   locked to one country does not surface addresses from another.
 * @param inlineError When true, the error label sits beside the field rather than below it.
 * @param onClearError Called when the field should clear its validation error.
 * @param controller Drives the suggestion list. Injected in tests; the default talks to Mapbox.
 * @param onSelect Called with the full address when the user picks a suggestion.
 */
@Composable
fun AddressAutocompleteField(
    prompt: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    countryCode: String?,
    modifier: Modifier = Modifier,
    inlineError: Boolean = false,
    onClearError: (() -> Unit)? = null,
    controller: AddressAutocompleteController = remember { AddressAutocompleteController() },
    onSelect: (FrameObjects.BillingAddress) -> Unit
) {
    val theme = LocalFrameTheme.current
    val suggestions by controller.suggestions.collectAsState()
    val scope = rememberCoroutineScope()
    var isFocused by remember { mutableStateOf(false) }
    // Suppresses the blur-clear while select() drops focus itself to fill the field.
    var isSelecting by remember { mutableStateOf(false) }
    // Autofill sets the whole street at once; that value is kept on blur like a picked suggestion.
    var autofilledValue by remember { mutableStateOf<String?>(null) }

    fun select(suggestion: AddressSuggestion) {
        scope.launch {
            val address = controller.select(suggestion) ?: return@launch
            isSelecting = true
            onSelect(address)
        }
    }

    Column(modifier = modifier) {
        ValidatedTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.length - value.length > 1) autofilledValue = newValue
                onValueChange(newValue)
                if (isFocused) controller.queryChanged(newValue, countryCode)
            },
            prompt = prompt,
            error = error,
            inlineError = inlineError,
            onClearError = onClearError,
            autofillContentType = ContentType.AddressStreet,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state ->
                    val wasFocused = isFocused
                    isFocused = state.isFocused
                    if (wasFocused && !state.isFocused) {
                        controller.clear()
                        if (isSelecting) {
                            isSelecting = false
                        } else if (value.isNotEmpty() && value != autofilledValue) {
                            // Left the field without picking a suggestion: hand-typed text never
                            // becomes a saved address, so drop it rather than let free text through.
                            onValueChange("")
                        }
                    }
                }
        )

        if (isFocused && suggestions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-8).dp)
                    .background(theme.colors.surface, RoundedCornerShape(theme.radii.medium))
                    .border(1.dp, theme.colors.surfaceStroke, RoundedCornerShape(theme.radii.medium))
            ) {
                suggestions.forEachIndexed { index, suggestion ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { select(suggestion) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = suggestion.title, style = theme.fonts.body, color = theme.colors.textPrimary)
                        if (suggestion.subtitle.isNotEmpty()) {
                            Text(text = suggestion.subtitle, style = theme.fonts.caption, color = theme.colors.textSecondary)
                        }
                    }
                    if (index != suggestions.lastIndex) {
                        Divider(color = theme.colors.surfaceStroke)
                    }
                }
            }
        }
    }
}
