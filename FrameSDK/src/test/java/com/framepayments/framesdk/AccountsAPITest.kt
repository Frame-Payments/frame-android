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
        val responseBody = """{"id":"acc_123","object":"account","type":"individual","status":"pending","created_at":1234567890,"updated_at":1234567890,"livemode":false}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val profile = AccountRequests.CreateAccountProfile(
            business = null,
            individual = AccountRequests.CreateIndividualAccount(
                name = AccountRequests.CreateAccountInfo("John", null, "Doe", null),
                email = "john@test.com",
                phone = AccountObjects.AccountPhoneNumber("1234567890", "1"),
                address = null,
                dob = null,
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
        val responseBody = """{"id":"acc_123","object":"account","type":"individual","status":"active","created_at":1234567890,"updated_at":1234567890,"livemode":false}"""
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
                    {"id":"acc_1","object":"account","type":"individual","status":"active","created_at":1234567890,"updated_at":1234567890,"livemode":false},
                    {"id":"acc_2","object":"account","type":"business","status":"pending","created_at":1234567890,"updated_at":1234567890,"livemode":false}
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
        val responseBody = """{"id":"acc_123","object":"account","type":"individual","status":"disabled","created_at":1234567890,"updated_at":1234567890,"livemode":false}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = AccountsAPI.deleteAccountWith("acc_123")

        assertNotNull(result)
        assertEquals("acc_123", result?.id)
    }
}
