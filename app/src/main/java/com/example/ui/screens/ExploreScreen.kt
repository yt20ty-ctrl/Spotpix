package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.model.PostEntity
import com.example.data.model.UserEntity
import com.example.ui.components.buildMediaColorFilter
import com.example.ui.theme.LocalLiquidGlass

@Composable
fun ExploreScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    posts: List<PostEntity>,
    allUsers: List<UserEntity>,
    onToggleFollow: (UserEntity) -> Unit,
    onUserClick: (String) -> Unit,
    onPostClick: (PostEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlass.current
    val categories = listOf("All", "Trending", "Photography", "Style", "Travel", "Tech", "Architecture", "Nature")

    val filteredUsers = remember(searchQuery, allUsers) {
        if (searchQuery.isBlank()) allUsers.take(4)
        else allUsers.filter {
            it.username.contains(searchQuery, ignoreCase = true) ||
            it.fullName.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredPosts = remember(searchQuery, category, posts) {
        posts.filter { post ->
            val matchQuery = searchQuery.isBlank() ||
                post.caption.contains(searchQuery, ignoreCase = true) ||
                post.authorUsername.contains(searchQuery, ignoreCase = true) ||
                post.location.contains(searchQuery, ignoreCase = true)

            val matchCategory = when (category) {
                "All" -> true
                "Trending" -> post.likeCount > 2000
                "Photography" -> post.caption.contains("Photography", ignoreCase = true) || post.caption.contains("frame", ignoreCase = true)
                "Travel" -> post.location.isNotBlank()
                "Tech" -> post.caption.contains("LiquidGlass", ignoreCase = true) || post.caption.contains("Instalk", ignoreCase = true)
                else -> true
            }
            matchQuery && matchCategory
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 14.dp)
    ) {
        // Top Bar: Rounded "Search with VETA AI" pill + Saved Bookmark icon (Matching Screenshot 2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Search with VETA AI",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = glass.textSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search with VETA AI",
                        tint = glass.textPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = glass.textSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = glass.textPrimary,
                    unfocusedTextColor = glass.textPrimary,
                    focusedBorderColor = glass.accentPrimary,
                    unfocusedBorderColor = glass.textTertiary.copy(alpha = 0.25f),
                    focusedContainerColor = glass.glassSurfaceElevated,
                    unfocusedContainerColor = glass.glassSurfaceElevated
                ),
                modifier = Modifier
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(28.dp))
                    .testTag("explore_search_bar")
            )

            IconButton(
                onClick = { onCategoryChange(if (category == "Trending") "All" else "Trending") },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(glass.glassSurfaceElevated)
                    .testTag("explore_saved_bookmark_button")
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Saved Explore",
                    tint = glass.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = cat == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) glass.accentPrimary else glass.glassSurfaceElevated
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) glass.accentPrimary else glass.textTertiary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onCategoryChange(cat) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else glass.textPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Creator User IDs Row / Search Results
        Text(
            text = if (searchQuery.isBlank()) "Suggested Creator IDs (Tap to inspect Public / Private)" else "Creator Results",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = glass.textSecondary,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filteredUsers, key = { it.id }) { user ->
                Row(
                    modifier = Modifier
                        .width(235.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(glass.glassSurfaceElevated)
                        .border(1.dp, glass.glassBorder, RoundedCornerShape(16.dp))
                        .clickable { onUserClick(user.id) }
                        .padding(10.dp)
                        .testTag("explore_user_${user.username}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.dp, glass.accentPrimary, CircleShape)
                    ) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = user.username,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "@${user.username}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = glass.textPrimary,
                                maxLines = 1
                            )
                            if (user.isPrivate) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Private",
                                    tint = glass.accentPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else if (user.isVerified) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = glass.accentPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Text(
                            text = if (user.isPrivate) "Private Account" else user.category,
                            fontSize = 10.sp,
                            color = glass.textSecondary,
                            maxLines = 1
                        )
                    }

                    val followLabel = when {
                        user.isFollowedByMe -> "Following"
                        user.isFollowRequested -> "Requested"
                        else -> "Follow"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (user.isFollowedByMe) glass.glassSurface else glass.accentPrimary)
                            .clickable { onToggleFollow(user) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = followLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (user.isFollowedByMe) glass.textPrimary else Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Media Grid (3-column Portrait Tiles with Eye + View Count Badge at Bottom-Left like Screenshot 2)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(bottom = 110.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredPosts, key = { it.id }) { post ->
                val rawViews = (post.likeCount * 95 + 180_000).coerceAtLeast(167_000)
                val formattedViews = if (rawViews >= 1_000_000) {
                    "%.1fM".format(rawViews / 1_000_000.0)
                } else {
                    "${rawViews / 1000}K"
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(0.76f)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onPostClick(post) }
                        .testTag("explore_tile_${post.id}")
                ) {
                    AsyncImage(
                        model = post.mediaUrl,
                        contentDescription = post.caption,
                        colorFilter = remember(post.filterName) { buildMediaColorFilter(post.filterName) },
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Bottom-left Eye + View Count Badge (319K, 226K, 1.2M, etc.) + Top-right @Author pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x66000000))
                            .clickable { onUserClick(post.authorId) }
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "@${post.authorUsername}",
                            fontSize = 8.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xAA000000))
                                )
                            )
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Views",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = formattedViews,
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
