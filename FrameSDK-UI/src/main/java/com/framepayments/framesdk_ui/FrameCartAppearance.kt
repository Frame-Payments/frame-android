package com.framepayments.framesdk_ui

import android.graphics.Typeface

/**
 * Appearance customization for [FrameCartView], aligned with iOS `FrameCartView` options.
 * All properties are optional; only set values you want to override from defaults.
 *
 * @property backgroundColor ARGB background color of the cart container. `null` uses the default.
 * @property cartTitle Override title text. Defaults to `"Your Cart"`.
 * @property cartTitleColor ARGB text color for the cart title. `null` uses the default.
 * @property cartTitleSizeSp Font size in SP for the cart title. `null` uses the default.
 * @property cartTitleTypeface Typeface style constant (e.g. [Typeface.BOLD]) for the cart title.
 * @property subtitle Optional subtitle displayed below the cart title. Hidden when `null`.
 * @property subtitleColor ARGB text color for the subtitle. `null` uses the default.
 * @property subtitleSizeSp Font size in SP for the subtitle. `null` uses the default.
 * @property cartItemColor ARGB text color for each line-item row. `null` uses the default.
 * @property cartItemSizeSp Font size in SP for line-item text. `null` uses the default.
 * @property cartItemBackgroundColor ARGB background color for each line-item row. `null` uses the default.
 * @property cartItemHeightPx Fixed height in pixels for each line-item row. `null` uses wrap-content.
 * @property auxiliaryLabelColor ARGB text color for subtotal and shipping labels. `null` uses the default.
 * @property auxiliaryLabelSizeSp Font size in SP for subtotal and shipping labels. `null` uses the default.
 * @property totalColor ARGB text color for the total row. `null` uses the default.
 * @property totalSizeSp Font size in SP for the total row. `null` uses the default.
 * @property totalTypeface Typeface style constant for the total row. Defaults to [Typeface.BOLD].
 * @property checkoutButtonTitle Override label for the checkout button. Defaults to `"Checkout"`.
 * @property checkoutButtonBackgroundColor ARGB background color for the checkout button. `null` uses the theme primary.
 * @property checkoutButtonTextColor ARGB text color for the checkout button. `null` uses the default.
 * @property checkoutButtonTextSizeSp Font size in SP for the checkout button label. `null` uses the default.
 */
data class FrameCartAppearance(
    val backgroundColor: Int? = null,
    val cartTitle: String? = null,
    val cartTitleColor: Int? = null,
    val cartTitleSizeSp: Float? = null,
    val cartTitleTypeface: Int = Typeface.NORMAL,
    val subtitle: String? = null,
    val subtitleColor: Int? = null,
    val subtitleSizeSp: Float? = null,
    val cartItemColor: Int? = null,
    val cartItemSizeSp: Float? = null,
    val cartItemBackgroundColor: Int? = null,
    val cartItemHeightPx: Int? = null,
    val auxiliaryLabelColor: Int? = null,
    val auxiliaryLabelSizeSp: Float? = null,
    val totalColor: Int? = null,
    val totalSizeSp: Float? = null,
    val totalTypeface: Int = Typeface.BOLD,
    val checkoutButtonTitle: String? = null,
    val checkoutButtonBackgroundColor: Int? = null,
    val checkoutButtonTextColor: Int? = null,
    val checkoutButtonTextSizeSp: Float? = null
)
