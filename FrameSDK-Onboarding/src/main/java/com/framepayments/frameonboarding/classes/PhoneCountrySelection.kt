package com.framepayments.frameonboarding.classes

import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

/**
 * Represents a country selection in the phone number entry field.
 *
 * Provides the dial code prefix, Unicode flag emoji, and localized display name for a given
 * ISO 3166-1 alpha-2 region code. Mirrors iOS `PhoneCountrySelection`.
 *
 * @property alpha2 ISO 3166-1 alpha-2 country code (e.g. "US").
 * @property dialCode E.164 dial code prefix including the leading "+" (e.g. "+1").
 */
data class PhoneCountrySelection(
    val alpha2: String,
    val dialCode: String
) {
    /** Unicode flag emoji for this country derived from [alpha2]. */
    val flag: String get() = flagFor(alpha2)

    /** Localized display name of this country in the device's default locale. */
    val displayName: String
        get() = Locale.Builder().setRegion(alpha2).build()
            .getDisplayCountry(Locale.getDefault())
            .ifEmpty { alpha2 }

    /** Factory methods, country list, and OFAC restriction set for the phone picker. */
    companion object {
        /** Countries excluded from address & phone pickers per OFAC compliance. Mirrors iOS. */
        val OFAC_RESTRICTED: Set<String> = setOf(
            "IR", "RU", "KP", "SY", "CU", "CD", "IQ", "LY", "ML", "NI", "SD", "VE", "YE"
        )

        private fun flagFor(alpha2: String): String {
            val base = 0x1F1A5
            val sb = StringBuilder()
            for (ch in alpha2.uppercase()) {
                if (ch in 'A'..'Z') {
                    sb.appendCodePoint(base + ch.code)
                }
            }
            return sb.toString()
        }

        /**
         * Lazily built list of all supported regions sorted alphabetically by display name,
         * with OFAC-restricted countries excluded.
         */
        val all: List<PhoneCountrySelection> by lazy {
            val util = PhoneNumberUtil.getInstance()
            util.supportedRegions
                .filter { it.uppercase() !in OFAC_RESTRICTED }
                .mapNotNull { code ->
                    val dial = util.getCountryCodeForRegion(code).takeIf { it > 0 }
                        ?: return@mapNotNull null
                    PhoneCountrySelection(alpha2 = code.uppercase(), dialCode = "+$dial")
                }
                .sortedBy { it.displayName }
        }

        /**
         * The default selection for the current device locale; falls back to "US" when the
         * locale region is unavailable or OFAC-restricted.
         */
        val default: PhoneCountrySelection
            get() {
                val region = Locale.getDefault().country.takeIf { it.length == 2 } ?: "US"
                return find(region) ?: find("US") ?: PhoneCountrySelection("US", "+1")
            }

        /**
         * Finds the [PhoneCountrySelection] for the given [alpha2] code, or null if the country
         * is not in [all] (e.g. it is OFAC-restricted or unsupported).
         *
         * @param alpha2 ISO 3166-1 alpha-2 country code to look up (case-insensitive).
         * @return The matching [PhoneCountrySelection], or null if not found.
         */
        fun find(alpha2: String): PhoneCountrySelection? {
            val key = alpha2.uppercase()
            return all.firstOrNull { it.alpha2 == key }
        }
    }
}
