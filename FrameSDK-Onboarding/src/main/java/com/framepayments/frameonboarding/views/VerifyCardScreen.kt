package com.framepayments.frameonboarding.views

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.framepayments.framesdk.accountevents.AccountEventEmitter
import com.framepayments.framesdk.accountevents.AccountEventName
import com.framepayments.framesdk.accountevents.AccountEventScreen
import com.framepayments.framesdk_ui.reusable.ContinueButton
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import com.framepayments.framesdk_ui.theme.FrameTheme
import com.framepayments.framesdk_ui.theme.FrameThemePreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VerifyCardScreen(
    headerTitle: String = "Verify Your Card",
    bodyText: String = "We've sent a security code to your bank registered phone number ending in *3432.",
    bodyAnnotated: AnnotatedString? = null,
    codeExpirationHint: String? = null,
    confirmButtonText: String = "Continue",
    digitCount: Int = 6,
    showResendCode: Boolean = false,
    showChangePhoneNumber: Boolean = false,
    embedInParentScaffold: Boolean = false,
    /** True only on the Twilio phone-OTP path — the 3DS and Prove-OTP call sites do not emit. */
    emitsPhoneCodeEntry: Boolean = false,
    onBack: () -> Unit,
    onResendCode: () -> Unit = {},
    onChangePhoneNumber: () -> Unit = {},
    onContinue: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    if (emitsPhoneCodeEntry) {
        LaunchedEffect(Unit) {
            AccountEventEmitter.emit(
                AccountEventName.PHONE_CODE_ENTRY_STARTED,
                AccountEventScreen.PHONE_VERIFICATION
            )
        }
    }
    val theme = LocalFrameTheme.current
    val focusRequesters = remember(digitCount) { List(digitCount) { FocusRequester() } }
    val canContinue = code.length == digitCount
    val digitTextStyle = theme.fonts.heading.copy(
        textAlign = TextAlign.Center,
        lineHeight = 40.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = theme.colors.fieldFocusStroke,
        unfocusedBorderColor = theme.colors.surfaceStroke,
        focusedContainerColor = theme.colors.surface,
        unfocusedContainerColor = theme.colors.surface,
        cursorColor = theme.colors.textPrimary,
    )

    @Composable
    fun VerifyCardBody(scaffoldContentPadding: PaddingValues) {
        LaunchedEffect(Unit) {
            focusRequesters[0].requestFocus()
        }

        // Action stack under the OTP digits; Continue sits a bit farther below the links.
        val otpActionSpacing = 8.dp
        Column(
            modifier = Modifier
                .padding(scaffoldContentPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .imePadding()
        ) {
            if (bodyAnnotated != null) {
                Text(
                    text = bodyAnnotated,
                    style = theme.fonts.bodySmall.copy(color = theme.colors.textSecondary)
                )
            } else {
                Text(
                    text = bodyText,
                    style = theme.fonts.bodySmall,
                    color = theme.colors.textSecondary,
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                            .heightIn(min = 70.dp)
                            .focusRequester(focusRequesters[index]),
                        textStyle = digitTextStyle,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        maxLines = 1,
                        colors = fieldColors,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(otpActionSpacing)) {
                codeExpirationHint?.let { hint ->
                    Text(
                        text = hint,
                        style = theme.fonts.caption,
                        color = theme.colors.textSecondary
                    )
                }

                if (showResendCode) {
                    Text(
                        "Resend code",
                        style = theme.fonts.bodySmall,
                        color = theme.colors.textPrimary,
                        modifier = Modifier.clickable(onClick = onResendCode)
                    )
                }
                if (showChangePhoneNumber) {
                    Text(
                        "Change phone number",
                        style = theme.fonts.bodySmall,
                        color = theme.colors.textPrimary,
                        modifier = Modifier.clickable(onClick = onChangePhoneNumber)
                    )
                }

                ContinueButton(
                    text = confirmButtonText,
                    enabled = canContinue,
                    modifier = Modifier.padding(top = 4.dp),
                    onClick = { onContinue(code) },
                )
            }

            Spacer(Modifier.weight(1f))
        }
    }

    if (embedInParentScaffold) {
        VerifyCardBody(PaddingValues(0.dp))
    } else {
        Scaffold(
            containerColor = theme.colors.surface,
            topBar = {
                TopAppBar(
                    title = { Text(headerTitle, style = theme.fonts.heading) },
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

@FrameThemePreviews
@Composable
private fun VerifyCardScreenPreview() {
    FrameTheme {
        VerifyCardScreen(
            onBack = {},
            onContinue = {}
        )
    }
}
