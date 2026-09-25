package com.framepayments.framesdk.accountevents

import com.framepayments.framesdk.FrameNetworking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Builds and queues account/diagnostic events for the backend's merchant dashboard.
 *
 * There is no integrator-facing emit API — every call site is SDK-owned instrumentation of a
 * moment the SDK already knows about.
 */
object AccountEventEmitter {
    /** The queue events are enqueued onto. Overridable so tests can inject a queue with a recording flush handler. */
    var queue: AccountEventQueue = AccountEventQueue.shared

    private const val MAX_PENDING = 200

    private val scope = CoroutineScope(Dispatchers.IO)
    private val pendingMutex = Mutex()
    private val pending = mutableListOf<PendingEvent>()

    private data class PendingEvent(
        val name: String,
        val screen: String,
        val occurredAt: String,
        val detail: String?
    )

    /**
     * Queues an event for the account this app run belongs to.
     *
     * A flow launched without an account ID (the common case — onboarding creates the account
     * mid-flow) buffers the event instead of dropping it; [onAccountIdResolved] flushes the
     * buffer once an account ID is known.
     *
     * @param detail Optional developer-facing detail, either a fixed [AccountEventDetail]
     *   constant or a dynamic debug description (e.g. `"$error"`). Must never contain PII,
     *   a PAN, a CVV, or a raw customer identifier — use a debug description, never
     *   user-facing copy.
     */
    fun emit(name: AccountEventName, screen: AccountEventScreen, detail: String? = null) {
        val occurredAt = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()

        // Must read accountId and decide buffer-vs-send under the same lock onAccountIdResolved
        // drains under, or a resolve landing in between can drop this event permanently.
        scope.launch {
            val accountId = pendingMutex.withLock {
                val resolved = FrameNetworking.accountId
                if (resolved == null) {
                    // occurredAt is captured before this coroutine is dispatched, so it reflects
                    // true emission order even though coroutines can acquire this lock out of
                    // that order — evict by it, not by buffer position, so overflow always drops
                    // the actually-oldest event.
                    if (pending.size >= MAX_PENDING) {
                        pending.remove(pending.minBy { it.occurredAt })
                    }
                    pending.add(PendingEvent(name.apiValue, screen.apiValue, occurredAt, detail))
                }
                resolved
            } ?: return@launch

            queue.enqueue(
                AccountEventsRequests.Event(
                    accountId = accountId,
                    name = name.apiValue,
                    screen = screen.apiValue,
                    platform = FrameNetworking.eventPlatform,
                    sdkVersion = FrameNetworking.CURRENT_VERSION,
                    hostSdkVersion = FrameNetworking.hostSDKVersion,
                    occurredAt = occurredAt,
                    detail = detail
                )
            )
        }
    }

    /** Flushes events buffered before [accountId] resolved, preserving each one's original `occurredAt`. */
    fun onAccountIdResolved(accountId: String) {
        scope.launch {
            // Held for the whole drain, not just the copy-and-clear, so a new emit() racing in
            // right after resolution can't enqueue its event ahead of the buffered ones — it
            // blocks on this same lock until the drain's enqueues below have gone out.
            pendingMutex.withLock {
                val taken = pending.toList()
                pending.clear()
                if (taken.isEmpty()) return@withLock
                taken.forEach { pendingEvent ->
                    queue.enqueue(
                        AccountEventsRequests.Event(
                            accountId = accountId,
                            name = pendingEvent.name,
                            screen = pendingEvent.screen,
                            platform = FrameNetworking.eventPlatform,
                            sdkVersion = FrameNetworking.CURRENT_VERSION,
                            hostSdkVersion = FrameNetworking.hostSDKVersion,
                            occurredAt = pendingEvent.occurredAt,
                            detail = pendingEvent.detail
                        )
                    )
                }
                queue.flush()
            }
        }
    }

    /** Test-only inspection of what is currently buffered, without triggering a flush. */
    suspend fun pendingEventNamesForTesting(): List<String> = pendingMutex.withLock { pending.map { it.name } }

    /** Test-only reset so buffer state doesn't leak between tests sharing this singleton. */
    suspend fun clearPendingForTesting() = pendingMutex.withLock { pending.clear() }
}
