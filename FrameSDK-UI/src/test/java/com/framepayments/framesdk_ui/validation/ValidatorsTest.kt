package com.framepayments.framesdk_ui.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test fun name_blankFails() {
        assertEquals(ValidationError.NAME_REQUIRED, Validators.validateName(""))
        assertEquals(ValidationError.NAME_REQUIRED, Validators.validateName("   "))
        assertEquals(ValidationError.NAME_REQUIRED, Validators.validateName(null))
    }

    @Test fun name_singleTokenFails() {
        assertEquals(ValidationError.NAME_REQUIRED, Validators.validateName("Tester"))
        assertEquals(ValidationError.NAME_REQUIRED, Validators.validateName("Tester  "))
    }

    @Test fun name_firstAndLastPasses() {
        assertNull(Validators.validateName("Tester McTest"))
        assertNull(Validators.validateName("Mary Jane Watson"))
        assertNull(Validators.validateName("  Tester   McTest  "))
    }

    @Test fun email_invalidFails() {
        assertEquals(ValidationError.EMAIL_INVALID, Validators.validateEmail(""))
        assertEquals(ValidationError.EMAIL_INVALID, Validators.validateEmail("foo@"))
        assertEquals(ValidationError.EMAIL_INVALID, Validators.validateEmail("foo.bar"))
        assertEquals(ValidationError.EMAIL_INVALID, Validators.validateEmail("foo @bar.com"))
        assertEquals(ValidationError.EMAIL_INVALID, Validators.validateEmail(null))
    }

    @Test fun email_validPasses() {
        assertNull(Validators.validateEmail("user@example.com"))
        assertNull(Validators.validateEmail("a.b+c@d.co"))
    }

    @Test fun zip_invalidFails() {
        assertEquals(ValidationError.ZIP_INVALID, Validators.validateZip(""))
        assertEquals(ValidationError.ZIP_INVALID, Validators.validateZip("1234"))
        assertEquals(ValidationError.ZIP_INVALID, Validators.validateZip("123456"))
        assertEquals(ValidationError.ZIP_INVALID, Validators.validateZip("abcde"))
        assertEquals(ValidationError.ZIP_INVALID, Validators.validateZip(null))
    }

    @Test fun zip_validPasses() {
        assertNull(Validators.validateZip("75115"))
    }

    @Test fun country_blankFails() {
        assertEquals(ValidationError.COUNTRY_REQUIRED, Validators.validateCountry(""))
        assertEquals(ValidationError.COUNTRY_REQUIRED, Validators.validateCountry(null))
    }

    @Test fun country_validPasses() {
        assertNull(Validators.validateCountry("US"))
    }

    @Test fun line1_blankFails() {
        assertEquals(ValidationError.ADDRESS_REQUIRED, Validators.validateAddressLine1(""))
        assertNull(Validators.validateAddressLine1("123 Main St"))
    }

    @Test fun city_blankFails() {
        assertEquals(ValidationError.CITY_REQUIRED, Validators.validateCity(""))
        assertNull(Validators.validateCity("Burbank"))
    }

    @Test fun state_blankFails() {
        assertEquals(ValidationError.STATE_REQUIRED, Validators.validateState(""))
        assertNull(Validators.validateState("CA"))
    }

    @Test fun card_nullFails() {
        assertEquals(ValidationError.CARD_INVALID, Validators.validateCard(null))
    }

    // --- Onboarding validators (relocated from OnboardingValidatorsTest; mirrors iOS
    // Tests/Frame-iOSTests/ValidatorsTests.swift) ---

    @Test fun nonEmpty_blankFails() {
        assertEquals("First name is required", Validators.validateNonEmpty("", "First name"))
        assertEquals("Email is required", Validators.validateNonEmpty("   ", "Email"))
    }

    @Test fun nonEmpty_passes() {
        assertNull(Validators.validateNonEmpty("hi", "Field"))
    }

    @Test fun fullName_singleTokenFails() {
        assertEquals("Enter first and last name", Validators.validateFullName("Tester"))
    }

    @Test fun fullName_emptyFails() {
        assertEquals("Full name is required", Validators.validateFullName(""))
    }

    @Test fun fullName_passes() {
        assertNull(Validators.validateFullName("Tester McTest"))
        assertNull(Validators.validateFullName("Mary Jane Watson"))
    }

    @Test fun emailAddress_invalid() {
        assertEquals("Email is required", Validators.validateEmailAddress(""))
        assertEquals("Enter a valid email address", Validators.validateEmailAddress("foo@"))
        assertEquals("Enter a valid email address", Validators.validateEmailAddress("foo.bar"))
        assertEquals("Enter a valid email address", Validators.validateEmailAddress("a @b.com"))
    }

    @Test fun emailAddress_valid() {
        assertNull(Validators.validateEmailAddress("user@example.com"))
        assertNull(Validators.validateEmailAddress("a.b+c@d.co"))
    }

    @Test fun zipUS_invalid() {
        assertEquals("Zip code is required", Validators.validateZipUS(""))
        assertEquals("Enter a 5-digit zip code", Validators.validateZipUS("1234"))
        assertEquals("Enter a 5-digit zip code", Validators.validateZipUS("123456"))
        assertEquals("Enter a 5-digit zip code", Validators.validateZipUS("abcde"))
    }

    @Test fun zipUS_valid() {
        assertNull(Validators.validateZipUS("75115"))
    }

    @Test fun ssnLast4_invalid() {
        assertEquals("SSN is required", Validators.validateSSNLast4(""))
        assertEquals("Enter last 4 digits of SSN", Validators.validateSSNLast4("123"))
        assertEquals("Enter last 4 digits of SSN", Validators.validateSSNLast4("12345"))
        assertEquals("Enter last 4 digits of SSN", Validators.validateSSNLast4("12a4"))
    }

    @Test fun ssnLast4_valid() {
        assertNull(Validators.validateSSNLast4("1234"))
    }

    // ABA checksum: 011000015 is a known-valid Federal Reserve routing number.
    @Test fun routingUS_validChecksum() {
        assertNull(Validators.validateRoutingNumberUS("011000015"))
    }

    @Test fun routingUS_invalidChecksum() {
        assertEquals(
            "Enter a valid routing number",
            Validators.validateRoutingNumberUS("123456789")
        )
    }

    @Test fun routingUS_wrongLength() {
        assertEquals("Routing number is required", Validators.validateRoutingNumberUS(""))
        assertEquals(
            "Enter a 9-digit routing number",
            Validators.validateRoutingNumberUS("12345")
        )
        assertEquals(
            "Enter a 9-digit routing number",
            Validators.validateRoutingNumberUS("01100001A")
        )
    }

    @Test fun accountNumberUS_bounds() {
        assertEquals("Account number is required", Validators.validateAccountNumberUS(""))
        assertEquals("Enter a valid account number", Validators.validateAccountNumberUS("123"))
        assertEquals(
            "Enter a valid account number",
            Validators.validateAccountNumberUS("123456789012345678")
        )
        assertEquals("Enter a valid account number", Validators.validateAccountNumberUS("12a4"))
        assertNull(Validators.validateAccountNumberUS("12345"))
        assertNull(Validators.validateAccountNumberUS("12345678901234567"))
    }

    @Test fun dob_required() {
        assertEquals(
            "Date of birth is required",
            Validators.validateDateOfBirth("", "1", "1")
        )
    }

    @Test fun dob_invalidComponents() {
        assertEquals(
            "Enter a valid date of birth",
            Validators.validateDateOfBirth("90", "1", "1")
        )
        assertEquals(
            "Enter a valid date of birth",
            Validators.validateDateOfBirth("1990", "13", "1")
        )
        assertEquals(
            "Enter a valid date of birth",
            Validators.validateDateOfBirth("1990", "0", "1")
        )
    }

    @Test fun dob_invalidDate() {
        assertEquals(
            "Enter a valid date of birth",
            Validators.validateDateOfBirth("1990", "2", "30")
        )
        // Feb 29 in non-leap year rejected
        assertEquals(
            "Enter a valid date of birth",
            Validators.validateDateOfBirth("2001", "2", "29")
        )
    }

    @Test fun dob_leapDayAccepted() {
        assertNull(Validators.validateDateOfBirth("2000", "02", "29"))
    }

    @Test fun dob_unpaddedAccepted() {
        // Adult, single-digit month/day
        assertNull(Validators.validateDateOfBirth("1990", "1", "5"))
    }

    @Test fun dob_underMinAge() {
        val now = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
        val recent = now.minusYears(10)
        val msg = Validators.validateDateOfBirth(
            year = recent.year.toString(),
            month = recent.monthValue.toString(),
            day = recent.dayOfMonth.toString()
        )
        assertEquals("You must be at least 18 years old", msg)
    }

    @Test fun dob_overMaxAge() {
        val msg = Validators.validateDateOfBirth("1850", "1", "1")
        assertEquals("Enter a valid date of birth", msg)
    }

    @Test fun postalCode_US() {
        assertEquals("Postal code is required", Validators.validatePostalCode("", "US"))
        assertNull(Validators.validatePostalCode("75115", "US"))
        assertNull(Validators.validatePostalCode("75115-1234", "US"))
        assertEquals(
            "Enter a valid postal code",
            Validators.validatePostalCode("1234", "US")
        )
    }

    @Test fun postalCode_GB() {
        assertNull(Validators.validatePostalCode("SW1A 1AA", "GB"))
        assertEquals(
            "Enter a valid postal code",
            Validators.validatePostalCode("12345", "GB")
        )
    }

    @Test fun postalCode_CA() {
        assertNull(Validators.validatePostalCode("M5V 3L9", "CA"))
        assertNull(Validators.validatePostalCode("M5V3L9", "CA"))
        assertEquals(
            "Enter a valid postal code",
            Validators.validatePostalCode("12345", "CA")
        )
    }

    @Test fun postalCode_unknownCountrySkipsValidation() {
        // Mirrors iOS: unmapped countries return nil (no blocking).
        assertNull(Validators.validatePostalCode("anything", "ZZ"))
    }

    @Test fun phoneE164_required() {
        assertEquals("Phone number is required", Validators.validatePhoneE164("", "US"))
        assertEquals("Phone number is required", Validators.validatePhoneE164("   ", "US"))
    }

    @Test fun phoneE164_invalid() {
        assertNotNull(Validators.validatePhoneE164("abc", "US"))
        assertNotNull(Validators.validatePhoneE164("123", "US"))
    }

    @Test fun phoneE164_validUS() {
        assertNull(Validators.validatePhoneE164("+1 415 555 0132", "US"))
        assertNull(Validators.validatePhoneE164("(415) 555-0132", "US"))
    }

    @Test fun cardExpiry_invalidMonth() {
        assertEquals("Invalid expiration month", Validators.validateCardExpiry("13", "2030"))
        assertEquals("Invalid expiration month", Validators.validateCardExpiry("ab", "2030"))
    }

    @Test fun cardExpiry_invalidYear() {
        assertEquals("Invalid expiration year", Validators.validateCardExpiry("06", "1999"))
    }

    @Test fun cardExpiry_expired() {
        assertEquals(
            "Card has expired",
            Validators.validateCardExpiry("01", "2000")
        )
    }

    @Test fun cardExpiry_futureValid() {
        val nextYear = java.time.LocalDate.now().year + 1
        assertNull(Validators.validateCardExpiry("06", nextYear.toString()))
    }

    @Test fun onboardingCard_nullFails() {
        assertEquals("Enter valid card details", Validators.validateOnboardingCard(null))
    }
}
