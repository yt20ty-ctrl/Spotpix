package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.StoryEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.StoryGradient
import com.example.ui.theme.StorySeenGradient

@Composable
fun StoryCarousel(
    stories: List<StoryEntity>,
    currentUser: UserEntity?,
    onStoryClick: (Int) -> Unit,
    onAddStoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlass.current

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // My Add Story Item
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onAddStoryClick() }
                    .testTag("add_story_button")
            ) {
                Box(
                    modifier = Modifier.size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(width = 1.5.dp, brush = glass.glassBorder, shape = CircleShape)
                    ) {
                        AsyncImage(
                            model = currentUser?.avatarUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500",
                            contentDescription = "My Story",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Plus Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(if (glass.isDark) Color.White else Color(0xFF111827))
                            .border(2.dp, if (glass.isDark) Color(0xFF0F172A) else Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Story",
                            tint = if (glass.isDark) Color(0xFF111827) else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your story",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = glass.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Creator Stories (with "+o" follow pill badge on suggested creators matching Screenshot 5)
        itemsIndexed(stories, key = { _, item -> item.id }) { index, story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onStoryClick(index) }
                    .testTag("story_item_$index")
            ) {
                Box(
                    modifier = Modifier.size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Story Ring (Glowing Gradient if unseen)
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(if (story.isSeen) StorySeenGradient else StoryGradient)
                            .padding(2.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(if (glass.isDark) Color(0xFF0F172A) else Color.White)
                                .padding(2.dp)
                        ) {
                            AsyncImage(
                                model = story.authorAvatarUrl,
                                contentDescription = story.authorUsername,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Suggested Creator "+o" Follow Pill Badge at Bottom Center (Matching Screenshot 5)
                    if (index >= 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .shadow(3.dp, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (glass.isDark) Color(0xFF1E293B) else Color.White)
                                .border(1.dp, glass.glassBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Follow Creator",
                                tint = glass.textPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.authorUsername,
                    fontSize = 11.sp,
                    fontWeight = if (story.isSeen) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (story.isSeen) glass.textTertiary else glass.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 66.dp)
                )
            }
        }
    }
}
