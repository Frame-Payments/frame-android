package com.framepayments.frameonboarding.viewmodels

import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.classes.OnboardingResult
import com.framepayments.frameonboarding.classes.OnboardingStep
import com.framepayments.framesdk.accounts.AccountObjects
import org.junit.Assert.assertEquals
import org.junit.Test

class FrameOnboardingNavigationTest {

    private fun makeVM() = FrameOnboardingViewModel(
        OnboardingConfig(
            clientSecret = "onb_sess_preview",
            requiredCapabilities = listOf(Capabilities.CARD_SEND, Capabilities.BANK_ACCOUNT_RECEIVE),
            skipInitNetwork = true
        )
    )

    @Test fun continueOnSavedCard_skipsAddPaymentMethod() {
        val vm = makeVM()
        vm.navigationState.goTo(OnboardingStep.SelectPaymentMethod)
        vm.moveToNextSegment()
        assertEquals(OnboardingStep.SelectPayoutMethod, vm.navigationState.currentStep)
    }

    @Test fun addCard_stillAdvancesIntoAddPaymentMethod() {
        val vm = makeVM()
        vm.navigationState.goTo(OnboardingStep.SelectPaymentMethod)
        vm.moveNext()
        assertEquals(OnboardingStep.AddPaymentMethod, vm.navigationState.currentStep)
    }

    @Test fun businessWithoutAccountId_setsMismatch() {
        val vm = FrameOnboardingViewModel(
            OnboardingConfig(
                clientSecret = "onb_sess_preview",
                accountType = AccountObjects.AccountType.BUSINESS,
                skipInitNetwork = true
            )
        )
        assertEquals(
            "Business onboarding needs an existing business account.",
            vm.accountTypeMismatch.value
        )
    }

    @Test fun dismissAccountTypeMismatch_setsFailed() {
        val vm = FrameOnboardingViewModel(
            OnboardingConfig(
                clientSecret = "onb_sess_preview",
                accountType = AccountObjects.AccountType.BUSINESS,
                skipInitNetwork = true
            )
        )
        vm.dismissAccountTypeMismatch()
        val result = vm.result.value as OnboardingResult.Failed
        assertEquals("Business onboarding needs an existing business account.", result.message)
    }
}
