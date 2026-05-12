package com.framepayments.frame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.framepayments.framesdk_ui.FrameCartView
import com.framepayments.framesdk_ui.FrameCartItem

class CartTestActivity : BaseActivity() {

    private val checkoutLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        val items = listOf(
            FrameCartItem("1", "Coffee Mug", 1299, "https://m.media-amazon.com/images/I/61NWeN3zY1L._AC_UF894,1000_QL80_.jpg"),
            FrameCartItem("2", "T-Shirt", 2599, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTP2BAKInCCh3PZ5BwPdCBOk1v92vBLOgsgVw&s")
        )

        val accountId = "INSERT_SANDBOX_ACCOUNT_ID"

        val cartView = FrameCartView(this).apply {
            configure(
                accountId = accountId,
                items = items,
                shippingCents = 500,
                onCheckout = { total: Int ->
                    val intent = Intent(this@CartTestActivity, CheckoutActivity::class.java).apply {
                        putExtra("totalCents", total)
                        putExtra("accountId", accountId)
                    }
                    checkoutLauncher.launch(intent)
                }
            )
            // Uncomment to set the demo theme
//            setTheme(demoTheme(this@CartTestActivity))
        }

        setContentView(cartView)
    }
}
