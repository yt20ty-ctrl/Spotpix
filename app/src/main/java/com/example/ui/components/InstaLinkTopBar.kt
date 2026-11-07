package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalLiquidGlass

@Composable
fun InstaLinkTopBar(
    onOpenMessages: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenUserSwitcher: () -> Unit,
    onDoubleTapSwitchAccount: () -> Unit,
    unreadMessages: Int,
    unreadNotifications: Int,
    currentUsername: String?,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlass.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left "+" Create icon + Spotpix by VETA Emblem & Wordmark (Matching Screenshot 5)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onOpenUserSwitcher() },
                        onDoubleTap = { onDoubleTapSwitchAccount() }
                    )
                }
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .testTag("app_logo_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create",
                tint = glass.textPrimary,
                modifier = Modifier
                    .size(26.dp)
                    .padding(end = 6.dp)
            )

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .shadow(6.dp, RoundedCornerShape(11.dp))
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF0066FF), Color(0xFF00E5FF), Color(0xFF6366F1))
                        )
                    )
                    .border(1.dp, Color(0x88FFFFFF), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier
                        .size(11.dp)
                        .align(Alignment.BottomEnd)
                        .padding(1.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Spotpix",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-0.5).sp,
                        color = glass.textPrimary
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "by VETA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0066FF)
                    )
                }
                if (!currentUsername.isNullOrBlank()) {
                    Text(
                        text = "@$currentUsername • Double-tap to switch",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = glass.accentPrimary
                    )
                }
            }
        }

        // Action Buttons: Heart Notifications & Direct Messages (Matching Screenshot 5)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Heart Notifications Icon with Glass Circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .background(glass.glassSurfaceElevated, shape = CircleShape)
                    .border(width = 1.dp, brush = glass.glassBorder, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable { onOpenNotifications() }
                    .testTag("topbar_notifications"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (unreadNotifications > 0) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Notifications",
                    tint = if (unreadNotifications > 0) Color(0xFFFF2D55) else glass.textPrimary,
                    modifier = Modifier.size(21.dp)
                )
                if (unreadNotifications > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF2D55))
                    )
                }
            }

            // Direct Messages Icon with Glass Circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .background(glass.glassSurfaceElevated, shape = CircleShape)
                    .border(width = 1.dp, brush = glass.glassBorder, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable { onOpenMessages() }
                    .testTag("topbar_messages"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Send,
                    contentDescription = "Direct Messages",
                    tint = glass.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
                if (unreadMessages > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 4.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(glass.accentPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadMessages.toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
