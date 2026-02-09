package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.classes.PayoutMethodDetails
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddPayoutMethodScreen(
    onBack: () -> Unit,
    onContinue: (PayoutMethodDetails) -> Unit
) {
    var routingNumber by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("Checking") }

    val canContinue = routingNumber.length >= 9 && accountNumber.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Bank Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Bank Account Details",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = routingNumber,
                    onValueChange = { value ->
                        if (value.length <= 9 && value.all { it.isDigit() }) {
                            routingNumber = value
                        }
                    },
                    label = { Text("Routing Number") },
                    placeholder = { Text("Routing Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() }) {
                            accountNumber = value
                        }
                    },
                    label = { Text("Account Number") },
                    placeholder = { Text("Account Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = accountType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Account Type") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Text("▾")
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
                                accountType = "Checking"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Savings") },
                            onClick = {
                                accountType = "Savings"
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canContinue,
                onClick = {
                    onContinue(
                        PayoutMethodDetails(
                            routingNumber = routingNumber,
                            accountNumber = accountNumber,
                            accountType = accountType
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = FramePrimaryColor,
                    contentColor = FrameOnPrimaryColor,
                    disabledContainerColor = FramePrimaryColor.copy(alpha = 0.35f),
                    disabledContentColor = FrameOnPrimaryColor.copy(alpha = 0.7f)
                )
            ) {
                Text("Add Bank Account")
            }
        }
    }
}
