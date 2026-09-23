package com.framepayments.frameonboarding.classes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ComputeOrderedStepsTest {

    @Test
    fun emptyHostConfig_startsAtPersonalInfo() {
        val segments = computeFlowSegments(emptyList(), emptyList())
        assertEquals(
            listOf(
                OnboardingFlowSegment.PERSONAL_INFORMATION,
                OnboardingFlowSegment.VERIFICATION_SUBMITTED
            ),
            segments
        )
    }

    @Test
    fun allCapabilitiesAlreadyComplete_goesToFinalScreen() {
        // Shrinking list is empty, but the host originally asked for something.
        val segments = computeFlowSegments(
            requiredCapabilities = emptyList(),
            originallyRequiredCapabilities = listOf(Capabilities.KYC, Capabilities.CARD_SEND)
        )
        assertEquals(listOf(OnboardingFlowSegment.VERIFICATION_SUBMITTED), segments)
    }

    @Test
    fun paymentSegment_doesNotIncludeVerifyYourCard() {
        val steps = computeOrderedSteps(listOf(Capabilities.CARD_SEND))
        assertFalse(steps.contains(OnboardingStep.VerifyYourCard))
        assertTrue(steps.contains(OnboardingStep.SelectPaymentMethod))
        assertTrue(steps.contains(OnboardingStep.AddPaymentMethod))
    }
}
