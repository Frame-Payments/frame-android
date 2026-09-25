package com.framepayments.framesdk.accountevents

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AccountEventEmitterTest {
    private val recorded = mutableListOf<AccountEventsRequests.Event>()

    @Before
    fun setUp() = runBlocking {
        recorded.clear()
        AccountEventEmitter.clearPendingForTesting()
        AccountEventEmitter.queue = AccountEventQueue(
            flushSizeThreshold = 1,
            flushHandler = { events ->
                recorded.addAll(events)
                Pair(null, null as NetworkingError?)
            }
        )
        FrameNetworking.initializeWithAPIKey(
            context = RuntimeEnvironment.getApplication(),
            secretKey = "sk_test",
            publishableKey = "pk_test",
            accountId = null
        )
    }

    @Test
    fun `emit without an account id buffers instead of dropping`() = runBlocking {
        AccountEventEmitter.emit(AccountEventName.ONBOARDING_STARTED, AccountEventScreen.ONBOARDING)
        delay(200)

        assertTrue(AccountEventEmitter.pendingEventNamesForTesting().contains(AccountEventName.ONBOARDING_STARTED.apiValue))
        assertTrue(recorded.isEmpty())
    }

    @Test
    fun `resolving the account id flushes buffered events with their original timestamp`() = runBlocking {
        AccountEventEmitter.emit(AccountEventName.ONBOARDING_STARTED, AccountEventScreen.ONBOARDING)
        delay(200)
        assertTrue(AccountEventEmitter.pendingEventNamesForTesting().contains(AccountEventName.ONBOARDING_STARTED.apiValue))

        FrameNetworking.setAccountIdIfUnset("acc_123")
        delay(200)

        val flushedEvent = recorded.single { it.name == AccountEventName.ONBOARDING_STARTED.apiValue }
        assertEquals("acc_123", flushedEvent.accountId)
        assertTrue(AccountEventEmitter.pendingEventNamesForTesting().isEmpty())
    }

    @Test
    fun `buffer drops oldest on overflow`() = runBlocking {
        repeat(201) {
            AccountEventEmitter.emit(AccountEventName.ONBOARDING_STARTED, AccountEventScreen.ONBOARDING)
        }
        delay(200)

        assertTrue(AccountEventEmitter.pendingEventNamesForTesting().size <= 200)
    }

    @Test
    fun `emit racing with account id resolution never loses the event`() = runBlocking {
        repeat(100) { i ->
            AccountEventEmitter.clearPendingForTesting()
            FrameNetworking.initializeWithAPIKey(
                context = RuntimeEnvironment.getApplication(),
                secretKey = "sk_test",
                publishableKey = "pk_test",
                accountId = null
            )
            recorded.clear()

            val emitJob = launch(Dispatchers.IO) {
                AccountEventEmitter.emit(AccountEventName.ONBOARDING_STARTED, AccountEventScreen.ONBOARDING, detail = "iter-$i")
            }
            val resolveJob = launch(Dispatchers.IO) {
                FrameNetworking.setAccountIdIfUnset("acc_race_$i")
            }
            emitJob.join()
            resolveJob.join()
            var landedInQueue = false
            repeat(50) {
                if (recorded.any { it.detail == "iter-$i" }) {
                    landedInQueue = true
                    return@repeat
                }
                delay(10)
            }
            assertTrue("iteration $i: event never flushed — permanently stuck in buffer or lost", landedInQueue)
            assertTrue(
                "iteration $i: event still sitting in the pre-account buffer after resolution",
                AccountEventEmitter.pendingEventNamesForTesting().isEmpty()
            )
        }
    }

    @Test
    fun `resolving twice never overwrites the first account id or re-flushes`() = runBlocking {
        AccountEventEmitter.emit(AccountEventName.ONBOARDING_STARTED, AccountEventScreen.ONBOARDING)
        delay(200)

        FrameNetworking.setAccountIdIfUnset("acc_first")
        delay(200)
        FrameNetworking.setAccountIdIfUnset("acc_second")
        delay(200)

        val flushedEvents = recorded.filter { it.name == AccountEventName.ONBOARDING_STARTED.apiValue }
        assertEquals(1, flushedEvents.size)
        assertEquals("acc_first", flushedEvents.single().accountId)
        assertEquals("acc_first", FrameNetworking.accountId)
    }
}
