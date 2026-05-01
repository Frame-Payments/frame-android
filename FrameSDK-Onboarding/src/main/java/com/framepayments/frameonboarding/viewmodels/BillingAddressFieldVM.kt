package com.framepayments.frameonboarding.viewmodels

import com.framepayments.framesdk.FrameObjects
import com.framepayments.frameonboarding.classes.AddressFormat
import com.framepayments.frameonboarding.validation.OnboardingValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class BillingAddressMode { US_ONLY, INTERNATIONAL }

/**
 * Per-screen view model for the billing address form.
 * 1:1 port of iOS BillingAddressViewModel.
 *
 * In [BillingAddressMode.US_ONLY] mode the country is pinned to "US" and zip uses
 * the US 5-digit validator. In [BillingAddressMode.INTERNATIONAL] mode the country
 * is selectable and postal code validation uses [OnboardingValidators.validatePostalCode]
 * for the active country.
 */
class BillingAddressFieldVM(
    initial: FrameObjects.BillingAddress,
    val mode: BillingAddressMode
) {

    enum class Field { LINE1, CITY, STATE, POSTAL, COUNTRY }

    private val _address = MutableStateFlow(
        when (mode) {
            BillingAddressMode.US_ONLY -> initial.copy(country = "US")
            BillingAddressMode.INTERNATIONAL ->
                if (initial.country.isNullOrBlank()) initial.copy(country = "US") else initial
        }
    )
    val address: StateFlow<FrameObjects.BillingAddress> = _address.asStateFlow()

    private val _errors = MutableStateFlow<Map<Field, String>>(emptyMap())
    val errors: StateFlow<Map<Field, String>> = _errors.asStateFlow()

    fun updateAddress(transform: (FrameObjects.BillingAddress) -> FrameObjects.BillingAddress) {
        _address.update(transform)
    }

    fun errorFor(field: Field): String? = _errors.value[field]

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
                _address.value.postalCode,
                alpha2.uppercase()
            )
            _errors.update { current ->
                if (updated == null) current - Field.POSTAL
                else current + (Field.POSTAL to updated)
            }
        }
    }

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
                OnboardingValidators.validateZipUS(addr.postalCode)
                    ?.let { next[Field.POSTAL] = it }
            BillingAddressMode.INTERNATIONAL ->
                OnboardingValidators.validatePostalCode(addr.postalCode, countryCode)
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
