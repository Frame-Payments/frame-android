package com.framepayments.framesdk_ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import com.framepayments.framesdk.chargeintents.ChargeIntent
import com.framepayments.framesdk.chargeintents.FrameThreeDSecureChallengePresenting
import com.framepayments.framesdk.chargeintents.FrameThreeDSecureChallengeResult
import com.framepayments.framesdk.chargeintents.UseFrameSDK
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Presents an issuer-controlled 3D Secure challenge in a modal [WebView].
 *
 * The page is served by the card network, so the code the cardholder enters never reaches
 * this app. This reports only that the challenge finished; the Frame API decides whether the
 * charge succeeded.
 */
class FrameThreeDSecureChallengePresenter(private val context: Context) : FrameThreeDSecureChallengePresenting {

    /** Holds the redirect path the challenge webview is watched for. */
    companion object {
        /**
         * Matches `GET /v1/evervault/3ds/callback/:id`, which answers an empty 200 — it is a
         * signal to stop, not a page.
         */
        private const val CALLBACK_PATH_COMPONENT = "/evervault/3ds/callback"
    }

    override suspend fun presentChallenge(challenge: UseFrameSDK, intent: ChargeIntent): FrameThreeDSecureChallengeResult {
        val challengeUrl = challenge.challengeUrl ?: return FrameThreeDSecureChallengeResult.UNAVAILABLE
        val activity = context as? Activity
        if (activity != null && (activity.isFinishing || activity.isDestroyed)) {
            return FrameThreeDSecureChallengeResult.UNAVAILABLE
        }
        return present(challengeUrl)
    }

    private suspend fun present(challengeUrl: String): FrameThreeDSecureChallengeResult =
        suspendCancellableCoroutine { continuation ->
            var resumed = false
            lateinit var dialog: Dialog

            fun finish(result: FrameThreeDSecureChallengeResult) {
                if (resumed) return
                resumed = true
                dialog.dismiss()
                continuation.resume(result)
            }

            val webView = WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        if (!request.url.toString().contains(CALLBACK_PATH_COMPONENT)) return false
                        finish(FrameThreeDSecureChallengeResult.COMPLETED)
                        return true
                    }

                    override fun onReceivedError(view: WebView, errorCode: Int, description: String?, failingUrl: String?) {
                        if (failingUrl?.contains(CALLBACK_PATH_COMPONENT) == true) return
                        finish(FrameThreeDSecureChallengeResult.UNAVAILABLE)
                    }
                }
            }

            val toolbar = Toolbar(context).apply {
                setBackgroundColor(Color.WHITE)
                setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
                setNavigationOnClickListener { finish(FrameThreeDSecureChallengeResult.FAILED) }
            }

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(toolbar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                addView(webView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }

            dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
                setContentView(layout)
                setCancelable(true)
                setOnCancelListener { finish(FrameThreeDSecureChallengeResult.FAILED) }
            }

            continuation.invokeOnCancellation { dialog.dismiss() }

            webView.loadUrl(challengeUrl)
            dialog.show()
        }
}
