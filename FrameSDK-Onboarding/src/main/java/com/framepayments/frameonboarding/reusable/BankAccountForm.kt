package com.framepayments.frameonboarding.reusable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Reusable bank account (ACH) form section.
 * Mirrors iOS BankAccountDetailView + account type picker.
 * Fields: Routing Number, Account Number, Account Type (Checking/Savings).
 */
@Composable
fun BankAccountForm(
    routingNumber: String,
    onRoutingNumberChange: (String) -> Unit,
    accountNumber: String,
    onAccountNumberChange: (String) -> Unit,
    accountType: String,
    onAccountTypeChange: (String) -> Unit,
    headerTitle: String = "Bank Account Details",
    showHeader: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        if (showHeader) {
            Text(
                text = headerTitle,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        OutlinedTextField(
            value = routingNumber,
            onValueChange = { value ->
                if (value.length <= 9 && value.all { it.isDigit() }) {
                    onRoutingNumberChange(value)
                }
            },
            label = { Text("Routing Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = accountNumber,
            onValueChange = { value ->
                if (value.all { it.isDigit() }) {
                    onAccountNumberChange(value)
                }
            },
            label = { Text("Account Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Box {
            OutlinedTextField(
                value = accountType,
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
                        onAccountTypeChange("Checking")
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Savings") },
                    onClick = {
                        onAccountTypeChange("Savings")
                        expanded = false
                    }
                )
            }
        }
    }
}
