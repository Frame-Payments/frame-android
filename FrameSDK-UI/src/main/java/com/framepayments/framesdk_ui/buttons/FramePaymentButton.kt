package com.framepayments.framesdk_ui.buttons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk_ui.R
import com.framepayments.framesdk_ui.theme.LocalFrameTheme

/**
 * Branded payment button — 1:1 port of iOS [FramePaymentButton]. Renders a wallet-provider
 * brand mark on a black or white pill, sized to match the surrounding [ContinueButton] for
 * visual consistency.
 *
 * Apple Pay is iOS-only; on Android, [PaymentButtonOption.GOOGLE] is the only valid value.
 * The enum is preserved for cross-platform API symmetry but `APPLE` is not exposed on Android.
 *
 * Note: this is a *styled* button. For a fully functional Google Pay flow with charge-intent
 * creation, use [FrameGooglePayButton] instead.
 */
/**
 * Wallet provider options for [FramePaymentButton].
 *
 * Only [GOOGLE] is supported on Android; the `APPLE` constant is omitted here to preserve
 * cross-platform API symmetry without shipping a non-functional option.
 */
enum class PaymentButtonOption {
    /** Renders the Google Pay brand mark. */
    GOOGLE
}

/**
 * Renders a styled wallet-provider button with the brand mark centered on a pill-shaped
 * black or white background.
 *
 * For a fully functional Google Pay flow with charge-intent creation, use
 * [FrameGooglePayButton] instead.
 *
 * @param paymentOption Wallet brand to display (default: [PaymentButtonOption.GOOGLE]).
 * @param blackButton When true renders a black pill; when false renders a white pill.
 * @param modifier Modifier applied to the outer button.
 * @param onClick Called when the customer taps the button.
 */
@Composable
fun FramePaymentButton(
    paymentOption: PaymentButtonOption = PaymentButtonOption.GOOGLE,
    blackButton: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = LocalFrameTheme.current
    val containerColor = if (blackButton) Color.Black else Color.White
    val brandTint = if (blackButton) Color.White else Color.Black

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(theme.radii.medium),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val drawable = when (paymentOption) {
                PaymentButtonOption.GOOGLE -> R.drawable.ic_google_pay
            }
            Icon(
                painter = painterResource(drawable),
                contentDescription = "Google Pay",
                tint = brandTint,
                modifier = Modifier.size(width = 50.dp, height = 30.dp)
            )
        }
    }
}
