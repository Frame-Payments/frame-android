package com.framepayments.frameonboarding.reusable

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import java.util.TimeZone

/**
 * Presents the platform spinner-style [DatePickerDialog] (month / day / year wheels).
 * Material3's calendar picker has no wheel mode; this matches the iOS `.wheel` date picker.
 *
 * Selected millis are UTC midnight for the chosen Y/M/D so ISO DOB formatting is timezone-stable.
 */
@Composable
fun SpinnerDatePickerDialog(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit,
) {
    val context = LocalContext.current
    DisposableEffect(initialMillis) {
        val utc = TimeZone.getTimeZone("UTC")
        val calendar = Calendar.getInstance(utc).apply { timeInMillis = initialMillis }
        val dialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val selected = Calendar.getInstance(utc).apply {
                    clear()
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                onDateSelected(selected.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.maxDate = System.currentTimeMillis()
        val min = Calendar.getInstance(utc).apply { add(Calendar.YEAR, -120) }
        dialog.datePicker.minDate = min.timeInMillis
        // Prefer spinner wheels when the platform still exposes them.
        @Suppress("DEPRECATION")
        dialog.datePicker.calendarViewShown = false
        @Suppress("DEPRECATION")
        dialog.datePicker.spinnersShown = true
        dialog.setOnCancelListener { onDismiss() }
        dialog.setOnDismissListener { onDismiss() }
        dialog.show()
        onDispose {
            if (dialog.isShowing) dialog.dismiss()
        }
    }
}
