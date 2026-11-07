package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.AppTab
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.StoryGradient

@Composable
fun LiquidGlassNavBar(
    selectedTab: AppTab,
    isCreateMenuExpanded: Boolean,
    onTabSelected: (AppTab) -> Unit,
    onToggleCreateMenu: () -> Unit,
    onSelectCreateMode: (CreateMode) -> Unit,
    onDoubleTapProfile: () -> Unit,
    onHoldProfile: () -> Unit = {},
    userAvatarUrl: String?,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlass.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val plusRotation by animateFloatAsState(
        targetValue = if (isCreateMenuExpanded) 45f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "plus_rotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating Liquid Glass Creation Pop-Up Menu
        AnimatedVisibility(
            visible = isCreateMenuExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }) + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }) + scaleOut(targetScale = 0.85f)
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .widthIn(max = 340.dp)
                    .fillMaxWidth()
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = Color(0x8800E5FF)
                    )
                    .background(
                        color = if (glass.isDark) Color(0xEE0F172A) else Color(0xF2FFFFFF),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(
                        width = 1.2.dp,
                        brush = glass.glassBorder,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(14.dp)
                    .testTag("floating_create_menu")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create & Edit Studio",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = glass.textPrimary
                    )
                    Text(
                        text = "Filters • Effects • HD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = glass.accentPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CreateMenuQuickOption(
                        icon = Icons.Default.AutoAwesomeMotion,
                        label = "Story",
                        subLabel = "Poll & FX",
                        gradientColors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)),
                        onClick = { onSelectCreateMode(CreateMode.STORY) },
                        testTag = "create_menu_story"
                    )
                    CreateMenuQuickOption(
                        icon = Icons.Default.PhotoCamera,
                        label = "Feed Post",
                        subLabel = "Crop & LUT",
                        gradientColors = listOf(Color(0xFF00E5FF), Color(0xFF2563EB)),
                        onClick = { onSelectCreateMode(CreateMode.POST) },
                        testTag = "create_menu_post"
                    )
                    CreateMenuQuickOption(
                        icon = Icons.Default.MovieFilter,
                        label = "Reel",
                        subLabel = "Audio & 9:16",
                        gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)),
                        onClick = { onSelectCreateMode(CreateMode.REEL) },
                        testTag = "create_menu_reel"
                    )
                    CreateMenuQuickOption(
                        icon = Icons.Default.Videocam,
                        label = "Video",
                        subLabel = "4K Ultra",
                        gradientColors = listOf(Color(0xFF10B981), Color(0xFF0284C7)),
                        onClick = { onSelectCreateMode(CreateMode.VIDEO) },
                        testTag = "create_menu_video"
                    )
                }
            }
        }

        // Floating Frosted Glass Pill Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = if (glass.isDark) Color(0x6600E5FF) else Color(0x33000000),
                    ambientColor = Color(0x33000000)
                )
                .background(
                    color = glass.glassSurfaceElevated,
                    shape = CircleShape
                )
                .border(
                    width = 1.2.dp,
                    brush = glass.glassBorder,
                    shape = CircleShape
                )
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavBarItem(
                selected = selectedTab == AppTab.HOME,
                onClick = { onTabSelected(AppTab.HOME) },
                icon = if (selectedTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                label = "Home",
                testTag = "nav_home"
            )

            NavBarItem(
                selected = selectedTab == AppTab.EXPLORE,
                onClick = { onTabSelected(AppTab.EXPLORE) },
                icon = if (selectedTab == AppTab.EXPLORE) Icons.Filled.Search else Icons.Outlined.Search,
                label = "Explore",
                testTag = "nav_explore"
            )

            // Center Glowing + Button (Opens Floating Liquid Glass Menu)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(8.dp, CircleShape, spotColor = Color(0xFF00E5FF))
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF00E5FF),
                                Color(0xFF3B82F6),
                                Color(0xFF8B5CF6)
                            )
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggleCreateMenu() }
                    .testTag("nav_create"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Story, Post, Reel or Video",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(plusRotation)
                )
            }

            NavBarItem(
                selected = selectedTab == AppTab.REELS,
                onClick = { onTabSelected(AppTab.REELS) },
                icon = if (selectedTab == AppTab.REELS) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircleOutline,
                label = "Reels",
                testTag = "nav_reels"
            )

            // Profile Tab: Single-tap opens Profile, Double-tap switches account!
            val isProfileSelected = selectedTab == AppTab.PROFILE
            val scale by animateFloatAsState(
                targetValue = if (isProfileSelected) 1.15f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "profile_scale"
            )

            Column(
                modifier = Modifier
                    .scale(scale)
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onTabSelected(AppTab.PROFILE) },
                            onDoubleTap = { onDoubleTapProfile() },
                            onLongPress = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onHoldProfile()
                            }
                        )
                    }
                    .padding(8.dp)
                    .testTag("nav_profile"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!userAvatarUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isProfileSelected) 2.dp else 1.dp,
                                brush = if (isProfileSelected) StoryGradient else Brush.linearGradient(listOf(glass.textTertiary, glass.textTertiary)),
                                shape = CircleShape
                            )
                    ) {
                        AsyncImage(
                            model = userAvatarUrl,
                            contentDescription = "My Profile (Double-tap to switch account)",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Icon(
                        imageVector = if (isProfileSelected) Icons.Filled.Person else Icons.Outlined.Person,
                        contentDescription = "Profile",
                        tint = if (isProfileSelected) glass.accentPrimary else glass.textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateMenuQuickOption(
    icon: ImageVector,
    label: String,
    subLabel: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    testTag: String
) {
    val glass = LocalLiquidGlass.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(gradientColors))
                .border(1.dp, Color(0x66FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = glass.textPrimary
        )
        Text(
            text = subLabel,
            fontSize = 9.sp,
            color = glass.textSecondary
        )
    }
}

@Composable
private fun NavBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    testTag: String
) {
    val glass = LocalLiquidGlass.current
    val tint by animateColorAsState(
        targetValue = if (selected) glass.accentPrimary else glass.textSecondary,
        label = "item_tint"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.18f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "item_scale"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(glass.accentPrimary)
            )
        }
    }
}
