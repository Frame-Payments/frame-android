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
import com.framepayments.frame.BuildConfig
import com.framepayments.framesdk.FrameNetworking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Sandbox keys live in local.properties (gitignored) and are baked into BuildConfig.
        val sandboxAccountId = BuildConfig.SANDBOX_ACCOUNT_ID
        FrameNetworking.initializeWithAPIKey(
            context = applicationContext,
            secretKey = BuildConfig.SANDBOX_SECRET_KEY,
            publishableKey = BuildConfig.SANDBOX_PUBLISHABLE_KEY,
            // Optional: an existing account this run belongs to, used to attribute account
            // events. Leave empty if you're onboarding a new account instead.
            accountId = sandboxAccountId.takeIf { it.isNotEmpty() },
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
