package com.framepayments.framesdk

import com.framepayments.framesdk.capabilities.CapabilityObjects
import com.framepayments.framesdk.capabilities.CapabilityRequests
import com.framepayments.framesdk.capabilities.CapabilitiesAPI
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CapabilitiesAPITest {
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
    fun testGetCapabilities() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"cap_1","object":"capability","name":"card_send","status":"active","created_at":1234567890,"updated_at":1234567890},
                    {"id":"cap_2","object":"capability","name":"card_receive","status":"pending","created_at":1234567890,"updated_at":1234567890}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = CapabilitiesAPI.getCapabilities("acc_123")

        assertNotNull(result)
        assertEquals(2, result?.data?.size)
        assertEquals("card_send", result?.data?.get(0)?.name)
        assertEquals("active", result?.data?.get(0)?.status)
    }

    @Test
    fun testRequestCapabilities() = runBlocking {
        val responseBody = """
            [
                {"id":"cap_1","object":"capability","name":"card_send","status":"pending","created_at":1234567890,"updated_at":1234567890}
            ]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(responseBody))

        val request = CapabilityRequests.RequestCapabilitiesRequest(capabilities = listOf("card_send"))
        val (result, error) = CapabilitiesAPI.requestCapabilities("acc_123", request)

        assertNotNull(result)
        assertEquals(1, result?.size)
        assertEquals("card_send", result?.get(0)?.name)
        assertEquals("pending", result?.get(0)?.status)
    }

    @Test
    fun testGetCapabilityWith() = runBlocking {
        val responseBody = """{"id":"cap_1","object":"capability","name":"card_send","status":"active","created_at":1234567890,"updated_at":1234567890}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = CapabilitiesAPI.getCapabilityWith("acc_123", "card_send")

        assertNotNull(result)
        assertEquals("cap_1", result?.id)
        assertEquals("card_send", result?.name)
        assertEquals("active", result?.status)
    }

    @Test
    fun testDisableCapabilityWith() = runBlocking {
        val responseBody = """{"id":"cap_1","object":"capability","name":"card_send","status":"disabled","disabled_reason":"User requested","created_at":1234567890,"updated_at":1234567890}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = CapabilitiesAPI.disableCapabilityWith("acc_123", "card_send")

        assertNotNull(result)
        assertEquals("cap_1", result?.id)
        assertEquals("disabled", result?.status)
    }
}
