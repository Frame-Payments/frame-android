package com.framepayments.framesdk.chargeintents

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.refunds.RefundRequests
import com.framepayments.framesdk.refunds.RefundsAPI
import com.framepayments.framesdk.subscriptions.SubscriptionRequest
import com.framepayments.framesdk.subscriptions.SubscriptionsAPI
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ChargeIntentAPITest {
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
    fun testCreateChargeIntent() = runBlocking {
        val responseBody = """{"id":"intent_123", "status":"pending"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = ChargeIntentsRequests.CreateChargeIntentRequest(amount = 100, currency = "usd", customer = null, description = null, confirm = true, paymentMethod = "1", receiptEmail = null, authorizationMode = AuthorizationMode.automatic, customerData = null, paymentMethodData = null)
        val result = ChargeIntentAPI.createChargeIntent(request, true)

        assertNotNull(result)
        assertEquals("intent_123", result?.id)
        assertEquals(ChargeIntentStatus.pending, result?.status)
    }

    @Test
    fun testCaptureChargeIntent() = runBlocking {
        val responseBody = """{"id":"intent_123","amount":100}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = ChargeIntentsRequests.CaptureChargeIntentRequest(amountCapturedCents = 100)
        val result = ChargeIntentAPI.captureChargeIntent("1", request, true)

        assertNotNull(result)
        assertEquals("intent_123", result?.id)
        assertEquals(100, result?.amount)
    }

    @Test
    fun testConfirmChargeIntent() = runBlocking {
        val responseBody = """{"id":"intent_123", "status":"succeeded"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val result = ChargeIntentAPI.confirmChargeIntent("intent_123", true)

        assertNotNull(result)
        assertEquals("intent_123", result?.id)
        assertEquals(ChargeIntentStatus.succeeded, result?.status)
    }

    @Test
    fun testCancelChargeIntent() = runBlocking {
        val responseBody = """{"id":"intent_123", "status":"canceled"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val result = ChargeIntentAPI.cancelChargeIntent("intent_123")

        assertNotNull(result)
        assertEquals("intent_123", result?.id)
        assertEquals(ChargeIntentStatus.canceled, result?.status)
    }

    @Test
    fun testGetAllChargeIntents() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"intent_1", "status":"refunded"},
                    {"id":"intent_2", "status":"processing"},
                    {"id":"intent_3", "status":"succeeded"}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val result = ChargeIntentAPI.getAllChargeIntents(1, perPage = 100)

        assertNotNull(result)
        assertEquals(3, result?.size)
        assertEquals("intent_2", result?.get(1)?.id)
        assertEquals(ChargeIntentStatus.succeeded, result?.get(2)?.status)
    }

    @Test
    fun testGetChargeIntent() = runBlocking {
        val responseBody = """{"id":"intent_123", "status":"pending"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val result = ChargeIntentAPI.getChargeIntent("intent_123")

        assertNotNull(result)
        assertEquals("intent_123", result?.id)
        assertEquals(ChargeIntentStatus.pending, result?.status)
    }

    @Test
    fun testUpdateChargeIntent() = runBlocking {
        val responseBody = """{"id":"intent_123", "amount":400}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = ChargeIntentsRequests.UpdateChargeIntentRequest(amount = 400, currency = "usd", customer = null, description = null, confirm = true, paymentMethod = null, receiptEmail = null)
        val result = ChargeIntentAPI.updateChargeIntent("intent_123", request)

        assertNotNull(result)
        assertEquals("intent_123", result?.id)
        assertEquals(400, result?.amount)
    }
}