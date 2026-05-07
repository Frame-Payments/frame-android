package com.framepayments.frameonboarding.reusable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/// Mirrors the "Or" divider used in the checkout layout to separate wallet payment options
/// (Google Pay) from the card form. Drop between the wallet button and the card section.
@Composable
fun PaymentDivider(
    modifier: Modifier = Modifier,
    label: String = "Or"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(label, style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}
