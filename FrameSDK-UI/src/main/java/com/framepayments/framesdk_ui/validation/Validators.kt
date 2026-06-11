package com.framepayments.framesdk_ui.validation

import com.evervault.sdk.input.model.card.PaymentCardData

/**
 * Stateless checkout form validators used by [com.framepayments.framesdk_ui.FrameCheckoutView].
 *
 * Each function returns null on success or a [ValidationError] constant on failure.
 */
object Validators {

    private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    /**
     * Validates that [value] contains at least a first and last name separated by whitespace.
     *
     * @param value Raw name string entered by the customer.
     * @return Null if valid, [ValidationError.NAME_REQUIRED] otherwise.
     */
    fun validateName(value: String?): ValidationError? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty()) return ValidationError.NAME_REQUIRED
        val parts = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        return if (parts.size >= 2) null else ValidationError.NAME_REQUIRED
    }

    /**
     * Validates that [value] is a non-empty, well-formed email address.
     *
     * @param value Raw email string entered by the customer.
     * @return Null if valid, [ValidationError.EMAIL_INVALID] otherwise.
     */
    fun validateEmail(value: String?): ValidationError? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty()) return ValidationError.EMAIL_INVALID
        return if (EMAIL_REGEX.matches(trimmed)) null else ValidationError.EMAIL_INVALID
    }

    /**
     * Validates that [value] is non-blank.
     *
     * @param value Address line 1 entered by the customer.
     * @return Null if valid, [ValidationError.ADDRESS_REQUIRED] otherwise.
     */
    fun validateAddressLine1(value: String?): ValidationError? =
        if (value.isNullOrBlank()) ValidationError.ADDRESS_REQUIRED else null

    /**
     * Validates that [value] is non-blank.
     *
     * @param value City name entered by the customer.
     * @return Null if valid, [ValidationError.CITY_REQUIRED] otherwise.
     */
    fun validateCity(value: String?): ValidationError? =
        if (value.isNullOrBlank()) ValidationError.CITY_REQUIRED else null

    /**
     * Validates that [value] is non-blank.
     *
     * @param value State or region entered by the customer.
     * @return Null if valid, [ValidationError.STATE_REQUIRED] otherwise.
     */
    fun validateState(value: String?): ValidationError? =
        if (value.isNullOrBlank()) ValidationError.STATE_REQUIRED else null

    /**
     * Validates that [value] is exactly 5 ASCII digits (US zip code format).
     *
     * @param value Zip code entered by the customer.
     * @return Null if valid, [ValidationError.ZIP_INVALID] otherwise.
     */
    fun validateZip(value: String?): ValidationError? {
        val v = value.orEmpty()
        return if (v.length == 5 && v.all { it.isDigit() }) null else ValidationError.ZIP_INVALID
    }

    /**
     * Validates that [alpha2Code] is non-blank.
     *
     * @param alpha2Code ISO 3166-1 alpha-2 country code selected by the customer.
     * @return Null if valid, [ValidationError.COUNTRY_REQUIRED] otherwise.
     */
    fun validateCountry(alpha2Code: String?): ValidationError? =
        if (alpha2Code.isNullOrBlank()) ValidationError.COUNTRY_REQUIRED else null

    /**
     * Validates Evervault card data using its built-in potential-validity check.
     *
     * @param cardData Card data snapshot from [com.framepayments.framesdk_ui.EncryptedPaymentCardInput].
     * @return Null if the card is potentially valid, [ValidationError.CARD_INVALID] otherwise.
     */
    fun validateCard(cardData: PaymentCardData?): ValidationError? =
        if (cardData != null && cardData.isPotentiallyValid) null else ValidationError.CARD_INVALID
}
