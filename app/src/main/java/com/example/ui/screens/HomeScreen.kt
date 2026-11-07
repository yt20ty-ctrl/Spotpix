package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PostEntity
import com.example.data.model.StoryEntity
import com.example.data.model.UserEntity
import com.example.ui.components.StoryCarousel
import com.example.ui.components.buildMediaColorFilter
import com.example.ui.theme.LocalLiquidGlass
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    posts: List<PostEntity>,
    stories: List<StoryEntity>,
    allUsers: List<UserEntity>,
    currentUser: UserEntity?,
    onStoryClick: (Int) -> Unit,
    onAddStoryClick: () -> Unit,
    onToggleLike: (PostEntity) -> Unit,
    onDoubleTapLike: (PostEntity) -> Unit,
    onToggleSave: (PostEntity) -> Unit,
    onToggleFollowAuthor: (String) -> Unit,
    onOpenComments: (String, String) -> Unit,
    onOpenLikesSheet: (PostEntity) -> Unit = {},
    onSharePost: (PostEntity) -> Unit,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlass.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // Stories Row
        item {
            StoryCarousel(
                stories = stories,
                currentUser = currentUser,
                onStoryClick = onStoryClick,
                onAddStoryClick = onAddStoryClick
            )
            HorizontalDivider(
                color = glass.textTertiary.copy(alpha = 0.15f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Post Cards
        items(posts, key = { it.id }) { post ->
            val authorUser = allUsers.find { it.id == post.authorId }
            PostCard(
                post = post,
                authorUser = authorUser,
                isMyPost = post.authorId == currentUser?.id,
                onToggleLike = { onToggleLike(post) },
                onDoubleTapLike = { onDoubleTapLike(post) },
                onToggleSave = { onToggleSave(post) },
                onToggleFollowAuthor = { onToggleFollowAuthor(post.authorId) },
                onOpenComments = { onOpenComments(post.id, "Post by @${post.authorUsername}") },
                onOpenLikesSheet = { onOpenLikesSheet(post) },
                onSharePost = { onSharePost(post) },
                onUserClick = { onUserClick(post.authorId) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PostCard(
    post: PostEntity,
    authorUser: UserEntity?,
    isMyPost: Boolean,
    onToggleLike: () -> Unit,
    onDoubleTapLike: () -> Unit,
    onToggleSave: () -> Unit,
    onToggleFollowAuthor: () -> Unit,
    onOpenComments: () -> Unit,
    onOpenLikesSheet: () -> Unit,
    onSharePost: () -> Unit,
    onUserClick: () -> Unit
) {
    val glass = LocalLiquidGlass.current
    val context = LocalContext.current
    var showHeartAnimation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val isFollowed = authorUser?.isFollowedByMe ?: post.isFollowingAuthor
    val isRequested = authorUser?.isFollowRequested ?: false
    val isPrivateAuthor = authorUser?.isPrivate ?: post.authorIsPrivate

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = if (glass.isDark) Color(0x3300E5FF) else Color(0x22000000)
            )
            .background(
                color = glass.glassSurfaceElevated,
                shape = RoundedCornerShape(26.dp)
            )
            .border(
                width = 1.dp,
                brush = glass.glassBorder,
                shape = RoundedCornerShape(26.dp)
            )
            .padding(14.dp)
            .testTag("post_card_${post.id}")
    ) {
        // Author Header Row with User ID + Follow/Following Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onUserClick)
                    .padding(vertical = 2.dp)
                    .testTag("post_author_${post.id}")
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, glass.accentPrimary, CircleShape)
                ) {
                    AsyncImage(
                        model = post.authorAvatarUrl,
                        contentDescription = post.authorUsername,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "@${post.authorUsername}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = glass.textPrimary
                        )
                        if (post.authorVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color(0xFF0066FF),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (post.isSponsoredAd) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0066FF).copy(alpha = 0.18f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Sponsored",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0066FF)
                                )
                            }
                        }
                        if (isPrivateAuthor) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Private Account",
                                tint = glass.textSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (post.location.isNotBlank()) post.location else "Tap User ID to view profile",
                            fontSize = 11.sp,
                            color = glass.textSecondary
                        )
                    }
                }
            }

            // Follow / Following / Requested Pill Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isMyPost) {
                    val followText = when {
                        isFollowed -> "Following"
                        isRequested -> "Requested"
                        else -> "Follow"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when {
                                    isFollowed -> glass.glassSurface
                                    isRequested -> Color(0x33F59E0B)
                                    else -> glass.accentPrimary
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isFollowed) glass.textTertiary.copy(alpha = 0.4f) else glass.accentPrimary,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable(onClick = onToggleFollowAuthor)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                            .testTag("post_follow_btn_${post.id}")
                    ) {
                        Text(
                            text = followText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFollowed) glass.textPrimary else Color.White
                        )
                    }
                }

                IconButton(
                    onClick = onSharePost,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = glass.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Post Media with Double-Tap to Like & Filter Rendering
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(20.dp))
                .pointerInput(post.id) {
                    detectTapGestures(
                        onDoubleTap = {
                            onDoubleTapLike()
                            showHeartAnimation = true
                            coroutineScope.launch {
                                delay(900)
                                showHeartAnimation = false
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = post.mediaUrl,
                contentDescription = post.caption,
                colorFilter = remember(post.filterName) { buildMediaColorFilter(post.filterName) },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Filter / Quality Badge
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (post.filterName != "Original") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x990F172A))
                            .border(0.8.dp, Color(0x6600E5FF), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "✨ ${post.filterName}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                if (post.isVideo) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xAA0284C7))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🎬 ${post.uploadQuality}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Overlay Text Banner (if added in Studio)
            if (post.overlayText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xBB0F172A))
                        .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = post.overlayText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Double Tap Heart Burst Animation
            androidx.compose.animation.AnimatedVisibility(
                visible = showHeartAnimation,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFF2D55),
                    modifier = Modifier.size(96.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions Row: Like, Comment, Share, Save
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleLike)
                        .padding(4.dp)
                        .testTag("post_like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByMe) Color(0xFFFF2D55) else glass.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    if (!post.hideLikes) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = post.likeCount.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = glass.textPrimary
                        )
                    }
                }

                // Comment Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenComments)
                        .padding(4.dp)
                        .testTag("post_comment_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = glass.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.commentCount.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = glass.textPrimary
                    )
                }

                // Share Button
                IconButton(
                    onClick = onSharePost,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Share",
                        tint = glass.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Save / Bookmark Button
            IconButton(
                onClick = onToggleSave,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("post_save_button_${post.id}")
            ) {
                Icon(
                    imageVector = if (post.isSavedByMe) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (post.isSavedByMe) glass.accentPrimary else glass.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Likes Summary / Private Account Notice
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (post.hideLikes) {
                "Liked by @AvaGlow and others"
            } else if (isPrivateAuthor && !isFollowed && !isMyPost) {
                "🔒 Liked by ${post.likeCount} people • Follow @${post.authorUsername} to see full likes & profile"
            } else {
                "Liked by @AvaGlow and ${post.likeCount} others • Tap to see likes"
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = glass.textSecondary,
            modifier = Modifier
                .clickable {
                    if (isPrivateAuthor && !isFollowed && !isMyPost) {
                        Toast.makeText(
                            context,
                            "@${post.authorUsername} is a Private Account. Follow them to view their likes & profile posts!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onUserClick()
                    } else {
                        onOpenLikesSheet()
                    }
                }
                .testTag("post_likes_summary_${post.id}")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Caption with clickable @username
        if (post.caption.isNotBlank()) {
            Text(
                text = "@${post.authorUsername}  ${post.caption}",
                fontSize = 13.sp,
                color = glass.textPrimary,
                lineHeight = 18.sp,
                modifier = Modifier.clickable { onUserClick() }
            )
        }

        // View comments teaser
        if (post.commentCount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "View all ${post.commentCount} comments",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = glass.textSecondary,
                modifier = Modifier
                    .clickable(onClick = onOpenComments)
                    .padding(vertical = 2.dp)
            )
        }
    }
}
