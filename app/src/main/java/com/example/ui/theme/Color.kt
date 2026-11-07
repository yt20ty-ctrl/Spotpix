package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Dark Liquid Glass Palette
val DarkBgStart = Color(0xFF050814)
val DarkBgMid = Color(0xFF0E1A38)
val DarkBgEnd = Color(0xFF070C1D)

val DarkGlassSurface = Color(0x381E294A)
val DarkGlassSurfaceElevated = Color(0x551E294A)
val DarkGlassBorderTop = Color(0x66FFFFFF)
val DarkGlassBorderBottom = Color(0x1F38BDF8)

val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextTertiary = Color(0xFF64748B)

// Light Liquid Glass Palette
val LightBgStart = Color(0xFFE9F2FC)
val LightBgMid = Color(0xFFF5F9FD)
val LightBgEnd = Color(0xFFDFEDFA)

val LightGlassSurface = Color(0xB8FFFFFF)
val LightGlassSurfaceElevated = Color(0xE0FFFFFF)
val LightGlassBorderTop = Color(0xE6FFFFFF)
val LightGlassBorderBottom = Color(0x4038BDF8)

val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextTertiary = Color(0xFF94A3B8)

// Shared Vibrant Accents
val CyanNeon = Color(0xFF00E5FF)
val ElectricBlue = Color(0xFF2563EB)
val SkyGlow = Color(0xFF38BDF8)
val PurpleNeon = Color(0xFF8B5CF6)
val PinkNeon = Color(0xFFEC4899)
val CoralHeart = Color(0xFFFF2D55)
val EmeraldOnline = Color(0xFF10B981)

val StoryGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF00E5FF),
        Color(0xFF3B82F6),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899)
    )
)

val StorySeenGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x4D94A3B8),
        Color(0x3394A3B8)
    )
)
