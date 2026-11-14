package cn.gdeiassistant.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** OpenType feature that keeps digits equal-width in grades, balances, prices, and times. */
const val TabularNumbers = "tnum"

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    fontFamily: FontFamily = FontFamily.Default,
    letterSpacing: Float = 0f,
) = TextStyle(
    fontFamily = fontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    fontFeatureSettings = TabularNumbers,
)

/** Returns a copy of this [TextStyle] enforcing equal-width tabular numerals (`tnum`). */
fun TextStyle.withTabularNumbers(): TextStyle = copy(fontFeatureSettings = TabularNumbers)

/** Pre-configured tabular numeral modifier for standalone numeric labels (balances, prices, GPAs). */
val TabularNumberTextStyle = TextStyle(fontFeatureSettings = TabularNumbers)

/** Lingnan editorial type scale: Serif display & headlines paired with clean Sans UI copy and tabular numerals. */
val AppTypography = Typography(
    displayLarge = style(44, 52, FontWeight.SemiBold, FontFamily.Serif, -0.4f),
    displayMedium = style(36, 44, FontWeight.SemiBold, FontFamily.Serif, -0.3f),
    displaySmall = style(32, 40, FontWeight.SemiBold, FontFamily.Serif, -0.2f),
    headlineLarge = style(28, 36, FontWeight.SemiBold, FontFamily.Serif, -0.2f),
    headlineMedium = style(24, 32, FontWeight.SemiBold, FontFamily.Serif, -0.15f),
    headlineSmall = style(21, 28, FontWeight.SemiBold),
    titleLarge = style(19, 26, FontWeight.SemiBold),
    titleMedium = style(16, 23, FontWeight.SemiBold),
    titleSmall = style(14, 20, FontWeight.SemiBold),
    bodyLarge = style(16, 24, FontWeight.Normal),
    bodyMedium = style(14, 20, FontWeight.Normal),
    bodySmall = style(12, 17, FontWeight.Normal),
    labelLarge = style(14, 20, FontWeight.Medium),
    labelMedium = style(12, 16, FontWeight.Medium),
    labelSmall = style(11, 15, FontWeight.Medium, letterSpacing = 0.25f),
)
