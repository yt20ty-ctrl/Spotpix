package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.NotificationEntity
import com.example.ui.theme.LocalLiquidGlass

@Composable
fun NotificationsScreen(
    notifications: List<NotificationEntity>,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    onToggleFollowActor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlass.current
    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("notifications_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = glass.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Activity & Requests",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = glass.textPrimary
                    )
                    Text(
                        text = "Tap any @username to inspect their profile",
                        fontSize = 11.sp,
                        color = glass.accentPrimary
                    )
                }
            }
        }

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = glass.accentPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No notifications yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = glass.textPrimary
                    )
                    Text(
                        text = "When people like, comment, or follow you, you'll see them here.",
                        fontSize = 13.sp,
                        color = glass.textSecondary,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notifications, key = { it.id }) { notif ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .background(glass.glassSurfaceElevated, RoundedCornerShape(16.dp))
                            .border(1.dp, glass.glassBorder, RoundedCornerShape(16.dp))
                            .clickable { onUserClick(notif.actorId) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Actor Avatar
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(1.dp, glass.accentPrimary, CircleShape)
                        ) {
                            AsyncImage(
                                model = notif.actorAvatarUrl,
                                contentDescription = notif.actorUsername,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Notification text
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "@${notif.actorUsername} ${notif.content}",
                                fontSize = 13.sp,
                                color = glass.textPrimary,
                                lineHeight = 17.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Recent • Tap to view profile",
                                fontSize = 10.sp,
                                color = glass.textTertiary
                            )
                        }

                        // Follow button or Target Media Thumbnail
                        if (notif.type == "FOLLOW") {
                            Button(
                                onClick = { onToggleFollowActor(notif.actorId) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (notif.isFollowingActor) glass.glassSurface else glass.accentPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = if (notif.isFollowingActor) "Following" else "Follow Back",
                                    fontSize = 11.sp,
                                    color = if (notif.isFollowingActor) glass.textPrimary else Color.White
                                )
                            }
                        } else if (!notif.targetMediaUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = notif.targetMediaUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
