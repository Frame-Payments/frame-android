package com.framepayments.framesdk

import com.framepayments.framesdk.chargeintents.AuthorizationMode
import com.framepayments.framesdk.chargeintents.ChargeIntentAPI
import com.framepayments.framesdk.chargeintents.ChargeIntentStatus
import com.framepayments.framesdk.chargeintents.ChargeIntentsRequests
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
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
        val testClient = OkHttpClient.Builder().build()
        FrameNetworking.asyncURLSession = DefaultURLSession(testClient)
        FrameNetworking.mainApiUrl = mockWebServer.url("/").toString()
        FrameNetworking.apiSecretKey = "sk_test_key"
        FrameNetworking.apiPublishableKey = "pk_test_key"
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testCreateChargeIntent() = runBlocking {
        val responseBody = """{"id":"intent_123", "status":"pending"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = ChargeIntentsRequests.CreateChargeIntentRequest(
            amount = 100,
            currency = "usd",
            customer = null,
            description = null,
            confirm = true,
            paymentMethod = "1",
            receiptEmail = null,
            authorizationMode = AuthorizationMode.AUTOMATIC,
            customerData = null,
            paymentMethodData = null
        )
        val (result, error) = ChargeIntentAPI.createChargeIntent(request)

        assertNotNull(result)
        assertEquals("intent_123", result?.id)
        assertEquals(ChargeIntentStatus.PENDING, result?.status)
    }

    @Test
    fun testCaptureChargeIntent() = runBlocking {
        val responseBody = """{"id":"intent_123","amount":100}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = ChargeIntentsRequests.CaptureChargeIntentRequest(amountCapturedCents = 100)
        val (result, error) = ChargeIntentAPI.captureChargeIntent("1", request)

        assertNotNull(result)
        assertEquals("intent_123", result?.id)
        assertEquals(100, result?.amount)
    }

    @Test
    fun testConfirmChargeIntent() = runBlocking {
        val responseBody = """{"id":"intent_123", "status":"succeeded"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = ChargeIntentAPI.confirmChargeIntent("intent_123", "ci_intent_123_secret_abc")

        assertNotNull(result)
        assertEquals("intent_123", result?.id)
        assertEquals(ChargeIntentStatus.SUCCEEDED, result?.status)
    }

    @Test
    fun testConfirmChargeIntentUsesClientSecretAsBearer() = runBlocking {
        val responseBody = """{"id":"intent_123", "status":"succeeded"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        ChargeIntentAPI.confirmChargeIntent("intent_123", "ci_intent_123_secret_abc")

        val recorded = mockWebServer.takeRequest()
        assertEquals("Bearer ci_intent_123_secret_abc", recorded.getHeader("Authorization"))
    }

    @Test
    fun testCancelChargeIntent() = runBlocking {
        val responseBody = """{"id":"intent_123", "status":"canceled"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = ChargeIntentAPI.cancelChargeIntent("intent_123")

        assertNotNull(result)
        assertEquals("intent_123", result?.id)
        assertEquals(ChargeIntentStatus.CANCELED, result?.status)
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

        val (result, error) = ChargeIntentAPI.getAllChargeIntents(1, perPage = 100)

        assertNotNull(result)
        assertEquals(3, result?.data?.size)
        assertEquals("intent_2", result?.data?.get(1)?.id)
        assertEquals(ChargeIntentStatus.SUCCEEDED, result?.data?.get(2)?.status)
    }

    @Test
    fun testGetChargeIntent() = runBlocking {
        val responseBody = """{"id":"intent_123", "status":"pending"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = ChargeIntentAPI.getChargeIntent("intent_123", "ci_intent_123_secret_abc")

        assertNotNull(result)
        assertEquals("intent_123", result?.id)
        assertEquals(ChargeIntentStatus.PENDING, result?.status)
    }

    @Test
    fun testGetChargeIntentUsesClientSecretAsBearer() = runBlocking {
        val responseBody = """{"id":"intent_123", "status":"pending"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        ChargeIntentAPI.getChargeIntent("intent_123", "ci_intent_123_secret_xyz")

        val recorded = mockWebServer.takeRequest()
        assertEquals("Bearer ci_intent_123_secret_xyz", recorded.getHeader("Authorization"))
    }

    @Test
    fun testUpdateChargeIntent() = runBlocking {
        val responseBody = """{"id":"intent_123", "amount":400}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = ChargeIntentsRequests.UpdateChargeIntentRequest(amount = 400, currency = "usd", customer = null, description = null, confirm = true, paymentMethod = null, receiptEmail = null)
        val (result, error) = ChargeIntentAPI.updateChargeIntent("intent_123", request)

        assertNotNull(result)
        assertEquals("intent_123", result?.id)
        assertEquals(400, result?.amount)
    }

    @Test
    fun testVoidRemainingChargeIntent() = runBlocking {
        val responseBody = """{"id":"intent_123", "status":"succeeded"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = ChargeIntentAPI.voidRemainingChargeIntent("intent_123")

        assertNotNull(result)
        assertEquals("intent_123", result?.id)
        assertEquals(ChargeIntentStatus.SUCCEEDED, result?.status)

        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.requestUrl?.encodedPath?.endsWith("/void_remaining") == true)
    }
}
