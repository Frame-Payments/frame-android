package com.framepayments.framesdk.checkoutsessions

import com.framepayments.framesdk.DefaultURLSession
import com.framepayments.framesdk.FrameNetworking
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import com.framepayments.framesdk.NetworkingError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CheckoutSessionsAPITest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        FrameNetworking.asyncURLSession = DefaultURLSession(OkHttpClient.Builder().build())
        FrameNetworking.mainApiUrl = server.url("/").toString()
        FrameNetworking.apiSecretKey = "sk_test_checkout"
        FrameNetworking.apiPublishableKey = "pk_test_checkout"
    }

    @After
    fun tearDown() {
        FrameNetworking.apiSecretKey = ""
        FrameNetworking.endOnboardingSession()
        server.shutdown()
    }

    @Test
    fun expiredTokenIsRefreshedBeforeTheRead() = runBlocking {
        server.enqueue(sessionResponse())
        server.enqueue(accountResponse())

        val secret = FrameCheckoutClientSecret("chk_sess_old", expiresAt = 0)
        val (account, error) = CheckoutSessionsAPI.loadAccount("acc_1", secret)

        assertNull(error)
        assertEquals("Ada", account?.profile?.individual?.name?.firstName)
        assertEquals("ada@example.com", account?.profile?.individual?.email)
        assertEquals("chk_sess_new", secret.clientSecret)
        assertEquals("Bearer sk_test_checkout", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer chk_sess_new", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun unauthorizedReadRefreshesOnce() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":"Invalid or expired client secret."}"""))
        server.enqueue(sessionResponse())
        server.enqueue(methodsResponse())

        val secret = FrameCheckoutClientSecret("chk_sess_old", expiresAt = 2_000_000_000)
        val (methods, error) = CheckoutSessionsAPI.loadPaymentMethods("acc_1", secret)

        assertNull(error)
        assertEquals("pm_1", methods?.first()?.id)
        assertEquals("4242", methods?.first()?.card?.lastFourDigits)
        assertEquals("visa", methods?.first()?.card?.brand)
        assertEquals("chk_sess_new", secret.clientSecret)
        assertEquals("Bearer chk_sess_old", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer sk_test_checkout", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer chk_sess_new", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun redactedPaymentMethodListMapsCardAndAch() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"data":[
                  {"id":"pm_card","object":"payment_method","type":"card","status":"active","card":{"brand":"visa","last_four":"4242","exp_month":"12","exp_year":"2027"}},
                  {"id":"pm_ach","object":"payment_method","type":"ach","status":"active","ach":{"account_type":"checking","last_four":"6789"}}
                ]}""",
            ),
        )

        val secret = FrameCheckoutClientSecret("chk_sess_live", expiresAt = 2_000_000_000)
        val (methods, error) = CheckoutSessionsAPI.loadPaymentMethods("acc_1", secret)

        assertNull(error)
        assertEquals(2, methods?.size)
        assertEquals("pm_card", methods?.get(0)?.id)
        assertEquals("4242", methods?.get(0)?.card?.lastFourDigits)
        assertNull(methods?.get(0)?.card?.issuer)
        assertEquals("pm_ach", methods?.get(1)?.id)
        assertEquals("6789", methods?.get(1)?.ach?.lastFour)
        assertNull(methods?.get(1)?.ach?.routingNumber)
    }

    @Test
    fun expiredTokenPropagatesMintErrorWhenRefreshFails() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"error":"mint failed"}"""))

        val secret = FrameCheckoutClientSecret("chk_sess_old", expiresAt = 0)
        val (account, error) = CheckoutSessionsAPI.loadAccount("acc_1", secret)

        assertNull(account)
        assertTrue(error is NetworkingError.ServerError)
        assertEquals(500, (error as NetworkingError.ServerError).statusCode)
        assertEquals("chk_sess_old", secret.clientSecret)
        assertEquals("Bearer sk_test_checkout", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun refreshReplaysLockedAmount() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"id":"cs_1","account_id":"acc_1","client_secret":"chk_sess_new","object":"checkout_session","expires_at":2000000000,"livemode":false,"amount":{"value":4398,"currency":"usd"}}"""
            )
        )
        server.enqueue(accountResponse())

        val secret = FrameCheckoutClientSecret(
            "chk_sess_old",
            expiresAt = 0,
            amountCents = 4398,
            amountCurrency = "usd",
        )
        val (account, error) = CheckoutSessionsAPI.loadAccount("acc_1", secret)

        assertNull(error)
        assertEquals("Ada", account?.profile?.individual?.name?.firstName)
        val mint = server.takeRequest()
        assertTrue(mint.body.readUtf8().contains("\"value\":4398"))
        assertEquals(4398, secret.amountCents)
        assertEquals("usd", secret.amountCurrency)
        assertEquals("chk_sess_new", secret.clientSecret)
    }

    @Test
    fun unauthorizedReadKeepsOriginal401WhenRefreshFails() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":"Invalid or expired client secret."}"""))
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"error":"mint failed"}"""))

        val secret = FrameCheckoutClientSecret("chk_sess_old", expiresAt = 2_000_000_000)
        val (methods, error) = CheckoutSessionsAPI.loadPaymentMethods("acc_1", secret)

        assertNull(methods)
        assertTrue(error is NetworkingError.ServerError)
        assertEquals(401, (error as NetworkingError.ServerError).statusCode)
        assertEquals("Bearer chk_sess_old", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer sk_test_checkout", server.takeRequest().getHeader("Authorization"))
    }

    private fun sessionResponse() = MockResponse().setBody(
        """{"id":"cs_1","account_id":"acc_1","client_secret":"chk_sess_new","object":"checkout_session","expires_at":2000000000,"livemode":false}"""
    )

    private fun accountResponse() = MockResponse().setBody(
        """{"id":"acc_1","object":"account","profile":{"individual":{"name":{"first_name":"Ada","last_name":"Lovelace"},"email":"ada@example.com"}}}"""
    )

    private fun methodsResponse() = MockResponse().setBody(
        """{"data":[{"id":"pm_1","object":"payment_method","type":"card","status":"active","card":{"brand":"visa","last_four":"4242","exp_month":"12","exp_year":"2027"}}]}"""
    )
}
