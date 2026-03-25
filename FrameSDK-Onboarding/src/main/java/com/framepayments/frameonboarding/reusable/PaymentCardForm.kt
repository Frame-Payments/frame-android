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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Reusable payment card form section.
 * Mirrors iOS PaymentCardDetailView.
 * Fields: Card Number, Expiry (MM/YY), CVC.
 */
@Composable
internal fun PaymentCardForm(
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
    Column {
        if (showHeader) {
            Text(
                text = headerTitle,
                style = MaterialTheme.typography.labelLarge,
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}
