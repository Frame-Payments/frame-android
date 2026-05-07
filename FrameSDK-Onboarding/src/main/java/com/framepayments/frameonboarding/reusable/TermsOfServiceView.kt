package com.framepayments.frameonboarding.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.framepayments.framesdk_ui.theme.LocalFrameTheme

@Composable
fun TermsOfServiceView(
    privacyPolicyUrl: String = "https://framepayments.com/privacy",
    termsOfServiceUrl: String = "https://framepayments.com/terms",
    textColor: Color = LocalFrameTheme.current.colors.textSecondary,
    linkColor: Color = LocalFrameTheme.current.colors.primaryButton,
    textAlign: TextAlign = TextAlign.Center,
    padded: Boolean = false
) {
    val theme = LocalFrameTheme.current
    val uriHandler = LocalUriHandler.current
    // Derive sizing from the theme's caption style so an integrator overriding
    // FrameFonts.caption propagates through to the legal text below.
    val captionSize = theme.fonts.caption.fontSize
    val bodySpan = SpanStyle(color = textColor, fontSize = captionSize)
    val linkSpan = SpanStyle(color = linkColor, fontSize = captionSize, fontWeight = FontWeight.Bold)

    val annotatedText = buildAnnotatedString {
        withStyle(bodySpan) {
            append("By clicking continue, you agree to the terms of Frame's ")
        }
        pushStringAnnotation(tag = "URL", annotation = privacyPolicyUrl)
        withStyle(linkSpan) {
            append("Privacy Policy")
        }
        pop()
        withStyle(bodySpan) {
            append(" and ")
        }
        pushStringAnnotation(tag = "URL", annotation = termsOfServiceUrl)
        withStyle(linkSpan) {
            append("Terms of Service")
        }
        pop()
        withStyle(bodySpan) {
            append(".")
        }
    }

    val baseModifier = Modifier
        .fillMaxWidth()
        .semantics {
            contentDescription = "By clicking continue, you agree to the terms of Frame's Privacy Policy and Terms of Service."
        }

    val modifier = if (padded) {
        baseModifier
            .background(theme.colors.surface, RoundedCornerShape(theme.radii.medium))
            .border(1.dp, theme.colors.surfaceStroke, RoundedCornerShape(theme.radii.medium))
            .padding(16.dp)
    } else {
        baseModifier
    }

    ClickableText(
        text = annotatedText,
        style = theme.fonts.caption.copy(textAlign = textAlign),
        modifier = modifier,
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()
                ?.let { uriHandler.openUri(it.item) }
        }
    )
}

@Preview(showBackground = true, name = "Default")
@Composable
private fun TermsOfServiceViewPreview() {
    TermsOfServiceView()
}

@Preview(showBackground = true, name = "Padded")
@Composable
private fun TermsOfServiceViewPaddedPreview() {
    TermsOfServiceView(padded = true)
}
