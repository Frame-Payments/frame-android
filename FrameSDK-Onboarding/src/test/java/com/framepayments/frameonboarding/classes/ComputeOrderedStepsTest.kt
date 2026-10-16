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

    @Test
    fun businessInsertsDetailsAndOwnersBeforePayment() {
        val segments = computeFlowSegments(
            requiredCapabilities = listOf(Capabilities.CARD_SEND, Capabilities.BANK_ACCOUNT_RECEIVE),
            accountType = com.framepayments.framesdk.accounts.AccountObjects.AccountType.BUSINESS
        )
        assertEquals(
            listOf(
                OnboardingFlowSegment.PERSONAL_INFORMATION,
                OnboardingFlowSegment.BUSINESS_DETAILS,
                OnboardingFlowSegment.OWNERS_AND_CONTROLLERS,
                OnboardingFlowSegment.CONFIRM_PAYMENT_METHOD,
                OnboardingFlowSegment.CONFIRM_PAYOUT_METHOD,
                OnboardingFlowSegment.VERIFICATION_SUBMITTED
            ),
            segments
        )
    }

    @Test
    fun businessWithoutCapabilitiesStillCollectsTheBusinessSteps() {
        val steps = computeOrderedSteps(
            requiredCapabilities = emptyList(),
            originallyRequiredCapabilities = emptyList(),
            accountType = com.framepayments.framesdk.accounts.AccountObjects.AccountType.BUSINESS
        )
        assertEquals(
            listOf(
                OnboardingStep.VerifyIdentification,
                OnboardingStep.BusinessDetails,
                OnboardingStep.OwnersAndControllers
            ),
            steps.filter {
                it != OnboardingStep.VerificationWelcome && it != OnboardingStep.VerificationSubmitted
            }
        )
    }
}
