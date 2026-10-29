package com.framepayments.framesdk.payoutsessions

import com.framepayments.framesdk.DefaultURLSession
import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.QueryItem
import com.framepayments.framesdk.checkoutsessions.decodeSessionPaymentMethods
import com.framepayments.framesdk.transfersv2.TransferV2Money
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PayoutSessionsAPITest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        FrameNetworking.asyncURLSession = DefaultURLSession(OkHttpClient.Builder().build())
        FrameNetworking.mainApiUrl = server.url("/").toString()
        FrameNetworking.apiSecretKey = "sk_test_payout"
        FrameNetworking.apiPublishableKey = "pk_test_payout"
    }

    @After
    fun tearDown() {
        FrameNetworking.apiSecretKey = ""
        FrameNetworking.endOnboardingSession()
        server.shutdown()
    }

    @Test
    fun clientSecretExpires() {
        val live = FramePayoutClientSecret(
            clientSecret = "pyt_sess_live",
            expiresAt = System.currentTimeMillis() / 1000 + 1_800,
            amountCents = 5_000,
        )
        assertFalse(live.isExpired())
        assertEquals("usd", live.amountCurrency)

        val dead = FramePayoutClientSecret(
            clientSecret = "pyt_sess_old",
            expiresAt = 0,
            amountCents = 5_000,
            amountCurrency = "usd",
        )
        assertTrue(dead.isExpired())
    }

    @Test
    fun decodesSlimPayoutAccount() {
        val account = decodePayoutAccountResource(
            """{"id":"acc_1","object":"account","payout_payment_method_id":"pm_bank"}""".toByteArray(),
        )
        assertEquals("acc_1", account?.id)
        assertEquals("pm_bank", account?.payoutPaymentMethodId)
    }

    @Test
    fun accountErrorBodyDoesNotBecomeAStub() {
        val account = decodePayoutAccountResource(
            """{"error":{"message":"Invalid or expired client secret."}}""".toByteArray(),
        )
        assertNull(account)
    }

    @Test
    fun accountWithoutObjectDoesNotBecomeAStub() {
        val account = decodePayoutAccountResource("""{"id":"acc_1"}""".toByteArray())
        assertNull(account)
    }

    @Test
    fun decodesStrippedPaymentMethodList() {
        val methods = decodeSessionPaymentMethods(
            """{"data":[{"id":"pm_1","object":"payment_method","type":"ach","status":"active","ach":{"account_type":"checking","last_four":"6789"}}]}""".toByteArray(),
        )
        assertEquals(listOf("pm_1"), methods?.map { it.id })
        assertEquals("6789", methods?.first()?.ach?.lastFour)
        assertNull(methods?.first()?.card)
    }

    @Test
    fun paymentMethodErrorBodyDoesNotLookLikeAnEmptyList() {
        val methods = decodeSessionPaymentMethods(
            """{"error":{"message":"Invalid or expired client secret."}}""".toByteArray(),
        )
        assertNull(methods)
    }

    @Test
    fun emptyPaymentMethodListStaysEmpty() {
        val methods = decodeSessionPaymentMethods("""{"data":[]}""".toByteArray())
        assertEquals(emptyList<Any>(), methods)
    }

    @Test
    fun expiredTokenIsRefreshedBeforeTheRead() = runBlocking {
        server.enqueue(sessionResponse())
        server.enqueue(accountResponse())

        val secret = FramePayoutClientSecret("pyt_sess_old", expiresAt = 0, amountCents = 5_000)
        val (primaryId, error) = PayoutSessionsAPI.loadAccount("acc_1", secret)

        assertNull(error)
        assertEquals("pm_bank", primaryId)
        assertEquals("pyt_sess_new", secret.clientSecret)
        assertEquals("Bearer sk_test_payout", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer pyt_sess_new", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun unauthorizedReadRefreshesOnce() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":"Invalid or expired client secret."}"""))
        server.enqueue(sessionResponse())
        server.enqueue(methodsResponse())

        val secret = FramePayoutClientSecret("pyt_sess_old", expiresAt = 2_000_000_000, amountCents = 5_000)
        val (methods, error) = PayoutSessionsAPI.loadPaymentMethods("acc_1", secret)

        assertNull(error)
        assertEquals("pm_1", methods?.first()?.id)
        assertEquals("6789", methods?.first()?.ach?.lastFour)
        assertEquals("pyt_sess_new", secret.clientSecret)
        assertEquals("Bearer pyt_sess_old", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer sk_test_payout", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer pyt_sess_new", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun refreshReplaysAmountCap() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"id":"ps_1","account_id":"acc_1","client_secret":"pyt_sess_new","object":"payout_session","expires_at":2000000000,"livemode":false,"amount":{"value":5000,"currency":"usd"}}""",
            ),
        )
        server.enqueue(accountResponse())

        val secret = FramePayoutClientSecret(
            "pyt_sess_old",
            expiresAt = 0,
            amountCents = 5_000,
            amountCurrency = "usd",
        )
        val (primaryId, error) = PayoutSessionsAPI.loadAccount("acc_1", secret)

        assertNull(error)
        assertEquals("pm_bank", primaryId)
        val mint = server.takeRequest()
        assertTrue(mint.body.readUtf8().contains("\"value\":5000"))
        assertEquals(5_000, secret.amountCents)
        assertEquals("usd", secret.amountCurrency)
        assertEquals("pyt_sess_new", secret.clientSecret)
    }

    @Test
    fun createPayoutSessionUsesSecretKeyEvenDuringOnboardingSession() = runBlocking {
        FrameNetworking.beginOnboardingSession("onb_sess_live")
        server.enqueue(sessionResponse())

        val (session, error) = PayoutSessionsAPI.createPayoutSession(
            "acc_1",
            TransferV2Money(value = 5_000, currency = "usd"),
        )

        assertNull(error)
        assertEquals("pyt_sess_new", session?.clientSecret)
        assertEquals("Bearer sk_test_payout", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun beginOnboardingSessionAcceptsPayoutSessionToken() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok":true}"""))

        FrameNetworking.beginOnboardingSession("pyt_sess_live")
        FrameNetworking.performDataTask(
            object : FrameNetworkingEndpoints {
                override val httpMethod = "GET"
                override val endpointURL = "/v1/accounts/acc_1/elect_payout_method"
                override val queryItems: List<QueryItem>? = null
            },
            FrameAuthMode.Secret,
        )

        assertEquals(
            "Bearer pyt_sess_live",
            server.takeRequest().getHeader("Authorization"),
        )
    }

    @Test
    fun expiredTokenWithoutSecretKeyStillSendsTheExistingBearer() = runBlocking {
        FrameNetworking.apiSecretKey = ""
        server.enqueue(accountResponse())

        val secret = FramePayoutClientSecret("pyt_sess_old", expiresAt = 0, amountCents = 5_000)
        val (primaryId, error) = PayoutSessionsAPI.loadAccount("acc_1", secret)

        assertNull(error)
        assertEquals("pm_bank", primaryId)
        assertEquals("Bearer pyt_sess_old", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun loadAccountReturnsDecodingFailedOnMalformedBody() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"id":"acc_1"}"""))

        val secret = FramePayoutClientSecret("pyt_sess_live", expiresAt = 2_000_000_000, amountCents = 5_000)
        val (primaryId, error) = PayoutSessionsAPI.loadAccount("acc_1", secret)

        assertNull(primaryId)
        assertEquals(NetworkingError.DecodingFailed, error)
    }

    @Test
    fun loadPaymentMethodsReturnsDecodingFailedOnErrorJson() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"error":{"message":"Invalid or expired client secret."}}"""))

        val secret = FramePayoutClientSecret("pyt_sess_live", expiresAt = 2_000_000_000, amountCents = 5_000)
        val (methods, error) = PayoutSessionsAPI.loadPaymentMethods("acc_1", secret)

        assertNull(methods)
        assertEquals(NetworkingError.DecodingFailed, error)
    }

    @Test
    fun concurrentExpiredReadsShareOneMint() = runBlocking {
        server.enqueue(sessionResponse())
        server.enqueue(accountResponse())
        server.enqueue(accountResponse())

        val secret = FramePayoutClientSecret("pyt_sess_old", expiresAt = 0, amountCents = 5_000)
        val first = async { PayoutSessionsAPI.loadAccount("acc_1", secret) }
        val second = async { PayoutSessionsAPI.loadAccount("acc_1", secret) }
        val (firstId, firstError) = first.await()
        val (secondId, secondError) = second.await()

        assertNull(firstError)
        assertNull(secondError)
        assertEquals("pm_bank", firstId)
        assertEquals("pm_bank", secondId)
        assertEquals("pyt_sess_new", secret.clientSecret)
        assertEquals("Bearer sk_test_payout", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer pyt_sess_new", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer pyt_sess_new", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun expiredTokenPropagatesMintErrorWhenRefreshFails() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"error":"mint failed"}"""))

        val secret = FramePayoutClientSecret("pyt_sess_old", expiresAt = 0, amountCents = 5_000)
        val (primaryId, error) = PayoutSessionsAPI.loadAccount("acc_1", secret)

        assertNull(primaryId)
        assertTrue(error is NetworkingError.ServerError)
        assertEquals(500, (error as NetworkingError.ServerError).statusCode)
        assertEquals("pyt_sess_old", secret.clientSecret)
        assertEquals("Bearer sk_test_payout", server.takeRequest().getHeader("Authorization"))
    }

    private fun sessionResponse() = MockResponse().setBody(
        """{"id":"ps_1","account_id":"acc_1","client_secret":"pyt_sess_new","object":"payout_session","expires_at":2000000000,"livemode":false,"amount":{"value":5000,"currency":"usd"}}""",
    )

    private fun accountResponse() = MockResponse().setBody(
        """{"id":"acc_1","object":"account","payout_payment_method_id":"pm_bank"}""",
    )

    private fun methodsResponse() = MockResponse().setBody(
        """{"data":[{"id":"pm_1","object":"payment_method","type":"ach","status":"active","ach":{"account_type":"checking","last_four":"6789"}}]}""",
    )
}
