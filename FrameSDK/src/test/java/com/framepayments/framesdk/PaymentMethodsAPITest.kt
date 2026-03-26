package com.framepayments.framesdk

import com.framepayments.framesdk.paymentmethods.PaymentMethodRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PaymentMethodsAPITest {
    private lateinit var mockWebServer: MockWebServer

    private companion object {
        /** Minimal body matching iOS `FrameObjects.PaymentMethod` decode requirements. */
        fun paymentMethodJson(
            id: String,
            type: String,
            customerId: String? = null,
            status: String = "active"
        ): String {
            val extra = if (customerId != null) ",\"customer_id\":\"$customerId\"" else ""
            return """{"id":"$id","type":"$type","object":"payment_method","created":1,"updated":1,"livemode":false,"status":"$status"$extra}"""
        }
    }

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
    fun testCreateCardPaymentMethod() = runBlocking {
        val responseBody = paymentMethodJson("method_123", "card")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = PaymentMethodRequests.CreateCardPaymentMethodRequest(
            cardNumber = "4242424242424242",
            expMonth = "08",
            expYear = "27",
            cvc = "424",
            customer = null,
            billing = null
        )
        val (result, error) = PaymentMethodsAPI.createCardPaymentMethod(request, encryptData = false)

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals(FrameObjects.PaymentMethodType.CARD, result?.type)
    }

    @Test
    fun testCreateACHPaymentMethod() = runBlocking {
        val responseBody = paymentMethodJson("method_123", "ach")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = PaymentMethodRequests.CreateACHPaymentMethodRequest(
            accountType = FrameObjects.PaymentAccountType.CHECKING,
            accountNumber = "12341234123412",
            routingNumber = "0000000000",
            customer = null,
            billing = null
        )
        val (result, error) = PaymentMethodsAPI.createACHPaymentMethod(request)

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals(FrameObjects.PaymentMethodType.ACH, result?.type)
    }

    @Test
    fun testGetPaymentMethodWithId() = runBlocking {
        val responseBody = paymentMethodJson("method_123", "card")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = PaymentMethodsAPI.getPaymentMethodWith("method_123")

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals(FrameObjects.PaymentMethodType.CARD, result?.type)
    }

    @Test
    fun testGetPaymentMethods() = runBlocking {
        val responseBody = """
            {
                "data": [
                    ${paymentMethodJson("method_123", "card")},
                    ${paymentMethodJson("method_124", "card", customerId = "cus_123")}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = PaymentMethodsAPI.getPaymentMethods(0, 0)

        assertNotNull(result)
        assertEquals("method_123", result?.data?.get(0)?.id)
        assertEquals("cus_123", result?.data?.get(1)?.customerId)
    }

    @Test
    fun testGetPaymentMethodWithCustomerId() = runBlocking {
        val responseBody = """
            {
                "data": [
                    ${paymentMethodJson("method_123", "card")}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = PaymentMethodsAPI.getPaymentMethodsWithCustomer("cus_123")

        assertNotNull(result)
        assertEquals("method_123", result?.get(0)?.id)
        assertEquals(FrameObjects.PaymentMethodType.CARD, result?.get(0)?.type)
    }

    @Test
    fun testUpdatePaymentMethod() = runBlocking {
        val responseBody = paymentMethodJson("method_123", "card")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = PaymentMethodRequests.UpdatePaymentMethodRequest(
            expMonth = "10",
            expYear = "2028",
            billing = null
        )
        val (result, error) = PaymentMethodsAPI.updatePaymentMethodWith("method_123", request)

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals(FrameObjects.PaymentMethodType.CARD, result?.type)
    }

    @Test
    fun testAttachPaymentMethodWithId() = runBlocking {
        val responseBody = paymentMethodJson("method_123", "card", customerId = "cus_111")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = PaymentMethodRequests.AttachPaymentMethodRequest(customer = "cus_111")
        val (result, error) = PaymentMethodsAPI.attachPaymentMethodWith("method_123", request)

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals("cus_111", result?.customerId)
    }

    @Test
    fun testDetachPaymentMethodWithId() = runBlocking {
        val responseBody = paymentMethodJson("method_123", "card", customerId = "cus_111")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = PaymentMethodsAPI.detachPaymentMethodWith("method_123")

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals("cus_111", result?.customerId)
    }

    @Test
    fun testBlockPaymentMethodWithId() = runBlocking {
        val responseBody = paymentMethodJson("method_123", "card", status = "blocked")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = PaymentMethodsAPI.blockPaymentMethodWith("method_123")

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals(FrameObjects.PaymentMethodStatus.BLOCKED, result?.status)
    }

    @Test
    fun testUnblockPaymentMethodWithId() = runBlocking {
        val responseBody = paymentMethodJson("method_123", "card", status = "active")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = PaymentMethodsAPI.unblockPaymentMethodWith("method_123")

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals(FrameObjects.PaymentMethodStatus.ACTIVE, result?.status)
    }
}
