package com.framepayments.framesdk.accountevents

import com.framepayments.framesdk.FrameNetworking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Builds and queues account/diagnostic events for the backend's merchant dashboard.
 *
 * There is no integrator-facing emit API — every call site is SDK-owned instrumentation of a
 * moment the SDK already knows about.
 */
object AccountEventEmitter {
    /** The queue events are enqueued onto. Overridable so tests can inject a queue with a recording flush handler. */
    var queue: AccountEventQueue = AccountEventQueue.shared

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Queues an event for the account this app run belongs to.
     *
     * Silently does nothing if no [FrameNetworking.accountId] was supplied to
     * [FrameNetworking.initializeWithAPIKey] — there is no account to attribute the event to,
     * and, per the queue's contract, this must never surface an error or block the caller.
     *
     * @param detail Optional developer-facing detail, either a fixed [AccountEventDetail]
     *   constant or a dynamic debug description (e.g. `"$error"`). Must never contain PII,
     *   a PAN, a CVV, or a raw customer identifier — use a debug description, never
     *   user-facing copy.
     */
    fun emit(name: AccountEventName, screen: AccountEventScreen, detail: String? = null) {
        val accountId = FrameNetworking.accountId ?: return

        val event = AccountEventsRequests.Event(
            accountId = accountId,
            name = name.apiValue,
            screen = screen.apiValue,
            platform = FrameNetworking.eventPlatform,
            sdkVersion = FrameNetworking.CURRENT_VERSION,
            hostSdkVersion = FrameNetworking.hostSDKVersion,
            occurredAt = Instant.now().toString(),
            detail = detail
        )

        scope.launch { queue.enqueue(event) }
    }
}
