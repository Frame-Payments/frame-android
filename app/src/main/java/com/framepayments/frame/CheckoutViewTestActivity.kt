package com.framepayments.frame

import android.os.Bundle
import android.widget.FrameLayout
import com.framepayments.framesdk.FrameResult
import com.framepayments.framesdk_ui.FrameCheckoutView

class CheckoutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        val accountId = intent.getStringExtra("accountId") ?: "default_account"
        val totalCents = intent.getIntExtra("totalCents", 0)

        val rootLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val frameCheckoutView = FrameCheckoutView(this).apply {
            configure(
                accountId = accountId,
                paymentAmount = totalCents
            ) { result ->
                when (result) {
                    is FrameResult.Completed -> {
                        println("Checkout Completed: ${result.id}")
                        setResult(RESULT_OK)
                        finish()
                    }
                    FrameResult.Cancelled -> {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                    is FrameResult.Failed -> {
                        println("Checkout Failed: ${result.error.message}")
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                }
            }
//            setTheme(demoTheme(this@CheckoutActivity))
        }

        rootLayout.addView(frameCheckoutView)
        setContentView(rootLayout)
    }
}
