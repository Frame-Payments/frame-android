package com.framepayments.framesdk_ui.buttons

import android.content.Context
import android.util.AttributeSet
import com.framepayments.framesdk_ui.R
import com.google.android.material.button.MaterialButton

class GooglePayButton @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : MaterialButton(ctx, attrs, com.google.android.material.R.attr.materialButtonOutlinedStyle) {

    init {
        setIconResource(R.drawable.ic_google_pay)
        iconPadding = 0
        iconGravity  = ICON_GRAVITY_TEXT_START
        text = ""
        strokeWidth = 0
        cornerRadius = 10
        setBackgroundColor(resources.getColor(android.R.color.black, null))
    }
}