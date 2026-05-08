package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import com.framepayments.framesdk_ui.theme.FrameTheme
import com.framepayments.framesdk_ui.theme.FrameThemePreviews

@Composable
internal fun VerificationSubmittedScreen(
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = LocalFrameTheme.current.colors.primaryButton
            )
            Text(
                text = "Verification Submitted",
                style = LocalFrameTheme.current.fonts.headline,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Congratulations! You've submitted your identity verification check. You're ready to proceed.",
                style = LocalFrameTheme.current.fonts.bodySmall,
                textAlign = TextAlign.Center,
                color = LocalFrameTheme.current.colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LocalFrameTheme.current.colors.primaryButton,
                contentColor = LocalFrameTheme.current.colors.primaryButtonText
            )
        ) {
            Text("Done")
        }
    }
}

@FrameThemePreviews
@Composable
private fun VerificationSubmittedScreenPreview() {
    FrameTheme {
    VerificationSubmittedScreen(onDone = {})
    }
}
