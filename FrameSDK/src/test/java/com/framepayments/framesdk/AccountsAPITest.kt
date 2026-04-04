package com.framepayments.framesdk

import com.framepayments.framesdk.accounts.AccountObjects
import com.framepayments.framesdk.accounts.AccountRequests
import com.framepayments.framesdk.accounts.AccountResponses
import com.framepayments.framesdk.accounts.AccountsAPI
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AccountsAPITest {
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
    fun testCreateAccount() = runBlocking {
        val responseBody = """{"id":"acc_123","object":"account","type":"individual","status":"pending","created":1234567890,"updated":1234567890,"livemode":false}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val profile = AccountRequests.CreateAccountProfile(
            business = null,
            individual = AccountRequests.CreateIndividualAccount(
                name = AccountObjects.IndividualAccountName("John", null, "Doe", null),
                email = "john@test.com",
                phone = AccountObjects.AccountPhoneNumber("1234567890", "1"),
                address = null,
                birthdate = null,
                ssn = null
            )
        )
        val request = AccountRequests.CreateAccountRequest(
            type = AccountObjects.AccountType.INDIVIDUAL,
            externalId = null,
            termsOfService = null,
            metadata = null,
            profile = profile
        )
        val (result, error) = AccountsAPI.createAccount(request)

        assertNotNull(result)
        assertEquals("acc_123", result?.id)
        assertEquals(AccountObjects.AccountStatus.PENDING, result?.status)
    }

    @Test
    fun testGetAccountWith() = runBlocking {
        val responseBody = """{"id":"acc_123","object":"account","type":"individual","status":"active","created":1234567890,"updated":1234567890,"livemode":false}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = AccountsAPI.getAccountWith("acc_123")

        assertNotNull(result)
        assertEquals("acc_123", result?.id)
        assertEquals(AccountObjects.AccountStatus.ACTIVE, result?.status)
    }

    @Test
    fun testGetAccounts() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"acc_1","object":"account","type":"individual","status":"active","created":1234567890,"updated":1234567890,"livemode":false},
                    {"id":"acc_2","object":"account","type":"business","status":"pending","created":1234567890,"updated":1234567890,"livemode":false}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = AccountsAPI.getAccounts()

        assertNotNull(result)
        assertEquals(2, result?.data?.size)
        assertEquals("acc_1", result?.data?.get(0)?.id)
        assertEquals("acc_2", result?.data?.get(1)?.id)
    }

    @Test
    fun testDeleteAccountWith() = runBlocking {
        val responseBody = """{"id":"acc_123","object":"account","type":"individual","status":"disabled","created":1234567890,"updated":1234567890,"livemode":false}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = AccountsAPI.deleteAccountWith("acc_123")

        assertNotNull(result)
        assertEquals("acc_123", result?.id)
    }

    @Test
    fun testSearchAccounts() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"acc_1","object":"account","type":"individual","status":"active","created":1234567890,"updated":1234567890,"livemode":false}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = AccountsAPI.searchAccounts(email = "john@example.com")

        assertNotNull(result)
        assertEquals(1, result?.data?.size)
        assertEquals("acc_1", result?.data?.get(0)?.id)

        val recorded = mockWebServer.takeRequest()
        assertEquals("/v1/accounts/search", recorded.requestUrl?.encodedPath)
        assertEquals("john@example.com", recorded.requestUrl?.queryParameter("email"))
    }

    @Test
    fun testRestrictAccount() = runBlocking {
        val responseBody = """{"id":"acc_123","object":"account","type":"individual","status":"restricted","created":1234567890,"updated":1234567890,"livemode":false}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = AccountsAPI.restrictAccount("acc_123")

        assertNotNull(result)
        assertEquals("acc_123", result?.id)
        assertEquals(AccountObjects.AccountStatus.RESTRICTED, result?.status)

        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.requestUrl?.encodedPath?.endsWith("/restrict") == true)
    }

    @Test
    fun testUnrestrictAccount() = runBlocking {
        val responseBody = """{"id":"acc_123","object":"account","type":"individual","status":"active","created":1234567890,"updated":1234567890,"livemode":false}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = AccountsAPI.unrestrictAccount("acc_123")

        assertNotNull(result)
        assertEquals("acc_123", result?.id)
        assertEquals(AccountObjects.AccountStatus.ACTIVE, result?.status)

        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.requestUrl?.encodedPath?.endsWith("/unrestrict") == true)
    }

    @Test
    fun testCreatePhoneVerification() = runBlocking {
        val responseBody = """{"id":"pv_123","object":"phone_verification","account_id":"acc_123","status":"pending","created":1234567890,"updated":1234567890,"livemode":false}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = AccountsAPI.createPhoneVerification("acc_123")

        assertNotNull(result)
        assertEquals("pv_123", result?.id)
        assertEquals("acc_123", result?.accountId)
        assertEquals("pending", result?.status)

        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.requestUrl?.encodedPath?.contains("/phone_verifications") == true)
    }

    @Test
    fun testConfirmPhoneVerification() = runBlocking {
        val responseBody = """{"id":"pv_123","object":"phone_verification","account_id":"acc_123","status":"verified","created":1234567890,"updated":1234567890,"livemode":false}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = AccountRequests.ConfirmPhoneVerificationRequest(code = "123456")
        val (result, error) = AccountsAPI.confirmPhoneVerification("acc_123", "pv_123", request)

        assertNotNull(result)
        assertEquals("pv_123", result?.id)
        assertEquals("verified", result?.status)

        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.requestUrl?.encodedPath?.endsWith("/confirm") == true)
    }

    @Test
    fun testGetPlaidLinkToken() = runBlocking {
        val responseBody = """{"link_token":"link-sandbox-abc123"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = AccountsAPI.getPlaidLinkToken("acc_123")

        assertNotNull(result)
        assertNull(error)
        assertEquals("link-sandbox-abc123", result?.linkToken)

        val recorded = mockWebServer.takeRequest()
        assertEquals("GET", recorded.method)
        assertTrue(recorded.requestUrl?.encodedPath?.endsWith("/plaid_link_token") == true)
    }

    @Test
    fun testGetPlaidLinkTokenEmptyId() = runBlocking {
        val (result, error) = AccountsAPI.getPlaidLinkToken("")

        assertNull(result)
        assertNull(error)
    }

    @Test
    fun testGetPlaidLinkTokenNetworkError() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val (result, error) = AccountsAPI.getPlaidLinkToken("acc_123")

        assertNull(result)
        assertNotNull(error)
    }
}
