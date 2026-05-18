package com.framepayments.framesdk_ui.snackbar

import android.view.View
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.coroutineScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Process-wide channel for transport-error messages surfaced by Frame UI surfaces.
 *
 * UI sites (Google Pay button, bundled checkout, etc.) call [emit] when a [com.framepayments.framesdk.NetworkingError]
 * with `isTransport == true` occurs. The currently presented modal listens via [observeWithSnackbar] (XML
 * Snackbar host) or [SnackbarBridge] (Compose Scaffold/SnackbarHostState). Server-validation errors
 * keep their existing inline UX and should not be routed through this controller.
 *
 * Internal-by-convention (only Frame SDK code emits); public for cross-module access.
 */
object FrameSnackbarController {
    private val _events = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val events: SharedFlow<String> = _events.asSharedFlow()

    fun emit(message: String) {
        _events.tryEmit(message)
    }

    /**
     * Wires the controller's events to a Material Snackbar anchored at [anchorView]. Intended for
     * XML hosts (e.g. [com.framepayments.framesdk_ui.FrameCheckoutView]). The collection stops when
     * [lifecycleOwner]'s lifecycle reaches [Lifecycle.State.DESTROYED].
     *
     * Color suppliers are evaluated at emission time, so a host that calls
     * `FrameCheckoutView.setTheme(...)` after construction sees the new colors on the very next
     * toast — no re-observation needed.
     */
    fun observeWithSnackbar(
        lifecycleOwner: LifecycleOwner,
        anchorView: View,
        backgroundColor: () -> Int,
        textColor: () -> Int,
    ) {
        val scope = lifecycleOwner.lifecycle.coroutineScope
        val job = scope.launch {
            _events.collect { message ->
                Snackbar.make(anchorView, message, 4000)
                    .setBackgroundTint(backgroundColor())
                    .setTextColor(textColor())
                    .show()
            }
        }
        lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) job.cancel()
        })
    }
}

/**
 * Compose-side hookup: bridges [FrameSnackbarController] events into the given [hostState]. Drop
 * this inside an existing [androidx.compose.material3.Scaffold] (the onboarding container has one)
 * so all Frame surfaces share a single snackbar pattern.
 */
@Composable
fun SnackbarBridge(hostState: SnackbarHostState) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, hostState) {
        val job = lifecycleOwner.lifecycle.coroutineScope.launch {
            FrameSnackbarController.events.collect { message ->
                hostState.showSnackbar(message, duration = SnackbarDuration.Short)
            }
        }
        onDispose { job.cancel() }
    }
}
