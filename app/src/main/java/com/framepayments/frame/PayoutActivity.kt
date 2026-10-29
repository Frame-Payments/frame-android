package com.framepayments.frame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.framepayments.frameonboarding.views.FramePayoutView
import com.framepayments.framesdk.FrameResult
import com.framepayments.framesdk.payoutsessions.FramePayoutClientSecret

/**
 * Example path for payout: host-minted `pyt_sess_`, then [FramePayoutView].
 *
 * The host mints the session with the withdrawable balance as the amount cap.
 */
class PayoutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        val accountId = intent.getStringExtra("accountId").orEmpty()
        val clientSecret = intent.getStringExtra("payoutClientSecret").orEmpty()
        val expiresAt = intent.getLongExtra("payoutClientSecretExpiresAt", 0L)
        val amountCents = intent.getIntExtra("payoutAmountCents", 0)
        val amountCurrency = intent.getStringExtra("payoutAmountCurrency") ?: "usd"
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    FramePayoutView(
                        accountId = accountId,
                        clientSecret = FramePayoutClientSecret(
                            clientSecret = clientSecret,
                            expiresAt = expiresAt,
                            amountCents = amountCents,
                            amountCurrency = amountCurrency,
                        ),
                    ) { result ->
                        val data = Intent().putExtra("accountId", accountId)
                        when (result) {
                            is FrameResult.Completed -> {
                                data.putExtra("transferId", result.id)
                                setResult(Activity.RESULT_OK, data)
                            }
                            is FrameResult.Cancelled -> setResult(Activity.RESULT_CANCELED, data)
                            is FrameResult.Failed -> {
                                data.putExtra("errorMessage", result.error.message)
                                setResult(Activity.RESULT_CANCELED, data)
                            }
                        }
                        finish()
                    }
                }
            }
        }
    }
}
