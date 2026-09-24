package com.framepayments.frameonboarding.networking.idv

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IdvCompletionRoutingTest {

    @Test
    fun terminalCategory_contactSupportCopy() {
        val msg = IdvCompletionRouting.idvFailureMessage(
            IdvCompleteResponse(verified = false, category = "terminal")
        )
        assertTrue(msg.contains("contact support"))
    }

    @Test
    fun reviewCategory_inReviewCopy() {
        val msg = IdvCompletionRouting.idvFailureMessage(
            IdvCompleteResponse(verified = false, category = "review")
        )
        assertTrue(msg.contains("in review"))
    }

    @Test
    fun retriableCategory_mismatchCopy() {
        val msg = IdvCompletionRouting.idvFailureMessage(
            IdvCompleteResponse(verified = false, category = "retriable_with_new_data")
        )
        assertTrue(msg.contains("didn't match"))
    }

    @Test
    fun declinedStatusFallback_contactSupport() {
        val msg = IdvCompletionRouting.idvFailureMessage(
            IdvCompleteResponse(verified = false, status = "declined")
        )
        assertTrue(msg.contains("contact support"))
    }

    @Test
    fun unknown_ssnFallbackCopy() {
        val msg = IdvCompletionRouting.idvFailureMessage(
            IdvCompleteResponse(verified = false)
        )
        assertTrue(msg.contains("Social Security Number"))
        assertEquals(
            msg,
            IdvCompletionRouting.idvFailureMessage(null)
        )
    }

    @Test
    fun unknown_ssnNotAllowed_omitsSsnSuggestion() {
        val msg = IdvCompletionRouting.idvFailureMessage(
            IdvCompleteResponse(verified = false),
            ssnAllowed = false
        )
        assertTrue(!msg.contains("Social Security Number"))
    }
}
