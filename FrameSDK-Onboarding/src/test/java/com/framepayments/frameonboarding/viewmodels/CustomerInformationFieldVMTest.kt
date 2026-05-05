package com.framepayments.frameonboarding.viewmodels

import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.customeridentity.CustomerIdentityRequests
import com.framepayments.frameonboarding.classes.PhoneCountrySelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomerInformationFieldVMTest {

    private fun emptyIdentity() = CustomerIdentityRequests.CreateCustomerIdentityRequest(
        address = FrameObjects.BillingAddress(
            city = "", country = "US", state = "", postalCode = "",
            addressLine1 = "", addressLine2 = null
        ),
        firstName = "", lastName = "", dateOfBirth = "",
        phoneNumber = "", email = "", ssn = ""
    )

    @Test fun emptyForm_fails_populatesAllErrors() {
        val vm = CustomerInformationFieldVM(
            emptyIdentity(),
            PhoneCountrySelection("US", "+1")
        )
        assertFalse(vm.validate())
        assertNotNull(vm.errorFor(CustomerInformationFieldVM.Field.FIRST_NAME))
        assertNotNull(vm.errorFor(CustomerInformationFieldVM.Field.LAST_NAME))
        assertNotNull(vm.errorFor(CustomerInformationFieldVM.Field.EMAIL))
        assertNotNull(vm.errorFor(CustomerInformationFieldVM.Field.PHONE))
        assertNotNull(vm.errorFor(CustomerInformationFieldVM.Field.BIRTH_MONTH))
        assertNotNull(vm.errorFor(CustomerInformationFieldVM.Field.BIRTH_DAY))
        assertNotNull(vm.errorFor(CustomerInformationFieldVM.Field.BIRTH_YEAR))
        assertNotNull(vm.errorFor(CustomerInformationFieldVM.Field.SSN))
    }

    @Test fun dobErrorAppliedToAllThreeFields() {
        val vm = CustomerInformationFieldVM(
            emptyIdentity(),
            PhoneCountrySelection("US", "+1")
        )
        vm.validate()
        val month = vm.errorFor(CustomerInformationFieldVM.Field.BIRTH_MONTH)
        val day = vm.errorFor(CustomerInformationFieldVM.Field.BIRTH_DAY)
        val year = vm.errorFor(CustomerInformationFieldVM.Field.BIRTH_YEAR)
        assertEquals(month, day)
        assertEquals(month, year)
    }

    @Test fun clearDateOfBirthErrors_removesAllThree() {
        val vm = CustomerInformationFieldVM(
            emptyIdentity(),
            PhoneCountrySelection("US", "+1")
        )
        vm.validate()
        vm.clearDateOfBirthErrors()
        assertNull(vm.errorFor(CustomerInformationFieldVM.Field.BIRTH_MONTH))
        assertNull(vm.errorFor(CustomerInformationFieldVM.Field.BIRTH_DAY))
        assertNull(vm.errorFor(CustomerInformationFieldVM.Field.BIRTH_YEAR))
        // Other errors remain
        assertNotNull(vm.errorFor(CustomerInformationFieldVM.Field.EMAIL))
    }

    @Test fun validForm_passes() {
        val vm = CustomerInformationFieldVM(
            CustomerIdentityRequests.CreateCustomerIdentityRequest(
                address = FrameObjects.BillingAddress(
                    city = "Burbank", country = "US", state = "CA", postalCode = "91501",
                    addressLine1 = "123 Main St", addressLine2 = null
                ),
                firstName = "Jane",
                lastName = "Doe",
                dateOfBirth = "1990-01-05",
                phoneNumber = "+14155550132",
                email = "jane@example.com",
                ssn = "1234"
            ),
            PhoneCountrySelection("US", "+1")
        )
        assertTrue(vm.validate())
    }
}
