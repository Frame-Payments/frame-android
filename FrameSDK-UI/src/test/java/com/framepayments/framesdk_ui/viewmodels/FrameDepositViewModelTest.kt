package com.framepayments.framesdk_ui.viewmodels

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FrameDepositViewModelTest {

    @Test
    fun rangeRequiresPositiveCentsAndHonorsMinAndMax() {
        val open = FrameDepositViewModel()
        assertFalse(open.isAmountInRange)
        open.appendDigit(1)
        assertEquals(1, open.typedCents)
        assertTrue(open.isAmountInRange)

        val belowMin = FrameDepositViewModel(minAmount = 500, maxAmount = 1_000)
        belowMin.appendDigit(4)
        belowMin.appendDigit(9)
        belowMin.appendDigit(9)
        assertEquals(499, belowMin.typedCents)
        assertFalse(belowMin.isAmountInRange)

        val atMin = FrameDepositViewModel(minAmount = 500, maxAmount = 1_000)
        atMin.appendDigit(5)
        atMin.appendDigit(0)
        atMin.appendDigit(0)
        assertTrue(atMin.isAmountInRange)

        val aboveMax = FrameDepositViewModel(minAmount = 500, maxAmount = 1_000)
        aboveMax.replaceTypedCents(1_001)
        assertFalse(aboveMax.isAmountInRange)
    }

    @Test
    fun appendIgnoresDigitsPastMax() {
        val capped = FrameDepositViewModel(maxAmount = 100)
        capped.appendDigit(1)
        capped.appendDigit(0)
        capped.appendDigit(0)
        assertEquals(100, capped.typedCents)
        capped.appendDigit(0)
        assertEquals(100, capped.typedCents)
        assertTrue(capped.isAmountInRange)
    }

    @Test
    fun quickAmountsApplyAndHonorBounds() {
        assertEquals(listOf(1_000, 2_500, 5_000, 10_000), FrameDepositViewModel.quickAmounts)

        val vm = FrameDepositViewModel(minAmount = 2_500, maxAmount = 5_000)
        assertFalse(vm.isQuickAmountAvailable(1_000))
        assertTrue(vm.isQuickAmountAvailable(2_500))
        assertTrue(vm.isQuickAmountAvailable(5_000))
        assertFalse(vm.isQuickAmountAvailable(10_000))

        vm.applyQuickAmount(1_000)
        assertEquals(0, vm.typedCents)

        vm.applyQuickAmount(2_500)
        assertEquals(2_500, vm.typedCents)
        assertTrue(vm.isAmountInRange)

        vm.applyQuickAmount(10_000)
        assertEquals(2_500, vm.typedCents)
    }

    @Test
    fun deleteRemovesTheLastDigit() {
        val vm = FrameDepositViewModel(maxAmount = 100)
        vm.appendDigit(1)
        vm.appendDigit(0)
        vm.appendDigit(0)
        vm.deleteDigit()
        assertEquals(10, vm.typedCents)
        vm.deleteDigit()
        vm.deleteDigit()
        assertEquals(0, vm.typedCents)
        assertFalse(vm.isAmountInRange)
        vm.deleteDigit()
        assertEquals(0, vm.typedCents)
    }
}
