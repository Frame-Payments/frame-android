package com.framepayments.frame

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.framepayments.framesdk_ui.theme.FrameTheme

fun demoTheme(context: Context): FrameTheme {
    val base = FrameTheme.default(context)
    return base.copy(
        colors = base.colors.copy(
            primaryButton = Color(0xFFFF6B00),
            primaryButtonText = Color.White,
        )
    )
}

@Composable
fun rememberDemoTheme(): FrameTheme = demoTheme(LocalContext.current)
