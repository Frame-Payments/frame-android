package com.framepayments.framesdk_ui.validation

import androidx.annotation.StringRes
import com.framepayments.framesdk_ui.R

/**
 * Typed validation failure for a checkout form field.
 *
 * Each constant carries a string resource reference so the UI can display a localized message
 * without coupling validation logic to Android context.
 *
 * @property messageRes String resource ID of the human-readable error message.
 */
enum class ValidationError(@StringRes val messageRes: Int) {
    /** Customer name is missing or does not contain a first and last name. */
    NAME_REQUIRED(R.string.error_name_required),
    /** Email address is empty or does not match a valid format. */
    EMAIL_INVALID(R.string.error_email_invalid),
    /** Billing address line 1 is empty. */
    ADDRESS_REQUIRED(R.string.error_address_required),
    /** Billing city is empty. */
    CITY_REQUIRED(R.string.error_city_required),
    /** Billing state/region is empty. */
    STATE_REQUIRED(R.string.error_state_required),
    /** Zip/postal code is not a valid 5-digit US zip. */
    ZIP_INVALID(R.string.error_zip_invalid),
    /** Country has not been selected. */
    COUNTRY_REQUIRED(R.string.error_country_required),
    /** Card data is absent or not potentially valid per Evervault's check. */
    CARD_INVALID(R.string.error_card_invalid)
}
