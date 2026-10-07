package com.framepayments.framesdk_ui.viewmodels

import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.framepayments.framesdk.DefaultURLSession
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.accounts.AccountObjects
import com.framepayments.framesdk.checkoutsessions.FrameCheckoutClientSecret
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class FrameCheckoutViewModelCheckoutSecretTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private lateinit var server: MockWebServer
    private lateinit var vm: FrameCheckoutViewModel

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        FrameNetworking.asyncURLSession = DefaultURLSession(OkHttpClient.Builder().build())
        FrameNetworking.mainApiUrl = server.url("/").toString()
        FrameNetworking.apiSecretKey = ""
        FrameNetworking.apiPublishableKey = "pk_test_checkout"
        vm = FrameCheckoutViewModel()
    }

    @After
    fun tearDown() {
        FrameNetworking.apiSecretKey = ""
        FrameNetworking.endOnboardingSession()
        server.shutdown()
    }

    @Test
    fun loadAccountDetails_withCheckoutClientSecret_prefillsNameEmailAndSavedMethods() = runBlocking {
        server.enqueue(accountResponse(firstName = "Ada", lastName = "Lovelace", email = "ada@example.com"))
        server.enqueue(methodsResponse(id = "pm_1", lastFour = "4242"))

        vm.loadAccountDetails(
            accountId = "acc_1",
            amount = 100,
            checkoutClientSecret = FrameCheckoutClientSecret("chk_sess_live", expiresAt = 2_000_000_000),
        )

        awaitUntil { vm.didLoadAccountPaymentMethods.value == true }

        assertEquals("Ada Lovelace", vm.customerName.value)
        assertEquals("ada@example.com", vm.customerEmail.value)
        assertEquals(false, vm.customerInfoRequired.value)
        assertEquals("pm_1", vm.selectedAccountPaymentOption.value?.id)
        assertEquals("4242", vm.selectedAccountPaymentOption.value?.card?.lastFourDigits)
        assertEquals("Bearer chk_sess_live", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer chk_sess_live", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun loadAccountDetails_cancelsPriorInFlightFetch() = runBlocking {
        server.enqueue(
            accountResponse(firstName = "Old", lastName = "Name", email = "old@example.com")
                .setBodyDelay(1, TimeUnit.SECONDS),
        )
        server.enqueue(methodsResponse(id = "pm_old", lastFour = "1111"))

        vm.loadAccountDetails(
            accountId = "acc_1",
            amount = 100,
            checkoutClientSecret = FrameCheckoutClientSecret("chk_sess_live", expiresAt = 2_000_000_000),
        )

        val hostAccount = AccountObjects.Account(
            id = "acc_1",
            accountObject = "account",
            type = AccountObjects.AccountType.INDIVIDUAL,
            status = AccountObjects.AccountStatus.ACTIVE,
            profile = AccountObjects.AccountProfile(
                individual = AccountObjects.IndividualAccount(
                    name = AccountObjects.IndividualAccountName(firstName = "New", lastName = "Name"),
                    email = "new@example.com",
                ),
            ),
            created = 0,
            updated = 0,
            livemode = false,
        )
        val hostMethod = FrameObjects.PaymentMethod(
            id = "pm_host",
            customerId = null,
            billing = null,
            type = FrameObjects.PaymentMethodType.CARD,
            methodObject = "payment_method",
            created = 0,
            updated = 0,
            livemode = false,
            card = null,
            ach = null,
            status = FrameObjects.PaymentMethodStatus.ACTIVE,
        )
        // Host-supplied data skips network and must cancel the in-flight secret fetch.
        vm.loadAccountDetails("acc_1", 100, account = hostAccount, paymentMethods = listOf(hostMethod))

        assertEquals("New Name", vm.customerName.value)
        assertEquals("new@example.com", vm.customerEmail.value)
        assertEquals("pm_host", vm.selectedAccountPaymentOption.value?.id)
        assertEquals(true, vm.didLoadAccountPaymentMethods.value)

        delay(1_200)
        assertEquals("New Name", vm.customerName.value)
        assertEquals("pm_host", vm.selectedAccountPaymentOption.value?.id)
    }

    private suspend fun awaitUntil(timeoutMs: Long = 5_000, predicate: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!predicate() && System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            delay(20)
        }
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue("Timed out waiting for condition", predicate())
    }

    private fun accountResponse(firstName: String, lastName: String, email: String) = MockResponse().setBody(
        """{"id":"acc_1","object":"account","profile":{"individual":{"name":{"first_name":"$firstName","last_name":"$lastName"},"email":"$email"}}}""",
    )

    private fun methodsResponse(id: String, lastFour: String) = MockResponse().setBody(
        """{"data":[{"id":"$id","object":"payment_method","type":"card","status":"active","card":{"brand":"visa","last_four":"$lastFour","exp_month":"12","exp_year":"2027"}}]}""",
    )
}
