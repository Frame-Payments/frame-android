package com.framepayments.frameonboarding.views

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.framepayments.frameonboarding.theme.FrameOnPrimaryColor
import com.framepayments.frameonboarding.theme.FramePrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewPhotoScreen(
    photoUri: Uri,
    onBack: () -> Unit,
    onUsePhoto: () -> Unit,
    onRetake: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Photo") },
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
                    text = "Make sure the entire ID is in frame, the text is readable, and there's no glare or blur.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(24.dp))

                AsyncImage(
                    model = photoUri,
                    contentDescription = "Captured photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentScale = ContentScale.Fit
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onUsePhoto,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FramePrimaryColor,
                        contentColor = FrameOnPrimaryColor
                    )
                ) {
                    Text("Use Photo")
                }

                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRetake
                ) {
                    Text("Retake Photo")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewPhotoScreenPreview() {
    ReviewPhotoScreen(
        photoUri = Uri.EMPTY,
        onBack = {},
        onUsePhoto = {},
        onRetake = {}
    )
}
