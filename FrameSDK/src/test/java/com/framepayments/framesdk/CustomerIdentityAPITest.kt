package com.framepayments.framesdk

import com.framepayments.framesdk.customeridentity.CustomerIdentityAPI
import com.framepayments.framesdk.customeridentity.CustomerIdentityRequests
import com.framepayments.framesdk.customeridentity.CustomerIdentityStatus
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CustomerIdentityAPITest {
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
    fun testCreateCustomerIdentity() = runBlocking {
        val responseBody = """{"id":"iden_123", "status":"pending"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = CustomerIdentityRequests.CreateCustomerIdentityRequest(
            address = FrameObjects.BillingAddress(
                city = "Los Angeles",
                country = "",
                state = "CA",
                postalCode = "11111",
                addressLine1 = "1 Tester Way",
                addressLine2 = ""
            ),
            firstName = "tester",
            lastName = "testing",
            dateOfBirth = "11/11/2011",
            phoneNumber = "111-111-1111",
            email = "tester@gmail.com",
            ssn = "XXX-XXX-XXXX"
        )
        val result = CustomerIdentityAPI.createCustomerIdentity(request)

        assertNotNull(result)
        assertEquals("iden_123", result?.id)
        assertEquals(CustomerIdentityStatus.pending, result?.status)
    }

    @Test
    fun testGetCustomerIdentityWithId() = runBlocking {
        val responseBody = """{"id":"iden_124", "status":"failed"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val result = CustomerIdentityAPI.getCustomerIdentityWith("iden_124")

        assertNotNull(result)
        assertEquals("iden_124", result?.id)
        assertEquals(CustomerIdentityStatus.failed, result?.status)
    }
}