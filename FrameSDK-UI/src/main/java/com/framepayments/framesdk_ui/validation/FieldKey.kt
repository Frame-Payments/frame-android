package com.framepayments.framesdk_ui.validation

/**
 * Identifies each validated input field in the checkout form.
 *
 * Used as a key in the [com.framepayments.framesdk_ui.viewmodels.FrameCheckoutViewModel]
 * error map so each field can independently show or clear its validation message.
 */
enum class FieldKey {
    /** Customer full name field. */
    NAME,
    /** Customer email address field. */
    EMAIL,
    /** Billing address line 1 field. */
    ADDRESS_LINE_1,
    /** Billing city field. */
    CITY,
    /** Billing state/region field. */
    STATE,
    /** Billing zip/postal code field. */
    ZIP,
    /** Billing country field. */
    COUNTRY,
    /** Encrypted card input field. */
    CARD
}
