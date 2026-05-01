package com.framepayments.frameonboarding.reusable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
 * Phone number text field with region-aware live formatting via libphonenumber's
 * AsYouTypeFormatter. 1:1 port of iOS PhoneNumberTextField (PhoneNumberKit-backed).
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

    // Re-format when the region changes mid-edit (matches iOS .onChange(of: regionCode)).
    LaunchedEffect(regionCode) {
        if (value.isNotEmpty()) {
            val reformatted = formatPhoneNumber(formatter, value)
            if (reformatted != value) onValueChange(reformatted)
        }
    }

    ValidatedTextField(
        value = value,
        onValueChange = { newValue ->
            onValueChange(formatPhoneNumber(formatter, newValue))
        },
        prompt = prompt,
        error = error,
        modifier = modifier,
        keyboardType = KeyboardType.Phone,
        compactError = compactError,
        inlineError = false,
        onClearError = onClearError
    )
}
