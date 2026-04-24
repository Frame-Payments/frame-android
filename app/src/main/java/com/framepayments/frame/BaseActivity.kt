package com.framepayments.frame

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

abstract class BaseActivity : AppCompatActivity() {

    override fun setContentView(view: View?) {
        super.setContentView(view)
        view?.let { applySystemBarInsets(it) }
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        view?.let { applySystemBarInsets(it) }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        findViewById<View>(android.R.id.content)?.let { applySystemBarInsets(it) }
    }

    private fun applySystemBarInsets(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.updatePadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}
