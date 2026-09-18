package com.framepayments.frameonboarding.viewmodels

import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Verifies the partitioned-errors pattern: validateAllPhoneAuth() only mutates
 * PHONE_AUTH-group keys. Mirrors iOS OnboardingContainerViewModel.applyValidation behavior.
 *
 * OnboardingFieldGroup has only PHONE_AUTH left since DOCS was removed with the native
 * document-capture flow, so applyValidation's cross-group preservation (it filters errors by
 * `it.group != group` before merging) has no coverage until a second group exists again.
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

    @Test fun clearError_removesSingleEntry() {
        val vm = makeVM()
        vm.validateAllPhoneAuth()
        assertNotNull(vm.errorFor(OnboardingField.AUTH_PHONE))
        vm.clearError(OnboardingField.AUTH_PHONE)
        assertNull(vm.errorFor(OnboardingField.AUTH_PHONE))
        assertNotNull(vm.errorFor(OnboardingField.AUTH_BIRTH_MONTH))
    }

}
