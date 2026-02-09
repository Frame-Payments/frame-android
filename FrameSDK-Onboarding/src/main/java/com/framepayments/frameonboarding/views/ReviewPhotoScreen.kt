package com.framepayments.frameonboarding.views

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
                    text = "Make sure the entire ID is in frame, the text is readable, and there's no glare or blur.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(24.dp))

                // Photo preview
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
