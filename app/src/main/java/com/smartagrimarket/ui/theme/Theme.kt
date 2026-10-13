package com.smartagrimarket.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ═════════════════════════════════════════════════════════════
// Fresh market palette — modern, clean, mint green
// ═════════════════════════════════════════════════════════════

// ── Backgrounds ──
val AgriCreamBg       = Color(0xFFF7F8FA)   // page background — cool light grey
val AgriCardBg        = Color(0xFFFFFFFF)   // cards and surfaces — pure white

// ── Primary green family ──
val AgriGreen         = Color(0xFF2EBD7A)   // primary action / brand
val AgriGreenDark     = Color(0xFF1F9E68)   // pressed / emphasis
val AgriMintLight     = Color(0xFFE6F6EE)   // promo banners, soft chips
val AgriMintSoft      = Color(0xFFF1FAF5)   // lightest mint

// ── Legacy aliases (retinted) — same names, new look ──
val AgriYellow        = Color(0xFF2EBD7A)   // was gold, now primary green
val AgriYellowDark    = Color(0xFF1F9E68)   // was darker gold, now darker green

// ── Text ──
val AgriTextPrimary   = Color(0xFF1A1D1F)   // headings — near black
val AgriTextSecondary = Color(0xFF8A9099)   // labels, supporting copy
val AgriTextTertiary  = Color(0xFFB8BEC7)   // hints, disabled

// ── Lines / borders ──
val AgriBorderLight   = Color(0xFFEEF0F3)

// ── Accents ──
val AgriRating        = Color(0xFFFFB800)   // star rating gold
val AgriRedAlert      = Color(0xFFF25C5C)   // errors, warnings
val AgriOrange        = Color(0xFFFF9F43)   // secondary warm accent

// ═════════════════════════════════════════════════════════════
// Material color scheme
// ═════════════════════════════════════════════════════════════

private val AgriMarketColorScheme = lightColorScheme(
    primary              = AgriGreen,
    onPrimary            = Color.White,
    primaryContainer     = AgriMintLight,
    onPrimaryContainer   = AgriGreenDark,
    secondary            = AgriGreenDark,
    onSecondary          = Color.White,
    background           = AgriCreamBg,
    onBackground         = AgriTextPrimary,
    surface              = AgriCardBg,
    onSurface            = AgriTextPrimary,
    surfaceVariant       = AgriMintSoft,
    onSurfaceVariant     = AgriTextSecondary,
    outline              = AgriBorderLight,
    error                = AgriRedAlert,
    onError              = Color.White
)

private val AgriMarketTypography = Typography(
    headlineLarge  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 28.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 24.sp),
    headlineSmall  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 22.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 20.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 15.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp),
    bodySmall      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 12.sp),
    labelLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
    labelSmall     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
)

@Composable
fun SmartAgriMarketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AgriMarketColorScheme,
        typography = AgriMarketTypography,
        content = content
    )
}