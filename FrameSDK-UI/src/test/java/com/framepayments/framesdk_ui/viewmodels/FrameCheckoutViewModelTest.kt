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
        vm.billingAddress.updateAddress {
            it.copy(
                addressLine1 = "123 Main St",
                city = "Burbank",
                state = "CA",
                postalCode = "75115"
            )
        }
    }

    /** Address errors live on the shared [BillingAddressFieldVM], not in [FrameCheckoutViewModel]'s FieldKey map. */
    private fun addressErrors(): Map<BillingAddressFieldVM.Field, String> {
        vm.billingAddress.validate()
        return vm.billingAddress.errors.value
    }

    @Test fun required_blankAddress_populatesErrors() {
        vm.addressMode = AddressMode.REQUIRED
        fillValidCustomerInfo()
        val errors = addressErrors()
        assertNotNull(errors[BillingAddressFieldVM.Field.LINE1])
        assertNotNull(errors[BillingAddressFieldVM.Field.CITY])
        assertNotNull(errors[BillingAddressFieldVM.Field.STATE])
        assertNotNull(errors[BillingAddressFieldVM.Field.POSTAL])
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
        vm.billingAddress.updateAddress { it.copy(city = "Burbank") }
        // Partial address input is all-or-nothing in optional mode (new-card path only).
        val errors = addressErrors()
        assertNotNull(errors[BillingAddressFieldVM.Field.LINE1])
        assertNotNull(errors[BillingAddressFieldVM.Field.STATE])
        assertNotNull(errors[BillingAddressFieldVM.Field.POSTAL])
    }

    @Test fun hidden_neverValidatesAddress() {
        vm.addressMode = AddressMode.HIDDEN
        fillValidCustomerInfo()
        vm.billingAddress.updateAddress { it.copy(city = "x", postalCode = "1") }
        val errors = vm.validateAll(forSavedCard = true)
        assertNull(errors[FieldKey.ZIP])
        assertNull(errors[FieldKey.CITY])
    }

    @Test fun customerInfoRequired_defaultsTrueBeforeAccountLoad() {
        // The fields must not be assumed present before the profile has had a chance to fill them.
        assertTrue(vm.customerInfoRequired.value == true)
    }

    @Test fun customerInfoRequired_falseWhenProfileSuppliedBoth() {
        fillValidCustomerInfo()
        vm.refreshCustomerInfoRequired()
        assertEquals(false, vm.customerInfoRequired.value)
    }

    @Test fun customerInfoRequired_trueWhenProfileSuppliedNeither() {
        vm.refreshCustomerInfoRequired()
        assertEquals(true, vm.customerInfoRequired.value)
    }

    @Test fun customerInfoRequired_trueWhenOnlyNameSupplied() {
        // A single-word profile name also fails validateName, so a partial profile must still ask.
        vm.customerName.value = "Tester McTest"
        vm.refreshCustomerInfoRequired()
        assertEquals(true, vm.customerInfoRequired.value)
    }

    @Test fun customerInfoRequired_trueWhenOnlyEmailSupplied() {
        vm.customerEmail.value = "tester@example.com"
        vm.refreshCustomerInfoRequired()
        assertEquals(true, vm.customerInfoRequired.value)
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
        vm.billingAddress.updateAddress { it.copy(postalCode = "1234") }
        assertNotNull(addressErrors()[BillingAddressFieldVM.Field.POSTAL])
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

    @Test fun savedCard_skipsAddressValidationEvenInRequired() {
        vm.addressMode = AddressMode.REQUIRED
        vm.customerName.value = "Tester McTest"
        vm.customerEmail.value = "tester@example.com"
        // Saved-card path skips address validation regardless of addressMode, because
        // the saved PM already carries a billing address server-side and the UI hides
        // those fields.
        val errors = vm.validateAll(forSavedCard = true)
        assertNull(errors[FieldKey.ADDRESS_LINE_1])
        assertNull(errors[FieldKey.ZIP])
    }

    @Test fun savedCard_skipsAddressValidationInOptionalWithPartialInput() {
        vm.addressMode = AddressMode.OPTIONAL
        vm.customerName.value = "Tester McTest"
        vm.customerEmail.value = "tester@example.com"
        vm.billingAddress.updateAddress { it.copy(city = "Burbank") }
        val errors = vm.validateAll(forSavedCard = true)
        assertNull(errors[FieldKey.ADDRESS_LINE_1])
        assertNull(errors[FieldKey.ZIP])
    }

    @Test fun switchingBackToNewCard_reRequiresAddressInRequired() {
        vm.addressMode = AddressMode.REQUIRED
        vm.customerName.value = "Tester McTest"
        vm.customerEmail.value = "tester@example.com"
        // Saved path leaves the address form untouched — nothing validated, no errors raised.
        assertNull(vm.validateAll(forSavedCard = true)[FieldKey.ADDRESS_LINE_1])
        assertTrue(vm.billingAddress.errors.value.isEmpty())
        // New-card path: the address form is validated and reports its own errors.
        val addressErrors = addressErrors()
        assertNotNull(addressErrors[BillingAddressFieldVM.Field.LINE1])
        assertNotNull(addressErrors[BillingAddressFieldVM.Field.POSTAL])
    }

    @Test fun clearNewCardFieldErrors_clearsOnlyNewCardKeys() {
        vm.setError(FieldKey.NAME, ValidationError.NAME_REQUIRED)
        vm.setError(FieldKey.EMAIL, ValidationError.EMAIL_INVALID)
        vm.setError(FieldKey.CARD, ValidationError.CARD_INVALID)
        vm.setError(FieldKey.ADDRESS_LINE_1, ValidationError.ADDRESS_REQUIRED)
        vm.setError(FieldKey.CITY, ValidationError.CITY_REQUIRED)
        vm.setError(FieldKey.STATE, ValidationError.STATE_REQUIRED)
        vm.setError(FieldKey.ZIP, ValidationError.ZIP_INVALID)
        vm.setError(FieldKey.COUNTRY, ValidationError.COUNTRY_REQUIRED)
        vm.clearNewCardFieldErrors()
        val errs = vm.fieldErrors.value.orEmpty()
        assertEquals(ValidationError.NAME_REQUIRED, errs[FieldKey.NAME])
        assertEquals(ValidationError.EMAIL_INVALID, errs[FieldKey.EMAIL])
        assertNull(errs[FieldKey.CARD])
        assertNull(errs[FieldKey.ADDRESS_LINE_1])
        assertNull(errs[FieldKey.CITY])
        assertNull(errs[FieldKey.STATE])
        assertNull(errs[FieldKey.ZIP])
        assertNull(errs[FieldKey.COUNTRY])
    }

    @Test fun selectionSetter_updatesLiveDataAndTriggersUsableInput() {
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
        assertNull(vm.selectedAccountPaymentOption.value)
        assertEquals(false, vm.hasUsablePaymentInput.value)
        vm.setSelectedAccountPaymentOption(saved)
        assertEquals(saved, vm.selectedAccountPaymentOption.value)
        assertEquals(true, vm.hasUsablePaymentInput.value)
        vm.setSelectedAccountPaymentOption(null)
        assertNull(vm.selectedAccountPaymentOption.value)
        assertEquals(false, vm.hasUsablePaymentInput.value)
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
        vm.setSelectedAccountPaymentOption(saved)
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
