package com.framepayments.framesdk_ui.validation

import com.evervault.sdk.input.model.card.PaymentCardData

object Validators {

    private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun validateName(value: String?): ValidationError? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty()) return ValidationError.NAME_REQUIRED
        val parts = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        return if (parts.size >= 2) null else ValidationError.NAME_REQUIRED
    }

    fun validateEmail(value: String?): ValidationError? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty()) return ValidationError.EMAIL_INVALID
        return if (EMAIL_REGEX.matches(trimmed)) null else ValidationError.EMAIL_INVALID
    }

    fun validateAddressLine1(value: String?): ValidationError? =
        if (value.isNullOrBlank()) ValidationError.ADDRESS_REQUIRED else null

    fun validateCity(value: String?): ValidationError? =
        if (value.isNullOrBlank()) ValidationError.CITY_REQUIRED else null

    fun validateState(value: String?): ValidationError? =
        if (value.isNullOrBlank()) ValidationError.STATE_REQUIRED else null

    fun validateZip(value: String?): ValidationError? {
        val v = value.orEmpty()
        return if (v.length == 5 && v.all { it.isDigit() }) null else ValidationError.ZIP_INVALID
    }

    fun validateCountry(alpha2Code: String?): ValidationError? =
        if (alpha2Code.isNullOrBlank()) ValidationError.COUNTRY_REQUIRED else null

    fun validateCard(cardData: PaymentCardData?): ValidationError? =
        if (cardData != null && cardData.isPotentiallyValid) null else ValidationError.CARD_INVALID
}
