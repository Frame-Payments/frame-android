package com.framepayments.frame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.framepayments.frameonboarding.viewmodels.FrameOnboarding
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.classes.OnboardingResult

class OnboardingDemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FrameOnboarding(
                config = OnboardingConfig(
                    customerId = "cus_123"
                ),
                onResult = { result ->
                    when (result) {
                        is OnboardingResult.Completed -> finish() // for now
                        OnboardingResult.Cancelled -> finish()
                    }
                }
            )
        }
    }
}