package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
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
import com.example.data.model.CommentEntity
import com.example.ui.theme.LocalLiquidGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    comments: List<CommentEntity>,
    targetTitle: String,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit,
    onToggleLike: (CommentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlass.current
    var inputComment by remember { mutableStateOf("") }
    val quickEmojis = listOf("❤️", "🔥", "✨", "💎", "🙌", "😍", "👏", "💯")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (glass.isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(glass.textTertiary)
            )
        },
        modifier = modifier.testTag("comment_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Comments (${comments.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = glass.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = glass.textSecondary
                    )
                }
            }

            HorizontalDivider(
                color = glass.textTertiary.copy(alpha = 0.2f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Comment List
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No comments yet",
                            fontWeight = FontWeight.SemiBold,
                            color = glass.textPrimary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start the conversation!",
                            color = glass.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, glass.glassBorder, CircleShape)
                            ) {
                                AsyncImage(
                                    model = comment.authorAvatarUrl,
                                    contentDescription = comment.authorUsername,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Comment Body
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = comment.authorUsername,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = glass.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "2h ago",
                                        fontSize = 11.sp,
                                        color = glass.textTertiary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = comment.text,
                                    fontSize = 13.sp,
                                    color = glass.textPrimary,
                                    lineHeight = 18.sp
                                )
                            }

                            // Like Comment
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { onToggleLike(comment) }
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (comment.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like Comment",
                                    tint = if (comment.isLikedByMe) Color(0xFFFF2D55) else glass.textTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                if (comment.likeCount > 0) {
                                    Text(
                                        text = comment.likeCount.toString(),
                                        fontSize = 10.sp,
                                        color = glass.textTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Emoji Pill Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                items(quickEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(glass.glassSurfaceElevated)
                            .clickable {
                                inputComment += emoji
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = emoji, fontSize = 16.sp)
                    }
                }
            }

            // Comment Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.ime)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputComment,
                    onValueChange = { inputComment = it },
                    placeholder = {
                        Text(
                            "Add a comment...",
                            fontSize = 13.sp,
                            color = glass.textTertiary
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = glass.textPrimary,
                        unfocusedTextColor = glass.textPrimary,
                        focusedBorderColor = glass.accentPrimary,
                        unfocusedBorderColor = glass.textTertiary.copy(alpha = 0.3f),
                        focusedContainerColor = glass.glassSurfaceElevated,
                        unfocusedContainerColor = glass.glassSurface
                    ),
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        if (inputComment.isNotBlank()) {
                            onAddComment(inputComment)
                            inputComment = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputComment.isNotBlank()) glass.accentPrimary else glass.glassSurfaceElevated
                        )
                        .testTag("comment_submit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Comment",
                        tint = if (inputComment.isNotBlank()) Color.White else glass.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
