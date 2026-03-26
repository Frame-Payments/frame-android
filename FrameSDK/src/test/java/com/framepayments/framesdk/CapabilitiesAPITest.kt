package com.framepayments.framesdk

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

    private companion object {
        fun capabilityJson(
            id: String,
            name: String,
            status: String,
            disabledReason: String? = null,
            disabled: Boolean? = null
        ): String {
            val dr = if (disabledReason != null) ",\"disabled_reason\":\"$disabledReason\"" else ""
            val dis = if (disabled != null) ",\"disabled\":$disabled" else ""
            return """{"id":"$id","object":"capability","name":"$name","account_id":"acc_123","status":"$status","created":"1234567890","updated":"1234567890"$dr$dis}"""
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
    fun testGetCapabilities() = runBlocking {
        val responseBody = """
            {
                "data": [
                    ${capabilityJson("cap_1", "card_send", "active")},
                    ${capabilityJson("cap_2", "card_receive", "pending")}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = CapabilitiesAPI.getCapabilities("acc_123")

        assertNotNull(result)
        assertEquals(2, result?.data?.size)
        assertEquals("card_send", result?.data?.get(0)?.name)
        assertEquals("active", result?.data?.get(0)?.status)
        assertEquals("acc_123", result?.data?.get(0)?.accountId)
    }

    @Test
    fun testRequestCapabilities() = runBlocking {
        val responseBody = """
            [
                ${capabilityJson("cap_1", "card_send", "pending")}
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
        val responseBody = capabilityJson("cap_1", "card_send", "active")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = CapabilitiesAPI.getCapabilityWith("acc_123", "card_send")

        assertNotNull(result)
        assertEquals("cap_1", result?.id)
        assertEquals("card_send", result?.name)
        assertEquals("active", result?.status)
    }

    @Test
    fun testDisableCapabilityWith() = runBlocking {
        val responseBody =
            capabilityJson("cap_1", "card_send", "disabled", disabledReason = "User requested", disabled = true)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = CapabilitiesAPI.disableCapabilityWith("acc_123", "card_send")

        assertNotNull(result)
        assertEquals("cap_1", result?.id)
        assertEquals("disabled", result?.status)
    }
}
