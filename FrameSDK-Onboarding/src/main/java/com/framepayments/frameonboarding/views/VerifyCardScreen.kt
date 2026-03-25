package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VerifyCardScreen(
    headerTitle: String = "Verify Your Card",
    bodyText: String = "We've sent a security code to your bank registered phone number ending in *3432.",
    confirmButtonText: String = "Continue",
    digitCount: Int = 6,
    showResendCode: Boolean = false,
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = bodyText,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(32.dp))

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

                if (showResendCode) {
                    TextButton(onClick = onResendCode) {
                        Text("Resend Code")
                    }
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

@Preview(showBackground = true)
@Composable
private fun VerifyCardScreenPreview() {
    VerifyCardScreen(
        onBack = {},
        onContinue = {}
    )
}
