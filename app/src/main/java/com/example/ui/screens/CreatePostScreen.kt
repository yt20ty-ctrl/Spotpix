package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserEntity
import com.example.ui.components.CreateMode
import com.example.ui.components.InstalkFilterPresets
import com.example.ui.components.buildMediaColorFilter
import com.example.ui.theme.LocalLiquidGlass

@Composable
fun CreatePostScreen(
    currentUser: UserEntity?,
    initialMode: CreateMode,
    defaultUploadQuality: String,
    defaultHideLikes: Boolean,
    onModeChange: (CreateMode) -> Unit,
    onCreateStory: (
        mediaUrl: String,
        filterName: String,
        overlayText: String,
        pollQuestion: String?,
        pollOptionA: String?,
        pollOptionB: String?
    ) -> Unit,
    onCreatePost: (
        mediaUrl: String,
        caption: String,
        location: String,
        filterName: String,
        overlayText: String,
        mediaAspect: Float,
        uploadQuality: String,
        hideLikes: Boolean,
        isVideo: Boolean
    ) -> Unit,
    onCreateReel: (
        videoUrl: String,
        caption: String,
        audioTitle: String,
        filterName: String,
        overlayText: String,
        uploadQuality: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlass.current
    val context = LocalContext.current

    val presetImages = listOf(
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=800&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=800&auto=format&fit=crop&q=80"
    )

    var selectedMediaUrl by remember { mutableStateOf(presetImages.first()) }
    var selectedFilter by remember { mutableStateOf("Original") }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var selectedAspectLabel by remember(initialMode) {
        mutableStateOf(
            when (initialMode) {
                CreateMode.STORY, CreateMode.REEL -> "9:16"
                CreateMode.VIDEO -> "16:9"
                CreateMode.POST -> "4:5"
            }
        )
    }

    var overlayText by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Tokyo, Japan") }
    var audioTitle by remember { mutableStateOf("${currentUser?.username ?: "creator"} • Liquid Glass Original Audio") }

    // Story Poll inputs
    var includePoll by remember { mutableStateOf(false) }
    var pollQuestion by remember { mutableStateOf("Love this new Instalk look?") }
    var pollOptionA by remember { mutableStateOf("Yes 🔥") }
    var pollOptionB by remember { mutableStateOf("Obsessed 💎") }

    // Upload Settings State
    var uploadQuality by remember(defaultUploadQuality) { mutableStateOf(defaultUploadQuality) }
    var hideLikes by remember(defaultHideLikes) { mutableStateOf(defaultHideLikes) }
    var showAdjustments by remember { mutableStateOf(false) }
    var showUploadSettings by remember { mutableStateOf(true) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaUrl = uri.toString()
            Toast.makeText(context, "Media loaded into Studio!", Toast.LENGTH_SHORT).show()
        }
    }

    val suggestedTags = listOf("#Instalk", "#LiquidGlass", "#CreatorStudio", "#Photography", "#Reels", "#4KUltra")
    val quickStickers = listOf("✨ New Drop", "🔥 Hot Reel", "📍 Tokyo Vibes", "💎 Liquid Glass", "🎧 Sound On")

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 130.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Studio Title & Active Account Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Instalk Create Studio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = glass.textPrimary
                )
                Text(
                    text = "Posting as @${currentUser?.username ?: "creator"} • ${initialMode.badgeText}",
                    fontSize = 11.sp,
                    color = glass.accentPrimary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(glass.glassSurfaceElevated)
                    .border(1.dp, glass.glassBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = uploadQuality,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = glass.accentPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4-Way Mode Switcher Pill (Story | Feed Post | Reel | Video)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, CircleShape)
                .background(glass.glassSurfaceElevated, CircleShape)
                .border(1.dp, glass.glassBorder, CircleShape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CreateMode.entries.forEach { mode ->
                val isSelected = mode == initialMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (isSelected) glass.accentPrimary else Color.Transparent)
                        .clickable { onModeChange(mode) }
                        .padding(vertical = 8.dp)
                        .testTag("studio_mode_${mode.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else glass.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Media Preview Canvas with Active Filter & Overlay
        val previewHeight = when (selectedAspectLabel) {
            "9:16" -> 330.dp
            "4:5" -> 290.dp
            "16:9" -> 210.dp
            else -> 260.dp
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(previewHeight)
                .shadow(14.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(glass.glassSurfaceElevated)
                .border(1.2.dp, glass.glassBorder, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = selectedMediaUrl,
                contentDescription = "Selected Media Preview",
                colorFilter = remember(selectedFilter, brightness, contrast) {
                    buildMediaColorFilter(selectedFilter, brightness, contrast)
                },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Top Left Active Filter & Aspect Badge
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xBB0F172A))
                        .border(1.dp, Color(0x6600E5FF), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "✨ $selectedFilter • $selectedAspectLabel",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Live Overlay Text Sticker on Canvas
            if (overlayText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xCC0F172A), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x8800E5FF), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = overlayText,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }

            // Bottom Controls inside Canvas: Gallery Picker & Adjustments Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Aspect Ratio Selector Pills
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xCC0F172A))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("1:1", "4:5", "9:16", "16:9").forEach { ratio ->
                        val active = selectedAspectLabel == ratio
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (active) Color(0xFF0284C7) else Color.Transparent)
                                .clickable { selectedAspectLabel = ratio }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = ratio,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xDD0F172A)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("pick_from_gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gallery", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sample Media Assets Row
        Text(
            text = "Sample Media Assets",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = glass.textSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(presetImages) { url ->
                val isSelected = url == selectedMediaUrl
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) glass.accentPrimary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedMediaUrl = url }
                ) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Filters & Effects Carousel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Filters & Shader Presets",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = glass.textPrimary
            )
            Text(
                text = if (showAdjustments) "Hide Sliders ▲" else "Adjust Tone ▼",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = glass.accentPrimary,
                modifier = Modifier.clickable { showAdjustments = !showAdjustments }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(InstalkFilterPresets) { preset ->
                val isSelected = preset.name == selectedFilter
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) preset.accentColor.copy(alpha = 0.2f)
                            else glass.glassSurfaceElevated
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) preset.accentColor else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedFilter = preset.name }
                        .padding(8.dp)
                        .testTag("filter_preset_${preset.name}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        AsyncImage(
                            model = selectedMediaUrl,
                            contentDescription = preset.name,
                            colorFilter = remember(preset.name) { buildMediaColorFilter(preset.name) },
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = preset.name,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) glass.accentPrimary else glass.textPrimary
                    )
                }
            }
        }

        // Brightness & Contrast Fine-Tuning Sliders
        AnimatedVisibility(visible = showAdjustments) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(glass.glassSurfaceElevated, RoundedCornerShape(16.dp))
                    .border(1.dp, glass.glassBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Brightness", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = glass.textPrimary)
                    Text("${brightness.toInt()}", fontSize = 12.sp, color = glass.accentPrimary)
                }
                Slider(
                    value = brightness,
                    onValueChange = { brightness = it },
                    valueRange = -40f..40f
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Contrast", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = glass.textPrimary)
                    Text("%.2fx".format(contrast), fontSize = 12.sp, color = glass.accentPrimary)
                }
                Slider(
                    value = contrast,
                    onValueChange = { contrast = it },
                    valueRange = 0.7f..1.4f
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Text Overlay & Quick Stickers
        OutlinedTextField(
            value = overlayText,
            onValueChange = { overlayText = it },
            label = { Text("Text Overlay / Sticker Banner") },
            placeholder = { Text("Add floating text on top of your media...") },
            leadingIcon = {
                Icon(Icons.Default.TextFields, contentDescription = null, tint = glass.accentPrimary)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_overlay_text_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = glass.textPrimary,
                unfocusedTextColor = glass.textPrimary,
                focusedBorderColor = glass.accentPrimary,
                unfocusedBorderColor = glass.textTertiary.copy(alpha = 0.3f),
                focusedContainerColor = glass.glassSurfaceElevated,
                unfocusedContainerColor = glass.glassSurface
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(quickStickers) { sticker ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(glass.glassSurfaceElevated)
                        .clickable { overlayText = sticker }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(text = sticker, fontSize = 11.sp, color = glass.textPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mode-Specific Inputs (Story Poll vs Caption/Location/Audio)
        if (initialMode == CreateMode.STORY) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(glass.glassSurfaceElevated, RoundedCornerShape(18.dp))
                    .border(1.dp, glass.glassBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Interactive Story Poll",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = glass.textPrimary
                        )
                        Text(
                            text = "Let viewers vote directly on your story",
                            fontSize = 11.sp,
                            color = glass.textSecondary
                        )
                    }
                    Switch(
                        checked = includePoll,
                        onCheckedChange = { includePoll = it }
                    )
                }

                if (includePoll) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pollQuestion,
                        onValueChange = { pollQuestion = it },
                        label = { Text("Poll Question") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pollOptionA,
                            onValueChange = { pollOptionA = it },
                            label = { Text("Option A") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = pollOptionB,
                            onValueChange = { pollOptionB = it },
                            label = { Text("Option B") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }
            }
        } else {
            // Caption Input for Post, Reel, or Video
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Write a caption...") },
                placeholder = { Text("Share the story behind this moment...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_caption_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = glass.textPrimary,
                    unfocusedTextColor = glass.textPrimary,
                    focusedBorderColor = glass.accentPrimary,
                    unfocusedBorderColor = glass.textTertiary.copy(alpha = 0.3f),
                    focusedContainerColor = glass.glassSurfaceElevated,
                    unfocusedContainerColor = glass.glassSurface
                ),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Hashtag suggestion chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(suggestedTags) { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(glass.glassSurfaceElevated)
                            .clickable {
                                if (!caption.contains(tag)) {
                                    caption = if (caption.isBlank()) tag else "$caption $tag"
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = tag, fontSize = 11.sp, color = glass.accentPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (initialMode == CreateMode.REEL || initialMode == CreateMode.VIDEO) {
                OutlinedTextField(
                    value = audioTitle,
                    onValueChange = { audioTitle = it },
                    label = { Text("Audio Track / Sound Mixer") },
                    leadingIcon = {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = glass.accentPrimary)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_audio_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = glass.textPrimary,
                        unfocusedTextColor = glass.textPrimary,
                        focusedBorderColor = glass.accentPrimary,
                        unfocusedBorderColor = glass.textTertiary.copy(alpha = 0.3f),
                        focusedContainerColor = glass.glassSurfaceElevated,
                        unfocusedContainerColor = glass.glassSurface
                    )
                )
            } else {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Add Location") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = glass.accentPrimary)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_location_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = glass.textPrimary,
                        unfocusedTextColor = glass.textPrimary,
                        focusedBorderColor = glass.accentPrimary,
                        unfocusedBorderColor = glass.textTertiary.copy(alpha = 0.3f),
                        focusedContainerColor = glass.glassSurfaceElevated,
                        unfocusedContainerColor = glass.glassSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Upload Settings Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(glass.glassSurfaceElevated, RoundedCornerShape(18.dp))
                .border(1.dp, glass.glassBorder, RoundedCornerShape(18.dp))
                .padding(14.dp)
                .testTag("upload_settings_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showUploadSettings = !showUploadSettings },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HighQuality,
                        contentDescription = null,
                        tint = glass.accentPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Upload & Privacy Settings",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = glass.textPrimary
                        )
                        Text(
                            text = "Quality: $uploadQuality • Hide Likes: ${if (hideLikes) "On" else "Off"}",
                            fontSize = 11.sp,
                            color = glass.textSecondary
                        )
                    }
                }
                Icon(
                    imageVector = if (showUploadSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = glass.textSecondary
                )
            }

            AnimatedVisibility(visible = showUploadSettings) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "Media Upload Resolution",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = glass.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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
                                    .clickable { uploadQuality = q }
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hide Like & View Counts",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = glass.textPrimary
                            )
                            Text(
                                text = "Only you will see the total number of likes",
                                fontSize = 10.sp,
                                color = glass.textSecondary
                            )
                        }
                        Switch(
                            checked = hideLikes,
                            onCheckedChange = { hideLikes = it }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Publish Button
        Button(
            onClick = {
                val finalCaption = if (caption.isNotBlank()) caption else "✨ Created in Instalk Studio #Instalk #LiquidGlass"
                when (initialMode) {
                    CreateMode.STORY -> {
                        onCreateStory(
                            selectedMediaUrl,
                            selectedFilter,
                            overlayText,
                            if (includePoll) pollQuestion else null,
                            if (includePoll) pollOptionA else null,
                            if (includePoll) pollOptionB else null
                        )
                        Toast.makeText(context, "Story published to your ring!", Toast.LENGTH_SHORT).show()
                    }
                    CreateMode.REEL -> {
                        onCreateReel(
                            selectedMediaUrl,
                            finalCaption,
                            audioTitle,
                            selectedFilter,
                            overlayText,
                            uploadQuality
                        )
                        Toast.makeText(context, "Reel published in $uploadQuality!", Toast.LENGTH_SHORT).show()
                    }
                    CreateMode.VIDEO -> {
                        onCreatePost(
                            selectedMediaUrl,
                            finalCaption,
                            location,
                            selectedFilter,
                            overlayText,
                            1.77f,
                            uploadQuality,
                            hideLikes,
                            true
                        )
                        Toast.makeText(context, "HD Video published to Feed!", Toast.LENGTH_SHORT).show()
                    }
                    CreateMode.POST -> {
                        onCreatePost(
                            selectedMediaUrl,
                            finalCaption,
                            location,
                            selectedFilter,
                            overlayText,
                            1.0f,
                            uploadQuality,
                            hideLikes,
                            false
                        )
                        Toast.makeText(context, "Post shared to your Feed!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(10.dp, RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF00E5FF), Color(0xFF2563EB), Color(0xFF8B5CF6))
                    ),
                    RoundedCornerShape(20.dp)
                )
                .testTag("create_publish_button")
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (initialMode) {
                    CreateMode.STORY -> "Add to Your Story ($uploadQuality)"
                    CreateMode.REEL -> "Share Reel ($uploadQuality)"
                    CreateMode.VIDEO -> "Publish Video ($uploadQuality)"
                    CreateMode.POST -> "Share to Feed ($uploadQuality)"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}
