package com.framepayments.frameonboarding.viewmodels

import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.classes.OnboardingStep
import org.junit.Assert.assertEquals
import org.junit.Test

class FrameOnboardingNavigationTest {

    private fun makeVM() = FrameOnboardingViewModel(
        OnboardingConfig(
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
}
