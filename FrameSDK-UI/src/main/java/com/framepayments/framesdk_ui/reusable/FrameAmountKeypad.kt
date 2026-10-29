package com.framepayments.framesdk_ui.reusable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk_ui.theme.LocalFrameTheme

/**
 * Numeric keypad that edits an amount in the currency's smallest unit (cents).
 *
 * Digits append from the right (`1`, `2`, `3` → `123` cents / $1.23). Extra digits that would
 * exceed [maxAmount] are ignored. Deposit and payout share this control.
 *
 * @param cents Current amount in cents.
 * @param onCentsChange Called with the next amount after a digit or backspace.
 * @param modifier Layout modifier.
 * @param maxAmount Optional cap. Digits that would exceed it are ignored.
 */
@Composable
fun FrameAmountKeypad(
    cents: Int,
    onCentsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxAmount: Int? = null,
) {
    val keys = listOf(
        listOf(KeypadKey.Digit(1), KeypadKey.Digit(2), KeypadKey.Digit(3)),
        listOf(KeypadKey.Digit(4), KeypadKey.Digit(5), KeypadKey.Digit(6)),
        listOf(KeypadKey.Digit(7), KeypadKey.Digit(8), KeypadKey.Digit(9)),
        listOf(KeypadKey.Blank, KeypadKey.Digit(0), KeypadKey.Backspace),
    )
    Column(modifier = modifier.fillMaxWidth()) {
        keys.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    KeypadButton(
                        key = key,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onCentsChange(
                                when (key) {
                                    is KeypadKey.Digit -> appendAmountDigit(key.value, cents, maxAmount)
                                    KeypadKey.Backspace -> deleteAmountDigit(cents)
                                    KeypadKey.Blank -> cents
                                }
                            )
                        },
                    )
                }
            }
        }
    }
}

/**
 * Appends [digit] from the right. Returns [to] when the next value would exceed [maxAmount].
 *
 * @param digit A single digit `0…9`.
 * @param to Current amount in cents.
 * @param maxAmount Optional cap.
 * @return The next amount in cents.
 */
fun appendAmountDigit(digit: Int, to: Int, maxAmount: Int?): Int {
    if (digit !in 0..9) return to
    if (to > (Int.MAX_VALUE - digit) / 10) return to
    val next = to * 10 + digit
    if (maxAmount != null && next > maxAmount) return to
    return next
}

/**
 * Removes the last digit from [from].
 *
 * @param from Current amount in cents.
 * @return The previous amount in cents, or `0`.
 */
fun deleteAmountDigit(from: Int): Int = from / 10

@Composable
private fun KeypadButton(
    key: KeypadKey,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val theme = LocalFrameTheme.current
    val clickable = key != KeypadKey.Blank
    Box(
        modifier = modifier
            .heightIn(min = 56.dp)
            .then(
                if (clickable) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (key) {
            is KeypadKey.Digit -> Text(
                text = key.value.toString(),
                style = theme.fonts.heading,
                color = theme.colors.textPrimary,
            )
            KeypadKey.Backspace -> Text(
                text = "⌫",
                style = theme.fonts.heading,
                color = theme.colors.textPrimary,
            )
            KeypadKey.Blank -> Unit
        }
    }
}

private sealed class KeypadKey {
    data class Digit(val value: Int) : KeypadKey()
    data object Backspace : KeypadKey()
    data object Blank : KeypadKey()
}
