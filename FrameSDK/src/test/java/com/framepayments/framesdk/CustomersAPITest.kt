package com.framepayments.framesdk

import com.framepayments.framesdk.customers.CustomersAPI
import com.framepayments.framesdk.customers.CustomersRequests
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CustomersAPITest {
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
    fun testCreateCustomer() = runBlocking {
        val responseBody = """{"id":"cus_123", "phone":"9999999999"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = CustomersRequests.CreateCustomerRequest(
            billingAddress = null,
            shippingAddress = null,
            name = "test",
            phone = "9999999999",
            email = null,
            description = null,
            metadata = null
        )
        val (result, error) = CustomersAPI.createCustomer(request, true)

        assertNotNull(result)
        assertEquals("cus_123", result?.id)
        assertEquals("9999999999", result?.phone)
    }

    @Test
    fun testUpdateCustomer() = runBlocking {
        val responseBody = """{"id":"cus_123", "name":"tester2"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = CustomersRequests.UpdateCustomerRequest(
            billingAddress = null,
            shippingAddress = null,
            name = "tester2",
            phone = null,
            email = null,
            description = null,
            metadata = null
        )
        val (result, error) = CustomersAPI.updateCustomer("cus_123", request)

        assertNotNull(result)
        assertEquals("cus_123", result?.id)
        assertEquals("tester2", result?.name)
    }

    @Test
    fun testGetCustomerWithId() = runBlocking {
        val responseBody = """{"id":"cus_999", "name":"tester3", "phone":"1999999999"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = CustomersAPI.getCustomerWith("cus_999", true)

        assertNotNull(result)
        assertEquals("cus_999", result?.id)
        assertEquals("1999999999", result?.phone)
    }

    @Test
    fun testGetCustomers() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"cus_997", "name":"tester1", "email":"tester1@gmail.com"},
                    {"id":"cus_998", "name":"tester2", "email":"tester2@gmail.com"},
                    {"id":"cus_999", "name":"tester3", "email":"tester3@gmail.com"}
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = CustomersAPI.getCustomers(0, 100)

        assertNotNull(result)
        assertEquals("cus_997", result?.data?.get(0)?.id)
        assertEquals("tester2@gmail.com", result?.data?.get(1)?.email)
        assertEquals("tester3", result?.data?.get(2)?.name)
    }

    @Test
    fun testSearchCustomers() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"cus_998", "name":"tester3", "email":"tester@gmail.com"}
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = CustomersRequests.SearchCustomersRequest(
            phone = null,
            name = null,
            email = "tester@gmail.com",
            createdBefore = null,
            createdAfter = null
        )
        val (result, error) = CustomersAPI.searchCustomers(request)

        assertNotNull(result)
        assertEquals("cus_998", result?.get(0)?.id)
        assertEquals("tester@gmail.com", result?.get(0)?.email)
    }

    @Test
    fun testDeleteCustomerWithId() = runBlocking {
        val responseBody = """{"id":"cus_999", "name":"tester3", "phone":"1999999999"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = CustomersAPI.deleteCustomer("cus_999")

        assertNotNull(result)
        assertEquals("cus_999", result?.id)
        assertEquals("tester3", result?.name)
    }

    @Test
    fun testBlockCustomerWithId() = runBlocking {
        val responseBody = """{"id":"cus_999", "name":"tester3", "phone":"1999999999", "status": "blocked"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = CustomersAPI.blockCustomerWith("cus_999")

        assertNotNull(result)
        assertEquals("cus_999", result?.id)
        assertEquals(FrameObjects.CustomerStatus.blocked, result?.status)
    }

    @Test
    fun testUnblockCustomerWithId() = runBlocking {
        val responseBody = """{"id":"cus_999", "name":"tester3", "phone":"1999999999", "status": "active"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = CustomersAPI.unblockCustomerWith("cus_999")

        assertNotNull(result)
        assertEquals("cus_999", result?.id)
        assertEquals(FrameObjects.CustomerStatus.active, result?.status)
    }
}