package com.framepayments.frame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.framepayments.framesdk.FrameNetworking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FrameNetworking.initializeWithAPIKey(
            context = applicationContext,
            key = "INSERT_SANDBOX_KEY_HERE",
            publishableKey = "pk_sandbox_WD88KqgcVPkwoFpSB5uH9F6x",
            debug = true
        )
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PlaygroundScreen()
                }
            }
        }
    }
}
