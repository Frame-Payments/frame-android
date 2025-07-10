package com.framepayments.framesdk

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FrameNetworkingTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        NetworkingConstants.MAIN_API_URL = mockWebServer.url("/").toString()
        FrameNetworking.apiKey = "test_api_key"
        FrameNetworking.debugMode = true
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun performDataTaskReturnsData() = runBlocking {
        val body = """{"message": "success"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val endpoint = TestEndpoint("GET", "test")

        val (data, error) = FrameNetworking.performDataTask(endpoint)

        assertNull(error)
        assertNotNull(data)
        assertTrue(String(data!!).contains("success"))
    }

    @Test
    fun performDataTaskReturnsError() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val endpoint = TestEndpoint("GET", "test")

        val (data, error) = FrameNetworking.performDataTask(endpoint)

        assertNull(data)
        assertTrue(error is NetworkingError.ServerError)
    }

    @Test
    fun performDataTaskWithRequestHandlesPOST() = runBlocking {
        val body = """{"status": "ok"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val endpoint = TestEndpoint("POST", "submit")
        val requestObj = mapOf("key" to "value")

        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, requestObj)

        assertNull(error)
        assertNotNull(data)
        assertTrue(String(data!!).contains("ok"))
    }

    @Test
    fun parseResponseCorrectlyParsesJSON() {
        val json = """{"name":"test"}""".toByteArray()
        data class TestData(val name: String)

        val result = FrameNetworking.parseResponse<TestData>(json)

        assertNotNull(result)
        assertEquals("test", result?.name)
    }

    class TestEndpoint(
        override val httpMethod: String,
        override val endpointURL: String,
        override val queryItems: List<QueryItem>? = null
    ) : FrameNetworkingEndpoints
}