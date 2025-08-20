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
    fun testCreatePaymentMethod() = runBlocking {
        val responseBody = """{"id":"method_123", "type":"card"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = PaymentMethodRequests.CreatePaymentMethodRequest(
            type = "card",
            cardNumber = "4242424242424242",
            expMonth = "08",
            expYear = "27",
            cvc = "424",
            customer = null,
            billing = null
        )
        val result = PaymentMethodsAPI.createPaymentMethod(request, encryptData = false)

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals("card", result?.type)
    }

    @Test
    fun testGetPaymentMethodWithId() = runBlocking {
        val responseBody = """{"id":"method_123", "type":"card"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val result = PaymentMethodsAPI.getPaymentMethodWith("method_123")

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals("card", result?.type)
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

        val result = PaymentMethodsAPI.getPaymentMethods(0, 0)

        assertNotNull(result)
        assertEquals("method_123", result?.get(0)?.id)
        assertEquals("cus_123", result?.get(1)?.customer)
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

        val result = PaymentMethodsAPI.getPaymentMethodsWithCustomer("cus_123")

        assertNotNull(result)
        assertEquals("method_123", result?.get(0)?.id)
        assertEquals("card", result?.get(0)?.type)
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
        val result = PaymentMethodsAPI.updatePaymentMethodWith("method_123", request)

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals("card", result?.type)
    }

    @Test
    fun testAttachPaymentMethodWithId() = runBlocking {
        val responseBody = """{"id":"method_123", "customer":"cus_111"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = PaymentMethodRequests.AttachPaymentMethodRequest(customer = "cus_111")
        val result = PaymentMethodsAPI.attachPaymentMethodWith("method_123", request)

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals("cus_111", result?.customer)
    }

    @Test
    fun testDetachPaymentMethodWithId() = runBlocking {
        val responseBody = """{"id":"method_123", "customer":"cus_111"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val result = PaymentMethodsAPI.detachPaymentMethodWith("method_123")

        assertNotNull(result)
        assertEquals("method_123", result?.id)
        assertEquals("cus_111", result?.customer)
    }
}