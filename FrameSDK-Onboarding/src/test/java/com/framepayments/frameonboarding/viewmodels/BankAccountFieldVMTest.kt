package com.framepayments.frameonboarding.viewmodels

import com.framepayments.frameonboarding.classes.BankAccountDraft
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BankAccountFieldVMTest {

    @Test fun emptyForm_validateFails_populatesErrors() {
        val vm = BankAccountFieldVM(BankAccountDraft())
        assertFalse(vm.validate())
        assertNotNull(vm.errorFor(BankAccountFieldVM.Field.ROUTING))
        assertNotNull(vm.errorFor(BankAccountFieldVM.Field.ACCOUNT))
    }

    @Test fun validForm_validatePasses_clearsErrors() {
        val vm = BankAccountFieldVM(
            BankAccountDraft(
                routingNumber = "011000015", // valid ABA
                accountNumber = "12345678",
                accountTypeLabel = "Checking"
            )
        )
        assertTrue(vm.validate())
        assertNull(vm.errorFor(BankAccountFieldVM.Field.ROUTING))
        assertNull(vm.errorFor(BankAccountFieldVM.Field.ACCOUNT))
    }

    @Test fun clearError_removesSingleEntry() {
        val vm = BankAccountFieldVM(BankAccountDraft())
        vm.validate()
        assertNotNull(vm.errorFor(BankAccountFieldVM.Field.ROUTING))
        vm.clearError(BankAccountFieldVM.Field.ROUTING)
        assertNull(vm.errorFor(BankAccountFieldVM.Field.ROUTING))
        assertNotNull(vm.errorFor(BankAccountFieldVM.Field.ACCOUNT))
    }
}
