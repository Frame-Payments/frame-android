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
        // Supply sandbox keys via local.properties / BuildConfig before running the example.
        // Do not commit real keys.
        FrameNetworking.initializeWithAPIKey(
            context = applicationContext,
            secretKey = "INSERT_SANDBOX_SECRET_KEY",
            publishableKey = "INSERT_SANDBOX_PUBLISHABLE_KEY",
            googlePayMerchantId = "BCR2DN4T_TEST_STUB",
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
