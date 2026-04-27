package com.framepayments.framesdk_ui.validation

import androidx.annotation.StringRes
import com.framepayments.framesdk_ui.R

enum class ValidationError(@StringRes val messageRes: Int) {
    NAME_REQUIRED(R.string.error_name_required),
    EMAIL_INVALID(R.string.error_email_invalid),
    ADDRESS_REQUIRED(R.string.error_address_required),
    CITY_REQUIRED(R.string.error_city_required),
    STATE_REQUIRED(R.string.error_state_required),
    ZIP_INVALID(R.string.error_zip_invalid),
    COUNTRY_REQUIRED(R.string.error_country_required),
    CARD_INVALID(R.string.error_card_invalid)
}
