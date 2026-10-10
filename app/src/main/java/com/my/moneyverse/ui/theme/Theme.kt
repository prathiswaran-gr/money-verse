package com.my.moneyverse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GreenBlackScheme = darkColorScheme(
    primary = Green,
    onPrimary = Black,
    primaryContainer = GreenContainer,
    onPrimaryContainer = Green,
    secondary = GreenDark,
    onSecondary = Black,
    secondaryContainer = GreenMuted,
    onSecondaryContainer = OnSurface,
    tertiary = Lime,
    onTertiary = Black,
    background = Black,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = OnSurfaceMuted,
    surfaceContainerLowest = Black,
    surfaceContainerLow = Surface,
    surfaceContainer = SurfaceHigh,
    surfaceContainerHigh = SurfaceHigh,
    surfaceContainerHighest = SurfaceHighest,
    outline = Outline,
    outlineVariant = Outline,
    error = ExpenseRed,
    onError = Black,
)

@Composable
fun MoneyVerseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GreenBlackScheme,
        typography = Typography,
        content = content
    )
}