package com.framepayments.frameonboarding.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import com.framepayments.frameonboarding.classes.IdType
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VerifyIdFormScreen(
    onBack: () -> Unit,
    onContinue: (issuingCountry: String, idType: IdType) -> Unit
) {
    var selectedCountry by remember { mutableStateOf<String?>(null) }
    var selectedIdType by remember { mutableStateOf<IdType?>(null) }

    val canContinue = selectedCountry != null && selectedIdType != null

    val countryOptions = listOf(
        "United States",
        "Canada",
        "United Kingdom",
        "Australia",
        "Mexico"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Your ID") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Select the country that issued your government ID and the type of ID you’ll use to verify your identity.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(20.dp))

                DropdownField(
                    label = "Issuing Country",
                    value = selectedCountry ?: "Selection",
                    options = countryOptions,
                    onSelected = { selectedCountry = it }
                )

                Spacer(Modifier.height(16.dp))

                DropdownField(
                    label = "ID Type",
                    value = selectedIdType?.displayName ?: "Selection",
                    options = IdType.entries.map { it.displayName },
                    onSelected = { picked ->
                        selectedIdType = IdType.entries.first { it.displayName == picked }
                    }
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canContinue,
                onClick = { onContinue(selectedCountry!!, selectedIdType!!) },
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

@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))

        Box {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Text("▾")
                    }
                }
            )

            // IMPORTANT: DropdownMenu must be inside the same Box to anchor properly
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            onSelected(item)
                            expanded = false
                        }
                    )
                }
            }

            // Make the whole field tap-open (overlay clickable)
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .padding(end = 48.dp) // leave room for trailing icon
                    .clickable { expanded = true }
            )
        }
    }
}