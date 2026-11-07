package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalLiquidGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareDialog(
    content: String,
    onDismiss: () -> Unit
) {
    val glass = LocalLiquidGlass.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

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
        modifier = Modifier.testTag("share_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Text(
                text = "Share to",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = glass.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                fontSize = 13.sp,
                color = glass.textSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ShareActionItem(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy Link",
                    onClick = {
                        clipboardManager.setText(AnnotatedString("https://instalink.dev/p/${System.currentTimeMillis()}"))
                        Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                ShareActionItem(
                    icon = Icons.Default.NearMe,
                    label = "Send in DM",
                    onClick = {
                        Toast.makeText(context, "Shared to Direct Messages!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                ShareActionItem(
                    icon = Icons.Default.Share,
                    label = "More Options",
                    onClick = {
                        Toast.makeText(context, "Sharing via Android ShareSheet...", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ShareActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val glass = LocalLiquidGlass.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(glass.glassSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = glass.accentPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = glass.textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
