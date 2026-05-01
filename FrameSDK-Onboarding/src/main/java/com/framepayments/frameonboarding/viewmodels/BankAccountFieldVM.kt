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
 * 1:1 port of iOS BankAccountViewModel.
 *
 * Constructed via `remember { }` in the composable — not an AAC ViewModel.
 * Account-type selection is intentionally NOT owned here (matches iOS, where it lives
 * outside BankAccountViewModel as @State on the parent screen).
 */
class BankAccountFieldVM(initial: BankAccountDraft = BankAccountDraft()) {

    enum class Field { ROUTING, ACCOUNT }

    private val _draft = MutableStateFlow(initial)
    val draft: StateFlow<BankAccountDraft> = _draft.asStateFlow()

    private val _errors = MutableStateFlow<Map<Field, String>>(emptyMap())
    val errors: StateFlow<Map<Field, String>> = _errors.asStateFlow()

    fun updateDraft(transform: (BankAccountDraft) -> BankAccountDraft) {
        _draft.update(transform)
    }

    fun errorFor(field: Field): String? = _errors.value[field]

    fun clearError(field: Field) {
        if (_errors.value.containsKey(field)) {
            _errors.update { it - field }
        }
    }

    fun validate(): Boolean {
        val next = mutableMapOf<Field, String>()
        OnboardingValidators.validateRoutingNumberUS(_draft.value.routingNumber)
            ?.let { next[Field.ROUTING] = it }
        OnboardingValidators.validateAccountNumberUS(_draft.value.accountNumber)
            ?.let { next[Field.ACCOUNT] = it }
        _errors.value = next
        return next.isEmpty()
    }

    companion object {
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
