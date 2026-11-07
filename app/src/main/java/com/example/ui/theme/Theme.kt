package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class LiquidGlassColors(
    val isDark: Boolean,
    val backgroundBrush: Brush,
    val glassSurface: Color,
    val glassSurfaceElevated: Color,
    val glassBorder: Brush,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val heartColor: Color = CoralHeart,
)

val LocalLiquidGlass = staticCompositionLocalOf {
    LiquidGlassColors(
        isDark = true,
        backgroundBrush = Brush.verticalGradient(listOf(DarkBgStart, DarkBgMid, DarkBgEnd)),
        glassSurface = DarkGlassSurface,
        glassSurfaceElevated = DarkGlassSurfaceElevated,
        glassBorder = Brush.verticalGradient(listOf(DarkGlassBorderTop, DarkGlassBorderBottom)),
        textPrimary = DarkTextPrimary,
        textSecondary = DarkTextSecondary,
        textTertiary = DarkTextTertiary,
        accentPrimary = SkyGlow,
        accentSecondary = PurpleNeon
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = SkyGlow,
    secondary = PurpleNeon,
    tertiary = CyanNeon,
    background = DarkBgStart,
    surface = DarkBgMid,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    secondary = PurpleNeon,
    tertiary = SkyGlow,
    background = LightBgStart,
    surface = LightBgMid,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary
)

@Composable
fun InstaLinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appBackgroundStyle: String = "Cobalt Liquid Glass",
    content: @Composable () -> Unit
) {
    val customBgBrush: Brush = when (appBackgroundStyle) {
        "OLED Pitch Black" -> Brush.verticalGradient(
            listOf(Color(0xFF000000), Color(0xFF020409), Color(0xFF000000))
        )
        "Cyber Aurora Cyan" -> Brush.verticalGradient(
            listOf(Color(0xFF021B29), Color(0xFF052E42), Color(0xFF040D1A))
        )
        "Royal Emerald Vault" -> Brush.verticalGradient(
            listOf(Color(0xFF022018), Color(0xFF063729), Color(0xFF040D14))
        )
        "Minimal Daylight Frost" -> Brush.verticalGradient(
            listOf(Color(0xFFF0F6FF), Color(0xFFE2EEFF), Color(0xFFF8FAFC))
        )
        else -> if (darkTheme) {
            Brush.verticalGradient(listOf(DarkBgStart, DarkBgMid, DarkBgEnd))
        } else {
            Brush.verticalGradient(listOf(LightBgStart, LightBgMid, LightBgEnd))
        }
    }

    val effectiveDark = when (appBackgroundStyle) {
        "Minimal Daylight Frost" -> false
        "OLED Pitch Black", "Cyber Aurora Cyan", "Royal Emerald Vault" -> true
        else -> darkTheme
    }

    val glassColors = if (effectiveDark) {
        LiquidGlassColors(
            isDark = true,
            backgroundBrush = customBgBrush,
            glassSurface = if (appBackgroundStyle == "OLED Pitch Black") Color(0xCC060911) else DarkGlassSurface,
            glassSurfaceElevated = if (appBackgroundStyle == "OLED Pitch Black") Color(0xE60B101E) else DarkGlassSurfaceElevated,
            glassBorder = Brush.verticalGradient(listOf(DarkGlassBorderTop, DarkGlassBorderBottom)),
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            textTertiary = DarkTextTertiary,
            accentPrimary = if (appBackgroundStyle == "Royal Emerald Vault") Color(0xFF10B981) else SkyGlow,
            accentSecondary = PurpleNeon
        )
    } else {
        LiquidGlassColors(
            isDark = false,
            backgroundBrush = customBgBrush,
            glassSurface = LightGlassSurface,
            glassSurfaceElevated = LightGlassSurfaceElevated,
            glassBorder = Brush.verticalGradient(listOf(LightGlassBorderTop, LightGlassBorderBottom)),
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            textTertiary = LightTextTertiary,
            accentPrimary = ElectricBlue,
            accentSecondary = PurpleNeon
        )
    }

    val materialColors = if (effectiveDark) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalLiquidGlass provides glassColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = Typography,
            content = content
        )
    }
}

