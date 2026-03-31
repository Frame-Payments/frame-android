package com.framepayments.framesdk

import com.framepayments.framesdk.disputes.DisputeReason
import com.framepayments.framesdk.disputes.DisputeRequests
import com.framepayments.framesdk.disputes.DisputeStatus
import com.framepayments.framesdk.disputes.DisputesAPI
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class DisputesAPITest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        FrameNetworking.mainApiUrl = mockWebServer.url("/").toString()
        FrameNetworking.apiKey = "test_key"
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testUpdateDispute() = runBlocking {
        val responseBody = """
            {
                "id": "dp_123",
                "amount": 1000,
                "currency": "usd",
                "reason": "product_not_received",
                "status": "under_review",
                "object": "dispute",
                "livemode": false,
                "created": 1234567890,
                "updated": 1234567891
            }
        """.trimIndent().replace("\n", "")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = DisputeRequests.UpdateDisputeRequest(
            shippingTrackingNumber = "1Z999AA10123456784",
            supportDescription = "Customer received the item"
        )
        val (result, error) = DisputesAPI.updateDispute("dp_123", request)

        assertNotNull(result)
        assertEquals("dp_123", result?.id)
        assertEquals(1000, result?.amount)
        assertEquals(DisputeReason.PRODUCT_NOT_RECEIVED, result?.reason)
        assertEquals(DisputeStatus.UNDER_REVIEW, result?.status)

        val recorded = mockWebServer.takeRequest()
        val body = recorded.body.readUtf8()
        assertTrue(body.contains("shipping_tracking_number"))
        assertTrue(body.contains("support_description"))
        assertFalse(body.contains("\"evidence\""))
        assertFalse(body.contains("\"submit\""))
    }

    @Test
    fun testUpdateDisputeWithFlatFields() = runBlocking {
        val responseBody = """
            {
                "id": "dp_999",
                "amount": 2000,
                "currency": "usd",
                "reason": "fraudulent",
                "status": "needs_response",
                "object": "dispute",
                "livemode": false,
                "created": 1234567890,
                "updated": 1234567891
            }
        """.trimIndent().replace("\n", "")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = DisputeRequests.UpdateDisputeRequest(
            shippingCarrier = "UPS",
            shippingDate = "2026-03-01",
            customerPurchaseIpAddress = "192.168.1.1",
            refundRefusalExplanation = "Item was delivered",
            accessActivityLog = "Customer logged in on 2026-02-28"
        )
        val (result, error) = DisputesAPI.updateDispute("dp_999", request)

        assertNotNull(result)
        assertEquals("dp_999", result?.id)
    }

    @Test
    fun testGetDispute() = runBlocking {
        val responseBody = """
            {
                "id": "dp_456",
                "amount": 5000,
                "currency": "usd",
                "reason": "fraudulent",
                "status": "needs_response",
                "object": "dispute",
                "livemode": false,
                "created": 1234567890,
                "updated": 1234567891
            }
        """.trimIndent().replace("\n", "")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = DisputesAPI.getDispute("dp_456")

        assertNotNull(result)
        assertEquals("dp_456", result?.id)
        assertEquals(5000, result?.amount)
        assertEquals(DisputeReason.FRAUDULENT, result?.reason)
        assertEquals(DisputeStatus.NEEDS_RESPONSE, result?.status)
    }

    @Test
    fun testGetDisputes() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {
                        "id": "dp_1",
                        "amount": 100,
                        "currency": "usd",
                        "reason": "duplicate",
                        "status": "under_review",
                        "object": "dispute",
                        "livemode": false,
                        "created": 1,
                        "updated": 2
                    },
                    {
                        "id": "dp_2",
                        "amount": 200,
                        "currency": "usd",
                        "reason": "general",
                        "status": "won",
                        "object": "dispute",
                        "livemode": false,
                        "created": 3,
                        "updated": 4
                    }
                ]
            }
        """.trimIndent().replace("\n", "")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = DisputesAPI.getDisputes(
            chargeId = null,
            chargeIntentId = null,
            perPage = 10,
            page = 1
        )

        assertNotNull(result)
        assertEquals(2, result?.data?.size)
        assertEquals("dp_1", result?.data?.get(0)?.id)
        assertEquals(DisputeStatus.UNDER_REVIEW, result?.data?.get(0)?.status)
        assertEquals("dp_2", result?.data?.get(1)?.id)
        assertEquals(DisputeStatus.WON, result?.data?.get(1)?.status)
    }
}
