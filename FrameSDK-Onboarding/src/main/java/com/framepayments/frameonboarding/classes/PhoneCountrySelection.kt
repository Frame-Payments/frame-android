package com.framepayments.frameonboarding.classes

import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

/**
 * Country selection for the phone number entry. 1:1 port of iOS PhoneCountrySelection.
 */
data class PhoneCountrySelection(
    val alpha2: String,
    val dialCode: String
) {
    val flag: String get() = flagFor(alpha2)
    val displayName: String
        get() = Locale("", alpha2).getDisplayCountry(Locale.getDefault()).ifEmpty { alpha2 }

    companion object {
        private val OFAC_RESTRICTED = setOf(
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

        val default: PhoneCountrySelection
            get() {
                val region = Locale.getDefault().country.takeIf { it.length == 2 } ?: "US"
                return find(region) ?: find("US") ?: PhoneCountrySelection("US", "+1")
            }

        fun find(alpha2: String): PhoneCountrySelection? {
            val key = alpha2.uppercase()
            return all.firstOrNull { it.alpha2 == key }
        }
    }
}
