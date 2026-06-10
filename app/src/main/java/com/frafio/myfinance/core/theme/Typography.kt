package com.frafio.myfinance.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import com.frafio.myfinance.R

val GoogleSansFlexFamily = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.grade(0),
            FontVariation.slant(0f),
            FontVariation.width(100f),
            FontVariation.Setting(name = "ROND", value = 0f)
        )
    )
)

val GoogleSansFlexEmphasizedFamily = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(900),
            FontVariation.grade(100),
            FontVariation.slant(-10f),
            FontVariation.width(50f),
            FontVariation.Setting(name = "ROND", value = 0f)
        )
    )
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = GoogleSansFlexFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = GoogleSansFlexFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = GoogleSansFlexFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = GoogleSansFlexFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = GoogleSansFlexFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = GoogleSansFlexFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = GoogleSansFlexFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = GoogleSansFlexFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = GoogleSansFlexFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = GoogleSansFlexFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = GoogleSansFlexFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = GoogleSansFlexFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = GoogleSansFlexFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = GoogleSansFlexFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = GoogleSansFlexFamily),
    displayLargeEmphasized = baseline.displayLarge.copy(fontFamily = GoogleSansFlexEmphasizedFamily),
    displayMediumEmphasized = baseline.displayMedium.copy(fontFamily = GoogleSansFlexEmphasizedFamily),
    displaySmallEmphasized = baseline.displaySmall.copy(fontFamily = GoogleSansFlexEmphasizedFamily),
    headlineLargeEmphasized = baseline.headlineLarge.copy(fontFamily = GoogleSansFlexEmphasizedFamily),
    headlineMediumEmphasized = baseline.headlineMedium.copy(fontFamily = GoogleSansFlexEmphasizedFamily),
    headlineSmallEmphasized = baseline.headlineSmall.copy(fontFamily = GoogleSansFlexEmphasizedFamily),
    titleLargeEmphasized = baseline.titleLarge.copy(fontFamily = GoogleSansFlexEmphasizedFamily),
    titleMediumEmphasized = baseline.titleMedium.copy(fontFamily = GoogleSansFlexEmphasizedFamily),
    titleSmallEmphasized = baseline.titleSmall.copy(fontFamily = GoogleSansFlexEmphasizedFamily),
    bodyLargeEmphasized = baseline.bodyLarge.copy(fontFamily = GoogleSansFlexEmphasizedFamily),
    bodyMediumEmphasized = baseline.bodyMedium.copy(fontFamily = GoogleSansFlexEmphasizedFamily),
    bodySmallEmphasized = baseline.bodySmall.copy(fontFamily = GoogleSansFlexEmphasizedFamily),
    labelLargeEmphasized = baseline.labelLarge.copy(fontFamily = GoogleSansFlexEmphasizedFamily),
    labelMediumEmphasized = baseline.labelMedium.copy(fontFamily = GoogleSansFlexEmphasizedFamily),
    labelSmallEmphasized = baseline.labelSmall.copy(fontFamily = GoogleSansFlexEmphasizedFamily)
)
