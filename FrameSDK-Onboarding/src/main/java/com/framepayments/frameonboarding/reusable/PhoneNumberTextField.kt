package com.framepayments.frameonboarding.reusable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.google.i18n.phonenumbers.AsYouTypeFormatter
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.concurrent.ConcurrentHashMap

private val formatterCache = ConcurrentHashMap<String, AsYouTypeFormatter>()

private fun formatterFor(regionCode: String): AsYouTypeFormatter {
    val key = regionCode.uppercase()
    return formatterCache.getOrPut(key) {
        PhoneNumberUtil.getInstance().getAsYouTypeFormatter(key)
    }
}

private fun formatPhoneNumber(raw: String, regionCode: String): String {
    val formatter = formatterFor(regionCode)
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
    // Re-format when the region changes mid-edit (matches iOS .onChange(of: regionCode)).
    LaunchedEffect(regionCode) {
        if (value.isNotEmpty()) {
            val reformatted = formatPhoneNumber(value, regionCode)
            if (reformatted != value) onValueChange(reformatted)
        }
    }

    ValidatedTextField(
        value = value,
        onValueChange = { newValue ->
            onValueChange(formatPhoneNumber(newValue, regionCode))
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
