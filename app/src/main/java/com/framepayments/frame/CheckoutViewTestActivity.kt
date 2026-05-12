package com.framepayments.frame

import android.os.Bundle
import android.widget.FrameLayout
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
            ) { transferId ->
                println("Checkout Completed: $transferId")
                setResult(RESULT_OK)
                finish()
            }
            setTheme(demoTheme(this@CheckoutActivity))
        }

        rootLayout.addView(frameCheckoutView)
        setContentView(rootLayout)
    }
}
