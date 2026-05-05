package com.framepayments.frameonboarding.viewmodels

import com.framepayments.framesdk.FrameObjects
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BillingAddressFieldVMTest {

    private fun emptyAddress() = FrameObjects.BillingAddress(
        city = "", country = "", state = "", postalCode = "",
        addressLine1 = "", addressLine2 = ""
    )

    @Test fun usOnly_emptyForm_fails() {
        val vm = BillingAddressFieldVM(emptyAddress(), BillingAddressMode.US_ONLY)
        assertFalse(vm.validate())
        assertNotNull(vm.errorFor(BillingAddressFieldVM.Field.LINE1))
        assertNotNull(vm.errorFor(BillingAddressFieldVM.Field.CITY))
        assertNotNull(vm.errorFor(BillingAddressFieldVM.Field.STATE))
        assertNotNull(vm.errorFor(BillingAddressFieldVM.Field.POSTAL))
    }

    @Test fun usOnly_validForm_passes() {
        val vm = BillingAddressFieldVM(
            FrameObjects.BillingAddress(
                city = "Burbank", country = "US", state = "CA", postalCode = "91501",
                addressLine1 = "123 Main St", addressLine2 = null
            ),
            BillingAddressMode.US_ONLY
        )
        assertTrue(vm.validate())
    }

    @Test fun international_blankCountrySeedsToUS() {
        // Constructor seeds country to "US" if blank in international mode.
        val vm = BillingAddressFieldVM(
            FrameObjects.BillingAddress(
                city = "X", country = null, state = "X", postalCode = "12345",
                addressLine1 = "1 Rd", addressLine2 = null
            ),
            BillingAddressMode.INTERNATIONAL
        )
        assert(vm.address.value.country == "US")
        assertTrue(vm.validate())
    }

    @Test fun international_explicitlyEmptyCountryFails() {
        // If country is explicitly cleared after construction, validation must fail
        // in international mode.
        val vm = BillingAddressFieldVM(
            FrameObjects.BillingAddress(
                city = "X", country = "US", state = "X", postalCode = "12345",
                addressLine1 = "1 Rd", addressLine2 = null
            ),
            BillingAddressMode.INTERNATIONAL
        )
        vm.updateAddress { it.copy(country = "") }
        assertFalse(vm.validate())
        assertNotNull(vm.errorFor(BillingAddressFieldVM.Field.COUNTRY))
    }

    @Test fun international_postalRevalidatesOnCountryChange() {
        // Start in US mode with a valid US zip → switch country to GB →
        // existing postal error must update to reflect GB rules.
        val vm = BillingAddressFieldVM(
            FrameObjects.BillingAddress(
                city = "Anywhere", country = "US", state = "CA", postalCode = "1234",
                addressLine1 = "1 Rd", addressLine2 = null
            ),
            BillingAddressMode.INTERNATIONAL
        )
        assertFalse(vm.validate())
        val usError = vm.errorFor(BillingAddressFieldVM.Field.POSTAL)
        assertNotNull(usError)

        vm.setCountry("GB")
        // Postal error should still be present (1234 isn't a valid GB postcode either),
        // but it should have been re-evaluated against GB regex.
        assertNotNull(vm.errorFor(BillingAddressFieldVM.Field.POSTAL))

        vm.updateAddress { it.copy(postalCode = "SW1A 1AA") }
        // Re-validation on country change only fires if an error was currently shown;
        // since we just edited postal, run validate() to confirm the new value passes.
        assertTrue(vm.validate())
        assertNull(vm.errorFor(BillingAddressFieldVM.Field.POSTAL))
    }

    @Test fun setCountry_clearsPostalErrorWhenNewCountryAccepts() {
        // US-only mode is pinned; use international mode for this test.
        val vm = BillingAddressFieldVM(
            FrameObjects.BillingAddress(
                city = "Anywhere", country = "GB", state = "X", postalCode = "12345",
                addressLine1 = "1 Rd", addressLine2 = null
            ),
            BillingAddressMode.INTERNATIONAL
        )
        assertFalse(vm.validate())
        assertNotNull(vm.errorFor(BillingAddressFieldVM.Field.POSTAL))

        // Switch to a country whose validator passes the same postal value.
        vm.setCountry("US")
        // 12345 is a valid US zip — error should clear after re-validation.
        assertNull(vm.errorFor(BillingAddressFieldVM.Field.POSTAL))
    }

    @Test fun unknownCountryWithEmptyPostalStillFails() {
        val vm = BillingAddressFieldVM(
            FrameObjects.BillingAddress(
                city = "X", country = "ZZ", state = "X", postalCode = "",
                addressLine1 = "1 Rd", addressLine2 = null
            ),
            BillingAddressMode.INTERNATIONAL
        )
        assertFalse(vm.validate())
        assertNotNull(vm.errorFor(BillingAddressFieldVM.Field.POSTAL))
    }

    @Test fun usOnly_pinsCountryToUS() {
        val vm = BillingAddressFieldVM(
            FrameObjects.BillingAddress(
                city = null, country = "GB", state = null, postalCode = "",
                addressLine1 = null, addressLine2 = null
            ),
            BillingAddressMode.US_ONLY
        )
        // Mode forces country to "US" on init regardless of input.
        assert(vm.address.value.country == "US")
    }
}
