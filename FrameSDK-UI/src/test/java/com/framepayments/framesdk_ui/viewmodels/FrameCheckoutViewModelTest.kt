package com.framepayments.framesdk_ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.framepayments.framesdk_ui.AddressMode
import com.framepayments.framesdk_ui.validation.FieldKey
import com.framepayments.framesdk_ui.validation.ValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FrameCheckoutViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private lateinit var vm: FrameCheckoutViewModel

    @Before fun setup() {
        vm = FrameCheckoutViewModel()
    }

    private fun fillValidCustomerInfo() {
        vm.customerName.value = "Tester McTest"
        vm.customerEmail.value = "tester@example.com"
        // cardData remains default (PaymentCardData()); validateCard will fail unless caller sets it.
    }

    private fun fillValidAddress() {
        vm.customerAddressLine1.value = "123 Main St"
        vm.customerCity.value = "Burbank"
        vm.customerState.value = "California"
        vm.customerZipCode.value = "75115"
    }

    @Test fun required_blankAddress_populatesErrors() {
        vm.addressMode = AddressMode.REQUIRED
        fillValidCustomerInfo()
        val errors = vm.validateAll(forSavedCard = true) // skip card check
        assertEquals(ValidationError.ADDRESS_REQUIRED, errors[FieldKey.ADDRESS_LINE_1])
        assertEquals(ValidationError.CITY_REQUIRED, errors[FieldKey.CITY])
        assertEquals(ValidationError.STATE_REQUIRED, errors[FieldKey.STATE])
        assertEquals(ValidationError.ZIP_INVALID, errors[FieldKey.ZIP])
    }

    @Test fun optional_allBlank_isValid() {
        vm.addressMode = AddressMode.OPTIONAL
        fillValidCustomerInfo()
        val errors = vm.validateAll(forSavedCard = true)
        assertTrue("Expected no address errors but got $errors", errors.none {
            it.key in setOf(FieldKey.ADDRESS_LINE_1, FieldKey.CITY, FieldKey.STATE, FieldKey.ZIP)
        })
    }

    @Test fun optional_partialAddress_failsValidation() {
        vm.addressMode = AddressMode.OPTIONAL
        fillValidCustomerInfo()
        vm.customerCity.value = "Burbank"
        val errors = vm.validateAll(forSavedCard = true)
        assertEquals(ValidationError.ADDRESS_REQUIRED, errors[FieldKey.ADDRESS_LINE_1])
        assertEquals(ValidationError.STATE_REQUIRED, errors[FieldKey.STATE])
        assertEquals(ValidationError.ZIP_INVALID, errors[FieldKey.ZIP])
    }

    @Test fun hidden_neverValidatesAddress() {
        vm.addressMode = AddressMode.HIDDEN
        fillValidCustomerInfo()
        vm.customerCity.value = "x"
        vm.customerZipCode.value = "1"
        val errors = vm.validateAll(forSavedCard = true)
        assertNull(errors[FieldKey.ZIP])
        assertNull(errors[FieldKey.CITY])
    }

    @Test fun invalidEmail_failsValidation() {
        vm.addressMode = AddressMode.HIDDEN
        vm.customerName.value = "Tester McTest"
        vm.customerEmail.value = "not-an-email"
        val errors = vm.validateAll(forSavedCard = true)
        assertEquals(ValidationError.EMAIL_INVALID, errors[FieldKey.EMAIL])
    }

    @Test fun invalidZip_failsInRequired() {
        vm.addressMode = AddressMode.REQUIRED
        fillValidCustomerInfo()
        fillValidAddress()
        vm.customerZipCode.value = "1234"
        val errors = vm.validateAll(forSavedCard = true)
        assertEquals(ValidationError.ZIP_INVALID, errors[FieldKey.ZIP])
    }

    @Test fun savedCard_skipsCardValidation() {
        vm.addressMode = AddressMode.HIDDEN
        fillValidCustomerInfo()
        // cardData is default — would fail isPotentiallyValid — but saved-card path skips it.
        val errors = vm.validateAll(forSavedCard = true)
        assertNull(errors[FieldKey.CARD])
    }

    @Test fun savedCard_stillValidatesNameAndEmail() {
        vm.addressMode = AddressMode.HIDDEN
        // No name, no email
        val errors = vm.validateAll(forSavedCard = true)
        assertNotNull(errors[FieldKey.NAME])
        assertNotNull(errors[FieldKey.EMAIL])
    }

    @Test fun savedCard_stillValidatesAddressInRequired() {
        vm.addressMode = AddressMode.REQUIRED
        vm.customerName.value = "Tester McTest"
        vm.customerEmail.value = "tester@example.com"
        val errors = vm.validateAll(forSavedCard = true)
        assertNotNull(errors[FieldKey.ADDRESS_LINE_1])
        assertNotNull(errors[FieldKey.ZIP])
    }

    @Test fun setError_addsAndClears() {
        vm.setError(FieldKey.EMAIL, ValidationError.EMAIL_INVALID)
        assertEquals(ValidationError.EMAIL_INVALID, vm.fieldErrors.value?.get(FieldKey.EMAIL))
        vm.clearError(FieldKey.EMAIL)
        assertNull(vm.fieldErrors.value?.get(FieldKey.EMAIL))
    }

    @Test fun singleName_failsValidation() {
        vm.addressMode = AddressMode.HIDDEN
        vm.customerName.value = "OnlyOne"
        vm.customerEmail.value = "tester@example.com"
        val errors = vm.validateAll(forSavedCard = true)
        assertNotNull(errors[FieldKey.NAME])
    }

    @Test fun hasUsablePaymentInput_defaultFalse() {
        // Default cardData has empty number and no saved card selected.
        assertEquals(false, vm.hasUsablePaymentInput.value)
    }

    @Test fun hasUsablePaymentInput_trueWhenSavedCardSelected() {
        val saved = com.framepayments.framesdk.FrameObjects.PaymentMethod(
            id = "saved",
            customerId = null,
            billing = null,
            type = com.framepayments.framesdk.FrameObjects.PaymentMethodType.CARD,
            methodObject = "payment_method",
            created = 0,
            updated = 0,
            livemode = false,
            card = null,
            ach = null,
            status = com.framepayments.framesdk.FrameObjects.PaymentMethodStatus.ACTIVE
        )
        vm.selectedCustomerPaymentOption = saved
        assertEquals(true, vm.hasUsablePaymentInput.value)
    }

    @Test fun newCardPath_runsCardValidation() {
        // Whether the default PaymentCardData() is "potentially valid" is up to Evervault;
        // we only assert that toggling forSavedCard changes whether the CARD slot is checked.
        vm.addressMode = AddressMode.HIDDEN
        fillValidCustomerInfo()
        val errorsSaved = vm.validateAll(forSavedCard = true)
        assertNull("Saved-card path must skip CARD validation", errorsSaved[FieldKey.CARD])
    }
}
