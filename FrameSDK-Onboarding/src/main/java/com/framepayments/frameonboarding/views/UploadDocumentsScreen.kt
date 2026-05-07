package com.framepayments.frameonboarding.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.reusable.ContinueButton
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UploadDocumentsScreen(
    frontPhotoComplete: Boolean = false,
    backPhotoComplete: Boolean = false,
    selfieComplete: Boolean = false,
    frontError: String? = null,
    backError: String? = null,
    selfieError: String? = null,
    isSubmitting: Boolean = false,
    onBack: () -> Unit,
    onFrontPhotoClick: () -> Unit,
    onBackPhotoClick: () -> Unit,
    onSelfieClick: () -> Unit,
    onSubmit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Your Identity") },
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
                    text = "Take photos of your government-issued ID and a selfie.",
                    style = LocalFrameTheme.current.fonts.bodySmall
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Driver's License",
                    style = LocalFrameTheme.current.fonts.label,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                DocumentItem(
                    title = "Front Photo",
                    isComplete = frontPhotoComplete,
                    onClick = onFrontPhotoClick
                )
                DocumentError(frontError)

                Spacer(Modifier.height(12.dp))

                DocumentItem(
                    title = "Back Photo",
                    isComplete = backPhotoComplete,
                    onClick = onBackPhotoClick
                )
                DocumentError(backError)

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Identity Verification",
                    style = LocalFrameTheme.current.fonts.label,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                DocumentItem(
                    title = "Selfie",
                    isComplete = selfieComplete,
                    onClick = onSelfieClick
                )
                DocumentError(selfieError)
            }

            ContinueButton(
                text = "Submit Photos",
                isLoading = isSubmitting,
                onClick = onSubmit
            )
        }
    }
}

@Composable
private fun DocumentError(message: String?) {
    if (message != null) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = message,
            style = LocalFrameTheme.current.fonts.caption,
            color = LocalFrameTheme.current.colors.error,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
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
        shape = RoundedCornerShape(LocalFrameTheme.current.radii.medium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = if (isComplete) LocalFrameTheme.current.colors.primaryButton else LocalFrameTheme.current.colors.surface,
                shape = RoundedCornerShape(LocalFrameTheme.current.radii.small)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isComplete) LocalFrameTheme.current.colors.primaryButtonText else LocalFrameTheme.current.colors.textSecondary
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = LocalFrameTheme.current.fonts.body,
                color = if (isComplete) LocalFrameTheme.current.colors.textPrimary else LocalFrameTheme.current.colors.textPrimary.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UploadDocumentsScreenPreview() {
    UploadDocumentsScreen(
        frontPhotoComplete = true,
        backPhotoComplete = false,
        selfieComplete = false,
        onBack = {},
        onFrontPhotoClick = {},
        onBackPhotoClick = {},
        onSelfieClick = {},
        onSubmit = {}
    )
}
