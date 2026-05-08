package com.framepayments.frameonboarding.reusable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk_ui.viewmodels.AvailableCountries
import com.framepayments.frameonboarding.classes.AddressFormat
import com.framepayments.frameonboarding.viewmodels.BillingAddressFieldVM
import com.framepayments.frameonboarding.viewmodels.BillingAddressMode
import com.framepayments.framesdk_ui.theme.LocalFrameTheme

/**
 * Billing address form bound to a [BillingAddressFieldVM]. 1:1 port of iOS BillingAddressDetailView.
 *
 * - Inline-error rendering for every field.
 * - State label and postal label/keyboard come from [AddressFormat] for the active country.
 * - In [BillingAddressMode.INTERNATIONAL] mode a country picker is rendered at the bottom.
 *   Switching country re-runs postal validation so error messages update for the new country's rules.
 */
@Composable
fun BillingAddressDetailView(
    viewModel: BillingAddressFieldVM,
    headerTitle: String = "Billing Address",
    showHeader: Boolean = true
) {
    val address by viewModel.address.collectAsState()
    val errors by viewModel.errors.collectAsState()
    var showCountryPicker by remember { mutableStateOf(false) }
    val theme = LocalFrameTheme.current

    val isInternational = viewModel.mode == BillingAddressMode.INTERNATIONAL
    val countryCode = address.country?.takeIf { it.isNotBlank() } ?: "US"
    val format = remember(countryCode) { AddressFormat.format(countryCode) }

    Column {
        if (showHeader) {
            Text(
                text = headerTitle,
                style = theme.fonts.label,
                color = theme.colors.textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        ValidatedTextField(
            value = address.addressLine1.orEmpty(),
            onValueChange = { v -> viewModel.updateAddress { it.copy(addressLine1 = v) } },
            prompt = "Address Line 1",
            error = errors[BillingAddressFieldVM.Field.LINE1],
            inlineError = true,
            onClearError = { viewModel.clearError(BillingAddressFieldVM.Field.LINE1) }
        )

        Spacer(Modifier.height(16.dp))

        ValidatedTextField(
            value = address.addressLine2.orEmpty(),
            onValueChange = { v ->
                viewModel.updateAddress { it.copy(addressLine2 = v.ifBlank { null }) }
            },
            prompt = "Address Line 2",
            error = null,
            inlineError = true
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ValidatedTextField(
                    value = address.city.orEmpty(),
                    onValueChange = { v -> viewModel.updateAddress { it.copy(city = v) } },
                    prompt = "City",
                    error = errors[BillingAddressFieldVM.Field.CITY],
                    inlineError = true,
                    onClearError = { viewModel.clearError(BillingAddressFieldVM.Field.CITY) }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ValidatedTextField(
                    value = address.state.orEmpty(),
                    onValueChange = { v ->
                        val limited = format.stateMaxLength?.let { v.take(it) } ?: v
                        viewModel.updateAddress { it.copy(state = limited) }
                    },
                    prompt = format.stateLabel,
                    error = errors[BillingAddressFieldVM.Field.STATE],
                    characterLimit = format.stateMaxLength,
                    inlineError = true,
                    onClearError = { viewModel.clearError(BillingAddressFieldVM.Field.STATE) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        val postalLimit = if (viewModel.mode == BillingAddressMode.US_ONLY) 5 else null
        ValidatedTextField(
            value = address.postalCode.orEmpty(),
            onValueChange = { v ->
                val filtered = if (postalLimit != null) v.filter(Char::isDigit) else v
                val limited = postalLimit?.let { filtered.take(it) } ?: filtered
                viewModel.updateAddress { it.copy(postalCode = limited) }
            },
            prompt = format.postalLabel,
            error = errors[BillingAddressFieldVM.Field.POSTAL],
            keyboardType = format.postalKeyboard,
            characterLimit = postalLimit,
            inlineError = true,
            onClearError = { viewModel.clearError(BillingAddressFieldVM.Field.POSTAL) }
        )

        if (isInternational) {
            Spacer(Modifier.height(16.dp))
            val displayName = remember(countryCode) {
                AvailableCountries.allCountries.firstOrNull { it.alpha2Code == countryCode }
                    ?.displayName
                    ?: countryCode
            }
            OutlinedTextField(
                value = displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Country") },
                isError = errors.containsKey(BillingAddressFieldVM.Field.COUNTRY),
                textStyle = theme.fonts.body,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCountryPicker = true },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand"
                    )
                }
            )
            errors[BillingAddressFieldVM.Field.COUNTRY]?.let { msg ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = msg,
                    style = theme.fonts.caption,
                    color = theme.colors.error,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }

    if (showCountryPicker) {
        CountryPickerSheet(
            onCountrySelected = { alpha2 ->
                viewModel.setCountry(alpha2)
                viewModel.clearError(BillingAddressFieldVM.Field.COUNTRY)
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false }
        )
    }
}
