package com.framepayments.framesdk

import com.framepayments.framesdk.refunds.RefundRequests
import com.framepayments.framesdk.refunds.RefundsAPI
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RefundsAPITest {
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
    fun testCreateRefundWithRequiredFieldOnly() = runBlocking {
        val responseBody = """{"id":"ref_123", "status":"refunded"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = RefundRequests.CreateRefundRequest(chargeIntent = "ci_123")
        val (result, error) = RefundsAPI.createRefund(request)

        assertNotNull(result)
        assertEquals("ref_123", result?.id)
        assertEquals("refunded", result?.status)

        val recorded = mockWebServer.takeRequest()
        val body = recorded.body.readUtf8()
        assertTrue(body.contains("charge_intent"))
        assertFalse(body.contains("\"amount\""))
        assertFalse(body.contains("\"reason\""))
    }

    @Test
    fun testCreateRefundWithOptionalFields() = runBlocking {
        val responseBody = """{"id":"ref_456", "status":"refunded"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = RefundRequests.CreateRefundRequest(chargeIntent = "ci_456", amount = 500, reason = "duplicate")
        val (result, error) = RefundsAPI.createRefund(request)

        assertNotNull(result)
        assertEquals("ref_456", result?.id)
    }

    @Test
    fun testGetRefundsList() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"ref_1", "status":"refunded"},
                    {"id":"ref_2", "status":"processing"}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = RefundsAPI.getRefunds(perPage = 10, page = 1, chargeId = "1", chargeIntentId = "2")

        assertNotNull(result)
        assertEquals(2, result?.data?.size)
        assertEquals("ref_2", result?.data?.get(1)?.id)
        assertEquals("processing", result?.data?.get(1)?.status)
    }

    @Test
    fun testGetRefundWithId() = runBlocking {
        val responseBody = """{"id":"ref_4", "status":"refunded"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = RefundsAPI.getRefundWith("ref_4")

        assertNotNull(result)
        assertEquals("ref_4", result?.id)
        assertEquals("refunded", result?.status)
    }
}
