package com.framepayments.framesdk

import com.framepayments.framesdk.transfers.TransferRequests
import com.framepayments.framesdk.transfers.TransferStatus
import com.framepayments.framesdk.transfers.TransfersAPI
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TransfersAPITest {
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
    fun testCreateTransferChargeFlow() = runBlocking {
        val responseBody = """{"id":"tr_123", "status":"pending", "amount":10000, "currency":"USD"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = TransferRequests.CreateTransferRequest(
            amount = 10000,
            accountId = "acc_123",
            currency = "USD",
            sourcePaymentMethodId = "pm_123"
        )
        val (result, _) = TransfersAPI.createTransfer(request)

        assertNotNull(result)
        assertEquals("tr_123", result?.id)
        assertEquals(TransferStatus.PENDING, result?.status)
        assertEquals(10000, result?.amount)

        val recorded = mockWebServer.takeRequest()
        val body = recorded.body.readUtf8()
        assertTrue(body.contains("account_id"))
        assertTrue(body.contains("source_payment_method_id"))
        assertFalse(body.contains("destination_payment_method_id"))
    }

    @Test
    fun testCreateTransferPayoutFlow() = runBlocking {
        val responseBody = """{"id":"tr_456", "status":"pending", "amount":5000}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = TransferRequests.CreateTransferRequest(
            amount = 5000,
            accountId = "acc_456",
            destinationPaymentMethodId = "pm_dest_1"
        )
        val (result, _) = TransfersAPI.createTransfer(request)

        assertNotNull(result)
        assertEquals("tr_456", result?.id)

        val body = mockWebServer.takeRequest().body.readUtf8()
        assertTrue(body.contains("destination_payment_method_id"))
        assertFalse(body.contains("source_payment_method_id"))
    }

    @Test
    fun testGetTransfersList() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"tr_1", "status":"pending", "amount":100},
                    {"id":"tr_2", "status":"completed", "amount":200}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, _) = TransfersAPI.getTransfers(perPage = 10, page = 1)

        assertNotNull(result)
        assertEquals(2, result?.data?.size)
        assertEquals("tr_2", result?.data?.get(1)?.id)
        assertEquals(TransferStatus.COMPLETED, result?.data?.get(1)?.status)
    }

    @Test
    fun testGetTransferWithId() = runBlocking {
        val responseBody = """{"id":"tr_4", "status":"completed", "amount":777}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, _) = TransfersAPI.getTransferWith("tr_4")

        assertNotNull(result)
        assertEquals("tr_4", result?.id)
        assertEquals(TransferStatus.COMPLETED, result?.status)
        assertEquals(777, result?.amount)
    }

    /**
     * A 400-class response must surface as a `NetworkingError.ServerError` with the
     * server status code, even if the wire body happens to be Gson-parseable into a
     * Transfer-shaped (all-null) instance. Guards against the silent-failure
     * pattern flagged in review: the caller must be able to distinguish "success"
     * from "server rejection."
     */
    @Test
    fun testCreateTransferReturnsServerError() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"error":"invalid_request"}""")
        )

        val request = TransferRequests.CreateTransferRequest(amount = 10000, accountId = "acc_123")
        val (_, error) = TransfersAPI.createTransfer(request)

        assertNotNull(error)
        when (val e = error) {
            is NetworkingError.ServerError -> assertEquals(400, e.statusCode)
            else -> throw AssertionError("Expected NetworkingError.ServerError, got $e")
        }
    }

}
