package com.framepayments.frame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.framepayments.framesdk.FrameNetworking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FrameNetworking.initializeWithAPIKey(
            context = applicationContext,
            secretKey = "INSERT_SANDBOX_KEY_HERE",
            publishableKey = "INSERT_PUBLISHABLE_KEY_HERE",
            debug = true
        )
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    PlaygroundScreen()
                }
            }
        }
    }
}
