package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.ThemeMode
import com.example.ui.theme.LocalLiquidGlass
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileModal(
    user: UserEntity?,
    onDismiss: () -> Unit,
    onSave: (fullName: String, username: String, bio: String, website: String, category: String) -> Unit
) {
    val glass = LocalLiquidGlass.current
    var fullName by remember(user) { mutableStateOf(user?.fullName ?: "") }
    var username by remember(user) { mutableStateOf(user?.username ?: "") }
    var category by remember(user) { mutableStateOf(user?.category ?: "Digital Creator") }
    var bio by remember(user) { mutableStateOf(user?.bio ?: "") }
    var website by remember(user) { mutableStateOf(user?.website ?: "") }

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
        modifier = Modifier.testTag("edit_profile_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Edit Professional Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = glass.textPrimary,
                    unfocusedTextColor = glass.textPrimary,
                    focusedBorderColor = glass.accentPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("User ID (@username)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = glass.textPrimary,
                    unfocusedTextColor = glass.textPrimary,
                    focusedBorderColor = glass.accentPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Creator Category") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = glass.textPrimary,
                    unfocusedTextColor = glass.textPrimary,
                    focusedBorderColor = glass.accentPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = glass.textPrimary,
                    unfocusedTextColor = glass.textPrimary,
                    focusedBorderColor = glass.accentPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = website,
                onValueChange = { website = it },
                label = { Text("Website Link") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = glass.textPrimary,
                    unfocusedTextColor = glass.textPrimary,
                    focusedBorderColor = glass.accentPrimary
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { onSave(fullName, username, bio, website, category) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = glass.accentPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_profile_button")
            ) {
                Text("Save Profile Changes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorInsightsModal(
    user: UserEntity?,
    onDismiss: () -> Unit
) {
    val glass = LocalLiquidGlass.current

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
        modifier = Modifier.testTag("creator_insights_modal")
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
                        text = "Creator Analytics & Insights",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = glass.textPrimary
                    )
                    Text(
                        text = "@${user?.username ?: "creator"} • Last 30 Days Performance",
                        fontSize = 12.sp,
                        color = glass.accentPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = glass.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // KPI Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InsightMetricCard(
                    title = "Accounts Reached",
                    value = user?.reach30d ?: "284.5K",
                    growth = "+148.2%",
                    modifier = Modifier.weight(1f)
                )
                InsightMetricCard(
                    title = "Engaged Accounts",
                    value = "42.9K",
                    growth = "+64.5%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InsightMetricCard(
                    title = "Total Followers",
                    value = "${(user?.followersCount ?: 14200) / 1000.0}K",
                    growth = "+12.4%",
                    modifier = Modifier.weight(1f)
                )
                InsightMetricCard(
                    title = "Reels Plays (4K)",
                    value = "1.18M",
                    growth = "+210%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Audience Reach Breakdown",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = glass.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(glass.glassSurfaceElevated, RoundedCornerShape(16.dp))
                    .border(1.dp, glass.glassBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AudienceBarRow("Reels & Vertical Video", 0.78f, "78%")
                AudienceBarRow("Home Feed & Carousels", 0.54f, "54%")
                AudienceBarRow("Interactive Stories & Polls", 0.42f, "42%")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InsightMetricCard(
    title: String,
    value: String,
    growth: String,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlass.current
    Column(
        modifier = modifier
            .background(glass.glassSurfaceElevated, RoundedCornerShape(16.dp))
            .border(1.dp, glass.glassBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(text = title, fontSize = 11.sp, color = glass.textSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = glass.textPrimary)
        Text(text = growth, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
    }
}

@Composable
private fun AudienceBarRow(label: String, fraction: Float, pctText: String) {
    val glass = LocalLiquidGlass.current
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, color = glass.textPrimary, fontWeight = FontWeight.Medium)
            Text(text = pctText, fontSize = 12.sp, color = glass.accentPrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(glass.textTertiary.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF8B5CF6))))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsModal(
    currentUser: UserEntity?,
    isSuperAdmin: Boolean = false,
    themeMode: ThemeMode,
    appBackgroundStyle: String = "Cobalt Liquid Glass",
    uploadQuality: String,
    dataSaverMode: Boolean,
    autoPlayVideos: Boolean,
    hideLikeCountsDefault: Boolean,
    twoFactorEnabled: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAppBackgroundStyleChange: (String) -> Unit = {},
    onUploadQualityChange: (String) -> Unit,
    onDataSaverChange: (Boolean) -> Unit,
    onAutoPlayChange: (Boolean) -> Unit,
    onHideLikesChange: (Boolean) -> Unit,
    onTwoFactorChange: (Boolean) -> Unit,
    onPrivacyChange: (Boolean) -> Unit,
    onOpenUserSwitcher: () -> Unit,
    onOpenVetaAuth: () -> Unit = {},
    onOpenBlueTickModal: () -> Unit = {},
    onOpenMonetizationHub: () -> Unit = {},
    onOpenSuperAdminConsole: () -> Unit = {},
    onSwitchToSuperAdminAndOpenConsole: () -> Unit = {},
    onResetData: () -> Unit,
    onDismiss: () -> Unit
) {
    val glass = LocalLiquidGlass.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var activityStatus by remember { mutableStateOf(true) }
    var settingsSearchQuery by remember { mutableStateOf("") }
    var selectedSlideCategory by remember { mutableStateOf("All") }
    var selectedGraphNodeIndex by remember { mutableStateOf(0) }
    var showVisualMapGraph by remember { mutableStateOf(true) }
    var activeSubSheetTitle by remember { mutableStateOf<String?>(null) }
    var activeSubSheetDesc by remember { mutableStateOf("") }
    val isPrivateAccount = currentUser?.isPrivate ?: false

    if (activeSubSheetTitle != null) {
        AlertDialog(
            onDismissRequest = { activeSubSheetTitle = null },
            title = { Text(activeSubSheetTitle ?: "", fontWeight = FontWeight.ExtraBold) },
            text = {
                Text(
                    text = activeSubSheetDesc,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(onClick = { activeSubSheetTitle = null }) {
                    Text("Done")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (glass.isDark) Color(0xFF0A0F1E) else Color(0xFFF8FAFC),
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
        modifier = Modifier.testTag("settings_modal")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 44.dp, top = 4.dp, bottom = 6.dp)
                    .verticalScroll(scrollState)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Top Header matching Screenshot 3 + Map Toggle Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = glass.textPrimary
                            )
                        }
                        Column {
                            Text(
                                text = "Settings and activity",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = glass.textPrimary
                            )
                            Text(
                                text = "Spotpix by VETA • Map Graph • Side-Slide • Anti-Hack Shield",
                                fontSize = 10.sp,
                                color = Color(0xFF0066FF)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showVisualMapGraph = !showVisualMapGraph },
                            modifier = Modifier.testTag("settings_toggle_map_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountTree,
                                contentDescription = "Toggle Settings Map",
                                tint = if (showVisualMapGraph) Color(0xFF00E5FF) else glass.textSecondary
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
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Search Bar inside Settings and activity
                OutlinedTextField(
                    value = settingsSearchQuery,
                    onValueChange = { settingsSearchQuery = it },
                    placeholder = { Text("Search settings, UPI, background, security...", fontSize = 13.sp, color = glass.textSecondary) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search Settings", tint = glass.textSecondary)
                    },
                    trailingIcon = {
                        if (settingsSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { settingsSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = glass.textSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = glass.glassSurfaceElevated,
                        unfocusedContainerColor = glass.glassSurfaceElevated,
                        focusedBorderColor = Color(0xFF0066FF),
                        unfocusedBorderColor = glass.textTertiary.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // HORIZONTAL SIDE-SLIDE CATEGORY DRAWER / CAROUSEL
                val slideCategories = listOf(
                    Triple("All", "🗺️ Full Map & Page", Color(0xFF0066FF)),
                    Triple("Background", "🎨 Background & Structure", Color(0xFF00E5FF)),
                    Triple("Security", "🛡️ Anti-Hack Shield", Color(0xFF10B981)),
                    Triple("Payments", "💳 Real UPI & Wallet", Color(0xFFF59E0B)),
                    Triple("Privacy", "🔒 Privacy & Content", Color(0xFF8B5CF6)),
                    Triple("Admin", "⚡ Master Admin Control", Color(0xFFEC4899))
                )
                Text(
                    text = "↔️ Side-Slide Settings Navigator (Swipe Left/Right or Tap to Filter)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = glass.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_side_slide_carousel")
                ) {
                    items(slideCategories) { (key, label, accent) ->
                        val isSelected = selectedSlideCategory == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) accent else glass.glassSurfaceElevated
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color.White else accent.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedSlideCategory = key }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else glass.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // INTERACTIVE SETTINGS MAP & ARCHITECTURE GRAPH (Canvas + Tappable Nodes)
                if (showVisualMapGraph && (selectedSlideCategory == "All" || selectedSlideCategory == "Background" || selectedSlideCategory == "Security")) {
                    SettingsArchitectureMapGraph(
                        selectedNodeIndex = selectedGraphNodeIndex,
                        appBackgroundStyle = appBackgroundStyle,
                        isSuperAdmin = isSuperAdmin,
                        onSelectNode = { idx ->
                            selectedGraphNodeIndex = idx
                            when (idx) {
                                1 -> onOpenVetaAuth()
                                2 -> {
                                    onDismiss()
                                    onOpenMonetizationHub()
                                }
                                5 -> {
                                    onDismiss()
                                    if (isSuperAdmin) onOpenSuperAdminConsole()
                                    else onSwitchToSuperAdminAndOpenConsole()
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // APP BACKGROUND & UI STRUCTURE LIVE CUSTOMIZER CARD
                if (selectedSlideCategory == "All" || selectedSlideCategory == "Background") {
                    AppBackgroundAndStructureCard(
                        currentStyle = appBackgroundStyle,
                        onStyleSelect = { style ->
                            onAppBackgroundStyleChange(style)
                            Toast.makeText(context, "App Background changed to $style", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // VETA ANTI-HACK & CRYPTOGRAPHIC SECURITY SHIELD CARD
                if (selectedSlideCategory == "All" || selectedSlideCategory == "Security" || selectedSlideCategory == "Payments") {
                    VetaAntiHackSecurityCard(
                        twoFactorEnabled = twoFactorEnabled,
                        onTwoFactorChange = onTwoFactorChange
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // SUPER ADMIN FULL CONTROL PANEL ENTRY (With 1-Tap Verified Owner Switch)
                if (selectedSlideCategory == "All" || selectedSlideCategory == "Admin") {
                    if (isSuperAdmin) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF064E3B), Color(0xFF0066FF))
                                    )
                                )
                                .border(1.5.dp, Color(0xFF10B981), RoundedCornerShape(16.dp))
                                .clickable {
                                    onDismiss()
                                    onOpenSuperAdminConsole()
                                }
                                .padding(14.dp)
                                .testTag("settings_veta_admin_console_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "VETA Admin Console",
                                    tint = Color(0xFF10B981)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "VETA Super Admin Master Console",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Full App Control (${currentUser?.email}) • Master Broadcast • Content Takedown • UPI Ledger",
                                        fontSize = 11.sp,
                                        color = Color(0xFF00E5FF)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                                    )
                                )
                                .border(1.2.dp, Color(0xFF00E5FF), RoundedCornerShape(16.dp))
                                .clickable {
                                    onSwitchToSuperAdminAndOpenConsole()
                                }
                                .padding(14.dp)
                                .testTag("settings_switch_to_super_admin_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Activate Super Admin",
                                    tint = Color(0xFF00E5FF)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Switch to VETA Super Admin (Full Control)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Authenticate as VaanijSingh28@gmail.com to control Users, Blue Tick, Wallet, Ads & App Config",
                                        fontSize = 10.sp,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

            // SECTION: Your account -> VETA Account (Matching Screenshot 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your account",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = glass.textSecondary
                )
                Text(
                    text = "VETA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0066FF)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(glass.glassSurfaceElevated, RoundedCornerShape(16.dp))
                    .border(1.dp, glass.glassBorder, RoundedCornerShape(16.dp))
                    .clickable {
                        onDismiss()
                        onOpenVetaAuth()
                    }
                    .padding(14.dp)
                    .testTag("settings_veta_account_center_row"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AllInclusive,
                        contentDescription = "VETA Account",
                        tint = Color(0xFF0066FF),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "VETA Account",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = glass.textPrimary
                        )
                        Text(
                            text = "Password, security, personal details, cross-profile experiences, ad preferences (@${currentUser?.username ?: "user"})",
                            fontSize = 11.sp,
                            color = glass.textSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = glass.textSecondary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenVetaAuth()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("VETA Login", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenBlueTickModal()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF0066FF), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Blue Tick", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenMonetizationHub()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Monetize/Ads", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION: How you use Spotpix
            SettingsSectionHeader("How you use Spotpix")
            SettingsNavRow(Icons.Default.BookmarkBorder, "Saved", query = settingsSearchQuery) {
                activeSubSheetTitle = "Saved Collection"
                activeSubSheetDesc = "All your bookmarked Posts and Reels are accessible in the Saved tab on your Spotpix profile."
            }
            SettingsNavRow(Icons.Default.History, "Archive", query = settingsSearchQuery) {
                activeSubSheetTitle = "Stories & Posts Archive"
                activeSubSheetDesc = "Your 24-hour stories and archived posts are automatically backed up privately in VETA Cloud."
            }
            SettingsNavRow(Icons.Default.ShowChart, "Your activity", query = settingsSearchQuery) {
                activeSubSheetTitle = "Your Activity"
                activeSubSheetDesc = "Manage your likes, comments, tags, sticker responses, and time spent on Spotpix by VETA."
            }
            SettingsNavRow(Icons.Default.NotificationsNone, "Notifications", query = settingsSearchQuery) {
                activeSubSheetTitle = "Push Notifications"
                activeSubSheetDesc = "Real-time notifications are enabled for Likes, Comments, Follow Requests, Creator Wallet Payouts, and Ad Approvals."
            }
            SettingsNavRow(Icons.Default.AccessTime, "Time management", query = settingsSearchQuery) {
                activeSubSheetTitle = "Time Management"
                activeSubSheetDesc = "Daily average: 42m. Set daily time limits and sleep mode reminders."
            }
            SettingsNavRow(Icons.Default.TabletMac, "Spotpix for tablets", query = settingsSearchQuery) {
                activeSubSheetTitle = "Spotpix Adaptive Layout"
                activeSubSheetDesc = "Optimized for foldables, tablets, and wide-screen displays."
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION: Subscriptions
            SettingsSectionHeader("Subscriptions")
            SettingsNavRow(
                icon = Icons.Default.Diamond,
                title = "Spotpix Plus",
                trailingValue = "Active",
                query = settingsSearchQuery
            ) {
                activeSubSheetTitle = "Spotpix Plus by VETA"
                activeSubSheetDesc = "Enjoy 4K Ultra Reel uploads, custom Liquid Glass themes, and priority creator analytics."
            }
            SettingsNavRow(
                icon = Icons.Default.Verified,
                title = "VETA Verified",
                trailingValue = if (currentUser?.isVerified == true) "Verified ✓" else currentUser?.verificationStatus ?: "Not subscribed",
                query = settingsSearchQuery
            ) {
                onDismiss()
                onOpenBlueTickModal()
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION: Your insights and tools (Creator Wallet & Monetize Analyst)
            SettingsSectionHeader("Your insights and tools")
            SettingsNavRow(
                icon = Icons.Default.DashboardCustomize,
                title = "Your dashboard",
                trailingValue = "Wallet & Analyst",
                query = settingsSearchQuery
            ) {
                onDismiss()
                onOpenMonetizationHub()
            }
            SettingsNavRow(
                icon = Icons.Default.BarChart,
                title = "Account type and tools",
                trailingValue = currentUser?.category ?: "Creator",
                query = settingsSearchQuery
            ) {
                onDismiss()
                onOpenMonetizationHub()
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION: Your orders and fundraisers (UPI / GPay / Paytm)
            SettingsSectionHeader("Your orders and fundraisers")
            SettingsNavRow(
                icon = Icons.Default.AccountBalanceWallet,
                title = "Orders and payments",
                trailingValue = "GPay / Paytm / UPI",
                query = settingsSearchQuery
            ) {
                onDismiss()
                onOpenMonetizationHub()
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION: Who can see your content
            SettingsSectionHeader("Who can see your content")
            SettingsSwitchRow(
                title = "Account privacy (@${currentUser?.username})",
                subtitle = if (isPrivateAccount) "Private — Only approved followers can see your posts & reels" else "Public — Anyone on Spotpix can view your content",
                checked = isPrivateAccount,
                onCheckedChange = {
                    onPrivacyChange(it)
                    Toast.makeText(
                        context,
                        if (it) "Account set to Private 🔒" else "Account set to Public 🌐",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
            SettingsNavRow(Icons.Default.Stars, "Close Friends", trailingValue = "12", query = settingsSearchQuery) {
                activeSubSheetTitle = "Close Friends"
                activeSubSheetDesc = "Share exclusive stories with your 12 selected Close Friends."
            }
            SettingsNavRow(Icons.Default.GridView, "Crossposting", query = settingsSearchQuery) {
                activeSubSheetTitle = "Crossposting across VETA"
                activeSubSheetDesc = "Automatically share your Spotpix Stories and Reels across your linked VETA profile."
            }
            SettingsNavRow(Icons.Default.Block, "Blocked", trailingValue = "0", query = settingsSearchQuery) {
                activeSubSheetTitle = "Blocked Accounts"
                activeSubSheetDesc = "You haven't blocked anyone on Spotpix."
            }
            SettingsNavRow(Icons.Default.LocationOn, "Story, live and location", query = settingsSearchQuery) {
                activeSubSheetTitle = "Story, Live and Location"
                activeSubSheetDesc = "Control who can view your live streams and location tags."
            }
            SettingsNavRow(Icons.Default.Group, "Activity in Friends feed", query = settingsSearchQuery) {
                activeSubSheetTitle = "Activity in Friends Tab"
                activeSubSheetDesc = "Choose whether your public Reel likes and reposts appear in the Reels Friends tab."
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION: How others can interact with you
            SettingsSectionHeader("How others can interact with you")
            SettingsNavRow(Icons.Default.ChatBubbleOutline, "Messages and story replies", query = settingsSearchQuery) {
                activeSubSheetTitle = "Messages & Story Replies"
                activeSubSheetDesc = "Allow message requests and story replies from followers."
            }
            SettingsNavRow(Icons.Default.AlternateEmail, "Tags and mentions", query = settingsSearchQuery) {
                activeSubSheetTitle = "Tags & Mentions"
                activeSubSheetDesc = "Everyone can @mention and tag you in photos and Reels."
            }
            SettingsNavRow(Icons.Default.ModeComment, "Comments", query = settingsSearchQuery) {
                activeSubSheetTitle = "Comment Controls"
                activeSubSheetDesc = "VETA AI spam filter is active on your posts and Reels."
            }
            SettingsNavRow(Icons.Default.Repeat, "Sharing and reuse", query = settingsSearchQuery) {
                activeSubSheetTitle = "Sharing & Remixing"
                activeSubSheetDesc = "Allow creators to remix and repost your public Reels with attribution."
            }
            SettingsNavRow(Icons.Default.PersonOff, "Restricted", trailingValue = "0", query = settingsSearchQuery) {
                activeSubSheetTitle = "Restricted Accounts"
                activeSubSheetDesc = "No restricted accounts."
            }
            SettingsNavRow(Icons.Default.ReportProblem, "Limit interactions", trailingValue = "Off", query = settingsSearchQuery) {
                activeSubSheetTitle = "Limit Unwanted Interactions"
                activeSubSheetDesc = "Temporarily limit comments and messages from non-followers."
            }
            SettingsNavRow(Icons.Default.TextFields, "Hidden Words", query = settingsSearchQuery) {
                activeSubSheetTitle = "Hidden Words"
                activeSubSheetDesc = "Offensive comments and message requests are automatically hidden."
            }
            SettingsNavRow(Icons.Default.PersonAddAlt, "Follow and invite friends", query = settingsSearchQuery) {
                activeSubSheetTitle = "Invite Friends to Spotpix"
                activeSubSheetDesc = "Share your @${currentUser?.username} Spotpix link via WhatsApp, SMS, or Gmail."
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION: What you see
            SettingsSectionHeader("What you see")
            SettingsNavRow(Icons.Default.StarOutline, "Favorites", trailingValue = "4", query = settingsSearchQuery) {
                activeSubSheetTitle = "Favorite Creators"
                activeSubSheetDesc = "Posts from your favorite creators appear higher in your Home feed."
            }
            SettingsNavRow(Icons.Default.NotificationsOff, "Muted", trailingValue = "0", query = settingsSearchQuery) {
                activeSubSheetTitle = "Muted Accounts"
                activeSubSheetDesc = "You haven't muted any accounts."
            }
            SettingsNavRow(Icons.Default.Tune, "Content preferences", query = settingsSearchQuery) {
                activeSubSheetTitle = "Content Preferences"
                activeSubSheetDesc = "Personalize your Explore and Reels recommendations powered by VETA AI."
            }
            SettingsSwitchRow(
                title = "Like and share counts",
                subtitle = "Hide like & view counts by default on posts and reels",
                checked = hideLikeCountsDefault,
                onCheckedChange = onHideLikesChange
            )

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION: Your app and media
            SettingsSectionHeader("Your app and media")
            SettingsNavRow(Icons.Default.PermDeviceInformation, "Device permissions", query = settingsSearchQuery) {
                activeSubSheetTitle = "Device Permissions"
                activeSubSheetDesc = "Camera, Microphone, and Photo Picker permissions are managed via Android system settings."
            }
            SettingsNavRow(Icons.Default.Download, "Archiving and downloading", query = settingsSearchQuery) {
                activeSubSheetTitle = "Archiving & Downloading"
                activeSubSheetDesc = "Save original photos and Reels to your device gallery automatically."
            }
            SettingsNavRow(Icons.Default.AccessibilityNew, "Accessibility", query = settingsSearchQuery) {
                activeSubSheetTitle = "Accessibility & Captions"
                activeSubSheetDesc = "Auto-generated captions and high-contrast Liquid Glass borders are enabled."
            }
            SettingsNavRow(Icons.Default.Translate, "Language and translations", trailingValue = "English (IN)", query = settingsSearchQuery) {
                activeSubSheetTitle = "Language & Translations"
                activeSubSheetDesc = "App language: English. Auto-translate captions and comments."
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Data usage and media quality card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(glass.glassSurfaceElevated, RoundedCornerShape(16.dp))
                    .border(1.dp, glass.glassBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Data usage and media quality",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = glass.textPrimary
                )
                Text(
                    text = "Always upload highest quality photos, reels, and videos",
                    fontSize = 11.sp,
                    color = glass.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Standard 720p", "HD 1080p", "4K Ultra").forEach { q ->
                        val selected = uploadQuality == q
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) glass.accentPrimary else glass.glassSurface)
                                .clickable { onUploadQualityChange(q) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = q,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else glass.textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            SettingsSwitchRow(
                title = "Cellular Data Saver Mode",
                subtitle = "Reduce cellular data usage while preloading Reels",
                checked = dataSaverMode,
                onCheckedChange = onDataSaverChange
            )
            Spacer(modifier = Modifier.height(6.dp))
            SettingsSwitchRow(
                title = "Auto-Play HD Reels & Videos",
                subtitle = "Smooth 60-120 FPS playback in Feed and Reels",
                checked = autoPlayVideos,
                onCheckedChange = onAutoPlayChange
            )
            Spacer(modifier = Modifier.height(6.dp))
            SettingsSwitchRow(
                title = "Two-Factor Authentication (2FA)",
                subtitle = "Require security code when signing in from new devices",
                checked = twoFactorEnabled,
                onCheckedChange = onTwoFactorChange
            )
            Spacer(modifier = Modifier.height(6.dp))
            SettingsSwitchRow(
                title = "Show Online Activity Status",
                subtitle = "Allow accounts you follow to see when you are active in Direct",
                checked = activityStatus,
                onCheckedChange = { activityStatus = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION: Family Center
            SettingsSectionHeader("Family Center")
            SettingsNavRow(Icons.Default.FamilyRestroom, "Supervision for Teen Accounts", query = settingsSearchQuery) {
                activeSubSheetTitle = "VETA Family Center"
                activeSubSheetDesc = "Parental supervision tools and safety protections for teen creators."
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION: More info and support
            SettingsSectionHeader("More info and support")
            SettingsNavRow(Icons.Default.HelpOutline, "Help", query = settingsSearchQuery) {
                activeSubSheetTitle = "Spotpix Help Center"
                activeSubSheetDesc = "Need help with Blue Tick verification, Creator Wallet withdrawals, or UPI ad campaigns? Contact VETA Support 24/7."
            }
            SettingsNavRow(Icons.Default.AutoAwesome, "VETA AI support assistant", query = settingsSearchQuery) {
                activeSubSheetTitle = "VETA AI Support Assistant"
                activeSubSheetDesc = "Instant automated troubleshooting for account access, monetization thresholds, and UTR verification."
            }
            SettingsNavRow(Icons.Default.PrivacyTip, "Privacy Center", query = settingsSearchQuery) {
                activeSubSheetTitle = "VETA Privacy Center"
                activeSubSheetDesc = "Zero-trust Firestore security rules protect your personal data, private profiles, and payment ledgers."
            }
            SettingsNavRow(Icons.Default.VerifiedUser, "Account Status", trailingValue = "Good Standing", query = settingsSearchQuery) {
                activeSubSheetTitle = "Account Status"
                activeSubSheetDesc = "@${currentUser?.username} complies with all Spotpix by VETA Community Guidelines and Monetization Policies."
            }
            SettingsNavRow(Icons.Default.Info, "About", trailingValue = "v4.5 VETA", query = settingsSearchQuery) {
                activeSubSheetTitle = "About Spotpix by VETA"
                activeSubSheetDesc = "Spotpix by VETA (v4.5.0) • Powered by VETA Ecosystem. Merchant UPI Settlement: vanij7@fam."
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION: Also from VETA
            SettingsSectionHeader("Also from VETA")
            SettingsNavRow(
                icon = Icons.Default.AutoFixHigh,
                title = "VETA AI",
                subtitle = "Get answers, advice and generate creative ideas",
                query = settingsSearchQuery
            ) {
                activeSubSheetTitle = "VETA AI Studio"
                activeSubSheetDesc = "Use Search with VETA AI in the Explore tab to discover trending creators and topics."
            }
            SettingsNavRow(
                icon = Icons.Default.AlternateEmail,
                title = "VETA Pulse",
                subtitle = "Share ideas and join creator conversations",
                query = settingsSearchQuery
            ) {
                activeSubSheetTitle = "VETA Pulse"
                activeSubSheetDesc = "Connected with your @${currentUser?.username} Spotpix identity."
            }
            SettingsNavRow(
                icon = Icons.Default.AllInclusive,
                title = "More from VETA",
                query = settingsSearchQuery
            ) {
                activeSubSheetTitle = "VETA Ecosystem"
                activeSubSheetDesc = "Spotpix • VETA Creator Wallet • VETA Ad Manager • VETA Cloud."
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Liquid Glass Theme Appearance
            SettingsSectionHeader("Liquid Glass Appearance")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionPill(
                    icon = Icons.Default.SettingsBrightness,
                    label = "Auto (System)",
                    isSelected = themeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                    modifier = Modifier.weight(1f)
                )

                ThemeOptionPill(
                    icon = Icons.Default.DarkMode,
                    label = "Dark Glass",
                    isSelected = themeMode == ThemeMode.DARK,
                    onClick = { onThemeModeChange(ThemeMode.DARK) },
                    modifier = Modifier.weight(1f)
                )

                ThemeOptionPill(
                    icon = Icons.Default.LightMode,
                    label = "Light Glass",
                    isSelected = themeMode == ThemeMode.LIGHT,
                    onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION: Login (Add profile / Log out matching Screenshot 3)
            SettingsSectionHeader("Login")
            Text(
                text = "Add profile",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0066FF),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        onOpenUserSwitcher()
                    }
                    .padding(vertical = 10.dp)
                    .testTag("settings_add_profile_button")
            )
            Text(
                text = "Log out @${currentUser?.username ?: "user"}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF2D55),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        onOpenVetaAuth()
                    }
                    .padding(vertical = 10.dp)
                    .testTag("settings_logout_button")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Reset Database Option
            OutlinedButton(
                onClick = {
                    onResetData()
                    Toast.makeText(context, "Database re-seeded to defaults!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF2D55)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Data & Sample Content")
            }

            Spacer(modifier = Modifier.height(24.dp))
            }

            // RIGHT-SIDE FLOATING QUICK-JUMP SLIDE RAIL (Long Page Side-Slide Index)
            val jumpAnchors = listOf(
                "TOP" to 0f,
                "MAP" to 0.08f,
                "BG" to 0.18f,
                "SEC" to 0.28f,
                "ACC" to 0.38f,
                "PAY" to 0.52f,
                "PRV" to 0.66f,
                "APP" to 0.82f,
                "END" to 1.0f
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (glass.isDark) Color(0xDD050914) else Color(0xEEFFFFFF))
                    .border(1.dp, Color(0xFF0066FF).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("settings_right_side_slide_rail"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                jumpAnchors.forEach { (tag, ratio) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0066FF).copy(alpha = 0.16f))
                            .clickable {
                                selectedSlideCategory = "All"
                                coroutineScope.launch {
                                    val targetPx = (scrollState.maxValue * ratio).toInt()
                                    scrollState.animateScrollTo(targetPx)
                                }
                            }
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tag,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (glass.isDark) Color(0xFF00E5FF) else Color(0xFF0066FF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsArchitectureMapGraph(
    selectedNodeIndex: Int,
    appBackgroundStyle: String,
    isSuperAdmin: Boolean,
    onSelectNode: (Int) -> Unit
) {
    val glass = LocalLiquidGlass.current
    val nodes = listOf(
        Triple("0. VETA Core", "Spotpix Engine v4.5 • 120FPS Liquid Glass", Color(0xFF0066FF)),
        Triple("1. Account & Auth", "Gmail OAuth • 2FA • Multi-Profile Switch", Color(0xFF38BDF8)),
        Triple("2. Wallet & Real UPI", "GPay / Paytm / PhonePe → vanij7@fam", Color(0xFF10B981)),
        Triple("3. Anti-Hack Shield", "HMAC-SHA256 • Replay Block • Rate Limit", Color(0xFFF59E0B)),
        Triple("4. App Structure", "Style: $appBackgroundStyle • 5 UI Layers", Color(0xFF8B5CF6)),
        Triple("5. Super Admin", if (isSuperAdmin) "Master Control Active ✓" else "Tap to Authenticate Admin", Color(0xFFEC4899))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (glass.isDark) Color(0xFF050B18) else Color(0xFFF1F5F9))
            .border(1.5.dp, Color(0xFF0066FF).copy(alpha = 0.65f), RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("settings_architecture_map_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🗺️ Interactive Settings Map & App Graph",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = glass.textPrimary
                )
                Text(
                    text = "Visual architecture of Spotpix by VETA — tap any node below to inspect or jump",
                    fontSize = 10.sp,
                    color = glass.textSecondary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.18f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("LIVE GRAPH", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive Node-and-Edge Architecture Graph Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF030712))
                .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(14.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width * 0.5f, size.height * 0.5f)
                val satellitePositions = listOf(
                    Offset(size.width * 0.16f, size.height * 0.24f),
                    Offset(size.width * 0.84f, size.height * 0.24f),
                    Offset(size.width * 0.16f, size.height * 0.78f),
                    Offset(size.width * 0.84f, size.height * 0.78f),
                    Offset(size.width * 0.5f, size.height * 0.16f)
                )
                val nodeColors = listOf(
                    Color(0xFF38BDF8),
                    Color(0xFF10B981),
                    Color(0xFFF59E0B),
                    Color(0xFF8B5CF6),
                    Color(0xFFEC4899)
                )

                // Draw subtle grid lines
                for (i in 1..4) {
                    val y = size.height * (i / 5f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                // Draw connecting edges from VETA Core to each node
                satellitePositions.forEachIndexed { idx, pos ->
                    val isHighlighted = selectedNodeIndex == (idx + 1)
                    drawLine(
                        color = if (isHighlighted) nodeColors[idx] else Color(0xFF0066FF).copy(alpha = 0.55f),
                        start = center,
                        end = pos,
                        strokeWidth = if (isHighlighted) 5f else 2.5f,
                        pathEffect = if (isHighlighted) null else PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )
                    drawCircle(
                        color = nodeColors[idx].copy(alpha = 0.25f),
                        radius = if (isHighlighted) 24f else 18f,
                        center = pos
                    )
                    drawCircle(
                        color = nodeColors[idx],
                        radius = if (isHighlighted) 11f else 8f,
                        center = pos
                    )
                }

                // Draw Center VETA Core Hub
                drawCircle(
                    color = Color(0xFF0066FF).copy(alpha = 0.3f),
                    radius = 32f,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = 16f,
                    center = center,
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = Color(0xFF0066FF),
                    radius = 12f,
                    center = center
                )
            }

            // Overlay Node Labels on the Graph
            Text(
                text = "👤 Auth",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 8.dp)
                    .clickable { onSelectNode(1) }
            )
            Text(
                text = "⚡ Admin",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEC4899),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .clickable { onSelectNode(5) }
            )
            Text(
                text = "💳 UPI/Wallet",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 12.dp, top = 8.dp)
                    .clickable { onSelectNode(2) }
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC050914))
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp))
                    .clickable { onSelectNode(0) }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("VETA CORE", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            Text(
                text = "🛡️ Anti-Hack",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF59E0B),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 8.dp)
                    .clickable { onSelectNode(3) }
            )
            Text(
                text = "🎨 Structure",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5CF6),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 8.dp)
                    .clickable { onSelectNode(4) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Side-Slide Node Selector Pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(nodes.size) { idx ->
                val (title, subtitle, color) = nodes[idx]
                val isSelected = selectedNodeIndex == idx
                Column(
                    modifier = Modifier
                        .width(155.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) color.copy(alpha = 0.2f) else glass.glassSurfaceElevated)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) color else glass.textTertiary.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectNode(idx) }
                        .padding(10.dp)
                ) {
                    Text(title, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = color)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(subtitle, fontSize = 9.sp, color = glass.textSecondary, maxLines = 2, lineHeight = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AppBackgroundAndStructureCard(
    currentStyle: String,
    onStyleSelect: (String) -> Unit
) {
    val glass = LocalLiquidGlass.current
    val styles = listOf(
        Triple("Cobalt Liquid Glass", "Signature VETA #0066FF", Color(0xFF0066FF)),
        Triple("OLED Pitch Black", "True #000000 Battery Saver", Color(0xFF111827)),
        Triple("Cyber Aurora Cyan", "Electric Cyan Mesh", Color(0xFF00E5FF)),
        Triple("Royal Emerald Vault", "Financial Security Glass", Color(0xFF10B981)),
        Triple("Minimal Daylight Frost", "Clean Alabaster Light", Color(0xFF38BDF8))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(glass.glassSurfaceElevated)
            .border(1.dp, glass.glassBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("settings_background_structure_card")
    ) {
        Text(
            text = "🎨 App Background & UI Structure Map",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = glass.textPrimary
        )
        Text(
            text = "Slide & tap to change the live app background and view the 5-layer Spotpix UI structure",
            fontSize = 10.sp,
            color = glass.textSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(styles) { (styleName, desc, swatchColor) ->
                val isSelected = currentStyle == styleName
                Column(
                    modifier = Modifier
                        .width(142.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) swatchColor.copy(alpha = 0.22f) else glass.glassSurface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) swatchColor else glass.textTertiary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onStyleSelect(styleName) }
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSelected) "ACTIVE ✓" else "APPLY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = swatchColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(styleName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary, maxLines = 1)
                    Text(desc, fontSize = 9.sp, color = glass.textSecondary, maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual 5-Layer App Structure Diagram
        Text(
            text = "📐 Spotpix 5-Layer App Structure (Easy Overview):",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0066FF)
        )
        Spacer(modifier = Modifier.height(6.dp))

        val structureLayers = listOf(
            "Layer 1: Top Header Bar" to "+ Create • Spotpix by VETA • ♡ Notifications • Direct Chat",
            "Layer 2: Story Carousel" to "Your Story (+) • Gradient Rings • Suggested Creators (+o)",
            "Layer 3: Core Media Engine" to "Home Feed • 4K Vertical Reels (Friends Tab) • 3x3 Explore AI",
            "Layer 4: Live Earnings Pill" to "Dynamic Island Watch Timer • $1/1K Views • Wallet Sync",
            "Layer 5: Liquid Glass Dock" to "Home • Explore • Studio (+) • Reels • Profile (Hold to Switch)"
        )
        structureLayers.forEachIndexed { idx, (layerTitle, layerDesc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(glass.glassSurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(layerTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
                    Text(layerDesc, fontSize = 9.sp, color = glass.textSecondary)
                }
                Text(
                    text = "L${idx + 1}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E5FF)
                )
            }
        }
    }
}

@Composable
private fun VetaAntiHackSecurityCard(
    twoFactorEnabled: Boolean,
    onTwoFactorChange: (Boolean) -> Unit
) {
    val glass = LocalLiquidGlass.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (glass.isDark) Color(0xFF061512) else Color(0xFFECFDF5))
            .border(1.5.dp, Color(0xFF10B981).copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("settings_anti_hack_security_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "🛡️ VETA Anti-Hack & Payment Shield",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = glass.textPrimary
                    )
                    Text(
                        text = "Zero-Trust Protection • Data & UPI Cannot Be Cracked",
                        fontSize = 10.sp,
                        color = Color(0xFF10B981)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF10B981))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("PROTECTED", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "• HMAC-SHA256 Cryptographic Signing on every UPI order to vanij7@fam\n" +
                "• Anti-Replay UTR Registry: Used 12-digit UTR numbers can never be reused\n" +
                "• Entropy & Brute-Force Lockout: Blocks sequential/synthetic UTRs & locks after 3 failed tries\n" +
                "• Zero-Trust Firestore Security Rules: Strict owner & Super Admin UID verification",
            fontSize = 10.sp,
            color = glass.textSecondary,
            lineHeight = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsSwitchRow(
            title = "Two-Factor Authentication (2FA) & Session Lock",
            subtitle = "Require cryptographic verification when switching or signing in",
            checked = twoFactorEnabled,
            onCheckedChange = onTwoFactorChange
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    val glass = LocalLiquidGlass.current
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = glass.textSecondary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingValue: String? = null,
    query: String = "",
    onClick: () -> Unit
) {
    if (query.isNotBlank() && !title.contains(query, ignoreCase = true) && (subtitle?.contains(query, ignoreCase = true) != true)) {
        return
    }
    val glass = LocalLiquidGlass.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = glass.textPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = glass.textPrimary
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = glass.textSecondary
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!trailingValue.isNullOrBlank()) {
                Text(
                    text = trailingValue,
                    fontSize = 12.sp,
                    color = glass.textSecondary,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = glass.textTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val glass = LocalLiquidGlass.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(glass.glassSurfaceElevated, RoundedCornerShape(14.dp))
            .border(1.dp, glass.glassBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = glass.textPrimary)
            Text(subtitle, fontSize = 11.sp, color = glass.textSecondary, lineHeight = 15.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ThemeOptionPill(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlass.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) glass.accentPrimary else glass.glassSurfaceElevated)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color.White else glass.textPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else glass.textPrimary
        )
    }
}
