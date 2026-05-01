package com.framepayments.frameonboarding.validation

import com.evervault.sdk.input.model.card.PaymentCardData
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

/**
 * Onboarding form validators. 1:1 port of iOS [Validators.swift].
 *
 * Returns null on success, English error string on failure (matches iOS).
 * Strings are intentionally hardcoded for parity with iOS — checkout-side
 * [com.framepayments.framesdk_ui.validation.Validators] keeps its typed-enum API.
 */
object OnboardingValidators {

    private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun validateNonEmpty(value: String, fieldName: String): String? =
        if (value.trim().isEmpty()) "$fieldName is required" else null

    fun validateFullName(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return "Full name is required"
        val parts = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        return if (parts.size >= 2) null else "Enter first and last name"
    }

    fun validateEmail(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return "Email is required"
        return if (EMAIL_REGEX.matches(trimmed)) null else "Enter a valid email address"
    }

    fun validateZipUS(value: String): String? {
        if (value.isEmpty()) return "Zip code is required"
        val isValid = value.length == 5 && value.all { it.isDigit() }
        return if (isValid) null else "Enter a 5-digit zip code"
    }

    fun validateCard(data: PaymentCardData?): String? =
        if (data != null && data.isPotentiallyValid) null else "Enter valid card details"

    fun validateCardExpiry(month: String, year: String): String? {
        val m = month.toIntOrNull() ?: return "Invalid expiration month"
        if (m !in 1..12) return "Invalid expiration month"
        val yearString = if (year.length == 2) "20$year" else year
        val y = yearString.toIntOrNull() ?: return "Invalid expiration year"
        if (y < 2000 || y > 2100) return "Invalid expiration year"

        val now = LocalDate.now(ZoneId.systemDefault())
        if (y < now.year || (y == now.year && m < now.monthValue)) {
            return "Card has expired"
        }
        return null
    }

    fun validateSSNLast4(value: String): String? {
        if (value.isEmpty()) return "SSN is required"
        val isValid = value.length == 4 && value.all { it.isDigit() }
        return if (isValid) null else "Enter last 4 digits of SSN"
    }

    fun validateRoutingNumberUS(value: String): String? {
        if (value.isEmpty()) return "Routing number is required"
        if (value.length != 9 || !value.all { it.isDigit() }) {
            return "Enter a 9-digit routing number"
        }
        val d = value.map { it.digitToInt() }
        val checksum = (3 * (d[0] + d[3] + d[6])
            + 7 * (d[1] + d[4] + d[7])
            + (d[2] + d[5] + d[8])) % 10
        return if (checksum == 0) null else "Enter a valid routing number"
    }

    fun validateAccountNumberUS(value: String, min: Int = 4, max: Int = 17): String? {
        if (value.isEmpty()) return "Account number is required"
        val isValid = value.all { it.isDigit() } && value.length in min..max
        return if (isValid) null else "Enter a valid account number"
    }

    fun validateDateOfBirth(
        year: String,
        month: String,
        day: String,
        minAge: Int = 18,
        maxAge: Int = 120
    ): String? {
        if (year.isEmpty() || month.isEmpty() || day.isEmpty()) {
            return "Date of birth is required"
        }
        if (year.length != 4) return "Enter a valid date of birth"
        val y = year.toIntOrNull() ?: return "Enter a valid date of birth"
        val m = month.toIntOrNull() ?: return "Enter a valid date of birth"
        if (m !in 1..12) return "Enter a valid date of birth"
        val d = day.toIntOrNull() ?: return "Enter a valid date of birth"
        if (d < 1) return "Enter a valid date of birth"

        val dob = try {
            LocalDate.of(y, m, d)
        } catch (_: java.time.DateTimeException) {
            return "Enter a valid date of birth"
        }

        val today = LocalDate.now(ZoneId.systemDefault())
        val age = Period.between(dob, today).years
        if (age < minAge) return "You must be at least $minAge years old"
        if (age > maxAge) return "Enter a valid date of birth"
        return null
    }

    private val postalRegexes: Map<String, Regex> = mapOf(
        "US" to Regex("^\\d{5}(-\\d{4})?$"),
        "CA" to Regex("^[A-Za-z]\\d[A-Za-z][ -]?\\d[A-Za-z]\\d$"),
        "GB" to Regex("^[A-Za-z]{1,2}\\d[A-Za-z\\d]?\\s*\\d[A-Za-z]{2}$"),
        "AU" to Regex("^\\d{4}$"),
        "DE" to Regex("^\\d{5}$"),
        "FR" to Regex("^\\d{5}$"),
        "NL" to Regex("^\\d{4}\\s?[A-Za-z]{2}$"),
        "MX" to Regex("^\\d{5}$"),
        "IN" to Regex("^\\d{6}$"),
        "JP" to Regex("^\\d{3}-?\\d{4}$"),
        "BR" to Regex("^\\d{5}-?\\d{3}$"),
        "IT" to Regex("^\\d{5}$"),
        "ES" to Regex("^\\d{5}$"),
        "IE" to Regex("^[A-Za-z]\\d{2}\\s?[A-Za-z\\d]{4}$"),
        "NZ" to Regex("^\\d{4}$"),
        "SG" to Regex("^\\d{6}$")
    )

    fun validatePostalCode(value: String, countryCode: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return "Postal code is required"
        val pattern = postalRegexes[countryCode.uppercase()] ?: return null
        return if (pattern.matches(trimmed)) null else "Enter a valid postal code"
    }

    private val phoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }

    fun validatePhoneE164(raw: String, regionCode: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return "Phone number is required"
        return try {
            val parsed = phoneUtil.parse(trimmed, regionCode.uppercase())
            if (phoneUtil.isValidNumber(parsed)) null else "Enter a valid phone number"
        } catch (_: NumberParseException) {
            "Enter a valid phone number"
        }
    }
}
