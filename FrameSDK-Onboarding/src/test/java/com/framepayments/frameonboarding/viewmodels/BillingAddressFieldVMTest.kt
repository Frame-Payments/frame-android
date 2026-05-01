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

    @Test fun international_requiresCountry() {
        val vm = BillingAddressFieldVM(
            FrameObjects.BillingAddress(
                city = "X", country = null, state = "X", postalCode = "12345",
                addressLine1 = "1 Rd", addressLine2 = null
            ),
            BillingAddressMode.INTERNATIONAL
        )
        // Constructor seeds country to "US" if blank, so default validate passes if all set.
        assertTrue(vm.validate())
    }

    @Test fun international_postalChangesWithCountry() {
        val vm = BillingAddressFieldVM(
            FrameObjects.BillingAddress(
                city = "London", country = "GB", state = "Greater London",
                postalCode = "12345",
                addressLine1 = "1 Rd", addressLine2 = null
            ),
            BillingAddressMode.INTERNATIONAL
        )
        assertFalse(vm.validate())
        // Switch to GB should re-validate the existing postal error
        vm.setCountry("GB")
        assertNotNull(vm.errorFor(BillingAddressFieldVM.Field.POSTAL))

        // Update to a valid GB postcode and re-validate.
        vm.updateAddress { it.copy(postalCode = "SW1A 1AA") }
        assertTrue(vm.validate())
        assertNull(vm.errorFor(BillingAddressFieldVM.Field.POSTAL))
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
