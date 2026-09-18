package com.framepayments.framesdk_ui.validation

import com.evervault.sdk.input.model.card.PaymentCardData
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

/**
 * Stateless form validators shared by checkout and onboarding. 1:1 port of iOS
 * [Validators.swift].
 *
 * The original checkout functions (below the marker) return a [ValidationError] constant so
 * the UI can display a localized string resource. The onboarding functions return null on
 * success or an English error string directly (matches iOS) — they are not re-typed into
 * [ValidationError] because doing so would require adding ~25 new string resources with no
 * behavioral benefit.
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

    // --- Onboarding validators (relocated from frameonboarding.validation.OnboardingValidators) ---

    private val ONBOARDING_EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    /**
     * Validates that [value] is non-blank.
     *
     * @param value The string to check.
     * @param fieldName Human-readable field name used in the error message (e.g. "First name").
     * @return Null if valid, a localized error string otherwise.
     */
    fun validateNonEmpty(value: String, fieldName: String): String? =
        if (value.trim().isEmpty()) "$fieldName is required" else null

    /**
     * Validates that [value] contains at least a first and last name separated by whitespace.
     *
     * @param value Full name string entered by the customer.
     * @return Null if valid, a localized error string otherwise.
     */
    fun validateFullName(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return "Full name is required"
        val parts = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        return if (parts.size >= 2) null else "Enter first and last name"
    }

    /**
     * Validates that [value] is a non-empty, well-formed email address.
     *
     * @param value Email string entered by the customer.
     * @return Null if valid, a localized error string otherwise.
     */
    fun validateEmailAddress(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return "Email is required"
        return if (ONBOARDING_EMAIL_REGEX.matches(trimmed)) null else "Enter a valid email address"
    }

    /**
     * Validates that [value] is a 5-digit US zip code.
     *
     * @param value Zip code string entered by the customer.
     * @return Null if valid, a localized error string otherwise.
     */
    fun validateZipUS(value: String): String? {
        if (value.isEmpty()) return "Zip code is required"
        val isValid = value.length == 5 && value.all { it.isDigit() }
        return if (isValid) null else "Enter a 5-digit zip code"
    }

    /**
     * Validates Evervault card data using its potential-validity check.
     *
     * @param data Card data snapshot from the Evervault input, or null if no data has been entered.
     * @return Null if the card is potentially valid, a localized error string otherwise.
     */
    fun validateOnboardingCard(data: PaymentCardData?): String? {
        // Evervault's isPotentiallyValid returns true for an empty card; require a
        // non-empty PAN as well (matches FrameCheckoutViewModel.isCardValid).
        if (data == null) return "Enter valid card details"
        if (data.card.number.isEmpty()) return "Enter valid card details"
        return if (data.isPotentiallyValid) null else "Enter valid card details"
    }

    /**
     * Validates a Card expiration date, ensuring the month is in range and the card has not expired.
     *
     * @param month Expiration month as a 1- or 2-digit string.
     * @param year Expiration year as a 2- or 4-digit string.
     * @return Null if valid, a localized error string otherwise.
     */
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

    /**
     * Validates that [value] is exactly 4 ASCII digits (last four of SSN).
     *
     * @param value SSN last-four digits entered by the customer.
     * @return Null if valid, a localized error string otherwise.
     */
    fun validateSSNLast4(value: String): String? {
        if (value.isEmpty()) return "SSN is required"
        val isValid = value.length == 4 && value.all { it.isDigit() }
        return if (isValid) null else "Enter last 4 digits of SSN"
    }

    /**
     * Validates that [value] is a 9-digit US ABA routing number with a valid checksum.
     *
     * @param value Routing number string entered by the customer.
     * @return Null if valid, a localized error string otherwise.
     */
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

    /**
     * Validates that [value] is a numeric US bank account number within the accepted length range.
     *
     * @param value Account number string entered by the customer.
     * @param min Minimum acceptable length (default: 4).
     * @param max Maximum acceptable length (default: 17).
     * @return Null if valid, a localized error string otherwise.
     */
    fun validateAccountNumberUS(value: String, min: Int = 4, max: Int = 17): String? {
        if (value.isEmpty()) return "Account number is required"
        val isValid = value.all { it.isDigit() } && value.length in min..max
        return if (isValid) null else "Enter a valid account number"
    }

    /**
     * Validates a date-of-birth split into year, month, and day components.
     *
     * Checks that the date is a real calendar date and that the resulting age falls within
     * [minAge] and [maxAge] years from today.
     *
     * @param year 4-digit year string.
     * @param month 1- or 2-digit month string (1–12).
     * @param day 1- or 2-digit day string.
     * @param minAge Minimum required age in years (default: 18).
     * @param maxAge Maximum accepted age in years (default: 120).
     * @return Null if valid, a localized error string otherwise.
     */
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

    private val onboardingPostalRegexes: Map<String, Regex> = mapOf(
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

    /**
     * Validates [value] against the postal code pattern for the given [countryCode].
     *
     * Countries without a known pattern are considered valid (returns null).
     *
     * @param value Postal code string entered by the customer.
     * @param countryCode ISO 3166-1 alpha-2 country code (case-insensitive).
     * @return Null if valid or if the country has no known pattern, a localized error string otherwise.
     */
    fun validatePostalCode(value: String, countryCode: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return "Postal code is required"
        val pattern = onboardingPostalRegexes[countryCode.uppercase()] ?: return null
        return if (pattern.matches(trimmed)) null else "Enter a valid postal code"
    }

    private val onboardingPhoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }

    /**
     * Validates that [raw] is a possible phone number for the given [regionCode] using
     * libphonenumber's "isPossibleNumber" check (less strict than full validation, matching
     * iOS PhoneNumberKit semantics).
     *
     * @param raw Raw phone number string as entered by the customer (without dial code prefix).
     * @param regionCode ISO 3166-1 alpha-2 region code used to parse the number (e.g. "US").
     * @return Null if the number is possibly valid for the region, a localized error string otherwise.
     */
    fun validatePhoneE164(raw: String, regionCode: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return "Phone number is required"
        return try {
            val parsed = onboardingPhoneUtil.parse(trimmed, regionCode.uppercase())
            // Match iOS PhoneNumberKit.parse semantics: accept anything libphonenumber
            // considers a "possible" number for the region. isValidNumber is too strict —
            // it rejects newly assigned mobile prefixes that iOS accepts.
            if (onboardingPhoneUtil.isPossibleNumber(parsed)) null else "Enter a valid phone number"
        } catch (_: NumberParseException) {
            "Enter a valid phone number"
        }
    }
}
