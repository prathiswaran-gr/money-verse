package com.my.moneyverse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.my.moneyverse.R

@OptIn(ExperimentalTextApi::class)
private fun outfit(weight: FontWeight) = Font(
    R.font.outfit,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val Outfit = FontFamily(
    listOf(
        FontWeight.Normal,
        FontWeight.Medium,
        FontWeight.SemiBold,
        FontWeight.Bold,
        FontWeight.ExtraBold,
        FontWeight.Black,
    ).map(::outfit),
)

private val base = Typography()

private fun TextStyle.outfit(weight: FontWeight? = null, tracking: Float? = null) = copy(
    fontFamily = Outfit,
    fontWeight = weight ?: fontWeight,
    letterSpacing = tracking?.sp ?: letterSpacing,
)

val Typography = Typography(
    displayLarge = base.displayLarge.outfit(FontWeight.Black, -1.5f),
    displayMedium = base.displayMedium.outfit(FontWeight.Black, -1f),
    displaySmall = base.displaySmall.outfit(FontWeight.ExtraBold, -0.8f),
    headlineLarge = base.headlineLarge.outfit(FontWeight.ExtraBold, -0.6f),
    headlineMedium = base.headlineMedium.outfit(FontWeight.ExtraBold, -0.5f),
    headlineSmall = base.headlineSmall.outfit(FontWeight.Bold, -0.3f),
    titleLarge = base.titleLarge.outfit(FontWeight.Bold),
    titleMedium = base.titleMedium.outfit(FontWeight.SemiBold),
    titleSmall = base.titleSmall.outfit(FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.outfit(),
    bodyMedium = base.bodyMedium.outfit(),
    bodySmall = base.bodySmall.outfit(),
    labelLarge = base.labelLarge.outfit(FontWeight.SemiBold),
    labelMedium = base.labelMedium.outfit(FontWeight.SemiBold),
    labelSmall = base.labelSmall.outfit(FontWeight.Bold, 1.2f),
)