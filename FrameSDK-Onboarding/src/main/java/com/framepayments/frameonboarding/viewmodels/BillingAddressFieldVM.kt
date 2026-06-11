package com.framepayments.frameonboarding.viewmodels

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.framepayments.framesdk.FrameObjects
import com.framepayments.frameonboarding.classes.AddressFormat
import com.framepayments.frameonboarding.validation.OnboardingValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Controls the validation behavior and available fields in [BillingAddressFieldVM].
 */
enum class BillingAddressMode {
    /** Pins the country to "US" and uses US 5-digit zip validation. */
    US_ONLY,
    /** Allows country selection and applies per-country postal code validation. */
    INTERNATIONAL
}

/**
 * Per-screen view model for the billing address form.
 *
 * Mirrors iOS `BillingAddressViewModel`. In [BillingAddressMode.US_ONLY] mode the country is
 * pinned to "US" and zip uses the US 5-digit validator. In [BillingAddressMode.INTERNATIONAL]
 * mode the country is selectable and postal code validation uses
 * [OnboardingValidators.validatePostalCode] for the active country.
 *
 * @param initial Pre-populated billing address (country defaults to "US" if blank).
 * @param mode Validation behavior and country-field visibility mode.
 */
class BillingAddressFieldVM(
    initial: FrameObjects.BillingAddress,
    val mode: BillingAddressMode
) {

    /** Identifies each input field in the billing address form. */
    enum class Field {
        /** Address line 1 field. */
        LINE1,
        /** City field. */
        CITY,
        /** State/province field. */
        STATE,
        /** Postal/zip code field. */
        POSTAL,
        /** Country selection field (only used in [BillingAddressMode.INTERNATIONAL] mode). */
        COUNTRY
    }

    private val _address = MutableStateFlow(
        when (mode) {
            BillingAddressMode.US_ONLY -> initial.copy(country = "US")
            BillingAddressMode.INTERNATIONAL ->
                if (initial.country.isNullOrBlank()) initial.copy(country = "US") else initial
        }
    )
    /** Current billing address state; collect to drive the form UI. */
    val address: StateFlow<FrameObjects.BillingAddress> = _address.asStateFlow()

    private val _errors = MutableStateFlow<Map<Field, String>>(emptyMap())
    /** Current field-level validation errors keyed by [Field]. */
    val errors: StateFlow<Map<Field, String>> = _errors.asStateFlow()

    /**
     * Applies [transform] to the current [address], replacing it with the result.
     *
     * @param transform Pure function that maps the current address to an updated address.
     */
    fun updateAddress(transform: (FrameObjects.BillingAddress) -> FrameObjects.BillingAddress) {
        _address.update(transform)
    }

    /**
     * Returns the current validation error message for [field], or null if the field is valid.
     *
     * @param field The field to query.
     * @return Error string, or null.
     */
    fun errorFor(field: Field): String? = _errors.value[field]

    /**
     * Clears the validation error for [field] if one is currently set.
     *
     * @param field The field whose error should be cleared.
     */
    fun clearError(field: Field) {
        if (_errors.value.containsKey(field)) {
            _errors.update { it - field }
        }
    }

    /**
     * Update country selection. If a postal error is currently shown, re-validate it
     * for the new country's rules so the user sees an updated message immediately
     * (matches iOS .onChange(of: selectedCountry) re-validation).
     */
    fun setCountry(alpha2: String) {
        _address.update { it.copy(country = alpha2.uppercase()) }
        if (_errors.value.containsKey(Field.POSTAL) && mode == BillingAddressMode.INTERNATIONAL) {
            val updated = OnboardingValidators.validatePostalCode(
                _address.value.postalCode.orEmpty(),
                alpha2.uppercase()
            )
            _errors.update { current ->
                if (updated == null) current - Field.POSTAL
                else current + (Field.POSTAL to updated)
            }
        }
    }

    /** Factory methods for constructing a [BillingAddressFieldVM] and its state [Saver]. */
    companion object {
        /** Saver for use with `rememberSaveable` so user typing survives config change. */
        fun Saver(mode: BillingAddressMode): Saver<BillingAddressFieldVM, Any> =
            listSaver(
                save = { vm ->
                    val a = vm._address.value
                    listOf(
                        a.addressLine1.orEmpty(),
                        a.addressLine2.orEmpty(),
                        a.city.orEmpty(),
                        a.state.orEmpty(),
                        a.postalCode.orEmpty(),
                        a.country.orEmpty()
                    )
                },
                restore = { saved ->
                    BillingAddressFieldVM(
                        initial = FrameObjects.BillingAddress(
                            addressLine1 = (saved[0] as String).ifBlank { null },
                            addressLine2 = (saved[1] as String).ifBlank { null },
                            city = (saved[2] as String).ifBlank { null },
                            state = (saved[3] as String).ifBlank { null },
                            postalCode = (saved[4] as String).ifBlank { null },
                            country = (saved[5] as String).ifBlank { null }
                        ),
                        mode = mode
                    )
                }
            )
    }

    /**
     * Validates the current [address] according to the active [mode] and updates [errors].
     *
     * @return True if all required fields pass validation; false if any errors were found.
     */
    fun validate(): Boolean {
        val next = mutableMapOf<Field, String>()
        val addr = _address.value

        OnboardingValidators.validateNonEmpty(addr.addressLine1.orEmpty(), "Address line 1")
            ?.let { next[Field.LINE1] = it }
        OnboardingValidators.validateNonEmpty(addr.city.orEmpty(), "City")
            ?.let { next[Field.CITY] = it }

        val countryCode = addr.country?.takeIf { it.isNotBlank() } ?: "US"
        val stateLabel = AddressFormat.format(countryCode).stateLabel
        OnboardingValidators.validateNonEmpty(addr.state.orEmpty(), stateLabel)
            ?.let { next[Field.STATE] = it }

        when (mode) {
            BillingAddressMode.US_ONLY ->
                OnboardingValidators.validateZipUS(addr.postalCode.orEmpty())
                    ?.let { next[Field.POSTAL] = it }
            BillingAddressMode.INTERNATIONAL ->
                OnboardingValidators.validatePostalCode(addr.postalCode.orEmpty(), countryCode)
                    ?.let { next[Field.POSTAL] = it }
        }

        if (mode == BillingAddressMode.INTERNATIONAL) {
            OnboardingValidators.validateNonEmpty(addr.country.orEmpty(), "Country")
                ?.let { next[Field.COUNTRY] = it }
        }

        _errors.value = next
        return next.isEmpty()
    }
}
