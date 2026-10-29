package com.framepayments.framesdk_ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyFormatterTest {
    @Test
    fun currencyCodeOverloadDoesNotMutateSharedFormatter() {
        val before = CurrencyFormatter.convertCentsToCurrencyString(1_000)
        CurrencyFormatter.convertCentsToCurrencyString(1_000, "gbp")
        assertEquals(before, CurrencyFormatter.convertCentsToCurrencyString(1_000))
    }

    @Test
    fun zeroDecimalCurrencyDoesNotDivideByOneHundred() {
        val yen = CurrencyFormatter.convertCentsToCurrencyString(100, "jpy")
        assertFalse(yen.contains("1.00"))
        assertTrue(yen.contains("100"))
        val dollars = CurrencyFormatter.convertCentsToCurrencyString(1_000, "usd")
        assertTrue(dollars.contains("10.00") || dollars.contains("10,00"))
    }
}
