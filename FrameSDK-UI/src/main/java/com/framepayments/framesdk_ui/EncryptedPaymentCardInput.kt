package com.framepayments.framesdk_ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import com.evervault.sdk.input.model.card.PaymentCardData
import com.evervault.sdk.input.ui.card.RowsPaymentCard
import com.framepayments.framesdk_ui.databinding.ViewEncryptedPaymentCardInputBinding

/**
 * Reusable Evervault-encrypted payment card input view, aligned with iOS [EncryptedPaymentCardInput].
 * Use this in any screen that needs card entry (checkout, add payment method, etc.).
 *
 * Set [onCardDataChange] to receive card data updates for validation or submission.
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

    init {
        binding.evervaultCardCompose.setContent {
            MaterialTheme {
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
