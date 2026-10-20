package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.classes.BusinessCategories
import com.framepayments.frameonboarding.classes.BusinessCategory
import com.framepayments.frameonboarding.classes.BusinessStructure
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk_ui.reusable.BillingAddressDetailView
import com.framepayments.framesdk_ui.reusable.ContinueButton
import com.framepayments.framesdk_ui.reusable.ValidatedTextField
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import com.framepayments.framesdk_ui.validation.Validators
import com.framepayments.framesdk_ui.viewmodels.BillingAddressFieldVM
import com.framepayments.framesdk_ui.viewmodels.BillingAddressMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BusinessDetailsScreen(viewModel: FrameOnboardingViewModel) {
    val theme = LocalFrameTheme.current
    val isLoading by viewModel.isPerformingAction.collectAsState()
    var legalName by remember { mutableStateOf("") }
    var structure by remember { mutableStateOf<BusinessStructure?>(null) }
    var category by remember { mutableStateOf<BusinessCategory?>(null) }
    var ein by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var samePhysicalAddress by remember { mutableStateOf(true) }
    var fieldError by remember { mutableStateOf<String?>(null) }
    var showTypePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    val legalAddress = remember { newAddressVM() }
    val physicalAddress = remember { newAddressVM() }

    Scaffold(
        containerColor = theme.colors.surface,
        topBar = {
            TopAppBar(
                title = { Text("Business details", style = theme.fonts.heading) },
                navigationIcon = {
                    TextButton(onClick = { viewModel.moveBack() }) { Text("Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = theme.colors.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Tell us about the business you're onboarding.",
                    style = theme.fonts.bodySmall,
                    color = theme.colors.textSecondary
                )
                Spacer(Modifier.height(theme.spacing.formBlock))
                ValidatedTextField(
                    value = legalName,
                    onValueChange = { legalName = it },
                    prompt = "Legal business name",
                    error = null,
                    inlineError = true
                )
                Text(
                    text = "Exactly as registered with the IRS.",
                    style = theme.fonts.caption,
                    color = theme.colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(Modifier.height(theme.spacing.formBlock))
                PickerRow("Business type", structure?.label) { showTypePicker = true }
                PickerRow("Business category", category?.name) { showCategoryPicker = true }
                Text(
                    text = "Choose the category that best describes what your business sells.",
                    style = theme.fonts.caption,
                    color = theme.colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(Modifier.height(theme.spacing.formBlock))
                ValidatedTextField(
                    value = website,
                    onValueChange = { website = it },
                    prompt = "Website",
                    error = null,
                    keyboardType = KeyboardType.Uri,
                    inlineError = true
                )
                Spacer(Modifier.height(theme.spacing.formBlock))
                ValidatedTextField(
                    value = ein,
                    onValueChange = { ein = it.filter(Char::isDigit).take(9) },
                    prompt = "EIN",
                    error = null,
                    keyboardType = KeyboardType.Number,
                    characterLimit = 9,
                    inlineError = true
                )
                Spacer(Modifier.height(theme.spacing.formBlock))
                ValidatedTextField(
                    value = email,
                    onValueChange = { email = it },
                    prompt = "Business email",
                    error = null,
                    keyboardType = KeyboardType.Email,
                    inlineError = true
                )
                Spacer(Modifier.height(theme.spacing.formBlock))
                BillingAddressDetailView(viewModel = legalAddress, headerTitle = "Legal address")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(
                        text = "Physical address is the same as my legal address",
                        style = theme.fonts.bodySmall,
                        color = theme.colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = samePhysicalAddress,
                        onCheckedChange = { samePhysicalAddress = it }
                    )
                }
                if (!samePhysicalAddress) {
                    BillingAddressDetailView(viewModel = physicalAddress, headerTitle = "Physical address")
                }
            }
            fieldError?.let { message ->
                Text(
                    text = message,
                    style = theme.fonts.caption,
                    color = theme.colors.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            ContinueButton(isLoading = isLoading) {
                val message = when {
                    legalName.isBlank() -> "Enter the legal business name."
                    structure == null -> "Select a business type."
                    category == null -> "Select a business category."
                    else -> Validators.validateEIN(ein)
                        ?: Validators.validateWebsite(website)
                        ?: Validators.validateEmailAddress(email)?.let {
                            if (it == "Email is required") "Enter a business email." else it
                        }
                        ?: run {
                            legalAddress.normalize()
                            if (!legalAddress.validate()) {
                                legalAddress.errors.value.values.firstOrNull() ?: "Enter the legal address."
                            } else if (!samePhysicalAddress) {
                                physicalAddress.normalize()
                                if (!physicalAddress.validate()) {
                                    physicalAddress.errors.value.values.firstOrNull()
                                        ?: "Enter the physical address."
                                } else null
                            } else null
                        }
                }
                if (message != null) {
                    fieldError = message
                    viewModel.reportUserError(message)
                } else {
                    fieldError = null
                    viewModel.submitBusinessDetails(
                        legalName = legalName.trim(),
                        businessType = structure!!.apiValue,
                        mcc = category!!.code,
                        ein = ein.filter { it.isDigit() },
                        email = email.trim(),
                        website = Validators.normalizeWebsite(website),
                        legalAddress = legalAddress.address.value,
                        samePhysicalAddress = samePhysicalAddress,
                        physicalAddress = physicalAddress.address.value
                    )
                }
            }
        }
    }

    if (showTypePicker) {
        OptionSheet(
            title = "Business type",
            options = BusinessStructure.entries.map { it.label },
            onSelect = { label ->
                structure = BusinessStructure.entries.first { it.label == label }
                showTypePicker = false
            },
            onDismiss = { showTypePicker = false }
        )
    }
    if (showCategoryPicker) {
        CategorySheet(
            onSelect = {
                category = it
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }
}

private fun newAddressVM() = BillingAddressFieldVM(
    initial = FrameObjects.BillingAddress(
        city = null,
        country = "US",
        state = null,
        postalCode = "",
        addressLine1 = null,
        addressLine2 = null
    ),
    mode = BillingAddressMode.INTERNATIONAL
)

@Composable
private fun PickerRow(title: String, value: String?, onClick: () -> Unit) {
    val theme = LocalFrameTheme.current
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(text = title, style = theme.fonts.label, color = theme.colors.textSecondary)
        TextButton(onClick = onClick) {
            Text(
                text = value ?: "Select",
                style = theme.fonts.body,
                color = theme.colors.textPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionSheet(
    title: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = title,
            style = LocalFrameTheme.current.fonts.heading,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            options.forEach { option ->
                TextButton(onClick = { onSelect(option) }, modifier = Modifier.fillMaxWidth()) {
                    Text(option, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySheet(
    onSelect: (BusinessCategory) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = BusinessCategories.all.filter { category ->
        query.isBlank() ||
            category.name.contains(query, ignoreCase = true) ||
            category.code.contains(query)
    }
    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Text(text = "Business category", style = LocalFrameTheme.current.fonts.heading)
            Spacer(Modifier.height(8.dp))
            ValidatedTextField(
                value = query,
                onValueChange = { query = it },
                prompt = "Search categories",
                error = null,
                inlineError = true
            )
            Column(modifier = Modifier.height(360.dp).verticalScroll(rememberScrollState())) {
                filtered.forEach { category ->
                    TextButton(onClick = { onSelect(category) }, modifier = Modifier.fillMaxWidth()) {
                        Text(category.name, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
