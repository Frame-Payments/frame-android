package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.classes.GeolocationState
import com.framepayments.frameonboarding.networking.geocompliance.GeocomplianceAPI
import com.framepayments.frameonboarding.networking.geocompliance.GeoComplianceBlockReason
import com.framepayments.frameonboarding.networking.geocompliance.GeoComplianceStatus
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor

@Composable
internal fun GeolocationVerificationScreen(
    accountId: String?,
    onContinue: () -> Unit,
    onDisableVpn: () -> Unit
) {
    var state by remember { mutableStateOf(GeolocationState.CHECKING) }

    LaunchedEffect(Unit) {
        val id = accountId
        if (id == null) {
            state = GeolocationState.VERIFIED
            return@LaunchedEffect
        }
        val (response, _) = GeocomplianceAPI.getAccountGeoComplianceStatus(id)
        state = when {
            response == null -> GeolocationState.VERIFIED
            response.status == GeoComplianceStatus.CLEAR -> GeolocationState.VERIFIED
            response.reason == GeoComplianceBlockReason.VPN_DETECTED -> GeolocationState.VPN_DETECTED
            else -> GeolocationState.VERIFIED
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (state) {
                GeolocationState.CHECKING -> {
                    CheckingLocationView()
                }
                GeolocationState.VERIFIED -> {
                    LocationVerifiedView()
                }
                GeolocationState.VPN_DETECTED -> {
                    VpnDetectedView(
                        onContinue = {
                            onContinue()
                        },
                        onDisableVpn = onDisableVpn
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckingLocationView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Gear icon placeholder - should be replaced with actual icon
        Surface(
            modifier = Modifier.size(96.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("⚙️", style = MaterialTheme.typography.displayMedium)
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Checking your location",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "This will only take a moment...",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LocationVerifiedView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Gear icon placeholder
        Surface(
            modifier = Modifier.size(96.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("⚙️", style = MaterialTheme.typography.displayMedium)
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Location verified",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "This will only take a moment...",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun VpnDetectedView(
    onContinue: () -> Unit,
    onDisableVpn: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Question mark icon placeholder
        Surface(
            modifier = Modifier.size(96.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("?", style = MaterialTheme.typography.displayMedium)
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "VPN Detected",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "This will only take a moment...",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(
                containerColor = FramePrimaryColor,
                contentColor = FrameOnPrimaryColor
            )
        ) {
            Text("Continue Anyway")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDisableVpn
        ) {
            Text("Disable VPN")
        }
    }
}
