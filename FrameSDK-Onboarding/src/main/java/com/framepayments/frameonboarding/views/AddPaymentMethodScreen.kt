package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.classes.PaymentMethodDetails
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddPaymentMethodScreen(
    onBack: () -> Unit,
    onContinue: (PaymentMethodDetails) -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf("") }
    var expiryYear by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var useForPayouts by remember { mutableStateOf(false) }

    val canContinue = cardNumber.length >= 16 &&
            expiryMonth.isNotEmpty() && expiryYear.isNotEmpty() &&
            cvc.length >= 3 &&
            addressLine1.isNotEmpty() && city.isNotEmpty() &&
            state.isNotEmpty() && zipCode.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Debit Card") },
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
                    text = "Card Details",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { value ->
                        // Format card number with spaces every 4 digits
                        val cleaned = value.replace(" ", "")
                        if (cleaned.length <= 16) {
                            cardNumber = cleaned.chunked(4).joinToString(" ")
                        }
                    },
                    label = { Text("Card Number") },
                    placeholder = { Text("4242 4242 4242 4242") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = "$expiryMonth/$expiryYear",
                        onValueChange = { value ->
                            val cleaned = value.replace("/", "").replace(Regex("[^0-9]"), "")
                            if (cleaned.length <= 4) {
                                if (cleaned.length >= 2) {
                                    expiryMonth = cleaned.take(2)
                                    expiryYear = cleaned.drop(2)
                                } else {
                                    expiryMonth = cleaned
                                    expiryYear = ""
                                }
                            }
                        },
                        label = { Text("MM/YY") },
                        placeholder = { Text("MM/YY") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cvc,
                        onValueChange = { value ->
                            if (value.length <= 4 && value.all { it.isDigit() }) {
                                cvc = value
                            }
                        },
                        label = { Text("CVC") },
                        placeholder = { Text("CVC") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Customer Address",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = addressLine1,
                    onValueChange = { addressLine1 = it },
                    label = { Text("Address Line 1") },
                    placeholder = { Text("Address Line 1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = addressLine2,
                    onValueChange = { addressLine2 = it },
                    label = { Text("Address Line 2") },
                    placeholder = { Text("Address Line 2") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        placeholder = { Text("City") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        placeholder = { Text("State") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = zipCode,
                    onValueChange = { value ->
                        if (value.length <= 10) {
                            zipCode = value
                        }
                    },
                    label = { Text("Zip Code") },
                    placeholder = { Text("Zip Code") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = useForPayouts,
                        onCheckedChange = { useForPayouts = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Use this card for payouts if eligible",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canContinue,
                onClick = {
                    onContinue(
                        PaymentMethodDetails(
                            cardNumber = cardNumber.replace(" ", ""),
                            expiryMonth = expiryMonth,
                            expiryYear = expiryYear,
                            cvc = cvc,
                            addressLine1 = addressLine1,
                            addressLine2 = if (addressLine2.isNotEmpty()) addressLine2 else null,
                            city = city,
                            state = state,
                            zipCode = zipCode,
                            useForPayouts = useForPayouts
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
                Text("Add Card")
            }
        }
    }
}
