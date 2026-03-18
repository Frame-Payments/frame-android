package com.framepayments.frame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.framepayments.frameonboarding.views.OnboardingContainerView
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.classes.OnboardingResult

class OnboardingDemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OnboardingContainerView(
                config = OnboardingConfig(
                    accountId = "acc_123",
                    requiredCapabilities = listOf(
                        Capabilities.KYC_PREFILL,
                        Capabilities.CARD_VERIFICATION,
                        Capabilities.BANK_ACCOUNT_VERIFICATION,
                        Capabilities.AGE_VERIFICATION
                    )
                ),
                onResult = { result ->
                    when (result) {
                        is OnboardingResult.Completed -> finish()
                        OnboardingResult.Cancelled -> finish()
                        is OnboardingResult.Error -> finish()
                    }
                }
            )
        }
    }
}