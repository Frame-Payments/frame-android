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
        val responseBody = """{"id":"method_123", "type":"card"}"""
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
        val responseBody = """{"id":"method_123", "type":"ach"}"""
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
        val responseBody = """{"id":"method_123", "type":"card"}"""
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
                    {"id":"method_123", "type":"card"},
                    {"id":"method_124", "customer":"cus_123"}
                ]
            }
        """.trimMargin()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = PaymentMethodsAPI.getPaymentMethods(0, 0)

        assertNotNull(result)
        assertEquals("method_123", result?.data?.get(0)?.id)
        assertEquals("cus_123", result?.data?.get(1)?.customer)
    }

    @Test
    fun testGetPaymentMethodWithCustomerId() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"method_123", "type":"card"}
                ]
            }
        """.trimMargin()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = PaymentMethodsAPI.getPaymentMethodsWithCustomer("cus_123")

        assertNotNull(result)
        assertEquals("method_123", result?.get(0)?.id)
        assertEquals(FrameObjects.PaymentMethodType.CARD, result?.get(0)?.type)
    }

    @Test
    fun testUpdatePaymentMethod() = runBlocking {
        val responseBody = """{"id":"method_123", "type":"card"}"""
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
        val responseBody = """{"id":"method_123", "customer":"cus_111"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = PaymentMethodRequests.AttachPaymentMethodRequest(customer = "cus_111")
        val (result, error) = PaymentMethodsAPI.attachPaymentMethodWith("method_123", request)

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals("cus_111", result?.customer)
    }

    @Test
    fun testDetachPaymentMethodWithId() = runBlocking {
        val responseBody = """{"id":"method_123", "customer":"cus_111"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = PaymentMethodsAPI.detachPaymentMethodWith("method_123")

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals("cus_111", result?.customer)
    }

    @Test
    fun testBlockPaymentMethodWithId() = runBlocking {
        val responseBody = """{"id":"method_123", "status":"blocked"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = PaymentMethodsAPI.blockPaymentMethodWith("method_123")

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals(FrameObjects.PaymentMethodStatus.BLOCKED, result?.status)
    }

    @Test
    fun testUnblockPaymentMethodWithId() = runBlocking {
        val responseBody = """{"id":"method_123", "status":"active"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = PaymentMethodsAPI.unblockPaymentMethodWith("method_123")

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals(FrameObjects.PaymentMethodStatus.ACTIVE, result?.status)
    }
}