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
