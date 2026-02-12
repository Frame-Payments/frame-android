package com.framepayments.framesdk

import com.framepayments.framesdk.disputes.DisputeEvidence
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

        val evidence = DisputeEvidence(
            evidenceProductDescription = "Widget",
            evidenceShippingTrackingNumber = "1Z999AA10123456784"
        )
        val request = DisputeRequests.UpdateDisputeRequest(evidence = evidence, submit = true)
        val (result, error) = DisputesAPI.updateDispute("dp_123", request)

        assertNotNull(result)
        assertEquals("dp_123", result?.id)
        assertEquals(1000, result?.amount)
        assertEquals(DisputeReason.PRODUCT_NOT_RECEIVED, result?.reason)
        assertEquals(DisputeStatus.UNDER_REVIEW, result?.status)
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

    @Test
    fun testCloseDispute() = runBlocking {
        val responseBody = """
            {
                "id": "dp_789",
                "amount": 1500,
                "currency": "usd",
                "reason": "customer_initiated",
                "status": "lost",
                "object": "dispute",
                "livemode": false,
                "created": 1234567890,
                "updated": 1234567892
            }
        """.trimIndent().replace("\n", "")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = DisputesAPI.closeDispute("dp_789")

        assertNotNull(result)
        assertEquals("dp_789", result?.id)
        assertEquals(DisputeStatus.LOST, result?.status)
    }
}
