package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VerifyCardScreen(
    headerTitle: String = "Verify Your Card",
    bodyText: String = "We've sent a security code to your bank registered phone number ending in *3432.",
    confirmButtonText: String = "Confirm",
    digitCount: Int = 6,
    onBack: () -> Unit,
    onResendCode: () -> Unit = {},
    onContinue: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    val canContinue = code.length == digitCount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(headerTitle) },
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = bodyText,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(32.dp))

                // N-digit code input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(digitCount) { index ->
                        OutlinedTextField(
                            value = code.getOrNull(index)?.toString() ?: "",
                            onValueChange = { value ->
                                if (value.length <= 1 && value.all { it.isDigit() }) {
                                    val newCode = code.toMutableList()
                                    if (value.isEmpty()) {
                                        if (index < newCode.size) {
                                            newCode.removeAt(index)
                                        }
                                    } else {
                                        if (index < newCode.size) {
                                            newCode[index] = value[0]
                                        } else {
                                            newCode.add(value[0])
                                        }
                                    }
                                    code = newCode.joinToString("")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            textStyle = MaterialTheme.typography.headlineSmall,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            maxLines = 1
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = onResendCode) {
                    Text("Resend Code")
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canContinue,
                onClick = { onContinue(code) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = FramePrimaryColor,
                    contentColor = FrameOnPrimaryColor,
                    disabledContainerColor = FramePrimaryColor.copy(alpha = 0.35f),
                    disabledContentColor = FrameOnPrimaryColor.copy(alpha = 0.7f)
                )
            ) {
                Text(confirmButtonText)
            }
        }
    }
}
