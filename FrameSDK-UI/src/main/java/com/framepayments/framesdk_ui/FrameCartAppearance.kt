package com.framepayments.framesdk_ui

import android.graphics.Typeface

/**
 * Appearance customization for [FrameCartView], aligned with iOS FrameCartView options.
 * All properties are optional; only set values you want to override from defaults.
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
