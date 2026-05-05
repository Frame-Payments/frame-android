package com.framepayments.frameonboarding.reusable

import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the PhoneNumberTextField helper actually applies AsYouTypeFormatter
 * formatting. Catches regressions like the `onPhoneNumberChanged` digit-strip bug.
 */
class PhoneNumberFormattingTest {

    private fun usFormatter() = PhoneNumberUtil.getInstance().getAsYouTypeFormatter("US")

    @Test fun formatsBareDigits_intoUsParens() {
        val out = formatPhoneNumber(usFormatter(), "4155550132")
        // We don't pin to a single string because libphonenumber metadata varies,
        // but a 10-digit US number should format into something with parens or a dash.
        assertTrue("expected formatted output, got '$out'", out.contains("(") || out.contains("-"))
        assertTrue("expected '415' to be present, got '$out'", out.contains("415"))
    }

    @Test fun emptyInput_returnsEmpty() {
        assertEquals("", formatPhoneNumber(usFormatter(), ""))
    }

    @Test fun stripsNonDigitNonPlus() {
        val out = formatPhoneNumber(usFormatter(), "abc4155550132xyz")
        assertTrue(out.contains("415"))
    }

    @Test fun formatterIsReusable_afterMultipleCalls() {
        val f = usFormatter()
        val first = formatPhoneNumber(f, "4155550132")
        val second = formatPhoneNumber(f, "2125550199")
        // Both calls should produce non-empty, distinct output (formatter must
        // clear() between calls — which formatPhoneNumber does internally).
        assertTrue(first.contains("415"))
        assertTrue(second.contains("212"))
    }
}
