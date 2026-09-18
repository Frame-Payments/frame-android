package com.framepayments.frameonboarding.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OnboardingValidatorsTest {

    @Test fun blank_fails() {
        assertEquals("State is required", OnboardingValidators.validateSubregion("", "US"))
    }

    @Test fun validUsCode_passes() {
        assertNull(OnboardingValidators.validateSubregion("CA", "US"))
    }

    @Test fun validUsCode_lowercase_passes() {
        assertNull(OnboardingValidators.validateSubregion("ca", "US"))
    }

    @Test fun invalidUsCode_fails() {
        assertEquals("Enter a valid 2-letter state", OnboardingValidators.validateSubregion("XX", "US"))
    }

    @Test fun validCanadaCode_passes() {
        assertNull(OnboardingValidators.validateSubregion("ON", "CA"))
    }

    @Test fun countryWithoutSubregionList_passes() {
        assertNull(OnboardingValidators.validateSubregion("anything", "GB"))
    }
}
