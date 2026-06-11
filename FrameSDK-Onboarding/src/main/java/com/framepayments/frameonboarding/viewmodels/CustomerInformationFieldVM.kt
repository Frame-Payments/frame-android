package com.framepayments.frameonboarding.viewmodels

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.customeridentity.CustomerIdentityRequests
import com.framepayments.frameonboarding.classes.PhoneCountrySelection
import com.framepayments.frameonboarding.validation.OnboardingValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Per-screen view model for the customer information form (first/last name, email, phone, DOB, SSN).
 *
 * Mirrors iOS `CustomerInformationViewModel`. Stores DOB as ISO "YYYY-MM-DD" inside [identity];
 * the view layer manages three local Compose state strings for month/day/year and syncs them via
 * [com.framepayments.frameonboarding.validation.DateOfBirthFormatter].
 *
 * @param initialIdentity Pre-populated identity request (all fields default to empty strings).
 * @param initialPhoneCountry Pre-selected phone country (defaults to the device locale country).
 */
class CustomerInformationFieldVM(
    initialIdentity: CustomerIdentityRequests.CreateCustomerIdentityRequest,
    initialPhoneCountry: PhoneCountrySelection = PhoneCountrySelection.default
) {

    /** Identifies each input field in the customer information form. */
    enum class Field {
        /** First name field. */
        FIRST_NAME,
        /** Last name field. */
        LAST_NAME,
        /** Email address field. */
        EMAIL,
        /** Phone number field. */
        PHONE,
        /** Birth month field (1–2 digit numeric string). */
        BIRTH_MONTH,
        /** Birth day field (1–2 digit numeric string). */
        BIRTH_DAY,
        /** Birth year field (4-digit numeric string). */
        BIRTH_YEAR,
        /** Last four digits of SSN field. */
        SSN
    }

    private val _identity = MutableStateFlow(initialIdentity)
    /** Current identity request state; collect to drive the form UI. */
    val identity: StateFlow<CustomerIdentityRequests.CreateCustomerIdentityRequest> =
        _identity.asStateFlow()

    private val _phoneCountry = MutableStateFlow(initialPhoneCountry)
    /** Currently selected phone country for dial-code prefix display and validation. */
    val phoneCountry: StateFlow<PhoneCountrySelection> = _phoneCountry.asStateFlow()

    private val _errors = MutableStateFlow<Map<Field, String>>(emptyMap())
    /** Current field-level validation errors keyed by [Field]. */
    val errors: StateFlow<Map<Field, String>> = _errors.asStateFlow()

    /**
     * First non-null DOB error, in month → day → year order. Computed from the current
     * errors snapshot — call from a `derivedStateOf` in the composable so reads recompose
     * when [errors] changes.
     */
    fun firstDateOfBirthError(): String? {
        val errs = _errors.value
        return errs[Field.BIRTH_MONTH] ?: errs[Field.BIRTH_DAY] ?: errs[Field.BIRTH_YEAR]
    }

    /**
     * Applies [transform] to the current [identity] request, replacing it with the result.
     *
     * @param transform Pure function that maps the current identity request to an updated one.
     */
    fun updateIdentity(
        transform: (CustomerIdentityRequests.CreateCustomerIdentityRequest) -> CustomerIdentityRequests.CreateCustomerIdentityRequest
    ) {
        _identity.update(transform)
    }

    /**
     * Updates the selected phone country to [selection].
     *
     * @param selection The [PhoneCountrySelection] chosen by the customer.
     */
    fun setPhoneCountry(selection: PhoneCountrySelection) {
        _phoneCountry.value = selection
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

    /** Clear all three DOB field errors together (used when user types in any DOB cell). */
    fun clearDateOfBirthErrors() {
        val current = _errors.value
        if (current.containsKey(Field.BIRTH_MONTH) ||
            current.containsKey(Field.BIRTH_DAY) ||
            current.containsKey(Field.BIRTH_YEAR)
        ) {
            _errors.update {
                it - Field.BIRTH_MONTH - Field.BIRTH_DAY - Field.BIRTH_YEAR
            }
        }
    }

    /**
     * Validates the current [identity] form and updates [errors] with any failures.
     *
     * @return True if all fields pass validation; false if any errors were found.
     */
    fun validate(): Boolean {
        val next = mutableMapOf<Field, String>()
        val id = _identity.value

        OnboardingValidators.validateNonEmpty(id.firstName, "First name")
            ?.let { next[Field.FIRST_NAME] = it }
        OnboardingValidators.validateNonEmpty(id.lastName, "Last name")
            ?.let { next[Field.LAST_NAME] = it }
        OnboardingValidators.validateEmail(id.email)
            ?.let { next[Field.EMAIL] = it }
        OnboardingValidators.validatePhoneE164(id.phoneNumber, _phoneCountry.value.alpha2)
            ?.let { next[Field.PHONE] = it }

        // Parse stored ISO date "YYYY-MM-DD" into components for validation.
        val parts = id.dateOfBirth.split("-")
        val year = parts.getOrNull(0).orEmpty()
        val month = parts.getOrNull(1).orEmpty()
        val day = parts.getOrNull(2).orEmpty()
        OnboardingValidators.validateDateOfBirth(year = year, month = month, day = day)
            ?.let { err ->
                next[Field.BIRTH_MONTH] = err
                next[Field.BIRTH_DAY] = err
                next[Field.BIRTH_YEAR] = err
            }

        OnboardingValidators.validateSSNLast4(id.ssn)
            ?.let { next[Field.SSN] = it }

        _errors.value = next
        return next.isEmpty()
    }

    /** Saver factory for use with `rememberSaveable` so customer information survives config change. */
    companion object {
        /** Saver so user typing survives config change (rotation, dark-mode toggle, etc.). */
        val Saver: Saver<CustomerInformationFieldVM, Any> = listSaver(
            save = { vm ->
                val id = vm._identity.value
                val a = id.address
                listOf(
                    id.firstName, id.lastName, id.email, id.phoneNumber,
                    id.dateOfBirth, id.ssn,
                    a.addressLine1.orEmpty(), a.addressLine2.orEmpty(),
                    a.city.orEmpty(), a.state.orEmpty(),
                    a.postalCode, a.country.orEmpty(),
                    vm._phoneCountry.value.alpha2, vm._phoneCountry.value.dialCode
                )
            },
            restore = { saved ->
                val identity = CustomerIdentityRequests.CreateCustomerIdentityRequest(
                    address = FrameObjects.BillingAddress(
                        addressLine1 = (saved[6] as String).ifBlank { null },
                        addressLine2 = (saved[7] as String).ifBlank { null },
                        city = (saved[8] as String).ifBlank { null },
                        state = (saved[9] as String).ifBlank { null },
                        postalCode = saved[10] as String,
                        country = (saved[11] as String).ifBlank { null }
                    ),
                    firstName = saved[0] as String,
                    lastName = saved[1] as String,
                    email = saved[2] as String,
                    phoneNumber = saved[3] as String,
                    dateOfBirth = saved[4] as String,
                    ssn = saved[5] as String
                )
                CustomerInformationFieldVM(
                    initialIdentity = identity,
                    initialPhoneCountry = PhoneCountrySelection(
                        alpha2 = saved[12] as String,
                        dialCode = saved[13] as String
                    )
                )
            }
        )
    }
}
