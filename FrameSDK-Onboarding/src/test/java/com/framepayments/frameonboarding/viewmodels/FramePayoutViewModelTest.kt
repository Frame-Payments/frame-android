package com.framepayments.frameonboarding.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.FrameResult
import com.framepayments.framesdk.NetworkingError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FramePayoutViewModelTest {

    private fun bank(id: String) = FrameObjects.PaymentMethod(
        id = id,
        customerId = null,
        billing = null,
        type = FrameObjects.PaymentMethodType.ACH,
        methodObject = "payment_method",
        created = 0,
        updated = 0,
        livemode = false,
        card = null,
        ach = FrameObjects.BankAccount(
            accountType = FrameObjects.PaymentAccountType.CHECKING,
            bankName = null,
            accountNumber = null,
            routingNumber = null,
            lastFour = "6789",
        ),
        status = FrameObjects.PaymentMethodStatus.ACTIVE,
    )

    private fun card(id: String) = FrameObjects.PaymentMethod(
        id = id,
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

    @Test
    fun zeroAndAboveAvailableCannotConfirm() {
        val vm = FramePayoutViewModel(availableAmount = 5_000)
        assertEquals(0, vm.typedCents)
        assertFalse(vm.isAmountInRange)
        assertFalse(vm.canConfirm)

        vm.applyLoadedMethods(listOf(bank("pm_1")), primaryId = "pm_1")
        assertEquals("pm_1", vm.selectedPaymentMethodId)
        assertFalse(vm.canConfirm)

        vm.appendDigit(5)
        vm.appendDigit(0)
        vm.appendDigit(0)
        vm.appendDigit(0)
        assertEquals(5_000, vm.typedCents)
        assertTrue(vm.canConfirm)

        vm.appendDigit(1)
        assertEquals(5_000, vm.typedCents)
        assertTrue(vm.canConfirm)

        vm.isLoadingMethods = true
        assertFalse(vm.canConfirm)
        vm.isLoadingMethods = false
        assertTrue(vm.canConfirm)

        vm.replaceTypedCents(5_001)
        assertFalse(vm.isAmountInRange)
        assertFalse(vm.canConfirm)
    }

    @Test
    fun negativeAvailableClampsToZero() {
        val vm = FramePayoutViewModel(availableAmount = -100)
        assertEquals(0, vm.availableAmount)
        vm.appendDigit(1)
        assertEquals(0, vm.typedCents)
        assertFalse(vm.isAmountInRange)
    }

    @Test
    fun quickAmountsHonorTheCap() {
        assertEquals(listOf(1_000, 2_500, 5_000, 10_000), FramePayoutViewModel.quickAmounts)

        val vm = FramePayoutViewModel(availableAmount = 5_000)
        assertTrue(vm.isQuickAmountAvailable(1_000))
        assertTrue(vm.isQuickAmountAvailable(5_000))
        assertFalse(vm.isQuickAmountAvailable(10_000))

        vm.applyQuickAmount(10_000)
        assertEquals(0, vm.typedCents)

        vm.applyQuickAmount(2_500)
        assertEquals(2_500, vm.typedCents)
        assertTrue(vm.isAmountInRange)
    }

    @Test
    fun deleteDigitReturnsToZero() {
        val vm = FramePayoutViewModel(availableAmount = 5_000)
        vm.appendDigit(1)
        vm.appendDigit(2)
        vm.deleteDigit()
        assertEquals(1, vm.typedCents)
        vm.deleteDigit()
        vm.deleteDigit()
        assertEquals(0, vm.typedCents)
        assertFalse(vm.isAmountInRange)
    }

    @Test
    fun loadedMethodsDropCardsAndPrefillPrimary() {
        val vm = FramePayoutViewModel(availableAmount = 5_000)
        assertFalse(vm.didLoadMethods)

        vm.applyLoadedMethods(listOf(card("card_1"), bank("pm_a"), bank("pm_b")), primaryId = "pm_b")
        assertTrue(vm.didLoadMethods)
        assertEquals(listOf("pm_a", "pm_b"), vm.payoutMethods.map { it.id })
        assertEquals("pm_b", vm.selectedPaymentMethodId)
        assertEquals("pm_b", vm.selectedPaymentMethod?.id)
    }

    @Test
    fun soleBankIsSelectedWhenNoPrimaryIsSet() {
        val vm = FramePayoutViewModel(availableAmount = 5_000)
        vm.applyLoadedMethods(listOf(bank("pm_only"), card("card_1")), primaryId = null)
        assertEquals("pm_only", vm.selectedPaymentMethodId)
        assertFalse(vm.canConfirm)
    }

    @Test
    fun severalBanksStayUnselectedWithoutAPrimary() {
        val vm = FramePayoutViewModel(availableAmount = 5_000)
        vm.applyLoadedMethods(listOf(bank("pm_a"), bank("pm_b")), primaryId = "missing")
        assertNull(vm.selectedPaymentMethodId)
        vm.replaceTypedCents(100)
        assertFalse(vm.canConfirm)
    }

    @Test
    fun reloadKeepsAnElectedBankOverThePrimary() {
        val vm = FramePayoutViewModel(availableAmount = 5_000)
        vm.applyLoadedMethods(listOf(bank("pm_a"), bank("pm_b")), primaryId = "pm_a")
        assertEquals("pm_a", vm.selectedPaymentMethodId)

        vm.selectElected("pm_b")
        vm.applyLoadedMethods(listOf(bank("pm_a"), bank("pm_b")), primaryId = "pm_a")
        assertEquals("pm_b", vm.selectedPaymentMethodId)
    }

    @Test
    fun emptyElectionIdIsIgnored() {
        val vm = FramePayoutViewModel(availableAmount = 5_000)
        vm.selectElected("")
        assertNull(vm.selectedPaymentMethodId)
    }

    @Test
    fun idempotencyKeyReusedForTheSameAmountAndBank() {
        val vm = FramePayoutViewModel(availableAmount = 5_000)
        val first = vm.rememberIdempotencyKey(1_000, "pm_1")
        val second = vm.rememberIdempotencyKey(1_000, "pm_1")
        assertEquals(first, second)
        val third = vm.rememberIdempotencyKey(2_000, "pm_1")
        assertTrue(first != third)
    }

    @Test
    fun restoredIdempotencyKeyReusedWhenFingerprintMatches() {
        val state = SavedStateHandle(
            mapOf(
                FramePayoutViewModel.IDEM_KEY to "restored-key",
                FramePayoutViewModel.IDEM_FINGERPRINT to "1000|pm_1",
            ),
        )
        val vm = FramePayoutViewModel(availableAmount = 5_000, savedState = state)
        assertEquals("restored-key", vm.rememberIdempotencyKey(1_000, "pm_1"))
        assertTrue("restored-key" != vm.rememberIdempotencyKey(2_000, "pm_1"))
    }

    @Test
    fun completedPayoutClearsSavedIdempotency() {
        val state = SavedStateHandle()
        val vm = FramePayoutViewModel(availableAmount = 5_000, savedState = state)
        val key = vm.rememberIdempotencyKey(1_000, "pm_1")
        vm.finish(FrameResult.Completed("tr_1"))
        assertNull(state.get<String>(FramePayoutViewModel.IDEM_KEY))
        assertNull(state.get<String>(FramePayoutViewModel.IDEM_FINGERPRINT))
        val restored = FramePayoutViewModel(availableAmount = 5_000, savedState = state)
        assertTrue(key != restored.rememberIdempotencyKey(1_000, "pm_1"))
    }

    @Test
    fun rotateClearsSavedIdempotency() {
        val state = SavedStateHandle()
        val vm = FramePayoutViewModel(availableAmount = 5_000, savedState = state)
        vm.rememberIdempotencyKey(1_000, "pm_1")
        vm.rotateIdempotencyKeyIfNeeded(NetworkingError.ServerError(409, "consumed"))
        assertNull(state.get<String>(FramePayoutViewModel.IDEM_KEY))
        assertNull(state.get<String>(FramePayoutViewModel.IDEM_FINGERPRINT))
    }

    @Test
    fun idempotencyKeyRotatesOnlyOnDeterministicClientErrors() {
        assertFalse(FramePayoutViewModel.shouldRotateIdempotency(NetworkingError.ServerError(500, "timeout")))
        assertFalse(FramePayoutViewModel.shouldRotateIdempotency(NetworkingError.ServerError(504, "gateway")))
        assertFalse(FramePayoutViewModel.shouldRotateIdempotency(NetworkingError.ServerError(408, "timeout")))
        assertFalse(FramePayoutViewModel.shouldRotateIdempotency(NetworkingError.ServerError(429, "rate")))
        assertTrue(FramePayoutViewModel.shouldRotateIdempotency(NetworkingError.ServerError(409, "consumed")))
        assertTrue(FramePayoutViewModel.shouldRotateIdempotency(NetworkingError.ServerError(400, "bad")))
        assertFalse(FramePayoutViewModel.shouldRotateIdempotency(NetworkingError.UnknownError))

        val vm = FramePayoutViewModel(availableAmount = 5_000)
        val key = vm.rememberIdempotencyKey(1_000, "pm_1")
        vm.rotateIdempotencyKeyIfNeeded(NetworkingError.ServerError(500, "timeout"))
        assertEquals(key, vm.rememberIdempotencyKey(1_000, "pm_1"))
        vm.rotateIdempotencyKeyIfNeeded(NetworkingError.ServerError(409, "consumed"))
        assertTrue(key != vm.rememberIdempotencyKey(1_000, "pm_1"))
    }

    @Test
    fun staleSelectionClearsWhenTheBankIsGone() {
        val vm = FramePayoutViewModel(availableAmount = 5_000)
        vm.selectElected("pm_gone")
        vm.applyLoadedMethods(listOf(bank("pm_a"), bank("pm_b")), primaryId = null)
        assertNull(vm.selectedPaymentMethodId)
    }

    @Test
    fun updateAvailableAmountClampsTypedCents() {
        val vm = FramePayoutViewModel(availableAmount = 5_000)
        vm.replaceTypedCents(5_000)
        vm.updateAvailableAmount(2_500)
        assertEquals(2_500, vm.availableAmount)
        assertEquals(2_500, vm.typedCents)
        vm.updateAvailableAmount(10_000)
        assertEquals(10_000, vm.availableAmount)
        assertEquals(2_500, vm.typedCents)
    }

    @Test
    fun sessionCurrencyFormatsAmounts() {
        val vm = FramePayoutViewModel(availableAmount = 2_000, currencyCode = "gbp")
        vm.replaceTypedCents(1_000)
        assertFalse(vm.amountText.contains("$"))
        assertFalse(vm.availableText.contains("$"))
        assertFalse(vm.formattedAmount(1_000).contains("$"))
        vm.updateAvailableAmount(1_500, currencyCode = "eur")
        assertFalse(vm.availableText.contains("$"))
        assertFalse(vm.amountText.contains("$"))
    }
}
