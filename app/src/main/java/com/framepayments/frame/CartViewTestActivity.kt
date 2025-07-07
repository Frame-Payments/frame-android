package com.framepayments.frame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.framepayments.framesdk_ui.FrameCartView
import com.framepayments.framesdk_ui.FrameCartItem

class CartTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // Build dummy data
        val items = listOf(
            FrameCartItem("1", "Coffee Mug", 1299, "https://m.media-amazon.com/images/I/61NWeN3zY1L._AC_UF894,1000_QL80_.jpg"),
            FrameCartItem("2", "T-Shirt", 2599, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTP2BAKInCCh3PZ5BwPdCBOk1v92vBLOgsgVw&s")
        )
        val customerId = "0"

        val cartView = FrameCartView(this).apply {
            configure(
                customerId = customerId,
                items = items,
                shippingCents = 500
            ) { total ->
                val intent = android.content.Intent(this@CartTestActivity, CheckoutActivity::class.java).apply {
                    putExtra("totalCents", total)
                    putExtra("customerId", customerId)
                }
                startActivity(intent)
            }
        }

        setContentView(cartView)
    }
}