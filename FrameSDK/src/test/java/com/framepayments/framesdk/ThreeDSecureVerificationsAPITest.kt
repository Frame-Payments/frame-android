package com.framepayments.framesdk

import com.framepayments.framesdk.threedsecure.ThreeDSecureVerificationsAPI
import com.framepayments.framesdk.threedsecure.ThreeDSecureRequests
import com.framepayments.framesdk.threedsecure.VerificationStatus
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ThreeDSecureVerificationsAPITest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        FrameNetworking.mainApiUrl = mockWebServer.url("/").toString()
        FrameNetworking.apiKey = "test_key"
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testCreate3DSecureVerification() = runBlocking {
        val responseBody = """
            {
                "id": "intent_3ds_123",
                "customer": "cus_123",
                "payment_method": "pm_456",
                "object": "3ds_intent",
                "livemode": false,
                "status": "pending",
                "challenge_url": "https://challenge.example.com/123",
                "completed": null,
                "created": 1234567890,
                "updated": 1234567890
            }
        """.trimIndent().replace("\n", "")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = ThreeDSecureRequests.CreateThreeDSecureVerification(paymentMethodId = "pm_456")
        val (verification, verificationError, error) = ThreeDSecureVerificationsAPI.create3DSecureVerification(request)

        assertNotNull(verification)
        assertNull(verificationError)
        assertEquals("intent_3ds_123", verification?.id)
        assertEquals("cus_123", verification?.customer)
        assertEquals("pm_456", verification?.paymentMethod)
        assertEquals(VerificationStatus.PENDING, verification?.status)
        assertEquals("https://challenge.example.com/123", verification?.challengeUrl)
    }

    @Test
    fun testCreate3DSecureVerificationReturnsError() = runBlocking {
        val responseBody = """{"error":{"type":"invalid_request","message":"Existing intent found","existing_intent_id":"intent_existing_1"}}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = ThreeDSecureRequests.CreateThreeDSecureVerification(paymentMethodId = "pm_456")
        val (verification, verificationError, error) = ThreeDSecureVerificationsAPI.create3DSecureVerification(request)

        assertNull(verification)
        assertNotNull(verificationError)
        assertNotNull(verificationError?.error)
        assertEquals("invalid_request", verificationError?.error?.type)
        assertEquals("Existing intent found", verificationError?.error?.message)
        assertEquals("intent_existing_1", verificationError?.error?.existingIntentId)
    }

    @Test
    fun testRetrieve3DSecureVerification() = runBlocking {
        val responseBody = """
            {
                "id": "intent_3ds_456",
                "customer": "cus_789",
                "payment_method": "pm_abc",
                "object": "3ds_intent",
                "livemode": false,
                "status": "succeeded",
                "challenge_url": "https://challenge.example.com/456",
                "completed": 1234567891,
                "created": 1234567890,
                "updated": 1234567891
            }
        """.trimIndent().replace("\n", "")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = ThreeDSecureVerificationsAPI.retrieve3DSecureVerification("intent_3ds_456")

        assertNotNull(result)
        assertEquals("intent_3ds_456", result?.id)
        assertEquals(VerificationStatus.SUCCEEDED, result?.status)
        assertEquals(1234567891, result?.completed)
    }

    @Test
    fun testResend3DSecureVerification() = runBlocking {
        val responseBody = """
            {
                "id": "intent_3ds_789",
                "customer": "cus_def",
                "payment_method": "pm_ghi",
                "object": "3ds_intent",
                "livemode": false,
                "status": "pending",
                "challenge_url": "https://challenge.example.com/resend",
                "completed": null,
                "created": 1234567890,
                "updated": 1234567892
            }
        """.trimIndent().replace("\n", "")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = ThreeDSecureVerificationsAPI.resend3DSecureVerification("intent_3ds_789")

        assertNotNull(result)
        assertEquals("intent_3ds_789", result?.id)
        assertEquals("https://challenge.example.com/resend", result?.challengeUrl)
        assertEquals(VerificationStatus.PENDING, result?.status)
    }
}
