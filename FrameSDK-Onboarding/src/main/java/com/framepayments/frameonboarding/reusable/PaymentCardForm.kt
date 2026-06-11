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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk_ui.theme.LocalFrameTheme

/**
 * A card number, expiry, and CVC input form for entering payment card details manually.
 *
 * @param cardNumber Current card number value (digits only, no spaces stored internally).
 * @param onCardNumberChange Called when the card number changes.
 * @param expiryMonth Two-digit expiry month.
 * @param expiryYear Two-digit expiry year.
 * @param onExpiryChange Called when either expiry component changes.
 * @param cvc Current CVC value.
 * @param onCvcChange Called when the CVC changes.
 * @param headerTitle Label displayed above the form when [showHeader] is true.
 * @param showHeader Whether to show the section header label.
 */
@Composable
fun PaymentCardForm(
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    expiryMonth: String,
    expiryYear: String,
    onExpiryChange: (month: String, year: String) -> Unit,
    cvc: String,
    onCvcChange: (String) -> Unit,
    headerTitle: String = "Card Details",
    showHeader: Boolean = true
) {
    val theme = LocalFrameTheme.current
    Column {
        if (showHeader) {
            Text(
                text = headerTitle,
                style = theme.fonts.label,
                color = theme.colors.textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        OutlinedTextField(
            value = cardNumber,
            onValueChange = { value ->
                val cleaned = value.replace(" ", "")
                if (cleaned.length <= 16) {
                    onCardNumberChange(cleaned.chunked(4).joinToString(" "))
                }
            },
            label = { Text("Card Number") },
            placeholder = { Text("4242 4242 4242 4242") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = theme.fonts.body,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = if (expiryMonth.isEmpty() && expiryYear.isEmpty()) "" else "$expiryMonth/$expiryYear",
                onValueChange = { value ->
                    val cleaned = value.replace("/", "").replace(Regex("[^0-9]"), "")
                    if (cleaned.length <= 4) {
                        if (cleaned.length >= 2) {
                            onExpiryChange(cleaned.take(2), cleaned.drop(2))
                        } else {
                            onExpiryChange(cleaned, "")
                        }
                    }
                },
                label = { Text("MM/YY") },
                placeholder = { Text("MM/YY") },
                modifier = Modifier.weight(1f),
                textStyle = theme.fonts.body,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = cvc,
                onValueChange = { value ->
                    if (value.length <= 4 && value.all { it.isDigit() }) {
                        onCvcChange(value)
                    }
                },
                label = { Text("CVC") },
                placeholder = { Text("CVC") },
                modifier = Modifier.weight(1f),
                textStyle = theme.fonts.body,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}
