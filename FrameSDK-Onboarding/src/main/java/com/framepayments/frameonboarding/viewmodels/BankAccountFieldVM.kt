package com.framepayments.frameonboarding.viewmodels

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.framepayments.frameonboarding.classes.BankAccountDraft
import com.framepayments.frameonboarding.validation.OnboardingValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Per-screen view model for the bank account (ACH) form.
 *
 * Constructed via `remember { }` in the composable — not an AAC ViewModel. Mirrors iOS
 * `BankAccountViewModel`. Account-type selection is intentionally NOT owned here; it lives
 * outside as `@State` on the parent screen.
 *
 * @param initial Initial [BankAccountDraft] pre-populating the form (default: empty draft).
 */
class BankAccountFieldVM(initial: BankAccountDraft = BankAccountDraft()) {

    /** Identifies each input field in the bank account form. */
    enum class Field {
        /** ABA routing number field. */
        ROUTING,
        /** Bank account number field. */
        ACCOUNT
    }

    private val _draft = MutableStateFlow(initial)
    /** Current bank account draft state; collect to drive the form UI. */
    val draft: StateFlow<BankAccountDraft> = _draft.asStateFlow()

    private val _errors = MutableStateFlow<Map<Field, String>>(emptyMap())
    /** Current field-level validation errors keyed by [Field]. */
    val errors: StateFlow<Map<Field, String>> = _errors.asStateFlow()

    /**
     * Applies [transform] to the current [draft], replacing it with the result.
     *
     * @param transform Pure function that maps the current draft to an updated draft.
     */
    fun updateDraft(transform: (BankAccountDraft) -> BankAccountDraft) {
        _draft.update(transform)
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
     * Validates the current [draft] and updates [errors] with any failures.
     *
     * @return True if all fields pass validation; false if any errors were found.
     */
    fun validate(): Boolean {
        val next = mutableMapOf<Field, String>()
        OnboardingValidators.validateRoutingNumberUS(_draft.value.routingNumber)
            ?.let { next[Field.ROUTING] = it }
        OnboardingValidators.validateAccountNumberUS(_draft.value.accountNumber)
            ?.let { next[Field.ACCOUNT] = it }
        _errors.value = next
        return next.isEmpty()
    }

    /** Saver for use with `rememberSaveable` so bank account field state survives config change. */
    companion object {
        /** [Saver] that persists routing number, account number, and account type across config changes. */
        val Saver: Saver<BankAccountFieldVM, Any> = listSaver(
            save = { vm ->
                val d = vm._draft.value
                listOf(d.routingNumber, d.accountNumber, d.accountTypeLabel)
            },
            restore = { saved ->
                BankAccountFieldVM(
                    BankAccountDraft(
                        routingNumber = saved[0] as String,
                        accountNumber = saved[1] as String,
                        accountTypeLabel = saved[2] as String
                    )
                )
            }
        )
    }
}
