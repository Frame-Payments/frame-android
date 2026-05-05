package com.framepayments.frameonboarding.validation

import org.junit.Assert.assertEquals
import org.junit.Test

class DateOfBirthFormatterTest {

    @Test fun pads_singleDigits() {
        assertEquals("1990-01-05", DateOfBirthFormatter.format("1990", "1", "5"))
    }

    @Test fun passes_padded() {
        assertEquals("1990-12-25", DateOfBirthFormatter.format("1990", "12", "25"))
    }

    @Test fun empty_anyPart_returnsEmpty() {
        assertEquals("", DateOfBirthFormatter.format("", "12", "25"))
        assertEquals("", DateOfBirthFormatter.format("1990", "", "25"))
        assertEquals("", DateOfBirthFormatter.format("1990", "12", ""))
    }

    @Test fun pads_shortYear() {
        assertEquals("0090-01-01", DateOfBirthFormatter.format("90", "1", "1"))
    }
}
