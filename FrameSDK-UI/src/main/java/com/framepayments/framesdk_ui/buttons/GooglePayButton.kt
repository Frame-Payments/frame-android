package com.framepayments.framesdk_ui.buttons

import android.content.Context
import android.util.AttributeSet
import com.framepayments.framesdk_ui.R
import com.google.android.material.button.MaterialButton

/**
 * Minimal Google Pay icon button used internally by [FrameGooglePayButton].
 *
 * Renders the Google Pay vector asset on a black rounded rectangle with no label text.
 * Merchants should use [FrameGooglePayButton] for a fully functional Google Pay integration.
 */
class GooglePayButton @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : MaterialButton(ctx, attrs, com.google.android.material.R.attr.materialButtonOutlinedStyle) {

    init {
        setIconResource(R.drawable.ic_google_pay)
        iconPadding = 0
        iconTint = null
        iconGravity = ICON_GRAVITY_TEXT_START
        text = ""
        cornerRadius = 10
        setBackgroundColor(resources.getColor(android.R.color.black, null))
    }
}