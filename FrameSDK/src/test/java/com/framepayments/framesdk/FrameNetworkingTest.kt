package com.framepayments.framesdk

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(org.robolectric.RobolectricTestRunner::class)
class FrameNetworkingTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val testClient = OkHttpClient.Builder().build()
        FrameNetworking.asyncURLSession = DefaultURLSession(testClient)

        FrameNetworking.mainApiUrl = mockWebServer.url("/").toString()
        FrameNetworking.apiSecretKey = "sk_test_key"
        FrameNetworking.apiPublishableKey = "pk_test_key"
        FrameNetworking.debugMode = true
    }

    @After
    fun tearDown() {
        FrameNetworking.endOnboardingSession()
        mockWebServer.shutdown()
    }

    @Test
    fun performDataTaskReturnsData() = runBlocking {
        val body = """{"message": "success"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val endpoint = TestEndpoint("GET", "/test")

        val (data, error) = FrameNetworking.performDataTask(endpoint)

        assertNull(error)
        assertNotNull(data)
        assertTrue(String(data!!).contains("success"))
    }

    @Test
    fun performDataTaskReturnsError() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("{\"error\":\"fail\"}"))

        val endpoint = TestEndpoint("GET", "/test")

        val (data, error) = FrameNetworking.performDataTask(endpoint)

        assertTrue(error is NetworkingError.ServerError)
        assertNotNull(data)
        assertTrue(String(data!!).contains("error"))
    }

    @Test
    fun performDataTaskWithRequestHandlesPOST() = runBlocking {
        val body = """{"status": "ok"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val endpoint = TestEndpoint("POST", "/submit")
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

    @Test
    fun clientSecretAuthModeUsedAsBearerToken() = runBlocking {
        val body = """{"id":"ci_123"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val endpoint = TestEndpoint("GET", "/test")
        FrameNetworking.performDataTask(endpoint, FrameAuthMode.ClientSecret("ci_123_secret_xyz"))

        val recorded = mockWebServer.takeRequest()
        assertEquals("Bearer ci_123_secret_xyz", recorded.getHeader("Authorization"))
    }

    @Test
    fun publishableKeyUsedAsBearerTokenByDefault() = runBlocking {
        val body = """{"ok":true}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val endpoint = TestEndpoint("GET", "/test")
        FrameNetworking.performDataTask(endpoint)

        val recorded = mockWebServer.takeRequest()
        assertEquals("Bearer pk_test_key", recorded.getHeader("Authorization"))
    }

    @Test
    fun onboardingSessionTokenOverridesPublishableKey() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok":true}"""))

        FrameNetworking.beginOnboardingSession("onb_sess_abc123")
        FrameNetworking.performDataTask(TestEndpoint("GET", "/test"))

        val recorded = mockWebServer.takeRequest()
        assertEquals("Bearer onb_sess_abc123", recorded.getHeader("Authorization"))
    }

    @Test
    fun onboardingSessionTokenOverridesSecretAuth() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok":true}"""))

        FrameNetworking.beginOnboardingSession("onb_sess_abc123")
        FrameNetworking.performDataTask(TestEndpoint("GET", "/test"), FrameAuthMode.Secret)

        val recorded = mockWebServer.takeRequest()
        assertEquals("Bearer onb_sess_abc123", recorded.getHeader("Authorization"))
    }

    @Test
    fun clientSecretWinsOverActiveOnboardingSession() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok":true}"""))

        FrameNetworking.beginOnboardingSession("onb_sess_abc123")
        FrameNetworking.performDataTask(TestEndpoint("GET", "/test"), FrameAuthMode.ClientSecret("ci_123_secret_xyz"))

        val recorded = mockWebServer.takeRequest()
        assertEquals("Bearer ci_123_secret_xyz", recorded.getHeader("Authorization"))
    }

    @Test
    fun endOnboardingSessionRestoresPublishableKey() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok":true}"""))

        FrameNetworking.beginOnboardingSession("onb_sess_abc123")
        FrameNetworking.endOnboardingSession()
        FrameNetworking.performDataTask(TestEndpoint("GET", "/test"))

        val recorded = mockWebServer.takeRequest()
        assertEquals("Bearer pk_test_key", recorded.getHeader("Authorization"))
    }

    class TestEndpoint(
        override val httpMethod: String,
        override val endpointURL: String,
        override val queryItems: List<QueryItem>? = null
    ) : FrameNetworkingEndpoints
}