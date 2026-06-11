package com.framepayments.framesdk_ui.theme

import androidx.compose.ui.graphics.toArgb
import com.framepayments.framesdk_ui.FrameCartAppearance

/**
 * Bridges a [FrameTheme] (Compose-side source of truth) to a [FrameCartAppearance]
 * for the Views-based [com.framepayments.framesdk_ui.FrameCartView] /
 * [com.framepayments.framesdk_ui.FrameCheckoutView].
 *
 * Only fields that map cleanly from the cross-platform theme tokens are populated.
 * Use [overlay] to layer Views-only customizations (titles, sizes, typefaces) on top.
 */
/**
 * Converts this [FrameTheme] to a [FrameCartAppearance] and merges an optional [overlay].
 *
 * Only token fields that map cleanly from the cross-platform theme are populated. Use [overlay]
 * to layer Views-only customizations (titles, text sizes, typefaces) on top; overlay values
 * win on conflict.
 *
 * @param overlay Optional appearance whose non-null fields override the theme-derived base.
 * @return A [FrameCartAppearance] suitable for passing to [com.framepayments.framesdk_ui.FrameCartView.configure].
 */
fun FrameTheme.toCartAppearance(overlay: FrameCartAppearance? = null): FrameCartAppearance {
    val base = FrameCartAppearance(
        backgroundColor = colors.surface.toArgb(),
        cartTitleColor = colors.textPrimary.toArgb(),
        subtitleColor = colors.textSecondary.toArgb(),
        cartItemColor = colors.textPrimary.toArgb(),
        cartItemBackgroundColor = colors.surface.toArgb(),
        auxiliaryLabelColor = colors.textSecondary.toArgb(),
        totalColor = colors.textPrimary.toArgb(),
        checkoutButtonBackgroundColor = colors.primaryButton.toArgb(),
        checkoutButtonTextColor = colors.primaryButtonText.toArgb(),
    )
    if (overlay == null) return base
    return base.copy(
        backgroundColor = overlay.backgroundColor ?: base.backgroundColor,
        cartTitle = overlay.cartTitle ?: base.cartTitle,
        cartTitleColor = overlay.cartTitleColor ?: base.cartTitleColor,
        cartTitleSizeSp = overlay.cartTitleSizeSp ?: base.cartTitleSizeSp,
        cartTitleTypeface = overlay.cartTitleTypeface,
        subtitle = overlay.subtitle ?: base.subtitle,
        subtitleColor = overlay.subtitleColor ?: base.subtitleColor,
        subtitleSizeSp = overlay.subtitleSizeSp ?: base.subtitleSizeSp,
        cartItemColor = overlay.cartItemColor ?: base.cartItemColor,
        cartItemSizeSp = overlay.cartItemSizeSp ?: base.cartItemSizeSp,
        cartItemBackgroundColor = overlay.cartItemBackgroundColor ?: base.cartItemBackgroundColor,
        cartItemHeightPx = overlay.cartItemHeightPx ?: base.cartItemHeightPx,
        auxiliaryLabelColor = overlay.auxiliaryLabelColor ?: base.auxiliaryLabelColor,
        auxiliaryLabelSizeSp = overlay.auxiliaryLabelSizeSp ?: base.auxiliaryLabelSizeSp,
        totalColor = overlay.totalColor ?: base.totalColor,
        totalSizeSp = overlay.totalSizeSp ?: base.totalSizeSp,
        totalTypeface = overlay.totalTypeface,
        checkoutButtonTitle = overlay.checkoutButtonTitle ?: base.checkoutButtonTitle,
        checkoutButtonBackgroundColor = overlay.checkoutButtonBackgroundColor ?: base.checkoutButtonBackgroundColor,
        checkoutButtonTextColor = overlay.checkoutButtonTextColor ?: base.checkoutButtonTextColor,
        checkoutButtonTextSizeSp = overlay.checkoutButtonTextSizeSp ?: base.checkoutButtonTextSizeSp,
    )
}
