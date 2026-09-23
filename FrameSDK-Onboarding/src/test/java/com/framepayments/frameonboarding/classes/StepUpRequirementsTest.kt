package com.framepayments.frameonboarding.classes

import com.framepayments.framesdk.capabilities.CapabilityObjects
import com.framepayments.framesdk.capabilities.CapabilityObjects.actionableRequirements
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StepUpRequirementsTest {

    private fun capability(status: String, due: List<String>) = CapabilityObjects.Capability(
        id = "cap_1", `object` = "capability", name = "kyc", accountId = "acct_1",
        status = status, currentlyDue = due, created = null, updated = null
    )

    private val identityDocument = CapabilityObjects.CapabilityRequirementKey.IDENTITY_DOCUMENT

    @Test fun pendingCapability_exposesCurrentlyDue() {
        assertEquals(listOf(identityDocument), capability("pending", listOf(identityDocument)).actionableRequirements)
    }

    @Test fun disabledCapability_hidesDeadKeys() {
        assertTrue(capability("disabled", listOf(identityDocument)).actionableRequirements.isEmpty())
    }

    @Test fun identityDocumentRequired_skipsSsn() {
        assertTrue(OnboardingData(identityDocumentRequired = true).skipsSsnEntry)
    }

    @Test fun correctedKycDetails_outranksGovIdSignals() {
        val data = OnboardingData(
            identityVerifiedViaGovId = true,
            identityDocumentRequired = true,
            correctedKycDetailsRequired = true
        )
        assertFalse(data.skipsSsnEntry)
    }
}
