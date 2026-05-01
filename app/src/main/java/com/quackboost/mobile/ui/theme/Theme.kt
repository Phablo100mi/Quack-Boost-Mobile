package com.quackboost.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── QuackBoost Accent ───────────────────────────────────────────────────────
val Accent       = Color(0xFFA78BFA) // purple
val AccentDim    = Color(0xFF7C5FCC)
val AccentGlow   = Color(0x33A78BFA)

// ── Surface Layers ──────────────────────────────────────────────────────────
val BgBase       = Color(0xFF0A0A0F)
val BgSurface    = Color(0xFF111118)
val BgCard       = Color(0xFF181825)
val BgCardBorder = Color(0xFF2A2A3A)

// ── Text ────────────────────────────────────────────────────────────────────
val TextPrimary   = Color(0xFFEEEEFF)
val TextSecondary = Color(0xFF8888AA)
val TextMuted     = Color(0xFF44445A)

// ── Semantic Colors ─────────────────────────────────────────────────────────
val ColorGood    = Color(0xFF4ADE80) // green
val ColorWarn    = Color(0xFFFBBF24) // yellow
val ColorDanger  = Color(0xFFF87171) // red
val ColorInfo    = Color(0xFF60A5FA) // blue

private val DarkColors = darkColorScheme(
    primary          = Accent,
    onPrimary        = Color.White,
    primaryContainer = AccentGlow,
    secondary        = AccentDim,
    background       = BgBase,
    surface          = BgSurface,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    surfaceVariant   = BgCard,
    outline          = BgCardBorder,
)

@Composable
fun QuackBoostTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography  = Typography,
        content     = content
    )
}

val Typography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 24.sp,
        color      = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        color      = TextPrimary
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        color      = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 13.sp,
        color      = TextSecondary
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 11.sp,
        color      = TextMuted
    )
)
