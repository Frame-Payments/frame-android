package com.framepayments.framesdk

import com.framepayments.framesdk.subscriptionphases.PhaseDurationType
import com.framepayments.framesdk.subscriptionphases.PhasePricingType
import com.framepayments.framesdk.subscriptionphases.SubscriptionPhase
import com.framepayments.framesdk.subscriptionphases.SubscriptionPhaseRequest
import com.framepayments.framesdk.subscriptionphases.SubscriptionPhasesAPI
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SubscriptionPhasesAPITest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        FrameNetworking.mainApiUrl = mockWebServer.url("/").toString()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testCreateSubscriptionPhase() = runBlocking {
        val responseBody = """{"id":"phase_123", "name":"new_phase"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = SubscriptionPhaseRequest.CreateSubscriptionPhaseRequest(
            ordinal = 1,
            pricingType = PhasePricingType.STATIC,
            durationType = PhaseDurationType.FINITE,
            name = "new_phase",
            amountCents = null,
            discountPercentage = null,
            periodCount = null,
            interval = null,
            intervalCount = null
        )
        val (result, error) = SubscriptionPhasesAPI.createSubscriptionPhase("sub_123", request)

        assertNotNull(result)
        assertEquals("phase_123", result?.id)
        assertEquals("new_phase", result?.name)
    }

    @Test
    fun testUpdateSubscriptionPhase() = runBlocking {
        val responseBody = """{"id":"phase_123", "name":"new_phase2"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = SubscriptionPhaseRequest.UpdateSubscriptionPhaseRequest(
            ordinal = 12,
            pricingType = PhasePricingType.RELATIVE,
            durationType = PhaseDurationType.FINITE,
            name = "new_phase2",
            amountCents = null,
            discountPercentage = null,
            periodCount = null,
            interval = null,
            intervalCount = null
        )
        val (result, error) = SubscriptionPhasesAPI.updateSubscriptionPhase("sub_123", "phase_123", request)

        assertNotNull(result)
        assertEquals("phase_123", result?.id)
        assertEquals("new_phase2", result?.name)
    }

    @Test
    fun testGetSubscriptionPhaseWithId() = runBlocking {
        val responseBody = """{"id":"phase_123", "duration_type":"finite"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = SubscriptionPhasesAPI.getSubscriptionPhaseWith("sub_456", "phase_123")

        assertNotNull(result)
        assertEquals("phase_123", result?.id)
        assertEquals(PhaseDurationType.FINITE, result?.durationType)
    }

    @Test
    fun testGetSubscriptionPhases() = runBlocking {
        val responseBody = """
            {
                "phases": [
                    {"id":"phase_123", "duration_type":"finite"},
                    {"id":"phase_234", "pricing_type":"static"}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = SubscriptionPhasesAPI.getSubscriptionPhases("sub_123")

        assertNotNull(result)
        assertEquals(2, result?.phases?.size)
        assertEquals(PhaseDurationType.FINITE, result?.phases?.get(0)?.durationType)
        assertEquals(PhasePricingType.STATIC, result?.phases?.get(1)?.pricingType)
    }

    @Test
    fun testBulkUpdateSubscriptionPhases() = runBlocking {
        val responseBody = """
            {
                "phases": [
                    {"id":"phase_123", "duration_type":"finite"},
                    {"id":"phase_234", "pricing_type":"static"}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val phase = SubscriptionPhase(
            id = "phase_123",
            ordinal = 2,
            name = "phase_2",
            pricingType = PhasePricingType.STATIC,
            durationType = PhaseDurationType.FINITE,
            amount = 100,
            currency = "usd",
            discountPercentage = null,
            periodCount = null,
            interval = null,
            intervalCount = 2,
            livemode = false,
            created = 0,
            updated = 0,
            phaseObject = "phase_subscription"
        )
        val request = SubscriptionPhaseRequest.BulkUpdateSubscriptionPhaseRequest(
            phases = mutableListOf(phase)
        )
        val (result, error) = SubscriptionPhasesAPI.bulkUpdateSubscriptionPhases("sub_123", request)

        assertNotNull(result)
        assertEquals(2, result?.size)
        assertEquals(PhaseDurationType.FINITE, result?.get(0)?.durationType)
        assertEquals(PhasePricingType.STATIC, result?.get(1)?.pricingType)
    }
}
