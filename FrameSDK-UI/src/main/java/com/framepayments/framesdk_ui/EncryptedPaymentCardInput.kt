package com.framepayments.framesdk_ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import com.evervault.sdk.input.model.card.PaymentCardData
import com.evervault.sdk.input.ui.card.RowsPaymentCard
import com.framepayments.framesdk_ui.databinding.ViewEncryptedPaymentCardInputBinding
import com.framepayments.framesdk_ui.theme.FrameTheme

/**
 * Reusable Evervault-encrypted payment card input view, aligned with iOS [EncryptedPaymentCardInput].
 * Use this in any screen that needs card entry (checkout, add payment method, etc.).
 *
 * Set [onCardDataChange] to receive card data updates for validation or submission.
 *
 * Surface (background, stroke, corner radius) and embedded card-input tint colors are driven
 * by [FrameTheme]. Pass a custom theme via [setTheme] to override; otherwise defaults are read
 * from the `frame_*` color resources (which themselves participate in dark mode via
 * `values-night/colors.xml`). iOS achieves the equivalent via SwiftUI tint inheritance and
 * a `RoundedRectangle` styled with `theme.colors.surface` / `theme.colors.surfaceStroke`.
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

    private var surfaceColor: Color = colorRes(R.color.frame_surface)
    private var surfaceStrokeColor: Color = colorRes(R.color.frame_surface_stroke)
    private var textPrimaryColor: Color = colorRes(R.color.frame_text_primary)
    private var textSecondaryColor: Color = colorRes(R.color.frame_text_secondary)
    private var cornerRadiusDp: Float = 10f

    /**
     * Accent color for the cursor / focus indicator / labels inside the Evervault input.
     * Re-setting after attach updates the next composition.
     */
    var accentColor: Color = colorRes(R.color.frame_primary_button)
        set(value) {
            field = value
            applyContent()
        }

    /**
     * Text color used on top of the brand color (cursor labels, etc.). Defaults to
     * `frame_primary_button_text` so dark / light brand colors get a readable contrast.
     */
    var onAccentColor: Color = colorRes(R.color.frame_primary_button_text)
        set(value) {
            field = value
            applyContent()
        }

    /**
     * Apply a [FrameTheme]: drives surface color, surface stroke, corner radius, accent
     * color, and on-accent text color in one call. Equivalent to setting each property
     * individually from `theme.colors.*` and `theme.radii.medium`.
     */
    fun setTheme(theme: FrameTheme) {
        surfaceColor = theme.colors.surface
        surfaceStrokeColor = theme.colors.surfaceStroke
        textPrimaryColor = theme.colors.textPrimary
        textSecondaryColor = theme.colors.textSecondary
        cornerRadiusDp = theme.radii.medium.value
        accentColor = theme.colors.primaryButton
        onAccentColor = theme.colors.primaryButtonText
        applySurface()
        applyContent()
    }

    init {
        applySurface()
        applyContent()
    }

    private fun applySurface() {
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                cornerRadiusDp,
                resources.displayMetrics
            )
            setColor(surfaceColor.toArgb())
            setStroke(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics
                ).toInt(),
                surfaceStrokeColor.toArgb()
            )
        }
    }

    private fun applyContent() {
        binding.evervaultCardCompose.setContent {
            // Evervault's RowsPaymentCard reads from Material 3's MaterialTheme.
            // Build a ColorScheme keyed off `isSystemInDarkTheme()` so the embedded
            // card field text + cursor adapt with the rest of the SDK; surface and
            // onSurface are pulled from the FrameTheme tokens so customer overrides
            // (or the values-night dark variants) flow through to the input contents.
            val isDark = isSystemInDarkTheme()
            val scheme = if (isDark) {
                darkColorScheme(
                    primary = accentColor,
                    onPrimary = onAccentColor,
                    secondary = accentColor,
                    onSecondary = onAccentColor,
                    tertiary = accentColor,
                    surface = surfaceColor,
                    onSurface = textPrimaryColor,
                    onSurfaceVariant = textSecondaryColor,
                    background = surfaceColor,
                    onBackground = textPrimaryColor,
                )
            } else {
                lightColorScheme(
                    primary = accentColor,
                    onPrimary = onAccentColor,
                    secondary = accentColor,
                    onSecondary = onAccentColor,
                    tertiary = accentColor,
                    surface = surfaceColor,
                    onSurface = textPrimaryColor,
                    onSurfaceVariant = textSecondaryColor,
                    background = surfaceColor,
                    onBackground = textPrimaryColor,
                )
            }
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

    private fun colorRes(resId: Int): Color = Color(ContextCompat.getColor(context, resId))
}
