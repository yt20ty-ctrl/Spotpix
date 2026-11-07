package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ReelEntity
import com.example.data.model.UserEntity
import com.example.ui.components.buildMediaColorFilter
import com.example.ui.theme.LocalLiquidGlass
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReelsScreen(
    reels: List<ReelEntity>,
    allUsers: List<UserEntity>,
    onToggleLike: (ReelEntity) -> Unit,
    onDoubleTapLike: (ReelEntity) -> Unit,
    onToggleSave: (ReelEntity) -> Unit,
    onToggleFollow: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onOpenComments: (String, String) -> Unit,
    onShareReel: (ReelEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (reels.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No Reels available", color = Color.White)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { reels.size })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val reel = reels[page]
            val authorUser = allUsers.find { it.id == reel.authorId }
            ReelItem(
                reel = reel,
                authorUser = authorUser,
                onToggleLike = { onToggleLike(reel) },
                onDoubleTapLike = { onDoubleTapLike(reel) },
                onToggleSave = { onToggleSave(reel) },
                onToggleFollow = { onToggleFollow(reel.authorId) },
                onUserClick = { onUserClick(reel.authorId) },
                onOpenComments = { onOpenComments(reel.id, "Reel by @${reel.authorUsername}") },
                onShareReel = { onShareReel(reel) }
            )
        }
    }
}

@Composable
private fun ReelItem(
    reel: ReelEntity,
    authorUser: UserEntity?,
    onToggleLike: () -> Unit,
    onDoubleTapLike: () -> Unit,
    onToggleSave: () -> Unit,
    onToggleFollow: () -> Unit,
    onUserClick: () -> Unit,
    onOpenComments: () -> Unit,
    onShareReel: () -> Unit
) {
    val glass = LocalLiquidGlass.current
    var isPlaying by remember { mutableStateOf(true) }
    var showPlayPauseIndicator by remember { mutableStateOf(false) }
    var showHeartBurst by remember { mutableStateOf(false) }
    var watchSeconds by remember(reel.id) { mutableStateOf(42) }
    var isIslandExpanded by remember(reel.id) { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(reel.id, isPlaying) {
        while (isPlaying) {
            delay(1000)
            watchSeconds += 1
        }
    }

    val isFollowed = authorUser?.isFollowedByMe ?: reel.isFollowingAuthor
    val isRequested = authorUser?.isFollowRequested ?: false
    val isPrivateAuthor = authorUser?.isPrivate ?: false

    // Rotating vinyl disc animation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(reel.id) {
                detectTapGestures(
                    onTap = {
                        isPlaying = !isPlaying
                        showPlayPauseIndicator = true
                        coroutineScope.launch {
                            delay(700)
                            showPlayPauseIndicator = false
                        }
                    },
                    onDoubleTap = {
                        onDoubleTapLike()
                        showHeartBurst = true
                        coroutineScope.launch {
                            delay(900)
                            showHeartBurst = false
                        }
                    }
                )
            }
    ) {
        // Full screen media with filter
        AsyncImage(
            model = reel.videoUrl,
            contentDescription = reel.caption,
            colorFilter = remember(reel.filterName) { buildMediaColorFilter(reel.filterName) },
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Top Reels Header: "+   Reels   Friends (Avatars)" + Dynamic Island Watch Time & Live Monetization Pill
        var selectedReelsFeedMode by remember { mutableStateOf("Reels") } // "Reels" or "Friends"
        var repostCount by remember(reel.id) { mutableStateOf((reel.shareCount * 3 + 1200).coerceAtLeast(1400)) }
        var isReposted by remember(reel.id) { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Overlay Row matching Screenshot 4: "+" on left, "Reels | Friends" + Overlapping Avatars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Reel",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onShareReel() }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "Reels",
                        color = if (selectedReelsFeedMode == "Reels") Color.White else Color.White.copy(alpha = 0.65f),
                        fontSize = 20.sp,
                        fontWeight = if (selectedReelsFeedMode == "Reels") FontWeight.ExtraBold else FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { selectedReelsFeedMode = "Reels" }
                            .testTag("reels_tab_header_reels")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { selectedReelsFeedMode = "Friends" }
                            .testTag("reels_tab_header_friends")
                    ) {
                        Text(
                            text = "Friends",
                            color = if (selectedReelsFeedMode == "Friends") Color.White else Color.White.copy(alpha = 0.75f),
                            fontSize = 20.sp,
                            fontWeight = if (selectedReelsFeedMode == "Friends") FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Overlapping Friend Avatars
                        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                            listOf(
                                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120",
                                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=120",
                                "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=120"
                            ).forEach { avatar ->
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, Color.White, CircleShape)
                                ) {
                                    AsyncImage(
                                        model = avatar,
                                        contentDescription = "Friend",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))
            }

            val mins = watchSeconds / 60
            val secs = watchSeconds % 60
            val formattedTimer = "%02d:%02d".format(mins, secs)
            val totalViews = (reel.likeCount * 16 + watchSeconds * 5).coerceAtLeast(2400)
            val earnedUsd = (totalViews / 1000.0) * 1.0
            val profileVisitsReached = (totalViews * 0.085).toInt()

            // Dynamic Island Watch Time & Live Earnings Pill
            Box(
                modifier = Modifier
                    .shadow(18.dp, RoundedCornerShape(28.dp), spotColor = Color(0xFF0066FF))
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xE6050914))
                    .border(
                        width = 1.2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF0066FF), Color(0xFF00E5FF), Color(0xFF10B981))
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .clickable { isIslandExpanded = !isIslandExpanded }
                    .animateContentSize(spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("reel_dynamic_island_watch_pill_${reel.id}")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0066FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        Text(
                            text = "⏱ Watch $formattedTimer",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "• 👁 ${"%.1f".format(totalViews / 1000.0)}K",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.22f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+$${"%.2f".format(earnedUsd)} ($1/1K)",
                                color = Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Icon(
                            imageVector = if (isIslandExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand Reel Analytics",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (isIslandExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { ((watchSeconds % 90) / 90f).coerceIn(0.12f, 1f) },
                            modifier = Modifier
                                .width(260.dp)
                                .height(4.dp)
                                .clip(CircleShape),
                            color = Color(0xFF00E5FF),
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Profile Reached: ${profileVisitsVisitsText(profileVisitsReached)} • Avg Watch: 94% • ${reel.uploadQuality}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📍 Where: Mumbai (36%) • Delhi (24%) • NYC (18%)  |  🎂 Age: 18-24 (48%) • 25-34 (34%)",
                            color = Color(0xFF38BDF8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Subtle gradient scrim on bottom for readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xAA000000), Color(0xDD000000))
                    )
                )
        )

        // Overlay Text Sticker (if added in Studio)
        if (reel.overlayText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 120.dp)
                    .shadow(12.dp, RoundedCornerShape(18.dp))
                    .background(Color(0xBB0F172A), RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0x8800E5FF), RoundedCornerShape(18.dp))
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    text = reel.overlayText,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Center Pause/Play Indicator
        androidx.compose.animation.AnimatedVisibility(
            visible = showPlayPauseIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000))
                    .border(1.dp, Color(0x66FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Center Double-Tap Heart Burst Animation
        androidx.compose.animation.AnimatedVisibility(
            visible = showHeartBurst,
            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color(0xFFFF2D55),
                modifier = Modifier.size(110.dp)
            )
        }

        // Right Interaction Rail (Matching Screenshot 4: Heart, Comment, Repost/Remix, Share, Save + Count, Rounded-Square Audio Art)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(end = 14.dp, bottom = 92.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like
            ReelActionItem(
                icon = if (reel.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                label = formatReelCount(reel.likeCount),
                tint = if (reel.isLikedByMe) Color(0xFFFF2D55) else Color.White,
                onClick = onToggleLike,
                testTag = "reel_like_${reel.id}"
            )

            // Comment
            ReelActionItem(
                icon = Icons.Outlined.ChatBubbleOutline,
                label = formatReelCount(reel.commentCount),
                tint = Color.White,
                onClick = onOpenComments,
                testTag = "reel_comment_${reel.id}"
            )

            // Repost / Remix (🔁 51.4K)
            ReelActionItem(
                icon = Icons.Default.Repeat,
                label = formatReelCount(repostCount),
                tint = if (isReposted) Color(0xFF10B981) else Color.White,
                onClick = {
                    isReposted = !isReposted
                    repostCount += if (isReposted) 1 else -1
                },
                testTag = "reel_repost_${reel.id}"
            )

            // Share
            ReelActionItem(
                icon = Icons.Outlined.Send,
                label = formatReelCount(reel.shareCount),
                tint = Color.White,
                onClick = onShareReel,
                testTag = "reel_share_${reel.id}"
            )

            // Save / Bookmark with Count
            val saveCount = (reel.shareCount * 2 + 840).coerceAtLeast(420) + (if (reel.isSavedByMe) 1 else 0)
            ReelActionItem(
                icon = if (reel.isSavedByMe) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                label = formatReelCount(saveCount),
                tint = if (reel.isSavedByMe) glass.accentPrimary else Color.White,
                onClick = onToggleSave,
                testTag = "reel_save_${reel.id}"
            )

            // Bottom-Right Rounded-Square Audio Album Art Thumbnail (Matching Screenshot 4)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                    .clickable(onClick = onUserClick),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = reel.authorAvatarUrl,
                    contentDescription = "Audio Album Art",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Bottom Left Author User ID, Follow Button & Caption
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 16.dp, end = 80.dp, bottom = 96.dp)
        ) {
            // Author + Follow Button Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onUserClick)
                        .padding(vertical = 2.dp, horizontal = 4.dp)
                        .testTag("reel_author_profile_${reel.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFF00E5FF), CircleShape)
                    ) {
                        AsyncImage(
                            model = reel.authorAvatarUrl,
                            contentDescription = reel.authorUsername,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "@${reel.authorUsername}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            if (reel.authorVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            if (isPrivateAuthor) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Private Account",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            text = "Tap User ID for Profile",
                            color = Color(0xBBFFFFFF),
                            fontSize = 10.sp
                        )
                    }
                }

                // Follow / Following / Requested Glass Button
                val followLabel = when {
                    isFollowed -> "Following"
                    isRequested -> "Requested"
                    else -> "Follow"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            when {
                                isFollowed -> Color(0x33FFFFFF)
                                isRequested -> Color(0x66F59E0B)
                                else -> Color(0xFF38BDF8)
                            }
                        )
                        .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(16.dp))
                        .clickable(onClick = onToggleFollow)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("reel_follow_button_${reel.id}")
                ) {
                    Text(
                        text = followLabel,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Caption
            Text(
                text = reel.caption,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                modifier = Modifier.clickable(onClick = onUserClick)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Audio Track Marquee
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x44000000))
                    .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = reel.audioTitle,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ReelActionItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun profileVisitsVisitsText(visits: Int): String = "${"%,d".format(visits)} profile visits"

private fun formatReelCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "%.1fM".format(count / 1_000_000.0)
        count >= 1_000 -> "%.1fK".format(count / 1_000.0)
        else -> count.toString()
    }
}

