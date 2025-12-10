package com.framepayments.framesdk

import com.framepayments.framesdk.subscriptionphases.PhaseDurationType
import com.framepayments.framesdk.subscriptionphases.PhasePricingType
import com.framepayments.framesdk.subscriptionphases.SubscriptionPhase
import com.framepayments.framesdk.productphases.ProductPhaseRequest
import com.framepayments.framesdk.productphases.ProductPhasesAPI
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProductPhasesAPITest {

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
    fun testCreateProductPhase() = runBlocking {
        val responseBody = """{"id":"phase_123", "name":"new_phase"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = ProductPhaseRequest.CreateProductPhaseRequest(
            ordinal = 1,
            pricingType = PhasePricingType.STATIC,
            name = "new_phase",
            amountCents = null,
            discountPercentage = null,
            periodCount = null
        )
        val (result, error) = ProductPhasesAPI.createProductPhase("prod_123", request)

        assertNotNull(result)
        assertEquals("phase_123", result?.id)
        assertEquals("new_phase", result?.name)
    }

    @Test
    fun testUpdateProductPhase() = runBlocking {
        val responseBody = """{"id":"phase_123", "name":"new_phase2"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = ProductPhaseRequest.UpdateProductPhaseRequest(
            ordinal = 12,
            pricingType = PhasePricingType.RELATIVE,
            name = "new_phase2",
            amountCents = null,
            discountPercentage = null,
            periodCount = null,
        )
        val (result, error) = ProductPhasesAPI.updateProductPhase("prod_123", "phase_123", request)

        assertNotNull(result)
        assertEquals("phase_123", result?.id)
        assertEquals("new_phase2", result?.name)
    }

    @Test
    fun testGetProductPhaseWithId() = runBlocking {
        val responseBody = """{"id":"phase_123", "duration_type":"finite"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = ProductPhasesAPI.getProductPhaseWith("prod_123", "phase_123")

        assertNotNull(result)
        assertEquals("phase_123", result?.id)
        assertEquals(PhaseDurationType.FINITE, result?.durationType)
    }

    @Test
    fun testGetProductPhases() = runBlocking {
        val responseBody = """
            {
                "phases": [
                    {"id":"phase_123", "duration_type":"finite"},
                    {"id":"phase_234", "pricing_type":"static"}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = ProductPhasesAPI.getProductPhases("prod_123")

        assertNotNull(result)
        assertEquals(2, result?.phases?.size)
        assertEquals(PhaseDurationType.FINITE, result?.phases?.get(0)?.durationType)
        assertEquals(PhasePricingType.STATIC, result?.phases?.get(1)?.pricingType)
    }

    @Test
    fun testBulkUpdateProductPhases() = runBlocking {
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
        val request = ProductPhaseRequest.BulkUpdateProductPhaseRequest(
            phases = mutableListOf(phase)
        )
        val (result, error) = ProductPhasesAPI.bulkUpdateProductPhases("prod_123", request)

        assertNotNull(result)
        assertEquals(2, result?.size)
        assertEquals(PhaseDurationType.FINITE, result?.get(0)?.durationType)
        assertEquals(PhasePricingType.STATIC, result?.get(1)?.pricingType)
    }
}
