package com.framepayments.frame

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.framepayments.framesdk_ui.FrameCheckoutView

class CheckoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        val customerId = intent.getStringExtra("customerId") ?: "default_customer"
        val totalCents = intent.getIntExtra("totalCents", 0)

        val rootLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val frameCheckoutView = FrameCheckoutView(this).apply {
            configure(
                customerId = customerId,
                paymentAmount = totalCents
            ) { intent ->
                println("Checkout Completed: $intent")
            }
        }

        rootLayout.addView(frameCheckoutView)
        setContentView(rootLayout)
    }
}