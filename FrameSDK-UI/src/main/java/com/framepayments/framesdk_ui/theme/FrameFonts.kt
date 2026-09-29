package com.framepayments.framesdk_ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.framepayments.framesdk_ui.R
import androidx.compose.material3.Typography

/**
 * Typography token set for the Frame SDK UI.
 *
 * Defaults use bundled Soehne (Söhne) fonts for FrameOS parity. Pass a customized
 * instance to [FrameTheme] to override fonts globally.
 *
 * @property title Large display title (e.g. OTP digit style).
 * @property heading Screen and sheet titles.
 * @property headline Section headers within a form or checkout sheet.
 * @property body Primary field and body copy.
 * @property bodySmall Secondary body copy and subtitles.
 * @property label Field labels above inputs.
 * @property caption Captions, hints, and fine print.
 * @property button Primary and secondary button labels.
 */
@Immutable
data class FrameFonts(
    val title: TextStyle,
    val heading: TextStyle,
    val headline: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val button: TextStyle,
) {
    /** Factory helpers for default and fallback [FrameFonts] instances. */
    companion object {
        /** Bundled Soehne (Söhne) family used by [defaults] / [defaultsForViews]. */
        val Soehne: FontFamily = FontFamily(
            Font(R.font.soehne_buch, FontWeight.Normal),
            Font(R.font.soehne_kraftig, FontWeight.Medium),
            Font(R.font.soehne_dreiviertelfett, FontWeight.SemiBold),
            Font(R.font.soehne_fett, FontWeight.Bold),
        )

        /** Default Soehne typography for Compose hosts. */
        @Composable
        @ReadOnlyComposable
        fun defaults(): FrameFonts = soehneDefaults()

        /** Default Soehne typography for View-based hosts (no composition required). */
        fun defaultsForViews(): FrameFonts = soehneDefaults()

        private fun soehneDefaults(): FrameFonts = FrameFonts(
            title = TextStyle(
                fontFamily = Soehne,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            heading = TextStyle(
                fontFamily = Soehne,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            ),
            headline = TextStyle(
                fontFamily = Soehne,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            ),
            body = TextStyle(
                fontFamily = Soehne,
                fontWeight = FontWeight.Normal,
                fontSize = 17.sp
            ),
            bodySmall = TextStyle(
                fontFamily = Soehne,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            ),
            label = TextStyle(
                fontFamily = Soehne,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            ),
            caption = TextStyle(
                fontFamily = Soehne,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
            ),
            button = TextStyle(
                fontFamily = Soehne,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            ),
        )

        /** Material 3 fallback when Soehne resources are unavailable. */
        fun fromTypography(typography: Typography): FrameFonts = FrameFonts(
            title = typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            heading = typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            headline = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            body = typography.bodyLarge,
            bodySmall = typography.bodyMedium,
            label = typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            caption = typography.labelSmall,
            button = typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}
