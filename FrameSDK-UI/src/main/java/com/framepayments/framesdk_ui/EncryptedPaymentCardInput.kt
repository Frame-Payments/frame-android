package com.framepayments.framesdk_ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.evervault.sdk.input.model.card.PaymentCardData
import com.evervault.sdk.input.ui.card.RowsPaymentCard
import com.framepayments.framesdk_ui.databinding.ViewEncryptedPaymentCardInputBinding

/**
 * Reusable Evervault-encrypted payment card input view, aligned with iOS [EncryptedPaymentCardInput].
 * Use this in any screen that needs card entry (checkout, add payment method, etc.).
 *
 * Set [onCardDataChange] to receive card data updates for validation or submission.
 *
 * Set [accentColor] to override the cursor / focus / label tint inside the embedded
 * Evervault card input. Defaults to [DEFAULT_ACCENT_COLOR] (Frame's brand teal) so the
 * input doesn't bleed Material 2's default purple into apps that use a non-purple
 * accent. iOS achieves the same via SwiftUI tint inheritance.
 */
class EncryptedPaymentCardInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewEncryptedPaymentCardInputBinding =
        ViewEncryptedPaymentCardInputBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     * Callback invoked when the user modifies card data. Use for validation or to pass
     * [PaymentCardData] to your payment flow (e.g. [FrameCheckoutViewModel] or PaymentMethodsAPI).
     */
    var onCardDataChange: ((PaymentCardData) -> Unit)? = null

    /**
     * Accent color for the cursor / focus indicator / labels inside the Evervault input.
     * Set this before the view is first laid out (e.g. immediately after construction).
     * Re-setting after attach updates the next composition.
     */
    var accentColor: Color = DEFAULT_ACCENT_COLOR
        set(value) {
            field = value
            applyContent()
        }

    companion object {
        /**
         * Frame's brand teal — kept in sync with `frameonboarding.theme.FramePrimaryColor`.
         * Duplicated here because FrameSDK-UI does not depend on FrameSDK-Onboarding.
         */
        val DEFAULT_ACCENT_COLOR: Color = Color(0xFF324D52)
    }

    init {
        applyContent()
    }

    private fun applyContent() {
        binding.evervaultCardCompose.setContent {
            // Evervault's RowsPaymentCard reads from Material 3's MaterialTheme, so we
            // override the primary slot in a Material 3 ColorScheme — Material 2's
            // lightColors() does nothing here.
            val scheme = lightColorScheme(
                primary = accentColor,
                onPrimary = Color.White,
                secondary = accentColor,
                onSecondary = Color.White,
                tertiary = accentColor
            )
            MaterialTheme(colorScheme = scheme) {
                RowsPaymentCard(
                    modifier = Modifier.fillMaxWidth(),
                    onDataChange = { data ->
                        onCardDataChange?.invoke(data)
                    }
                )
            }
        }
    }
}
