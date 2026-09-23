package com.framepayments.framesdk_ui.reusable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import com.framepayments.framesdk.AddressSubregions

/**
 * Reusable modal subregion picker. Mirrors iOS `SubregionPickerSheet` so any onboarding screen
 * can present a themed list of selectable states / provinces / territories for [countryCode]
 * without re-implementing the bottom-sheet plumbing.
 *
 * Caller controls visibility — render this inside an `if (show) { ... }` block and dismiss
 * by handling [onDismiss] (sheet swipe / scrim tap).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubregionPickerSheet(
    countryCode: String,
    onSubregionSelected: (code: String) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Select State"
) {
    val theme = LocalFrameTheme.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val subregions = remember(countryCode) {
        AddressSubregions.subregions(forCountry = countryCode) ?: emptyList()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = theme.fonts.headline,
                color = theme.colors.textPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(subregions, key = { it.code }) { subregion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSubregionSelected(subregion.code)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subregion.name,
                            style = theme.fonts.body,
                            color = theme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = subregion.code,
                            style = theme.fonts.bodySmall,
                            color = theme.colors.textSecondary
                        )
                    }
                    HorizontalDivider(color = theme.colors.surfaceStroke)
                }
            }
        }
    }
}
