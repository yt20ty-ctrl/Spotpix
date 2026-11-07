package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.StoryEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StoryViewerModal(
    story: StoryEntity,
    authorUser: UserEntity?,
    currentIndex: Int,
    totalStories: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit,
    onToggleLike: () -> Unit,
    onDoubleTapLike: () -> Unit,
    onVotePoll: (Int) -> Unit,
    onAuthorClick: (String) -> Unit,
    onToggleFollowAuthor: (String) -> Unit,
    onSendReply: (String) -> Unit
) {
    var progress by remember(story.id) { mutableFloatStateOf(0f) }
    var replyText by remember { mutableStateOf("") }
    var showHeartBurst by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val isFollowed = authorUser?.isFollowedByMe ?: story.isFollowingAuthor
    val isRequested = authorUser?.isFollowRequested ?: false
    val isCurrentUser = authorUser?.isCurrentUser ?: false

    LaunchedEffect(story.id) {
        progress = 0f
        val stepTime = 50L
        val totalTime = 6500L // 6.5 seconds per story
        val steps = (totalTime / stepTime).toInt()
        for (i in 1..steps) {
            delay(stepTime)
            progress = i.toFloat() / steps.toFloat()
        }
        onNext()
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Full Screen Story Image with Live Filter
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = "Story from ${story.authorUsername}",
                colorFilter = remember(story.filterName) { buildMediaColorFilter(story.filterName) },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Top and Bottom Gradient Scrims for readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xDD000000), Color.Transparent)
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xEE000000))
                        )
                    )
            )

            // Left & Right Tap Zones (Single-tap navigates, Double-tap likes story with heart burst!)
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(story.id) {
                            detectTapGestures(
                                onTap = { onPrevious() },
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
                )
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .pointerInput(story.id) {
                            detectTapGestures(
                                onTap = { onNext() },
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
                )
            }

            // Overlay Text Badge (if present)
            if (story.overlayText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 120.dp, start = 24.dp, end = 24.dp)
                        .shadow(10.dp, RoundedCornerShape(18.dp))
                        .background(Color(0xAA0F172A), RoundedCornerShape(18.dp))
                        .border(1.dp, Color(0x6600E5FF), RoundedCornerShape(18.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = story.overlayText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // Interactive Story Poll Card (if present)
            if (!story.pollQuestion.isNullOrBlank()) {
                val totalVotes = (story.pollVotesA + story.pollVotesB).coerceAtLeast(1)
                val pctA = ((story.pollVotesA * 100f) / totalVotes).toInt()
                val pctB = 100 - pctA

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .widthIn(max = 300.dp)
                        .fillMaxWidth(0.85f)
                        .shadow(16.dp, RoundedCornerShape(24.dp))
                        .background(Color(0xD90F172A), RoundedCornerShape(24.dp))
                        .border(1.2.dp, Color(0x8838BDF8), RoundedCornerShape(24.dp))
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "INTERACTIVE POLL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E5FF),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = story.pollQuestion,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Option A
                    PollOptionBar(
                        label = story.pollOptionA ?: "Option A",
                        percentage = pctA,
                        isVoted = story.userVotedOption == 1,
                        showResults = story.userVotedOption != null,
                        onClick = { onVotePoll(1) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option B
                    PollOptionBar(
                        label = story.pollOptionB ?: "Option B",
                        percentage = pctB,
                        isVoted = story.userVotedOption == 2,
                        showResults = story.userVotedOption != null,
                        onClick = { onVotePoll(2) }
                    )
                }
            }

            // Double-Tap Heart Burst Animation
            androidx.compose.animation.AnimatedVisibility(
                visible = showHeartBurst,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFF2D55),
                    modifier = Modifier.size(110.dp)
                )
            }

            // Top Header: Progress Bars, Author User ID, & Follow Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter)
            ) {
                // Segmented Progress Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 0 until totalStories) {
                        val segProgress = when {
                            i < currentIndex -> 1f
                            i == currentIndex -> progress
                            else -> 0f
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0x66FFFFFF))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(segProgress)
                                    .background(Color.White)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Author Info Row (Tapping opens author's Profile!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onAuthorClick(story.authorId) }
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                                .testTag("story_author_profile_link")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color(0xFF00E5FF), CircleShape)
                            ) {
                                AsyncImage(
                                    model = story.authorAvatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "@${story.authorUsername}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (authorUser?.isPrivate == true) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Private Account",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (story.filterName != "Original") "2h • ✨ ${story.filterName}" else "2h ago • Tap to view profile",
                                    color = Color(0xCCFFFFFF),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Follow / Following / Requested Pill inside Story
                        if (!isCurrentUser) {
                            val followLabel = when {
                                isFollowed -> "Following"
                                isRequested -> "Requested"
                                else -> "Follow"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when {
                                            isFollowed -> Color(0x33FFFFFF)
                                            isRequested -> Color(0x55F59E0B)
                                            else -> Color(0xFF0284C7)
                                        }
                                    )
                                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(14.dp))
                                    .clickable { onToggleFollowAuthor(story.authorId) }
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                                    .testTag("story_follow_button")
                            ) {
                                Text(
                                    text = followLabel,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_story_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Story",
                            tint = Color.White
                        )
                    }
                }
            }

            // Bottom Story Reactions & Reply Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .align(Alignment.BottomCenter)
            ) {
                // Quick Emoji Reactions Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("🔥", "😍", "💎", "🙌", "✨").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0x44FFFFFF))
                                    .clickable { onSendReply("Reacted $emoji to your story") }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(text = emoji, fontSize = 14.sp)
                            }
                        }
                    }

                    Text(
                        text = "Double-tap to ❤️",
                        fontSize = 11.sp,
                        color = Color(0xAAFFFFFF)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = {
                            Text(
                                "Reply to @${story.authorUsername}...",
                                color = Color(0xBBFFFFFF),
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("story_reply_input"),
                        shape = RoundedCornerShape(26.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0x88FFFFFF),
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedContainerColor = Color(0x44000000),
                            unfocusedContainerColor = Color(0x44000000)
                        ),
                        singleLine = true
                    )

                    // Like Story Button with Counter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0x44000000))
                            .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(22.dp))
                            .clickable { onToggleLike() }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("story_like_button")
                    ) {
                        Icon(
                            imageVector = if (story.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like Story",
                            tint = if (story.isLikedByMe) Color(0xFFFF2D55) else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${story.likeCount}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (replyText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onSendReply(replyText)
                                replyText = ""
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF38BDF8), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Reply",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PollOptionBar(
    label: String,
    percentage: Int,
    isVoted: Boolean,
    showResults: Boolean,
    onClick: () -> Unit
) {
    val animatedFill by animateFloatAsState(
        targetValue = if (showResults) (percentage / 100f) else 0f,
        label = "poll_fill"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x33FFFFFF))
            .border(
                width = if (isVoted) 1.5.dp else 1.dp,
                color = if (isVoted) Color(0xFF00E5FF) else Color(0x44FFFFFF),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
    ) {
        if (showResults) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFill)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x8800E5FF), Color(0x883B82F6))
                        )
                    )
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = if (isVoted) FontWeight.ExtraBold else FontWeight.SemiBold
            )
            if (showResults) {
                Text(
                    text = "$percentage%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
