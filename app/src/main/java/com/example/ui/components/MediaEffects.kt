package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix

enum class CreateMode(val label: String, val badgeText: String) {
    STORY("Story", "24h Ephemeral Story"),
    POST("Feed Post", "Photo / Carousel Feed"),
    REEL("Reel", "Full-Screen Vertical Reel"),
    VIDEO("Video", "HD Wide / Creator Video")
}

data class FilterPreset(
    val name: String,
    val accentColor: Color,
    val matrixValues: FloatArray
)

val InstalkFilterPresets = listOf(
    FilterPreset(
        name = "Original",
        accentColor = Color(0xFF38BDF8),
        matrixValues = floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    FilterPreset(
        name = "Liquid Cyan",
        accentColor = Color(0xFF00E5FF),
        matrixValues = floatArrayOf(
            0.85f, 0f, 0.1f, 0f, -5f,
            0f, 1.08f, 0.1f, 0f, 8f,
            0.05f, 0.15f, 1.25f, 0f, 18f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    FilterPreset(
        name = "Neon Nights",
        accentColor = Color(0xFF8B5CF6),
        matrixValues = floatArrayOf(
            1.15f, 0f, 0.15f, 0f, 10f,
            0f, 0.9f, 0.1f, 0f, -8f,
            0.15f, 0f, 1.3f, 0f, 22f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    FilterPreset(
        name = "Golden Hour",
        accentColor = Color(0xFFF59E0B),
        matrixValues = floatArrayOf(
            1.2f, 0.08f, 0f, 0f, 15f,
            0.05f, 1.05f, 0f, 0f, 8f,
            0f, 0f, 0.82f, 0f, -12f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    FilterPreset(
        name = "Vintage Film",
        accentColor = Color(0xFFEC4899),
        matrixValues = floatArrayOf(
            0.95f, 0.18f, 0.08f, 0f, 12f,
            0.08f, 0.92f, 0.08f, 0f, 8f,
            0.05f, 0.10f, 0.78f, 0f, 4f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    FilterPreset(
        name = "Cyber Noir",
        accentColor = Color(0xFF94A3B8),
        matrixValues = floatArrayOf(
            0.33f, 0.59f, 0.11f, 0f, 5f,
            0.33f, 0.59f, 0.11f, 0f, 5f,
            0.35f, 0.62f, 0.16f, 0f, 12f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    FilterPreset(
        name = "Vivid Pop",
        accentColor = Color(0xFF10B981),
        matrixValues = floatArrayOf(
            1.28f, -0.08f, -0.08f, 0f, 4f,
            -0.08f, 1.28f, -0.08f, 0f, 4f,
            -0.08f, -0.08f, 1.28f, 0f, 4f,
            0f, 0f, 0f, 1f, 0f
        )
    )
)

fun buildMediaColorFilter(
    filterName: String,
    brightness: Float = 0f, // -50f..50f
    contrast: Float = 1f    // 0.6f..1.5f
): ColorFilter? {
    if (filterName == "Original" && brightness == 0f && contrast == 1f) {
        return null
    }
    val preset = InstalkFilterPresets.find { it.name == filterName } ?: InstalkFilterPresets.first()
    val base = preset.matrixValues.copyOf()

    val offset = brightness + (1f - contrast) * 128f
    base[0] *= contrast
    base[6] *= contrast
    base[12] *= contrast
    base[4] += offset
    base[9] += offset
    base[14] += offset

    return ColorFilter.colorMatrix(ColorMatrix(base))
}
