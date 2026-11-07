package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PostEntity
import com.example.data.model.ReelEntity
import com.example.data.model.StoryEntity
import com.example.data.model.UserEntity
import com.example.ui.components.buildMediaColorFilter
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.StoryGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ProfileTab {
    POSTS,
    FEED,
    REELS,
    STORIES,
    LIKED,
    SAVED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: UserEntity?,
    isOwnProfile: Boolean,
    allUsers: List<UserEntity> = emptyList(),
    posts: List<PostEntity>,
    reels: List<ReelEntity>,
    stories: List<StoryEntity> = emptyList(),
    savedPosts: List<PostEntity>,
    onBackFromOtherProfile: () -> Unit,
    onToggleFollow: (UserEntity) -> Unit,
    onApproveFollowRequest: (String) -> Unit,
    onMessageUser: (String) -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenUserSwitcher: () -> Unit,
    onDoubleTapSwitchAccount: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCreatorInsights: () -> Unit,
    onOpenBlueTickModal: () -> Unit = {},
    onOpenMonetizationHub: () -> Unit = {},
    onOpenCreatorWallet: () -> Unit = onOpenMonetizationHub,
    onOpenMonetizeAnalyst: () -> Unit = onOpenMonetizationHub,
    onOpenVetaAuth: () -> Unit = {},
    onTogglePostLike: (PostEntity) -> Unit = {},
    onDoubleTapPostLike: (PostEntity) -> Unit = {},
    onTogglePostSave: (PostEntity) -> Unit = {},
    onToggleReelLike: (ReelEntity) -> Unit = {},
    onDoubleTapReelLike: (ReelEntity) -> Unit = {},
    onToggleReelSave: (ReelEntity) -> Unit = {},
    onOpenStoryAt: (Int) -> Unit = {},
    onOpenLikesSheet: (PostEntity) -> Unit = {},
    onShareContent: (String) -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onPostClick: (PostEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlass.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedTab by remember { mutableStateOf(ProfileTab.POSTS) }
    var inspectedPost by remember { mutableStateOf<PostEntity?>(null) }
    var inspectedReel by remember { mutableStateOf<ReelEntity?>(null) }
    var followersSheetMode by remember { mutableStateOf<String?>(null) } // "FOLLOWERS" or "FOLLOWING"

    val userPosts = remember(user?.id, posts) {
        val own = posts.filter { it.authorId == user?.id }
        if (own.isNotEmpty()) own else posts
    }
    val userReels = remember(user?.id, reels) {
        val own = reels.filter { it.authorId == user?.id }
        if (own.isNotEmpty()) own else reels
    }
    val userStories = remember(user?.id, stories) {
        val own = stories.filter { it.authorId == user?.id }
        if (own.isNotEmpty()) own else stories
    }
    val likedPosts = remember(posts) {
        posts.filter { it.isLikedByMe }
    }

    // Instagram Private Account check: locked if viewing another user who is private & not followed by me
    val isPrivateLocked = !isOwnProfile && (user?.isPrivate == true) && (user.isFollowedByMe == false)

    val highlights = listOf(
        Pair("Studio", "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=400"),
        Pair("Travel", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=400"),
        Pair("Reels BTS", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400"),
        Pair("VETA UI", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400"),
        Pair("Gear", "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=400")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 14.dp)
    ) {
        var customNoteText by remember { mutableStateOf("Unpopular\nopinion...") }
        var showNoteDialog by remember { mutableStateOf(false) }
        var showDiscoverPeopleRow by remember { mutableStateOf(false) }
        var profileBannerTag by remember { mutableStateOf<String?>(null) }

        if (showNoteDialog) {
            var draftNote by remember { mutableStateOf(customNoteText.replace("\n", " ")) }
            AlertDialog(
                onDismissRequest = { showNoteDialog = false },
                title = { Text("Share a Thought / Note", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = draftNote,
                        onValueChange = { draftNote = it.take(40) },
                        label = { Text("Your 24h Note bubble") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (draftNote.isNotBlank()) customNoteText = draftNote
                            showNoteDialog = false
                        }
                    ) { Text("Share Note") }
                },
                dismissButton = {
                    TextButton(onClick = { showNoteDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Top Profile Bar matching Screenshot 1: "+" on Left, "username v" in Center-Left, "@" VETA Pulse + "≡" Hamburger Menu on Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isOwnProfile) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Content",
                        tint = glass.textPrimary,
                        modifier = Modifier
                            .size(26.dp)
                            .clickable {
                                Toast.makeText(context, "Tap + in bottom bar to create Post, Reel, or Story", Toast.LENGTH_SHORT).show()
                            }
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = glass.textPrimary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onBackFromOtherProfile() }
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .pointerInput(isOwnProfile) {
                            detectTapGestures(
                                onTap = {
                                    if (isOwnProfile) onOpenUserSwitcher()
                                    else onBackFromOtherProfile()
                                },
                                onDoubleTap = {
                                    if (isOwnProfile) onDoubleTapSwitchAccount()
                                },
                                onLongPress = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onOpenUserSwitcher()
                                }
                            )
                        }
                        .padding(4.dp)
                        .testTag("profile_username_switcher")
                ) {
                    if (user?.isPrivate == true) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Private Account",
                            tint = glass.accentPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Text(
                        text = user?.username ?: "vaanijsingh_",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = glass.textPrimary
                    )

                    if (isOwnProfile) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Switch Account",
                            tint = glass.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (isOwnProfile) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onOpenVetaAuth,
                        modifier = Modifier.testTag("profile_gmail_login_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AlternateEmail,
                            contentDescription = "VETA Pulse & Gmail Account",
                            tint = glass.textPrimary
                        )
                    }

                    IconButton(
                        onClick = onOpenCreatorInsights,
                        modifier = Modifier.testTag("profile_insights_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "Creator Insights",
                            tint = glass.accentPrimary
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("profile_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Settings and activity",
                            tint = glass.textPrimary
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(glass.glassSurfaceElevated)
                        .border(1.dp, glass.glassBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (user?.isPrivate == true) "🔒 Private ID" else "🌐 Public ID",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = glass.accentPrimary
                    )
                }
            }
        }

        // Main Profile Glass Card (Matching Screenshot 1 Layout)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(24.dp))
                .background(glass.glassSurfaceElevated, RoundedCornerShape(24.dp))
                .border(1.dp, glass.glassBorder, RoundedCornerShape(24.dp))
                .padding(14.dp)
        ) {
            // Avatar with Floating Note Bubble ("Unpopular opinion...") + Full Name above Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar + Floating Note Bubble + Bottom-Right "+" Badge
                Box(
                    modifier = Modifier
                        .width(88.dp)
                        .height(98.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(StoryGradient)
                            .pointerInput(isOwnProfile) {
                                detectTapGestures(
                                    onTap = {
                                        if (stories.isNotEmpty()) onOpenStoryAt(0)
                                    },
                                    onDoubleTap = {
                                        if (isOwnProfile) onDoubleTapSwitchAccount()
                                    },
                                    onLongPress = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onOpenUserSwitcher()
                                    }
                                )
                            }
                            .padding(2.5.dp)
                            .testTag("profile_avatar_box")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(if (glass.isDark) Color(0xFF0F172A) else Color.White)
                                .padding(2.dp)
                        ) {
                            AsyncImage(
                                model = user?.avatarUrl,
                                contentDescription = user?.fullName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Floating Speech-Bubble Note Overlay ("Unpopular opinion...") at Top of Avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (glass.isDark) Color(0xFF1E293B) else Color.White)
                            .border(1.dp, glass.glassBorder, RoundedCornerShape(16.dp))
                            .clickable { if (isOwnProfile) showNoteDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("profile_note_bubble")
                    ) {
                        Text(
                            text = customNoteText,
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = glass.textPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 3
                        )
                    }

                    // Dark Circular "+" Badge at Bottom-Right of Avatar
                    if (isOwnProfile) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 4.dp, bottom = 2.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (glass.isDark) Color.White else Color(0xFF111827))
                                .border(2.dp, if (glass.isDark) Color(0xFF0F172A) else Color.White, CircleShape)
                                .clickable { showNoteDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Note or Story",
                                tint = if (glass.isDark) Color(0xFF111827) else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Right Column: Full Name above Posts / Followers / Following
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = user?.fullName ?: "VSR | Vaanij Singh Raajput",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = glass.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (user?.isVerified == true) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color(0xFF0066FF),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileStatItem(
                            count = "${userPosts.size.coerceAtLeast(user?.postsCount ?: 0)}",
                            label = "posts",
                            onClick = { selectedTab = ProfileTab.FEED }
                        )
                        ProfileStatItem(
                            count = (user?.followersCount ?: 0).let {
                                if (it >= 1000) "%.1fk".format(it / 1000.0) else it.toString()
                            },
                            label = "followers",
                            onClick = {
                                if (!isPrivateLocked) followersSheetMode = "FOLLOWERS"
                            }
                        )
                        ProfileStatItem(
                            count = "${user?.followingCount ?: 0}",
                            label = "following",
                            onClick = {
                                if (!isPrivateLocked) followersSheetMode = "FOLLOWING"
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Multi-line Bio + Website Link + "+ Add banners" Pill (Matching Screenshot 1)
            Text(
                text = user?.bio?.ifBlank {
                    "🎭 Actor | 🎬 Video Editor\n✨ Creating Stories, Crafting Emotions\n🎥 Cinematic Edits & Reels\n📩 Email for Collaborations"
                } ?: "",
                fontSize = 12.sp,
                color = glass.textPrimary,
                lineHeight = 16.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Opening ${user?.website ?: "spotpix.veta.app"}", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = "Website",
                    tint = Color(0xFF0066FF),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = user?.website?.ifBlank { "www.spotpix.veta/${user.username}" } ?: "www.spotpix.veta/creator",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = glass.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isOwnProfile) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(glass.glassSurface)
                            .border(1.dp, glass.glassBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                profileBannerTag = if (profileBannerTag == null) "🎬 Available for Brand Collaborations" else null
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("profile_add_banners_pill")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add banners",
                                tint = glass.textPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = profileBannerTag ?: "Add banners",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = glass.textPrimary
                            )
                        }
                    }

                    if (!user?.email.isNullOrBlank()) {
                        Text(
                            text = "📧 ${user?.email}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = glass.accentPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Professional Creator Dashboard Banner & VETA Quick Bar (On Own Profile)
            if (isOwnProfile) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0x330066FF),
                                    Color(0x3300E5FF),
                                    Color(0x336366F1)
                                )
                            )
                        )
                        .border(1.dp, glass.glassBorder, RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenMonetizationHub)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("professional_dashboard_banner"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Professional dashboard • Spotpix by VETA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = glass.textPrimary
                        )
                        Text(
                            text = "${user?.reach30d ?: "142K"} reach • Monetization (${user?.monetizationStatus ?: "NONE"}) • Paid Ads (₹100/₹120)",
                            fontSize = 9.sp,
                            color = Color(0xFF0066FF)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = glass.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.18f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable(onClick = onOpenCreatorWallet)
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("profile_creator_wallet_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Wallet (GPay/Paytm)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary, maxLines = 1)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF00E5FF).copy(alpha = 0.16f))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                            .clickable(onClick = onOpenMonetizeAnalyst)
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("profile_monetize_analyst_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Monetize Analyst", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0066FF).copy(alpha = 0.16f))
                            .border(1.dp, Color(0xFF0066FF).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable(onClick = onOpenBlueTickModal)
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("profile_blue_tick_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF0066FF), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Blue Tick", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.16f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable(onClick = onOpenMonetizationHub)
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("profile_monetize_ads_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("VETA Ads", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(glass.glassSurface)
                            .border(1.dp, glass.glassBorder, RoundedCornerShape(10.dp))
                            .clickable(onClick = onOpenVetaAuth)
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("profile_veta_auth_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0066FF), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gmail Login", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons matching Screenshot 1: "Edit profile" | "Share profile" | "+o" (Discover People toggle)
            if (isOwnProfile) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(glass.glassSurface)
                            .border(1.dp, glass.glassBorder, RoundedCornerShape(10.dp))
                            .clickable(onClick = onOpenEditProfile)
                            .testTag("edit_profile_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Edit profile", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(glass.glassSurface)
                            .border(1.dp, glass.glassBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                onShareContent("Check out @${user?.username ?: "vaanijsingh_"} on Spotpix by VETA!")
                            }
                            .testTag("share_profile_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Share profile", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (showDiscoverPeopleRow) Color(0xFF0066FF).copy(alpha = 0.2f) else glass.glassSurface)
                            .border(1.dp, glass.glassBorder, RoundedCornerShape(10.dp))
                            .clickable { showDiscoverPeopleRow = !showDiscoverPeopleRow }
                            .testTag("profile_discover_people_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Discover People",
                            tint = glass.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (showDiscoverPeopleRow && allUsers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allUsers.filter { it.id != user?.id }, key = { it.id }) { suggested ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(110.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(glass.glassSurface)
                                    .border(1.dp, glass.glassBorder, RoundedCornerShape(12.dp))
                                    .clickable { onUserClick(suggested.id) }
                                    .padding(8.dp)
                            ) {
                                AsyncImage(
                                    model = suggested.avatarUrl,
                                    contentDescription = suggested.username,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = suggested.username,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = glass.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (suggested.isFollowedByMe) glass.glassSurfaceElevated else Color(0xFF0066FF))
                                        .clickable { onToggleFollow(suggested) }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (suggested.isFollowedByMe) "Following" else "Follow",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (suggested.isFollowedByMe) glass.textPrimary else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (user != null) {
                val followBtnText = when {
                    user.isFollowedByMe -> "Following ✓"
                    user.isFollowRequested -> "Requested ⏳"
                    else -> "Follow"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onToggleFollow(user) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                user.isFollowedByMe -> glass.glassSurface
                                user.isFollowRequested -> Color(0xFFD97706)
                                else -> Color(0xFF0066FF)
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("other_profile_follow_button")
                    ) {
                        Text(
                            text = followBtnText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (user.isFollowedByMe) glass.textPrimary else Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = { onMessageUser(user.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = glass.textPrimary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = glass.glassBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("other_profile_message_button")
                    ) {
                        Text("Message", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // IF PRIVATE ACCOUNT AND NOT FOLLOWED: Show Instagram Private Lock Screen!
        if (isPrivateLocked && user != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .background(glass.glassSurfaceElevated, RoundedCornerShape(24.dp))
                    .border(1.dp, glass.glassBorder, RoundedCornerShape(24.dp))
                    .padding(24.dp)
                    .testTag("private_account_lock_card"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(glass.accentPrimary.copy(alpha = 0.15f))
                        .border(1.5.dp, glass.accentPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Private Account",
                        tint = glass.accentPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "This Account is Private",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = glass.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Follow @${user.username} to see their photos, reels, stories, and likes.",
                    fontSize = 13.sp,
                    color = glass.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (user.isFollowRequested) {
                    Text(
                        text = "Follow request sent to @${user.username}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onApproveFollowRequest(user.id)
                            Toast.makeText(
                                context,
                                "@${user.username} accepted your follow request! Profile unlocked.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.testTag("approve_follow_request_demo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Accept Request (Simulate @${user.username})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Button(
                        onClick = { onToggleFollow(user) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF))
                    ) {
                        Text(
                            text = "Request to Follow @${user.username}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            return
        }

        // UNLOCKED PROFILE CONTENT: Interactive Story Highlights Row (Tap opens full-screen Story Viewer!)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(highlights.size) { index ->
                val (name, img) = highlights[index]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        if (stories.isNotEmpty()) {
                            onOpenStoryAt(index % stories.size)
                        } else {
                            selectedTab = ProfileTab.STORIES
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(StoryGradient)
                            .padding(2.dp)
                    ) {
                        AsyncImage(
                            model = img,
                            contentDescription = name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = name, fontSize = 10.sp, color = glass.textSecondary, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Instagram-Style Complete Profile Tabs: Grid (Posts) | Feed | Reels | Stories | Likes | Saved
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(glass.glassSurfaceElevated, RoundedCornerShape(16.dp))
                .border(1.dp, glass.glassBorder, RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ProfileTabButton(
                icon = Icons.Default.GridOn,
                label = "Grid",
                isSelected = selectedTab == ProfileTab.POSTS,
                onClick = { selectedTab = ProfileTab.POSTS }
            )

            ProfileTabButton(
                icon = Icons.Default.ViewAgenda,
                label = "Feed",
                isSelected = selectedTab == ProfileTab.FEED,
                onClick = { selectedTab = ProfileTab.FEED }
            )

            ProfileTabButton(
                icon = Icons.Default.PlayCircle,
                label = "Reels",
                isSelected = selectedTab == ProfileTab.REELS,
                onClick = { selectedTab = ProfileTab.REELS }
            )

            ProfileTabButton(
                icon = Icons.Default.AutoAwesomeMotion,
                label = "Stories",
                isSelected = selectedTab == ProfileTab.STORIES,
                onClick = { selectedTab = ProfileTab.STORIES }
            )

            ProfileTabButton(
                icon = Icons.Default.Favorite,
                label = "Likes",
                isSelected = selectedTab == ProfileTab.LIKED,
                onClick = { selectedTab = ProfileTab.LIKED }
            )

            if (isOwnProfile) {
                ProfileTabButton(
                    icon = Icons.Default.Bookmark,
                    label = "Saved",
                    isSelected = selectedTab == ProfileTab.SAVED,
                    onClick = { selectedTab = ProfileTab.SAVED }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Content
        when (selectedTab) {
            ProfileTab.POSTS -> {
                var gridColumns by remember { mutableStateOf(3) }
                val gridState = rememberLazyGridState()
                val isGridScrolled by remember {
                    derivedStateOf {
                        gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 20
                    }
                }
                val liquidGlassAlpha by animateFloatAsState(
                    targetValue = if (isGridScrolled) 0.48f else 0.88f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "iphone_liquid_glass_alpha"
                )
                val tileCornerRadius by animateDpAsState(
                    targetValue = when (gridColumns) {
                        1 -> 20.dp
                        2 -> 14.dp
                        else -> if (isGridScrolled) 12.dp else 8.dp
                    },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "tile_corner_radius"
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(gridColumns),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(top = 42.dp, bottom = 110.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(userPosts, key = { it.id }) { post ->
                            val postViews = (post.likeCount * 14 + 1250).coerceAtLeast(1400)
                            val postEarnedUsd = (postViews / 1000.0) * 1.0
                            Box(
                                modifier = Modifier
                                    .aspectRatio(if (gridColumns == 1) 1.15f else 1f)
                                    .clip(RoundedCornerShape(tileCornerRadius))
                                    .border(
                                        width = if (isGridScrolled) 1.dp else 0.5.dp,
                                        brush = Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(alpha = if (isGridScrolled) 0.45f else 0.2f),
                                                Color(0xFF00E5FF).copy(alpha = if (isGridScrolled) 0.45f else 0.15f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(tileCornerRadius)
                                    )
                                    .clickable {
                                        // Open Full-Screen Scrollable Profile Feed starting at this post!
                                        inspectedPost = post
                                    }
                                    .testTag("profile_grid_post_${post.id}")
                            ) {
                                AsyncImage(
                                    model = post.mediaUrl,
                                    contentDescription = post.caption,
                                    colorFilter = remember(post.filterName) { buildMediaColorFilter(post.filterName) },
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Top-left Liquid Glass Views & $1/1K Badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF050914).copy(alpha = liquidGlassAlpha))
                                        .border(0.6.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "👁 ${"%.1f".format(postViews / 1000.0)}K • $${"%.1f".format(postEarnedUsd)}",
                                        fontSize = 8.sp,
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                // Bottom-right Frosted Liquid Glass Likes & Comments Pill
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF050914).copy(alpha = liquidGlassAlpha))
                                        .border(0.6.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "❤️ ${post.likeCount}  💬 ${post.commentCount}",
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Floating iPhone Photos-Inspired Smooth Liquid Glass Lens Header (Turns Transparent on Scroll!)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .shadow(if (isGridScrolled) 12.dp else 4.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Color(0xFF060B19).copy(alpha = liquidGlassAlpha)
                            )
                            .border(
                                1.dp,
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.45f),
                                        Color(0xFF00E5FF).copy(alpha = 0.55f),
                                        Color(0xFF0066FF).copy(alpha = 0.45f)
                                    )
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isGridScrolled) "✨ Liquid Glass View (Transparent)" else "📸 Photo Grid • Tap Post to Scroll Feed",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(3 to "3×3", 2 to "2×2", 1 to "1×1").forEach { (cols, label) ->
                                val active = gridColumns == cols
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (active) Color(0xFF0066FF) else Color.White.copy(alpha = 0.12f)
                                        )
                                        .clickable { gridColumns = cols }
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = label,
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

            ProfileTab.FEED -> {
                // Full Scrollable Instagram Feed View right inside Profile!
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 110.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(userPosts, key = { "profile_feed_${it.id}" }) { post ->
                        ProfileInteractivePostCard(
                            post = post,
                            onToggleLike = { onTogglePostLike(post) },
                            onDoubleTapLike = { onDoubleTapPostLike(post) },
                            onToggleSave = { onTogglePostSave(post) },
                            onOpenComments = { onPostClick(post) },
                            onOpenLikesSheet = { onOpenLikesSheet(post) },
                            onShare = { onShareContent("Post by @${post.authorUsername}: ${post.caption}") }
                        )
                    }
                }
            }

            ProfileTab.REELS -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    contentPadding = PaddingValues(bottom = 110.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(userReels, key = { it.id }) { reel ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(0.65f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    // Open full interactive Reel Viewer modal!
                                    inspectedReel = reel
                                }
                                .testTag("profile_grid_reel_${reel.id}")
                        ) {
                            AsyncImage(
                                model = reel.thumbnailUrl,
                                contentDescription = reel.caption,
                                colorFilter = remember(reel.filterName) { buildMediaColorFilter(reel.filterName) },
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x88000000))
                                    .padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${reel.likeCount}",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            ProfileTab.STORIES -> {
                // Interactive Stories Archive & Live Stories Grid on Profile
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 110.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(userStories.size) { idx ->
                        val story = userStories[idx]
                        val globalIndex = stories.indexOfFirst { it.id == story.id }.coerceAtLeast(0)
                        Box(
                            modifier = Modifier
                                .aspectRatio(0.62f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.5.dp, StoryGradient, RoundedCornerShape(10.dp))
                                .clickable { onOpenStoryAt(globalIndex) }
                        ) {
                            AsyncImage(
                                model = story.mediaUrl,
                                contentDescription = story.overlayText,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(5.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xAA0066FF))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (story.pollQuestion != null) "📊 Poll Story" else "✨ Story",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(5.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x99000000))
                                    .padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "❤️ ${story.likeCount} • @${story.authorUsername}",
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            ProfileTab.LIKED -> {
                val displayLikes = if (likedPosts.isNotEmpty()) likedPosts else posts
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    contentPadding = PaddingValues(bottom = 110.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayLikes, key = { "liked_${it.id}" }) { post ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    inspectedPost = post
                                }
                        ) {
                            AsyncImage(
                                model = post.mediaUrl,
                                contentDescription = post.caption,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xBBFF2D55))
                                    .clickable { onOpenLikesSheet(post) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "❤️ ${post.likeCount} Likes",
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            ProfileTab.SAVED -> {
                if (savedPosts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = "No saved posts yet • Tap bookmark on any post to save",
                            color = glass.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        contentPadding = PaddingValues(bottom = 110.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(savedPosts, key = { "saved_${it.id}" }) { post ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { inspectedPost = post }
                            ) {
                                AsyncImage(
                                    model = post.mediaUrl,
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

    // FULL-SCREEN INSTAGRAM-STYLE SCROLLABLE PROFILE POSTS FEED (Starts at tapped post & scrolls to watch all posts!)
    val currentInspectedPost = inspectedPost?.let { target ->
        posts.find { it.id == target.id } ?: target
    }
    if (currentInspectedPost != null) {
        val feedList = remember(userPosts, posts, currentInspectedPost.id) {
            if (userPosts.any { it.id == currentInspectedPost.id }) userPosts else posts
        }
        val initialIdx = remember(currentInspectedPost.id, feedList) {
            feedList.indexOfFirst { it.id == currentInspectedPost.id }.coerceAtLeast(0)
        }
        val feedScrollState = rememberLazyListState(initialFirstVisibleItemIndex = initialIdx)

        ModalBottomSheet(
            onDismissRequest = { inspectedPost = null },
            containerColor = if (glass.isDark) Color(0xFF060B19) else Color(0xFFF8FAFC)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Posts Feed • @${currentInspectedPost.authorUsername}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = glass.textPrimary
                        )
                        Text(
                            text = "Scroll down to watch more posts • Views, Likes, Age, Where & Profile Reach",
                            fontSize = 10.sp,
                            color = Color(0xFF0066FF)
                        )
                    }
                    IconButton(onClick = { inspectedPost = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = glass.textSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    state = feedScrollState,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(feedList, key = { "inspector_scroll_${it.id}" }) { itemPost ->
                        ProfileInteractivePostCard(
                            post = itemPost,
                            onToggleLike = { onTogglePostLike(itemPost) },
                            onDoubleTapLike = { onDoubleTapPostLike(itemPost) },
                            onToggleSave = { onTogglePostSave(itemPost) },
                            onOpenComments = {
                                inspectedPost = null
                                onPostClick(itemPost)
                            },
                            onOpenLikesSheet = {
                                inspectedPost = null
                                onOpenLikesSheet(itemPost)
                            },
                            onAuthorProfileClick = {
                                inspectedPost = null
                                onUserClick(itemPost.authorId)
                            },
                            onShare = {
                                onShareContent("Post by @${itemPost.authorUsername}: ${itemPost.caption}")
                            }
                        )
                    }
                }
            }
        }
    }

    // FULL-SCREEN INSTAGRAM-STYLE PROFILE REEL PLAYER MODAL
    val currentInspectedReel = inspectedReel?.let { target ->
        reels.find { it.id == target.id } ?: target
    }
    if (currentInspectedReel != null) {
        var reelHeartBurst by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        ModalBottomSheet(
            onDismissRequest = { inspectedReel = null },
            containerColor = Color(0xFF050914)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Spotpix Reel • @${currentInspectedReel.authorUsername}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = { inspectedReel = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .pointerInput(currentInspectedReel.id) {
                            detectTapGestures(
                                onDoubleTap = {
                                    onDoubleTapReelLike(currentInspectedReel)
                                    reelHeartBurst = true
                                    scope.launch {
                                        delay(850)
                                        reelHeartBurst = false
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = currentInspectedReel.thumbnailUrl,
                        contentDescription = currentInspectedReel.caption,
                        colorFilter = remember(currentInspectedReel.filterName) {
                            buildMediaColorFilter(currentInspectedReel.filterName)
                        },
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    androidx.compose.animation.AnimatedVisibility(
                        visible = reelHeartBurst,
                        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFFF2D55),
                            modifier = Modifier.size(96.dp)
                        )
                    }

                    // Bottom Overlay Info & Actions
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xDD000000))
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "@${currentInspectedReel.authorUsername}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = currentInspectedReel.caption,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onToggleReelLike(currentInspectedReel) }
                                ) {
                                    Icon(
                                        imageVector = if (currentInspectedReel.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Like Reel",
                                        tint = if (currentInspectedReel.isLikedByMe) Color(0xFFFF2D55) else Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${currentInspectedReel.likeCount}", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        inspectedReel = null
                                        onShareContent("Reel by @${currentInspectedReel.authorUsername}: ${currentInspectedReel.caption}")
                                    }
                                ) {
                                    Icon(Icons.Outlined.Send, contentDescription = "Share", tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share", color = Color.White, fontSize = 12.sp)
                                }
                            }

                            IconButton(onClick = { onToggleReelSave(currentInspectedReel) }) {
                                Icon(
                                    imageVector = if (currentInspectedReel.isSavedByMe) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Save Reel",
                                    tint = if (currentInspectedReel.isSavedByMe) Color(0xFF00E5FF) else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // INTERACTIVE FOLLOWERS / FOLLOWING LIST SHEET
    if (followersSheetMode != null) {
        ModalBottomSheet(
            onDismissRequest = { followersSheetMode = null },
            containerColor = if (glass.isDark) Color(0xFF060B19) else Color(0xFFF8FAFC)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (followersSheetMode == "FOLLOWERS") "Followers • @${user?.username}"
                        else "Following • @${user?.username}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = glass.textPrimary
                    )
                    IconButton(onClick = { followersSheetMode = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = glass.textSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.heightIn(max = 360.dp)
                ) {
                    items(allUsers, key = { "flw_${it.id}" }) { u ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(glass.glassSurfaceElevated)
                                .border(1.dp, glass.glassBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    followersSheetMode = null
                                    onUserClick(u.id)
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = u.avatarUrl,
                                    contentDescription = u.username,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "@${u.username}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = glass.textPrimary
                                        )
                                        if (u.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = null,
                                                tint = Color(0xFF0066FF),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(u.fullName, fontSize = 11.sp, color = glass.textSecondary)
                                }
                            }
                            if (!u.isCurrentUser) {
                                Button(
                                    onClick = { onToggleFollow(u) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (u.isFollowedByMe) glass.glassSurface else Color(0xFF0066FF)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = when {
                                            u.isFollowedByMe -> "Following"
                                            u.isFollowRequested -> "Requested"
                                            else -> "Follow"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (u.isFollowedByMe) glass.textPrimary else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInteractivePostCard(
    post: PostEntity,
    onToggleLike: () -> Unit,
    onDoubleTapLike: () -> Unit,
    onToggleSave: () -> Unit,
    onOpenComments: () -> Unit,
    onOpenLikesSheet: () -> Unit,
    onAuthorProfileClick: () -> Unit = {},
    onShare: () -> Unit
) {
    val glass = LocalLiquidGlass.current
    var showHeart by remember { mutableStateOf(false) }
    var showAnalyticsDrawer by remember(post.id) { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val postViews = remember(post.id, post.likeCount) {
        (post.likeCount * 14 + 1850).coerceAtLeast(1850)
    }
    val profileVisitsReached = remember(postViews) {
        (postViews * 0.092).toInt().coerceAtLeast(48)
    }
    val earnedUsd = remember(postViews) {
        (postViews / 1000.0) * 1.0
    }
    val earnedInr = remember(earnedUsd) {
        (earnedUsd * 84.0).toInt()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(glass.glassSurfaceElevated, RoundedCornerShape(20.dp))
            .border(1.dp, glass.glassBorder, RoundedCornerShape(20.dp))
            .animateContentSize()
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onAuthorProfileClick() }
                    .padding(2.dp)
            ) {
                AsyncImage(
                    model = post.authorAvatarUrl,
                    contentDescription = post.authorUsername,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "@${post.authorUsername}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = glass.textPrimary
                        )
                        if (post.authorVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF0066FF),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Text(
                        text = if (post.location.isNotBlank()) "${post.location} • Tap to Reach Profile" else "Tap to Reach Profile",
                        fontSize = 10.sp,
                        color = Color(0xFF0066FF)
                    )
                }
            }

            // Live Post Views & $1/1K Earnings Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.16f))
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .clickable { showAnalyticsDrawer = !showAnalyticsDrawer }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "👁 ${"%,d".format(postViews)} • +$${"%.2f".format(earnedUsd)}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF10B981)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .clip(RoundedCornerShape(16.dp))
                .pointerInput(post.id) {
                    detectTapGestures(
                        onDoubleTap = {
                            onDoubleTapLike()
                            showHeart = true
                            scope.launch {
                                delay(850)
                                showHeart = false
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
            androidx.compose.animation.AnimatedVisibility(
                visible = showHeart,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFF2D55),
                    modifier = Modifier.size(84.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleLike() }
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByMe) Color(0xFFFF2D55) else glass.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.likeCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenComments() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = glass.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.commentCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
                }

                Icon(
                    imageVector = Icons.Outlined.Send,
                    contentDescription = "Share",
                    tint = glass.textPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onShare() }
                )

                TextButton(
                    onClick = { showAnalyticsDrawer = !showAnalyticsDrawer },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Icon(Icons.Default.Insights, contentDescription = null, tint = Color(0xFF0066FF), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showAnalyticsDrawer) "Hide Insights" else "View Insights",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0066FF)
                    )
                }
            }

            Icon(
                imageVector = if (post.isSavedByMe) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = "Save",
                tint = if (post.isSavedByMe) Color(0xFF0066FF) else glass.textPrimary,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onToggleSave() }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Liked by @AvaGlow and ${post.likeCount} others • Tap to view all likes",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0066FF),
            modifier = Modifier.clickable { onOpenLikesSheet() }
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "@${post.authorUsername}  ${post.caption}",
            fontSize = 12.sp,
            color = glass.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // INSTAGRAM-STYLE POST ANALYTICS: VIEWS, PROFILE REACH, WHERE (LOCATIONS), AGE, & MONETIZATION ($1/1K VIEWS)
        androidx.compose.animation.AnimatedVisibility(visible = showAnalyticsDrawer) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0066FF).copy(alpha = 0.12f),
                                Color(0xFF00E5FF).copy(alpha = 0.08f)
                            )
                        ),
                        RoundedCornerShape(14.dp)
                    )
                    .border(1.dp, glass.glassBorder, RoundedCornerShape(14.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊 Spotpix Post Insights & Profile Reach",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = glass.textPrimary
                    )
                    Text(
                        text = "Earned: $${"%.2f".format(earnedUsd)} (₹$earnedInr)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF10B981)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("👁 Views: ${"%,d".format(postViews)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
                    Text("👤 Profile Reached: $profileVisitsReached visits", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0066FF))
                    Text("💬 Comments: ${post.commentCount}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
                }

                Text(
                    text = "📍 Where (Top Locations): Mumbai (34%) • Delhi NCR (24%) • Bengaluru (18%) • NYC (14%) • London (10%)",
                    fontSize = 10.sp,
                    color = glass.textSecondary
                )

                Text(
                    text = "🎂 Age Groups: 18–24 (46%) • 25–34 (34%) • 13–17 (11%) • 35+ (9%)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = glass.textPrimary
                )
            }
        }
    }
}

@Composable
private fun ProfileStatItem(
    count: String,
    label: String,
    onClick: () -> Unit = {}
) {
    val glass = LocalLiquidGlass.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = count,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = glass.textPrimary
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = glass.textSecondary
        )
    }
}

@Composable
private fun ProfileTabButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val glass = LocalLiquidGlass.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF0066FF) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color.White else glass.textSecondary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else glass.textSecondary
        )
    }
}
