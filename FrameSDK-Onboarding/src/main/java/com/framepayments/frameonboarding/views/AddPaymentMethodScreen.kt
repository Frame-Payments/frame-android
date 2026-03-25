package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.classes.PaymentMethodDetails
import com.framepayments.frameonboarding.reusable.BillingAddressForm
import com.framepayments.frameonboarding.reusable.PaymentCardForm
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

    val canContinue = cardNumber.replace(" ", "").length >= 16 &&
            expiryMonth.isNotEmpty() && expiryYear.isNotEmpty() &&
            cvc.length >= 3 &&
            addressLine1.isNotEmpty() && city.isNotEmpty() &&
            state.isNotEmpty() && zipCode.length == 5

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Payment Method") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                PaymentCardForm(
                    cardNumber = cardNumber,
                    onCardNumberChange = { cardNumber = it },
                    expiryMonth = expiryMonth,
                    expiryYear = expiryYear,
                    onExpiryChange = { m, y -> expiryMonth = m; expiryYear = y },
                    cvc = cvc,
                    onCvcChange = { cvc = it }
                )

                Spacer(Modifier.height(24.dp))

                BillingAddressForm(
                    addressLine1 = addressLine1,
                    onAddressLine1Change = { addressLine1 = it },
                    addressLine2 = addressLine2,
                    onAddressLine2Change = { addressLine2 = it },
                    city = city,
                    onCityChange = { city = it },
                    state = state,
                    onStateChange = { state = it },
                    zipCode = zipCode,
                    onZipCodeChange = { zipCode = it },
                    headerTitle = "Customer Address"
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
                Text("Continue")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPaymentMethodScreenPreview() {
    AddPaymentMethodScreen(onBack = {}, onContinue = {})
}
