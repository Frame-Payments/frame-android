package com.framepayments.framesdk_ui.reusable

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk_ui.R
import com.framepayments.framesdk_ui.theme.LocalFrameTheme

/**
 * Renders a single selectable payment method row: brand or bank icon, masked account number,
 * and expiration or account-type details. Shows a selection indicator when [isSelected].
 *
 * @param paymentMethod The payment method whose details are rendered.
 * @param isSelected Pass true to render the row in its selected state.
 * @param onTap Called when the user taps the row.
 */
@Composable
fun FramePaymentMethodRow(
    paymentMethod: FrameObjects.PaymentMethod,
    isSelected: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalFrameTheme.current
    val isAch = paymentMethod.type == FrameObjects.PaymentMethodType.ACH

    val icon = if (isAch) R.drawable.ic_bank else cardBrandIcon(paymentMethod.card?.brand.orEmpty())
    val primaryText = if (isAch) {
        "•••• ${paymentMethod.ach?.lastFour.orEmpty()}"
    } else {
        "•••• ${paymentMethod.card?.lastFourDigits.orEmpty()}"
    }
    val secondaryText = if (isAch) {
        "${paymentMethod.ach?.accountType?.name?.lowercase()?.replaceFirstChar(Char::uppercase).orEmpty()} Account"
    } else {
        "Exp. ${paymentMethod.card?.expirationMonth.orEmpty()}/${paymentMethod.card?.expirationYear.orEmpty()}"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTap),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(theme.radii.medium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(text = primaryText, style = theme.fonts.bodySmall, color = theme.colors.textPrimary)
                Spacer(Modifier.width(1.dp))
                Text(text = secondaryText, style = theme.fonts.caption, color = theme.colors.textSecondary)
            }
            RadioButton(selected = isSelected, onClick = onTap)
        }
    }
}
