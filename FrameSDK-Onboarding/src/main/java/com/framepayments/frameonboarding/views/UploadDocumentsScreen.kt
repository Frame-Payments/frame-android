package com.framepayments.frameonboarding.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.classes.PhotoType
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UploadDocumentsScreen(
    frontPhotoComplete: Boolean = false,
    backPhotoComplete: Boolean = false,
    selfieComplete: Boolean = false,
    onBack: () -> Unit,
    onFrontPhotoClick: () -> Unit,
    onBackPhotoClick: () -> Unit,
    onSelfieClick: () -> Unit,
    onSubmit: () -> Unit
) {
    val allComplete = frontPhotoComplete && backPhotoComplete && selfieComplete

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Your Identity") },
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
                    text = "Take photos of your government-issued ID and a selfie.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Driver's License",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                DocumentItem(
                    title = "Front Photo",
                    isComplete = frontPhotoComplete,
                    onClick = onFrontPhotoClick
                )

                Spacer(Modifier.height(12.dp))

                DocumentItem(
                    title = "Back Photo",
                    isComplete = backPhotoComplete,
                    onClick = onBackPhotoClick
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Identity Verification",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                DocumentItem(
                    title = "Selfie",
                    isComplete = selfieComplete,
                    onClick = onSelfieClick
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = allComplete,
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FramePrimaryColor,
                    contentColor = FrameOnPrimaryColor,
                    disabledContainerColor = FramePrimaryColor.copy(alpha = 0.35f),
                    disabledContentColor = FrameOnPrimaryColor.copy(alpha = 0.7f)
                )
            ) {
                Text("Submit Photos")
            }
        }
    }
}

@Composable
private fun DocumentItem(
    title: String,
    isComplete: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Person icon - using a placeholder, should be replaced with actual icon
            Surface(
                modifier = Modifier.size(40.dp),
                color = if (isComplete) FramePrimaryColor else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Placeholder - replace with actual person icon
                    Text(
                        text = "👤",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isComplete) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.weight(1f))
            Text(text = "›", style = MaterialTheme.typography.titleLarge)
        }
    }
}
