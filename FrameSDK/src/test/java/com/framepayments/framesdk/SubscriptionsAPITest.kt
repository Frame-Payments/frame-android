package com.framepayments.framesdk

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.subscriptions.SubscriptionRequest
import com.framepayments.framesdk.subscriptions.SubscriptionsAPI
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SubscriptionsAPITest {

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
    fun testCreateSubscription() = runBlocking {
        val responseBody = """{"id":"sub_123", "status":"active"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = SubscriptionRequest.CreateSubscriptionRequest("123", "1", currency= "USD", defaultPaymentMethod = "1", description = "")
        val result = SubscriptionsAPI.createSubscription(request)

        assertNotNull(result)
        assertEquals("sub_123", result?.id)
        assertEquals("active", result?.status)
    }

    @Test
    fun testUpdateSubscription() = runBlocking {
        val responseBody = """{"id":"sub_123", "default_payment_method":"method_123"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = SubscriptionRequest.UpdateSubscriptionRequest(
            description = null,
            defaultPaymentMethod = null
        )
        val result = SubscriptionsAPI.updateSubscription("sub_123", request)

        assertNotNull(result)
        assertEquals("sub_123", result?.id)
        assertEquals("method_123", result?.defaultPaymentMethod)
    }

    @Test
    fun testGetSubscriptionWithId() = runBlocking {
        val responseBody = """{"id":"sub_456", "status":"canceled"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val result = SubscriptionsAPI.getSubscriptionWith("sub_456")

        assertNotNull(result)
        assertEquals("sub_456", result?.id)
        assertEquals("canceled", result?.status)
    }

    @Test
    fun testGetSubscriptions() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"sub_1", "status":"active"},
                    {"id":"sub_2", "status":"active"}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val result = SubscriptionsAPI.getSubscriptions(perPage = 10, page = 1)

        assertNotNull(result)
        assertEquals(2, result?.size)
        assertEquals("sub_1", result?.get(0)?.id)
    }

    @Test
    fun testSearchSubscriptions() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"sub_3", "status":"trialing"}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val result = SubscriptionsAPI.searchSubscriptions(status = "trialing", createdBefore = null, createdAfter = null)

        assertNotNull(result)
        assertEquals(1, result?.size)
        assertEquals("sub_3", result?.get(0)?.id)
    }

    @Test
    fun testCancelSubscriptionWithId() = runBlocking {
        val responseBody = """{"id":"sub_789", "status":"canceled"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val result = SubscriptionsAPI.cancelSubscriptionWith("sub_789")

        assertNotNull(result)
        assertEquals("sub_789", result?.id)
        assertEquals("canceled", result?.status)
    }
}
