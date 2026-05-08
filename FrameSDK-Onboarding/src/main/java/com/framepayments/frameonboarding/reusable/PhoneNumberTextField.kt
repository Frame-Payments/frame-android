package com.framepayments.frameonboarding.reusable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.i18n.phonenumbers.AsYouTypeFormatter
import com.google.i18n.phonenumbers.PhoneNumberUtil

/**
 * Format a raw input string for the given region using a libphonenumber
 * AsYouTypeFormatter. The formatter holds per-call mutable state so callers must
 * `clear()` between sequences — we own the formatter here and do that internally.
 *
 * Exposed `internal` so tests can verify formatting without spinning up Compose.
 */
internal fun formatPhoneNumber(
    formatter: AsYouTypeFormatter,
    raw: String
): String {
    formatter.clear()
    var formatted = ""
    for (ch in raw) {
        if (ch.isDigit() || ch == '+') {
            formatted = formatter.inputDigit(ch)
        }
    }
    return formatted
}

/**
 * Count the number of "input characters" (digits or leading '+') in [text] up to
 * [endExclusive]. Used to translate a cursor offset between the formatted display
 * string and the raw digit stream that drives the formatter.
 */
private fun countInputCharsUpTo(text: String, endExclusive: Int): Int {
    val end = endExclusive.coerceIn(0, text.length)
    var n = 0
    for (i in 0 until end) {
        val c = text[i]
        if (c.isDigit() || c == '+') n++
    }
    return n
}

/**
 * Map a digit-count back to a character offset in [formatted]. Returns the smallest
 * offset such that the formatted prefix contains exactly [digitsConsumed] digit-or-plus
 * characters. Always anchors after a digit (not before a separator) so the cursor
 * appears immediately after the most recently entered character — which is the
 * behavior users expect when typing into a live-formatted phone field.
 */
private fun cursorAfterNDigits(formatted: String, digitsConsumed: Int): Int {
    if (digitsConsumed <= 0) return 0
    var seen = 0
    for (i in formatted.indices) {
        val c = formatted[i]
        if (c.isDigit() || c == '+') {
            seen++
            if (seen == digitsConsumed) return i + 1
        }
    }
    return formatted.length
}

/**
 * Phone number text field with region-aware live formatting via libphonenumber's
 * AsYouTypeFormatter. 1:1 port of iOS PhoneNumberTextField (PhoneNumberKit-backed).
 *
 * Cursor handling: the formatter inserts/removes separators (parens, spaces, dashes)
 * as the user types. We translate the cursor offset to/from the underlying digit
 * stream so the cursor stays anchored after the most recently typed character —
 * Compose's default String/`OutlinedTextField` plumbing would always snap to the
 * end of the formatted string, losing the user's position.
 *
 * The formatter is owned by `remember(regionCode)` so each composable instance has
 * its own — `AsYouTypeFormatter` is not thread-safe and a shared cache could
 * interleave state across concurrent callers.
 */
@Composable
fun PhoneNumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    prompt: String,
    regionCode: String,
    error: String?,
    modifier: Modifier = Modifier,
    compactError: Boolean = false,
    onClearError: (() -> Unit)? = null
) {
    val formatter = remember(regionCode) {
        PhoneNumberUtil.getInstance().getAsYouTypeFormatter(regionCode.uppercase())
    }

    // Internal TextFieldValue so we control the selection (cursor) explicitly.
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(value, selection = TextRange(value.length)))
    }

    // Re-sync internal state when the parent's value diverges from ours — e.g. async
    // prefill or programmatic clear. Preserves cursor position when text is unchanged.
    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            fieldValue = TextFieldValue(value, selection = TextRange(value.length))
        }
    }

    // Re-format current text when region changes mid-edit (iOS .onChange(of: regionCode)).
    LaunchedEffect(regionCode) {
        if (fieldValue.text.isNotEmpty()) {
            val reformatted = formatPhoneNumber(formatter, fieldValue.text)
            if (reformatted != fieldValue.text) {
                fieldValue = TextFieldValue(reformatted, selection = TextRange(reformatted.length))
                onValueChange(reformatted)
            }
        }
    }

    val showError = error != null && !compactError
    val theme = LocalFrameTheme.current

    val handleChange: (TextFieldValue) -> Unit = { incoming ->
        // 1. Compute how many digit/plus chars precede the incoming cursor in the
        //    user's edited (pre-format) string. That's the position we want to
        //    preserve in the final formatted output.
        val cursor = incoming.selection.end
        val digitsBeforeCursor = countInputCharsUpTo(incoming.text, cursor)

        // 2. Run the formatter over the full input to produce the new display string.
        val formatted = formatPhoneNumber(formatter, incoming.text)

        // 3. Translate the digit-count back to a character offset in the formatted
        //    string so the cursor lands right after the most recently typed digit.
        val newCursor = cursorAfterNDigits(formatted, digitsBeforeCursor)

        fieldValue = TextFieldValue(
            text = formatted,
            selection = TextRange(newCursor.coerceIn(0, formatted.length))
        )
        onValueChange(formatted)
        if (error != null) onClearError?.invoke()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedTextField(
                value = fieldValue,
                onValueChange = handleChange,
                placeholder = { Text(prompt) },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                singleLine = true,
                isError = showError,
                textStyle = theme.fonts.body,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }
        if (showError) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = error!!,
                style = theme.fonts.caption,
                color = theme.colors.error,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}
