package com.framepayments.frameonboarding.viewmodels

import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the partitioned-errors pattern: validateAllPhoneAuth() only mutates
 * PHONE_AUTH-group keys; validateAllDocs() only mutates DOCS-group keys.
 * Mirrors iOS OnboardingContainerViewModel.applyValidation behavior.
 */
class FrameOnboardingViewModelValidationTest {

    private fun makeVM() = FrameOnboardingViewModel(
        OnboardingConfig(
            requiredCapabilities = listOf(Capabilities.KYC_PREFILL),
            skipInitNetwork = true
        )
    )

    @Test fun validateAllPhoneAuth_emptyForm_populatesAuthErrors() {
        val vm = makeVM()
        assertFalse(vm.validateAllPhoneAuth())
        assertNotNull(vm.errorFor(OnboardingField.AUTH_PHONE))
        assertNotNull(vm.errorFor(OnboardingField.AUTH_BIRTH_MONTH))
        assertNotNull(vm.errorFor(OnboardingField.AUTH_BIRTH_DAY))
        assertNotNull(vm.errorFor(OnboardingField.AUTH_BIRTH_YEAR))
    }

    @Test fun validateAllDocs_emptyForm_populatesDocErrors() {
        val vm = makeVM()
        assertFalse(vm.validateAllDocs())
        assertNotNull(vm.errorFor(OnboardingField.DOC_FRONT))
        assertNotNull(vm.errorFor(OnboardingField.DOC_BACK))
        assertNotNull(vm.errorFor(OnboardingField.DOC_SELFIE))
    }

    @Test fun phoneAuthValidation_doesNotClobberDocsErrors() {
        val vm = makeVM()
        vm.validateAllDocs()
        assertNotNull(vm.errorFor(OnboardingField.DOC_FRONT))

        // Run phone-auth validation; doc errors must persist.
        vm.validateAllPhoneAuth()
        assertNotNull(vm.errorFor(OnboardingField.DOC_FRONT))
        assertNotNull(vm.errorFor(OnboardingField.DOC_BACK))
        assertNotNull(vm.errorFor(OnboardingField.DOC_SELFIE))
        assertNotNull(vm.errorFor(OnboardingField.AUTH_PHONE))
    }

    @Test fun docsValidation_doesNotClobberPhoneAuthErrors() {
        val vm = makeVM()
        vm.validateAllPhoneAuth()
        assertNotNull(vm.errorFor(OnboardingField.AUTH_PHONE))

        vm.validateAllDocs()
        assertNotNull(vm.errorFor(OnboardingField.AUTH_PHONE))
        assertNotNull(vm.errorFor(OnboardingField.DOC_FRONT))
    }

    @Test fun clearError_removesSingleEntry() {
        val vm = makeVM()
        vm.validateAllPhoneAuth()
        assertNotNull(vm.errorFor(OnboardingField.AUTH_PHONE))
        vm.clearError(OnboardingField.AUTH_PHONE)
        assertNull(vm.errorFor(OnboardingField.AUTH_PHONE))
        assertNotNull(vm.errorFor(OnboardingField.AUTH_BIRTH_MONTH))
    }

    @Test fun fieldGroup_correctMapping() {
        assertEquals(OnboardingFieldGroup.PHONE_AUTH, OnboardingField.AUTH_PHONE.group)
        assertEquals(OnboardingFieldGroup.PHONE_AUTH, OnboardingField.AUTH_BIRTH_MONTH.group)
        assertEquals(OnboardingFieldGroup.DOCS, OnboardingField.DOC_FRONT.group)
    }

    private fun assertEquals(a: Any?, b: Any?) {
        org.junit.Assert.assertEquals(a, b)
    }
}
