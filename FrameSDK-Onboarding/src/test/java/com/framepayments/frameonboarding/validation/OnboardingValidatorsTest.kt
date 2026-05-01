package com.framepayments.frameonboarding.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Mirrors iOS Tests/Frame-iOSTests/ValidatorsTests.swift. Covers the 13 onboarding validators
 * with the same edge cases (DOB leap day, ABA checksum, postal regex per country, etc.).
 */
class OnboardingValidatorsTest {

    @Test fun nonEmpty_blankFails() {
        assertEquals("First name is required", OnboardingValidators.validateNonEmpty("", "First name"))
        assertEquals("Email is required", OnboardingValidators.validateNonEmpty("   ", "Email"))
    }

    @Test fun nonEmpty_passes() {
        assertNull(OnboardingValidators.validateNonEmpty("hi", "Field"))
    }

    @Test fun fullName_singleTokenFails() {
        assertEquals("Enter first and last name", OnboardingValidators.validateFullName("Tester"))
    }

    @Test fun fullName_emptyFails() {
        assertEquals("Full name is required", OnboardingValidators.validateFullName(""))
    }

    @Test fun fullName_passes() {
        assertNull(OnboardingValidators.validateFullName("Tester McTest"))
        assertNull(OnboardingValidators.validateFullName("Mary Jane Watson"))
    }

    @Test fun email_invalid() {
        assertEquals("Email is required", OnboardingValidators.validateEmail(""))
        assertEquals("Enter a valid email address", OnboardingValidators.validateEmail("foo@"))
        assertEquals("Enter a valid email address", OnboardingValidators.validateEmail("foo.bar"))
        assertEquals("Enter a valid email address", OnboardingValidators.validateEmail("a @b.com"))
    }

    @Test fun email_valid() {
        assertNull(OnboardingValidators.validateEmail("user@example.com"))
        assertNull(OnboardingValidators.validateEmail("a.b+c@d.co"))
    }

    @Test fun zipUS_invalid() {
        assertEquals("Zip code is required", OnboardingValidators.validateZipUS(""))
        assertEquals("Enter a 5-digit zip code", OnboardingValidators.validateZipUS("1234"))
        assertEquals("Enter a 5-digit zip code", OnboardingValidators.validateZipUS("123456"))
        assertEquals("Enter a 5-digit zip code", OnboardingValidators.validateZipUS("abcde"))
    }

    @Test fun zipUS_valid() {
        assertNull(OnboardingValidators.validateZipUS("75115"))
    }

    @Test fun ssnLast4_invalid() {
        assertEquals("SSN is required", OnboardingValidators.validateSSNLast4(""))
        assertEquals("Enter last 4 digits of SSN", OnboardingValidators.validateSSNLast4("123"))
        assertEquals("Enter last 4 digits of SSN", OnboardingValidators.validateSSNLast4("12345"))
        assertEquals("Enter last 4 digits of SSN", OnboardingValidators.validateSSNLast4("12a4"))
    }

    @Test fun ssnLast4_valid() {
        assertNull(OnboardingValidators.validateSSNLast4("1234"))
    }

    // ABA checksum: 011000015 is a known-valid Federal Reserve routing number.
    @Test fun routingUS_validChecksum() {
        assertNull(OnboardingValidators.validateRoutingNumberUS("011000015"))
    }

    @Test fun routingUS_invalidChecksum() {
        assertEquals(
            "Enter a valid routing number",
            OnboardingValidators.validateRoutingNumberUS("123456789")
        )
    }

    @Test fun routingUS_wrongLength() {
        assertEquals("Routing number is required", OnboardingValidators.validateRoutingNumberUS(""))
        assertEquals(
            "Enter a 9-digit routing number",
            OnboardingValidators.validateRoutingNumberUS("12345")
        )
        assertEquals(
            "Enter a 9-digit routing number",
            OnboardingValidators.validateRoutingNumberUS("01100001A")
        )
    }

    @Test fun accountNumberUS_bounds() {
        assertEquals("Account number is required", OnboardingValidators.validateAccountNumberUS(""))
        assertEquals("Enter a valid account number", OnboardingValidators.validateAccountNumberUS("123"))
        assertEquals(
            "Enter a valid account number",
            OnboardingValidators.validateAccountNumberUS("123456789012345678")
        )
        assertEquals("Enter a valid account number", OnboardingValidators.validateAccountNumberUS("12a4"))
        assertNull(OnboardingValidators.validateAccountNumberUS("12345"))
        assertNull(OnboardingValidators.validateAccountNumberUS("12345678901234567"))
    }

    @Test fun dob_required() {
        assertEquals(
            "Date of birth is required",
            OnboardingValidators.validateDateOfBirth("", "1", "1")
        )
    }

    @Test fun dob_invalidComponents() {
        assertEquals(
            "Enter a valid date of birth",
            OnboardingValidators.validateDateOfBirth("90", "1", "1")
        )
        assertEquals(
            "Enter a valid date of birth",
            OnboardingValidators.validateDateOfBirth("1990", "13", "1")
        )
        assertEquals(
            "Enter a valid date of birth",
            OnboardingValidators.validateDateOfBirth("1990", "0", "1")
        )
    }

    @Test fun dob_invalidDate() {
        assertEquals(
            "Enter a valid date of birth",
            OnboardingValidators.validateDateOfBirth("1990", "2", "30")
        )
        // Feb 29 in non-leap year rejected
        assertEquals(
            "Enter a valid date of birth",
            OnboardingValidators.validateDateOfBirth("2001", "2", "29")
        )
    }

    @Test fun dob_leapDayAccepted() {
        assertNull(OnboardingValidators.validateDateOfBirth("2000", "02", "29"))
    }

    @Test fun dob_unpaddedAccepted() {
        // Adult, single-digit month/day
        assertNull(OnboardingValidators.validateDateOfBirth("1990", "1", "5"))
    }

    @Test fun dob_underMinAge() {
        val now = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
        val recent = now.minusYears(10)
        val msg = OnboardingValidators.validateDateOfBirth(
            year = recent.year.toString(),
            month = recent.monthValue.toString(),
            day = recent.dayOfMonth.toString()
        )
        assertEquals("You must be at least 18 years old", msg)
    }

    @Test fun dob_overMaxAge() {
        val msg = OnboardingValidators.validateDateOfBirth("1850", "1", "1")
        assertEquals("Enter a valid date of birth", msg)
    }

    @Test fun postalCode_US() {
        assertEquals("Postal code is required", OnboardingValidators.validatePostalCode("", "US"))
        assertNull(OnboardingValidators.validatePostalCode("75115", "US"))
        assertNull(OnboardingValidators.validatePostalCode("75115-1234", "US"))
        assertEquals(
            "Enter a valid postal code",
            OnboardingValidators.validatePostalCode("1234", "US")
        )
    }

    @Test fun postalCode_GB() {
        assertNull(OnboardingValidators.validatePostalCode("SW1A 1AA", "GB"))
        assertEquals(
            "Enter a valid postal code",
            OnboardingValidators.validatePostalCode("12345", "GB")
        )
    }

    @Test fun postalCode_CA() {
        assertNull(OnboardingValidators.validatePostalCode("M5V 3L9", "CA"))
        assertNull(OnboardingValidators.validatePostalCode("M5V3L9", "CA"))
        assertEquals(
            "Enter a valid postal code",
            OnboardingValidators.validatePostalCode("12345", "CA")
        )
    }

    @Test fun postalCode_unknownCountrySkipsValidation() {
        // Mirrors iOS: unmapped countries return nil (no blocking).
        assertNull(OnboardingValidators.validatePostalCode("anything", "ZZ"))
    }

    @Test fun phoneE164_required() {
        assertEquals("Phone number is required", OnboardingValidators.validatePhoneE164("", "US"))
        assertEquals("Phone number is required", OnboardingValidators.validatePhoneE164("   ", "US"))
    }

    @Test fun phoneE164_invalid() {
        assertNotNull(OnboardingValidators.validatePhoneE164("abc", "US"))
        assertNotNull(OnboardingValidators.validatePhoneE164("123", "US"))
    }

    @Test fun phoneE164_validUS() {
        assertNull(OnboardingValidators.validatePhoneE164("+1 415 555 0132", "US"))
        assertNull(OnboardingValidators.validatePhoneE164("(415) 555-0132", "US"))
    }

    @Test fun cardExpiry_invalidMonth() {
        assertEquals("Invalid expiration month", OnboardingValidators.validateCardExpiry("13", "2030"))
        assertEquals("Invalid expiration month", OnboardingValidators.validateCardExpiry("ab", "2030"))
    }

    @Test fun cardExpiry_invalidYear() {
        assertEquals("Invalid expiration year", OnboardingValidators.validateCardExpiry("06", "1999"))
    }

    @Test fun cardExpiry_expired() {
        assertEquals(
            "Card has expired",
            OnboardingValidators.validateCardExpiry("01", "2000")
        )
    }

    @Test fun cardExpiry_futureValid() {
        val nextYear = java.time.LocalDate.now().year + 1
        assertNull(OnboardingValidators.validateCardExpiry("06", nextYear.toString()))
    }

    @Test fun card_nullFails() {
        assertEquals("Enter valid card details", OnboardingValidators.validateCard(null))
    }
}
