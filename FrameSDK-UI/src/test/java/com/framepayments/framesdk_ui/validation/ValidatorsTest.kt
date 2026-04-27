package com.framepayments.framesdk_ui.validation

import org.junit.Assert.assertEquals
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
}
