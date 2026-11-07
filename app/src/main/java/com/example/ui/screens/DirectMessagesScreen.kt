package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DirectMessageEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.LocalLiquidGlass

@Composable
fun DirectMessagesScreen(
    allUsers: List<UserEntity>,
    allMessages: List<DirectMessageEntity>,
    currentUser: UserEntity?,
    activeChatUserId: String?,
    chatMessages: List<DirectMessageEntity>,
    chatThemeName: String,
    onChatThemeChange: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onCloseChat: () -> Unit,
    onCloseMessages: () -> Unit,
    onUserClick: (String) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onSendMessage: (text: String, isVoiceNote: Boolean, durationSec: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeChatUserId != null) {
        val targetUser = allUsers.find { it.id == activeChatUserId } ?: allUsers.firstOrNull()
        ChatRoomScreen(
            targetUser = targetUser,
            currentUser = currentUser,
            messages = chatMessages,
            chatThemeName = chatThemeName,
            onChatThemeChange = onChatThemeChange,
            onBack = onCloseChat,
            onUserClick = { targetUser?.id?.let(onUserClick) },
            onAcceptRequest = { targetUser?.id?.let(onAcceptRequest) },
            onDeleteConversation = {
                targetUser?.id?.let(onDeleteConversation)
                onCloseChat()
            },
            onSendMessage = onSendMessage,
            modifier = modifier
        )
    } else {
        MessagesInboxScreen(
            allUsers = allUsers.filter { it.id != currentUser?.id },
            allMessages = allMessages,
            currentUser = currentUser,
            onOpenChat = onOpenChat,
            onUserClick = onUserClick,
            onAcceptRequest = onAcceptRequest,
            onDeleteConversation = onDeleteConversation,
            onBack = onCloseMessages,
            modifier = modifier
        )
    }
}

@Composable
private fun MessagesInboxScreen(
    allUsers: List<UserEntity>,
    allMessages: List<DirectMessageEntity>,
    currentUser: UserEntity?,
    onOpenChat: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlass.current
    val context = LocalContext.current
    var selectedInboxTab by remember { mutableStateOf("Primary") }
    BackHandler(onBack = onBack)

    val requestUserIds = remember(allMessages) {
        allMessages.filter { it.isRequest }.map { it.conversationWithUserId }.toSet()
    }

    val displayedUsers = remember(selectedInboxTab, allUsers, requestUserIds) {
        when (selectedInboxTab) {
            "Requests" -> allUsers.filter { it.id in requestUserIds || !it.isFollowedByMe }
            "General" -> allUsers.filter { it.isFollowedByMe }.reversed()
            else -> allUsers.filter { it.id !in requestUserIds }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp)
    ) {
        // Inbox Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("inbox_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = glass.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column {
                    Text(
                        text = "@${currentUser?.username ?: "Messages"}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = glass.textPrimary
                    )
                    Text(
                        text = "Encrypted Liquid Glass Direct",
                        fontSize = 11.sp,
                        color = glass.accentPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(glass.glassSurfaceElevated)
                    .border(1.dp, glass.glassBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${requestUserIds.size} Requests",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF59E0B)
                )
            }
        }

        // Active Creators Notes & Status Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            items(allUsers) { user ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenChat(user.id) }
                ) {
                    Box(modifier = Modifier.size(56.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(1.5.dp, glass.accentPrimary, CircleShape)
                        ) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = user.username,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        // Online badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .border(2.dp, Color(0xFF0F172A), CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "@${user.username}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = glass.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Segmented Tabs: Primary | General | Requests
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(glass.glassSurfaceElevated, RoundedCornerShape(16.dp))
                .border(1.dp, glass.glassBorder, RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Primary", "General", "Requests").forEach { tab ->
                val isSelected = selectedInboxTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) glass.accentPrimary else Color.Transparent)
                        .clickable { selectedInboxTab = tab }
                        .padding(vertical = 7.dp)
                        .testTag("inbox_tab_${tab.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (tab == "Requests" && requestUserIds.isNotEmpty()) "Requests (${requestUserIds.size})" else tab,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else glass.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Conversation List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayedUsers, key = { it.id }) { user ->
                val lastMessage = allMessages.firstOrNull { it.conversationWithUserId == user.id }
                val isRequestItem = selectedInboxTab == "Requests" || (lastMessage?.isRequest == true)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .background(glass.glassSurfaceElevated, RoundedCornerShape(20.dp))
                        .border(1.dp, glass.glassBorder, RoundedCornerShape(20.dp))
                        .clickable { onOpenChat(user.id) }
                        .padding(12.dp)
                        .testTag("chat_item_${user.username}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(1.2.dp, glass.accentPrimary, CircleShape)
                                .clickable { onUserClick(user.id) }
                        ) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = user.fullName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.fullName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = glass.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "@${user.username}",
                                    fontSize = 11.sp,
                                    color = glass.accentPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = lastMessage?.text ?: "Tap to start a Liquid Glass chat with @${user.username}",
                                fontSize = 12.sp,
                                color = glass.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isRequestItem) "Request" else "Active",
                                fontSize = 10.sp,
                                color = if (isRequestItem) Color(0xFFF59E0B) else glass.accentPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // If Message Request, show Accept / Delete / Profile buttons right on the card!
                    if (isRequestItem) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onAcceptRequest(user.id)
                                    selectedInboxTab = "Primary"
                                    Toast.makeText(context, "Accepted message request from @${user.username}", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = glass.accentPrimary),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                            ) {
                                Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = { onUserClick(user.id) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                            ) {
                                Text("View ID", fontSize = 11.sp, color = glass.textPrimary)
                            }

                            OutlinedButton(
                                onClick = {
                                    onDeleteConversation(user.id)
                                    Toast.makeText(context, "Request removed", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF2D55)),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                            ) {
                                Text("Decline", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRoomScreen(
    targetUser: UserEntity?,
    currentUser: UserEntity?,
    messages: List<DirectMessageEntity>,
    chatThemeName: String,
    onChatThemeChange: (String) -> Unit,
    onBack: () -> Unit,
    onUserClick: () -> Unit,
    onAcceptRequest: () -> Unit,
    onDeleteConversation: () -> Unit,
    onSendMessage: (text: String, isVoiceNote: Boolean, durationSec: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlass.current
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var showThemePicker by remember { mutableStateOf(false) }
    BackHandler(onBack = onBack)

    val bubbleGradient = when (chatThemeName) {
        "Cyber Violet" -> listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
        "Emerald Mist" -> listOf(Color(0xFF10B981), Color(0xFF0284C7))
        "Sunset Gold" -> listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
        else -> listOf(Color(0xFF00E5FF), Color(0xFF2563EB))
    }

    val hasPendingRequest = messages.any { it.isRequest }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Chat Header (Tap user avatar/name to view their Profile!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(glass.glassSurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("chat_room_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = glass.textPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onUserClick)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, glass.accentPrimary, CircleShape)
                    ) {
                        AsyncImage(
                            model = targetUser?.avatarUrl,
                            contentDescription = targetUser?.username,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = targetUser?.fullName ?: "Chat",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = glass.textPrimary
                        )
                        Text(
                            text = "@${targetUser?.username ?: ""} • Tap for Profile",
                            fontSize = 11.sp,
                            color = glass.accentPrimary
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = { showThemePicker = !showThemePicker }) {
                    Icon(Icons.Default.Palette, contentDescription = "Chat Theme", tint = glass.accentPrimary)
                }
                IconButton(onClick = {
                    Toast.makeText(context, "Voice calling @${targetUser?.username}...", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = glass.textPrimary)
                }
                IconButton(onClick = {
                    Toast.makeText(context, "Video calling @${targetUser?.username}...", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = glass.textPrimary)
                }
            }
        }

        // Chat Theme Selector Bar
        if (showThemePicker) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(glass.glassSurfaceElevated)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Liquid Sapphire", "Cyber Violet", "Emerald Mist", "Sunset Gold").forEach { theme ->
                    val active = chatThemeName == theme
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (active) glass.accentPrimary else glass.glassSurface)
                            .clickable { onChatThemeChange(theme) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = theme.split(" ").last(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) Color.White else glass.textPrimary
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = glass.textTertiary.copy(alpha = 0.2f), thickness = 0.5.dp)

        // Messages Bubble Stream
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMe = msg.senderId == currentUser?.id

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 285.dp)
                            .shadow(6.dp, RoundedCornerShape(18.dp))
                            .background(
                                brush = if (isMe) {
                                    Brush.linearGradient(bubbleGradient)
                                } else {
                                    Brush.linearGradient(
                                        listOf(glass.glassSurfaceElevated, glass.glassSurfaceElevated)
                                    )
                                },
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = if (isMe) 18.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 18.dp
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = glass.glassBorder,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        if (msg.isVoiceNote) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircleFilled,
                                    contentDescription = "Play Voice Note",
                                    tint = if (isMe) Color.White else glass.accentPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Column {
                                    Text(
                                        text = "ılılıllıılılıllıılı (${msg.voiceDurationSec}s)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMe) Color.White else glass.textPrimary
                                    )
                                    Text(
                                        text = msg.text,
                                        fontSize = 11.sp,
                                        color = if (isMe) Color(0xDDFFFFFF) else glass.textSecondary
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = msg.text,
                                fontSize = 14.sp,
                                color = if (isMe) Color.White else glass.textPrimary,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }
        }

        // Pending Message Request Banner
        if (hasPendingRequest) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(glass.glassSurfaceElevated)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onAcceptRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = glass.accentPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Accept Request", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                OutlinedButton(
                    onClick = onDeleteConversation,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF2D55)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Block / Delete", fontSize = 12.sp)
                }
            }
        }

        // Message Input Field Bar with Photo & Voice Note Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    onSendMessage("📷 Shared an HD Liquid Glass snapshot", false, 0)
                },
                modifier = Modifier
                    .size(42.dp)
                    .background(glass.glassSurfaceElevated, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Send Photo",
                    tint = glass.accentPrimary
                )
            }

            IconButton(
                onClick = {
                    onSendMessage("Voice Note • Sent from Instalk Audio", true, 9)
                    Toast.makeText(context, "Voice note sent!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(42.dp)
                    .background(glass.glassSurfaceElevated, CircleShape)
                    .testTag("chat_voice_note_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Send Voice Note",
                    tint = Color(0xFFEC4899)
                )
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text("Message @${targetUser?.username ?: ""}...", fontSize = 13.sp, color = glass.textTertiary)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text_field"),
                shape = RoundedCornerShape(22.dp),
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
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText, false, 0)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank()) glass.accentPrimary else glass.glassSurfaceElevated
                    )
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) Color.White else glass.textTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
