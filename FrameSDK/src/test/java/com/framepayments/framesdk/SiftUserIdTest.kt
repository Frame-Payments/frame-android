package com.framepayments.framesdk

import com.framepayments.framesdk.accounts.AccountRequests
import com.framepayments.framesdk.accounts.AccountsAPI
import com.framepayments.framesdk.checkoutsessions.CheckoutSessionsAPI
import com.framepayments.framesdk.checkoutsessions.FrameCheckoutClientSecret
import com.framepayments.framesdk.managers.SiftManager
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SiftUserIdTest {
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        FrameNetworking.mainApiUrl = mockWebServer.url("/").toString()
        FrameNetworking.apiSecretKey = "sk_test_sift"
        SiftManager.userId = ""
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        SiftManager.userId = ""
    }

    @Test
    fun createAccountSetsSiftUserIdFromResponse() = runBlocking {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"id":"acc_123","object":"account","type":"individual","status":"pending","sift_user_id":"idt_789","created":1,"updated":1,"livemode":false}"""
            )
        )

        val profile = AccountRequests.CreateAccountProfile(
            business = null,
            individual = AccountRequests.CreateIndividualAccount(
                name = com.framepayments.framesdk.accounts.AccountObjects.IndividualAccountName("John", null, "Doe", null),
                email = "john@test.com",
                phone = com.framepayments.framesdk.accounts.AccountObjects.AccountPhoneNumber("1234567890", "1"),
                address = null,
                birthdate = null,
                ssn = null,
            ),
        )
        val (result, _) = AccountsAPI.createAccount(
            AccountRequests.CreateAccountRequest(
                type = com.framepayments.framesdk.accounts.AccountObjects.AccountType.INDIVIDUAL,
                externalId = null,
                termsOfService = null,
                metadata = null,
                profile = profile,
            )
        )

        assertEquals("idt_789", result?.siftUserId)
        assertEquals("idt_789", result?.siftAttributionId)
        assertEquals("idt_789", SiftManager.userId)
    }

    @Test
    fun siftAttributionIdFallsBackToAccountId() = runBlocking {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"id":"acc_123","object":"account","type":"individual","status":"active","created":1,"updated":1,"livemode":false}"""
            )
        )

        val (result, _) = AccountsAPI.getAccountWith("acc_123")

        assertNull(result?.siftUserId)
        assertEquals("acc_123", result?.siftAttributionId)
        assertEquals("acc_123", SiftManager.userId)
    }

    @Test
    fun collectLoginEventOverwriteFalseDoesNotReplace() {
        SiftManager.collectLoginEvent(customerId = "idt_keep", email = "a@example.com")
        SiftManager.collectLoginEvent(customerId = "acc_fallback", email = "", overwrite = false)
        assertEquals("idt_keep", SiftManager.userId)
    }

    @Test
    fun collectLoginEventDefaultOverwriteReplacesExistingId() {
        SiftManager.collectLoginEvent(customerId = "acc_old", email = "a@example.com")
        SiftManager.collectLoginEvent(customerId = "idt_new", email = "a@example.com")
        assertEquals("idt_new", SiftManager.userId)
    }

    @Test
    fun checkoutLoadAccountSetsSiftUserId() = runBlocking {
        mockWebServer.enqueue(
            MockResponse().setBody(
                """{"id":"acc_1","object":"account","sift_user_id":"idt_checkout","profile":{"individual":{"name":{"first_name":"Ada","last_name":"Lovelace"},"email":"ada@example.com"}}}"""
            )
        )

        val secret = FrameCheckoutClientSecret("chk_sess_live", expiresAt = 2_000_000_000L)
        val (account, error) = CheckoutSessionsAPI.loadAccount("acc_1", secret)

        assertNull(error)
        assertEquals("idt_checkout", account?.siftUserId)
        assertEquals("idt_checkout", SiftManager.userId)
    }
}
