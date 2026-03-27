package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    embedInParentScaffold: Boolean = false,
    onBack: () -> Unit,
    onResendCode: () -> Unit = {},
    onContinue: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    val focusRequesters = remember(digitCount) { List(digitCount) { FocusRequester() } }
    val canContinue = code.length == digitCount
    val digitTextStyle = MaterialTheme.typography.headlineSmall.copy(
        textAlign = TextAlign.Center,
        lineHeight = 40.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )

    @Composable
    fun VerifyCardBody(scaffoldContentPadding: PaddingValues) {
        LaunchedEffect(Unit) {
            focusRequesters[0].requestFocus()
        }

        Column(
            modifier = Modifier
                .padding(scaffoldContentPadding)
                .padding(24.dp)
                .fillMaxSize()
                .imePadding(),
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
                            onValueChange = { newValue ->
                                if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                    val newCode = code.toMutableList()
                                    if (newValue.isEmpty()) {
                                        if (index < newCode.size) {
                                            newCode.removeAt(index)
                                        }
                                        code = newCode.joinToString("")
                                        if (index > 0) {
                                            focusRequesters[index - 1].requestFocus()
                                        }
                                    } else {
                                        if (index < newCode.size) {
                                            newCode[index] = newValue[0]
                                        } else {
                                            newCode.add(newValue[0])
                                        }
                                        code = newCode.joinToString("")
                                        if (index < digitCount - 1) {
                                            focusRequesters[index + 1].requestFocus()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 72.dp)
                                .focusRequester(focusRequesters[index]),
                            textStyle = digitTextStyle,
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

    if (embedInParentScaffold) {
        VerifyCardBody(PaddingValues(0.dp))
    } else {
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
            VerifyCardBody(padding)
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
