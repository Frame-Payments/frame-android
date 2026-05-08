package com.framepayments.frameonboarding.reusable

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
import com.framepayments.framesdk_ui.viewmodels.AvailableCountries
import com.framepayments.frameonboarding.classes.PhoneCountrySelection

/**
 * Reusable modal country picker. Mirrors iOS `CountryPickerSheet` so any onboarding or
 * checkout screen can present a themed list of selectable countries without re-implementing
 * the bottom-sheet plumbing. OFAC-restricted countries are filtered by default; pass
 * `excludeRestricted = false` to include them.
 *
 * Caller controls visibility — render this inside an `if (show) { ... }` block and dismiss
 * by handling [onDismiss] (sheet swipe / scrim tap).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerSheet(
    onCountrySelected: (alpha2Code: String) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Select Country",
    excludeRestricted: Boolean = true
) {
    val theme = LocalFrameTheme.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val countries = remember(excludeRestricted) {
        if (excludeRestricted) {
            AvailableCountries.allCountries.filter {
                it.alpha2Code.uppercase() !in PhoneCountrySelection.OFAC_RESTRICTED
            }
        } else {
            AvailableCountries.allCountries.toList()
        }
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
                items(countries, key = { it.alpha2Code }) { country ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCountrySelected(country.alpha2Code)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = country.displayName,
                            style = theme.fonts.body,
                            color = theme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = country.alpha2Code,
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
