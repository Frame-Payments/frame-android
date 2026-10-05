package com.framepayments.framesdk.checkoutsessions

import com.framepayments.framesdk.DefaultURLSession
import com.framepayments.framesdk.FrameNetworking
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
