package com.framepayments.frameonboarding.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.framepayments.frameonboarding.theme.FramePrimaryColor

@Composable
internal fun TermsOfServiceView(
    privacyPolicyUrl: String = "https://framepayments.com/privacy",
    termsOfServiceUrl: String = "https://framepayments.com/terms",
    textColor: Color = Color.Gray,
    linkColor: Color = FramePrimaryColor,
    textAlign: TextAlign = TextAlign.Center,
    padded: Boolean = false
) {
    val uriHandler = LocalUriHandler.current

    val annotatedText = buildAnnotatedString {
        withStyle(SpanStyle(color = textColor, fontSize = 13.sp)) {
            append("By clicking continue, you agree to the terms of Frame's ")
        }
        pushStringAnnotation(tag = "URL", annotation = privacyPolicyUrl)
        withStyle(SpanStyle(color = linkColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)) {
            append("Privacy Policy")
        }
        pop()
        withStyle(SpanStyle(color = textColor, fontSize = 13.sp)) {
            append(" and ")
        }
        pushStringAnnotation(tag = "URL", annotation = termsOfServiceUrl)
        withStyle(SpanStyle(color = linkColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)) {
            append("Terms of Service")
        }
        pop()
        withStyle(SpanStyle(color = textColor, fontSize = 13.sp)) {
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
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    } else {
        baseModifier
    }

    ClickableText(
        text = annotatedText,
        style = TextStyle(textAlign = textAlign),
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
