package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalLiquidGlass

fun Modifier.liquidGlassSurface(
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 8.dp,
    isElevated: Boolean = false,
    customBorderBrush: Brush? = null
): Modifier = this.then(
    Modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = Color(0x33000000),
            spotColor = Color(0x4038BDF8)
        )
)

fun Modifier.liquidGlassPill(
    borderWidth: Dp = 1.dp,
    elevation: Dp = 12.dp
): Modifier = this.then(
    Modifier.shadow(
        elevation = elevation,
        shape = CircleShape,
        clip = false,
        ambientColor = Color(0x40000000),
        spotColor = Color(0x6600E5FF)
    )
)
