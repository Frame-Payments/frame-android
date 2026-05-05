package com.framepayments.frameonboarding.reusable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.viewmodels.BankAccountFieldVM

/**
 * Bank account form bound to a [BankAccountFieldVM]. 1:1 port of iOS BankAccountDetailView.
 *
 * Inline error rendering on routing and account fields. Account-type dropdown is rendered
 * here for parity with the existing Android form, but its state lives in the draft (not the VM)
 * to mirror iOS where account type is held outside BankAccountViewModel.
 */
@Composable
fun BankAccountDetailView(
    viewModel: BankAccountFieldVM,
    headerTitle: String = "Bank Account Details",
    showHeader: Boolean = true,
    showAccountTypePicker: Boolean = true
) {
    val draft by viewModel.draft.collectAsState()
    val errors by viewModel.errors.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Column {
        if (showHeader) {
            Text(
                text = headerTitle,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        ValidatedTextField(
            value = draft.routingNumber,
            onValueChange = { v ->
                val filtered = v.filter(Char::isDigit)
                viewModel.updateDraft { it.copy(routingNumber = filtered) }
            },
            prompt = "Routing Number",
            error = errors[BankAccountFieldVM.Field.ROUTING],
            keyboardType = KeyboardType.Number,
            characterLimit = 9,
            inlineError = true,
            onClearError = { viewModel.clearError(BankAccountFieldVM.Field.ROUTING) }
        )

        Spacer(Modifier.height(16.dp))

        ValidatedTextField(
            value = draft.accountNumber,
            onValueChange = { v ->
                val filtered = v.filter(Char::isDigit)
                viewModel.updateDraft { it.copy(accountNumber = filtered) }
            },
            prompt = "Account Number",
            error = errors[BankAccountFieldVM.Field.ACCOUNT],
            keyboardType = KeyboardType.Number,
            characterLimit = 17,
            inlineError = true,
            onClearError = { viewModel.clearError(BankAccountFieldVM.Field.ACCOUNT) }
        )

        if (showAccountTypePicker) {
            Spacer(Modifier.height(16.dp))
            Box {
                OutlinedTextField(
                    value = draft.accountTypeLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Account Type") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Expand"
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DropdownMenuItem(
                        text = { Text("Checking") },
                        onClick = {
                            viewModel.updateDraft { it.copy(accountTypeLabel = "Checking") }
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Savings") },
                        onClick = {
                            viewModel.updateDraft { it.copy(accountTypeLabel = "Savings") }
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
