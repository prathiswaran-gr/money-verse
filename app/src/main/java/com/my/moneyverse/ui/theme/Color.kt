package com.my.moneyverse.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Design tokens from money-verse-website.vercel.app (:root CSS variables)
val Green = Color(0xFF00E676)
val GreenDark = Color(0xFF00C853)
val Lime = Color(0xFFB2FF59)
val Amber = Color(0xFFFFD740)
val Blue = Color(0xFF448AFF)
val Purple = Color(0xFFB388FF)
val ExpenseRed = Color(0xFFFF5252)

val Black = Color(0xFF050505)
val Surface = Color(0xFF0D0D0D)
val SurfaceHigh = Color(0xFF151515)
val SurfaceHighest = Color(0xFF1E1E1E)
val OnSurface = Color(0xFFF5F5F5)
val OnSurfaceMuted = Color(0xFF888888)
val OnSurfaceDim = Color(0xFF555555)
val Border = Color(0x0FFFFFFF)
val Outline = Color(0xFF222222)

val GreenTint = Green.copy(alpha = 0.06f)
val GreenContainer = Color(0xFF0B2416)
val GreenMuted = Color(0xFF123D25)

val GreenGradient = Brush.linearGradient(listOf(Green, Lime))
val PrimaryGradient = Brush.linearGradient(listOf(Green, GreenDark))
val SummaryGradient = Brush.linearGradient(listOf(Green.copy(alpha = 0.12f), Lime.copy(alpha = 0.06f)))