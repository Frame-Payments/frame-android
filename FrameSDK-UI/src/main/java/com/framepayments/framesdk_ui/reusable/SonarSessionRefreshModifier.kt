package com.framepayments.framesdk_ui.reusable

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.framepayments.framesdk.FrameNetworking

/**
 * Records a Sonar device event when this composable is presented — onboarding, checkout, or a
 * standalone payment element. Mirrors iOS's `View.refreshesSonarSession(accountId:)`: the web SDK
 * writes the session once per page load, and presenting one of these entry points is the native
 * equivalent moment.
 *
 * Runs in a [LaunchedEffect] so presentation is never blocked and a failure never surfaces here —
 * the payment path still calls the SDK's own session check before charging.
 *
 * @param accountId The Frame account the flow belongs to, when known. Pass `null` before an
 *   account exists so the event lands on the pre-account session that later gets adopted.
 */
fun Modifier.refreshesSonarSession(accountId: String? = null): Modifier = composed {
    LaunchedEffect(accountId) {
        val manager = FrameNetworking.sonarSessionManagerOrNull() ?: return@LaunchedEffect
        try {
            manager.refreshOnFlowEntry(accountId)
        } catch (_: Exception) {
            // Fire-and-forget: failures here don't block presentation.
        }
    }
    this
}
