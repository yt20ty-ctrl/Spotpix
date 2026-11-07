package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserEntity
import com.example.data.repository.SocialRepository
import com.example.ui.theme.LocalLiquidGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSwitcherDialog(
    allUsers: List<UserEntity>,
    currentUser: UserEntity?,
    onSelectUser: (String) -> Unit,
    onOpenGmailLogin: () -> Unit = {},
    onLaunchGoogleCredentialSignIn: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val glass = LocalLiquidGlass.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (glass.isDark) Color(0xFF060B19) else Color(0xFFF8FAFC),
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
        modifier = Modifier.testTag("user_switcher_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Switch Spotpix Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = glass.textPrimary
                    )
                    Text(
                        text = "Hold or Double-Tap Profile Icon • Verified Gmail Accounts",
                        fontSize = 11.sp,
                        color = Color(0xFF0066FF)
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = glass.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                items(allUsers, key = { it.id }) { user ->
                    val isSelected = user.id == currentUser?.id
                    val isAdmin = SocialRepository.isVerifiedSuperAdmin(user)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Color(0xFF0066FF).copy(alpha = 0.18f)
                                else glass.glassSurfaceElevated
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF0066FF) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectUser(user.id) }
                            .padding(12.dp)
                            .testTag("user_switch_item_${user.username}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFF0066FF), CircleShape)
                        ) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = user.fullName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.fullName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = glass.textPrimary
                                )
                                if (user.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF0066FF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                if (isAdmin) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "SUPER ADMIN",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "@${user.username} • ${user.email}",
                                fontSize = 11.sp,
                                color = glass.textSecondary
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = Color(0xFF0066FF),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Login with Gmail (Credential Manager One-Tap + VETA Account Portal)
            Button(
                onClick = {
                    onLaunchGoogleCredentialSignIn()
                    onOpenGmailLogin()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("switcher_login_with_gmail_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Login with Gmail / Add Spotpix Account",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
