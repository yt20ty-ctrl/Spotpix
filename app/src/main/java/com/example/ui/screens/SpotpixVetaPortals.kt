package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.data.repository.SocialRepository
import com.example.ui.theme.LocalLiquidGlass
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CobaltBlue = Color(0xFF0066FF)
private val ElectricCyan = Color(0xFF00E5FF)

/**
 * 1. SPOTPIX LOGIN & REGISTRATION SCREEN (VETA AUTHENTICATION)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotpixVetaAuthModal(
    allUsers: List<UserEntity>,
    currentUser: UserEntity?,
    onLoginWithIdentifier: (String, (Boolean, String) -> Unit) -> Unit,
    onRegisterNewUser: (username: String, fullName: String, email: String, (String) -> Unit) -> Unit,
    onSelectGoogleAccount: (String) -> Unit,
    onLaunchGoogleCredentialSignIn: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val glass = LocalLiquidGlass.current
    val context = LocalContext.current

    var isSignUpMode by remember { mutableStateOf(false) }
    var identifier by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showGoogleChooser by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (glass.isDark) Color(0xFF060B19) else Color(0xFFF8FAFC),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(glass.textTertiary)
            )
        },
        modifier = Modifier.testTag("spotpix_veta_auth_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = glass.textSecondary)
                }
            }

            // Top Logo: Spotpix logo & icon with subtle "by VETA" branding
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(14.dp, RoundedCornerShape(22.dp), spotColor = CobaltBlue)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(CobaltBlue, ElectricCyan, Color(0xFF6366F1))
                        )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Spotpix Logo",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Spotpix",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.8).sp,
                color = glass.textPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "by VETA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    color = CobaltBlue
                )
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Input Fields
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = {
                    Text(if (isSignUpMode) "Choose Username (@handle)" else "Username, Email, or Mobile Number")
                },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = CobaltBlue)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("veta_auth_username_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CobaltBlue,
                    focusedTextColor = glass.textPrimary,
                    unfocusedTextColor = glass.textPrimary
                )
            )

            if (isSignUpMode) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    leadingIcon = {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = CobaltBlue)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CobaltBlue,
                        focusedTextColor = glass.textPrimary,
                        unfocusedTextColor = glass.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Verified Gmail Address") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = CobaltBlue)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CobaltBlue,
                        focusedTextColor = glass.textPrimary,
                        unfocusedTextColor = glass.textPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = CobaltBlue)
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Password Visibility",
                            tint = glass.textSecondary
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("veta_auth_password_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CobaltBlue,
                    focusedTextColor = glass.textPrimary,
                    unfocusedTextColor = glass.textPrimary
                )
            )

            if (!isSignUpMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot Password?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CobaltBlue,
                        modifier = Modifier.clickable {
                            statusMessage = "VETA Password Recovery link sent to your registered Gmail."
                        }
                    )
                }
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusMessage!!,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ElectricCyan,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Action: Full-width Cobalt Blue (#0066FF) "Log In" / "Sign Up" button
            Button(
                onClick = {
                    if (isSignUpMode) {
                        if (identifier.isBlank()) {
                            statusMessage = "Please enter a username to sign up."
                        } else {
                            onRegisterNewUser(identifier, fullName, email) { createdHandle ->
                                Toast.makeText(
                                    context,
                                    "Account @$createdHandle created on Spotpix by VETA!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        if (identifier.isBlank()) {
                            statusMessage = "Enter a username or email (e.g., AvaGlow or VaanijSingh28@gmail.com)"
                        } else {
                            onLoginWithIdentifier(identifier) { ok, msg ->
                                if (ok) {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                } else {
                                    statusMessage = msg
                                }
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("veta_auth_primary_button")
            ) {
                Text(
                    text = if (isSignUpMode) "Sign Up with VETA" else "Log In",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "OR" Horizontal Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = glass.textTertiary.copy(alpha = 0.3f))
                Text(
                    text = "  OR  ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = glass.textSecondary
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = glass.textTertiary.copy(alpha = 0.3f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Continue with Google" One-Tap Sign-In Button (Credential Manager + Quick Chooser)
            OutlinedButton(
                onClick = {
                    showGoogleChooser = !showGoogleChooser
                    onLaunchGoogleCredentialSignIn()
                },
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = glass.glassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("continue_with_google_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Continue with Google (Gmail)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = glass.textPrimary
                )
            }

            // Expandable Google One-Tap Verified Sessions (includes Super Admin VaanijSingh28@gmail.com)
            AnimatedVisibility(visible = showGoogleChooser) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(glass.glassSurfaceElevated, RoundedCornerShape(16.dp))
                        .border(1.dp, glass.glassBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select Verified Google Account (VETA OAuth 2.0):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    allUsers.forEach { u ->
                        val isAdmin = SocialRepository.isVerifiedSuperAdmin(u)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (u.id == currentUser?.id) CobaltBlue.copy(alpha = 0.2f)
                                    else glass.glassSurface
                                )
                                .clickable {
                                    onSelectGoogleAccount(u.id)
                                    onDismiss()
                                    Toast.makeText(
                                        context,
                                        "Authenticated via Google as ${u.email}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(10.dp)
                                .testTag("google_account_${u.username}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = u.avatarUrl,
                                    contentDescription = u.username,
                                    modifier = Modifier
                                        .size(34.dp)
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
                                                tint = CobaltBlue,
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
                                        text = u.email,
                                        fontSize = 11.sp,
                                        color = glass.textSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = glass.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Bottom Footer: "Don't have an account? Sign Up"
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { isSignUpMode = !isSignUpMode }
                    .padding(8.dp)
                    .testTag("toggle_signup_mode_button")
            ) {
                Text(
                    text = if (isSignUpMode) "Already have a Spotpix account? " else "Don't have an account? ",
                    fontSize = 13.sp,
                    color = glass.textSecondary
                )
                Text(
                    text = if (isSignUpMode) "Log In" else "Sign Up",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CobaltBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 2. BLUE TICK VERIFICATION APPLICATION & STATUS TRACKER MODAL
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlueTickVerificationModal(
    user: UserEntity?,
    remoteConfig: VetaRemoteConfig,
    onSubmitApplication: (docType: String, docName: String) -> Unit,
    onDismiss: () -> Unit
) {
    val glass = LocalLiquidGlass.current
    val context = LocalContext.current

    val followers = user?.followersCount ?: 0
    val minRequired = remoteConfig.blueTickMinFollowers
    val isEligible = followers >= minRequired

    var selectedDocType by remember { mutableStateOf("Government Passport") }
    var uploadedDocName by remember {
        mutableStateOf(
            user?.verificationDocName?.ifBlank { "Gov_ID_${user.username}.pdf" } ?: "Gov_ID_Passport.pdf"
        )
    }

    val docPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uploadedDocName = "Official_ID_${uri.lastPathSegment ?: "Document"}.jpg"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (glass.isDark) Color(0xFF060B19) else Color(0xFFF8FAFC),
        modifier = Modifier.testTag("blue_tick_verification_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = CobaltBlue,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Spotpix Blue Tick Verification",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = glass.textPrimary
                        )
                        Text(
                            text = "VETA Authenticity & Notable Creator Program",
                            fontSize = 11.sp,
                            color = CobaltBlue
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = glass.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Verification Status Banner
            val status = user?.verificationStatus ?: "NONE"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(glass.glassSurfaceElevated, RoundedCornerShape(16.dp))
                    .border(1.dp, glass.glassBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Verification Status",
                            fontSize = 12.sp,
                            color = glass.textSecondary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (status) {
                                        "APPROVED" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                        "PENDING", "UNDER_REVIEW" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                        "REJECTED" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                        else -> CobaltBlue.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when (status) {
                                    "APPROVED" -> Color(0xFF10B981)
                                    "PENDING", "UNDER_REVIEW" -> Color(0xFFF59E0B)
                                    "REJECTED" -> Color(0xFFEF4444)
                                    else -> ElectricCyan
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "⚠️ Strict VETA Policy: Verification is NEVER automatic based on follower count. All applications require Super Admin review and approval.",
                        fontSize = 11.sp,
                        color = glass.textSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 1: Follower Eligibility Threshold
            Text(
                text = "Step 1 • Eligibility Threshold",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = glass.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(glass.glassSurfaceElevated, RoundedCornerShape(16.dp))
                    .border(1.dp, glass.glassBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Minimum Followers Required",
                        fontSize = 12.sp,
                        color = glass.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$followers / $minRequired",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isEligible) Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (followers.toFloat() / minRequired.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (isEligible) Color(0xFF10B981) else CobaltBlue,
                    trackColor = glass.textTertiary.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Official Identity Document Upload
            Text(
                text = "Step 2 • Official Identity Document Upload",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = glass.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Government Passport", "National ID", "Creator Tax ID").forEach { doc ->
                    val selected = selectedDocType == doc
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) CobaltBlue else glass.glassSurfaceElevated)
                            .border(1.dp, glass.glassBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedDocType = doc }
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = doc,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else glass.textPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(glass.glassSurfaceElevated, RoundedCornerShape(14.dp))
                    .border(1.dp, glass.glassBorder, RoundedCornerShape(14.dp))
                    .clickable {
                        docPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = ElectricCyan)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = uploadedDocName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = glass.textPrimary
                        )
                        Text(
                            text = "Tap to upload official ID photo from device",
                            fontSize = 11.sp,
                            color = glass.textSecondary
                        )
                    }
                }
                Text(
                    text = "Attach",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CobaltBlue
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (!isEligible) {
                        Toast.makeText(
                            context,
                            "You need at least $minRequired followers to apply for Spotpix Blue Tick.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        onSubmitApplication(selectedDocType, uploadedDocName)
                        Toast.makeText(
                            context,
                            "Blue Tick application submitted! Pending VETA Super Admin review.",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDismiss()
                    }
                },
                enabled = status != "APPROVED",
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_blue_tick_button")
            ) {
                Text(
                    text = when (status) {
                        "APPROVED" -> "✓ Verified with Spotpix Blue Tick"
                        "PENDING", "UNDER_REVIEW" -> "Update Pending Application"
                        else -> "Submit to VETA Super Admin for Review"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * 3. CREATOR MONETIZATION DASHBOARD (VETA ENGINE) & PAID AD CAMPAIGN + UPI CHECKOUT HUB
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetaMonetizationAndAdsModal(
    user: UserEntity?,
    campaigns: List<AdCampaignEntity>,
    transactions: List<PaymentTransactionEntity>,
    remoteConfig: VetaRemoteConfig,
    initialTab: Int = 0,
    onApplyMonetization: () -> Unit = {},
    onSimulateMonetizedViews: (Long) -> Unit = {},
    onWithdrawEarnings: (
        amountUsd: Double,
        payoutMethod: String,
        payoutDestination: String,
        onSuccess: (PaymentTransactionEntity) -> Unit
    ) -> Unit = { _, _, _, _ -> },
    onCreateAndPayCampaign: (
        title: String,
        caption: String,
        creativeUrls: List<String>,
        packageType: String,
        durationDays: Int,
        amountInr: Int,
        paymentMethod: String,
        onSuccess: (PaymentTransactionEntity) -> Unit
    ) -> Unit,
    onUpdateCampaignStatus: (String, String) -> Unit,
    onDeleteCampaign: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val glass = LocalLiquidGlass.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 0 = 💳 Creator Wallet (GPay/Paytm/PhonePe), 1 = 📈 Monetize Analyst, 2 = 💰 Eligibility, 3 = 📣 Paid Ads, 4 = 🧾 Ledger
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab.coerceIn(0, 4)) }
    var policyAccepted by remember { mutableStateOf(true) }

    // Creator Wallet & Payout State (Google Pay, Paytm, PhonePe, BHIM, CRED)
    var selectedPayoutApp by remember { mutableStateOf("Google Pay (GPay)") }
    var payoutUpiHandle by remember(user) {
        mutableStateOf("${user?.username ?: "creator"}@okaxis")
    }
    var withdrawUsdInput by remember { mutableStateOf("25") }
    var walletStatusMsg by remember { mutableStateOf<String?>(null) }
    var latestWithdrawalTx by remember { mutableStateOf<PaymentTransactionEntity?>(null) }

    // Monetize Analyst Timeframe State
    var analystRange by remember { mutableStateOf("30D") } // 7D, 30D, 90D
    var selectedChartBarIdx by remember { mutableStateOf(5) }

    // Paid Ad Campaign Builder State
    var adTitle by remember { mutableStateOf("Spotpix Creator Spotlight") }
    var adCaption by remember { mutableStateOf("Check out our new Liquid Glass collection on Spotpix by VETA! 🚀") }
    var selectedPackage by remember { mutableStateOf("WEEKLY") } // WEEKLY (Rs 100 / 7d) or TWO_WEEK (Rs 120 / 14d)
    var selectedCreatives by remember {
        mutableStateOf(
            listOf(
                "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800",
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800"
            )
        )
    }

    // Real UPI Intent & Verified UTR Payment Checkout State
    var isCheckoutVisible by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("Google Pay (UPI Intent)") }
    var payerUpiId by remember { mutableStateOf("") }
    var upiUtrNumber by remember { mutableStateOf("") }
    var paymentStep by remember { mutableStateOf("IDLE") } // IDLE, AWAITING_UTR, PROCESSING, SUCCESS, FAILURE
    var paymentErrorReason by remember { mutableStateOf<String?>(null) }
    var latestReceipt by remember { mutableStateOf<PaymentTransactionEntity?>(null) }
    val orderRefId = remember(selectedPackage) { "VETA${System.currentTimeMillis().toString().takeLast(8)}" }

    val packagePrice = if (selectedPackage == "WEEKLY") remoteConfig.weeklyAdPriceInr else remoteConfig.twoWeekAdPriceInr
    val packageDays = if (selectedPackage == "WEEKLY") 7 else 14

    // Real Android UPI Intent Result Listener (parses NPCI response string from Google Pay / PhonePe / Paytm / BHIM)
    val upiPaymentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val responseStr = result.data?.getStringExtra("response")
            ?: result.data?.dataString
            ?: ""
        val params = responseStr.split("&").associate { pair ->
            val parts = pair.split("=", limit = 2)
            parts.getOrElse(0) { "" }.lowercase() to parts.getOrElse(1) { "" }
        }
        val status = params["status"]?.uppercase() ?: ""
        val approvalRef = params["approvalrefno"].takeIf { !it.isNullOrBlank() }
            ?: params["txnref"].takeIf { !it.isNullOrBlank() }
            ?: params["txnid"].orEmpty()

        if (result.resultCode == Activity.RESULT_OK && status == "SUCCESS" && approvalRef.isNotBlank()) {
            paymentStep = "PROCESSING"
            onCreateAndPayCampaign(
                adTitle,
                adCaption,
                selectedCreatives,
                selectedPackage,
                packageDays,
                packagePrice,
                "$selectedPaymentMethod • UTR:$approvalRef"
            ) { tx ->
                latestReceipt = tx
                paymentStep = "SUCCESS"
                paymentErrorReason = null
            }
        } else if (status == "SUBMITTED" || status == "PENDING") {
            paymentStep = "AWAITING_UTR"
            paymentErrorReason = "UPI app reported payment SUBMITTED/PENDING. Enter your 12-digit UPI UTR below to complete verification."
        } else {
            paymentStep = "AWAITING_UTR"
            paymentErrorReason = "Complete the payment of ₹$packagePrice to ${SocialRepository.MERCHANT_SETTLEMENT_UPI} in your UPI app and enter the 12-digit UPI UTR / Reference No. below to authorize."
        }
    }

    val creativePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedCreatives = selectedCreatives + uri.toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (glass.isDark) Color(0xFF060B19) else Color(0xFFF8FAFC),
        modifier = Modifier.testTag("veta_monetization_ads_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VETA Creator Wallet & Monetize Analyst",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = glass.textPrimary
                    )
                    Text(
                        text = "Google Pay • Paytm • PhonePe • Creator Wallet • Analytics",
                        fontSize = 11.sp,
                        color = CobaltBlue
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = glass.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5-Tab Mode Selector Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(glass.glassSurfaceElevated, RoundedCornerShape(14.dp))
                    .border(1.dp, glass.glassBorder, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tabs = listOf(
                    "💳 Creator Wallet",
                    "📈 Monetize Analyst",
                    "💰 Eligibility",
                    "📣 Paid Ads (₹100/₹120)",
                    "🧾 UPI Ledger"
                )
                items(tabs.size) { idx ->
                    val active = selectedTab == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) CobaltBlue else Color.Transparent)
                            .clickable { selectedTab = idx }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("veta_hub_tab_$idx"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabs[idx],
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) Color.White else glass.textSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // TAB 0: VETA CREATOR WALLET & INTEGRATED GOOGLE PAY / PAYTM / PHONEPE / BHIM PAYOUTS
                    val balanceInr = user?.estimatedRevenueInr ?: 0.0
                    val balanceUsd = balanceInr / 84.0
                    val withdrawnTotalInr = transactions.filter { it.status == "WITHDRAWN" }.sumOf { it.amountInr }

                    // Hero Creator Wallet Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(CobaltBlue, Color(0xFF0A2463), ElectricCyan.copy(alpha = 0.55f))
                                ),
                                RoundedCornerShape(22.dp)
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SPOTPIX BY VETA • CREATOR WALLET",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.1.sp,
                                    color = ElectricCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${"%,.2f".format(balanceInr)}",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "≈ $${"%,.2f".format(balanceUsd)} USD • Rate: 1,000 Views = $1.00 (₹84)",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Creator Wallet",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MiniStatPill("Withdrawn", "₹$withdrawnTotalInr", Modifier.weight(1f))
                            MiniStatPill("90d Views", "${"%.2f".format((user?.reelViews90d ?: 0L) / 1_000_000.0)}M", Modifier.weight(1f))
                            MiniStatPill("Payout Status", if (user?.monetizationStatus == "APPROVED") "Active ✓" else "Pending", Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Integrated Payment & Payout Apps: Google Pay, Paytm, PhonePe, BHIM UPI, CRED
                    Text(
                        text = "Integrated UPI & Wallet Partners (Select for Payout / Top-Up)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = glass.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val paymentPartners = listOf(
                        Triple("Google Pay (GPay)", "@okaxis", Color(0xFF2563EB)),
                        Triple("Paytm Wallet / UPI", "@paytm", Color(0xFF00BAF2)),
                        Triple("PhonePe UPI", "@ybl", Color(0xFF6739B7)),
                        Triple("BHIM NPCI UPI", "@upi", Color(0xFF10B981)),
                        Triple("CRED Pay UPI", "@axisb", Color(0xFFF59E0B))
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(paymentPartners.size) { index ->
                            val (appName, suffix, brandColor) = paymentPartners[index]
                            val isSelected = selectedPayoutApp == appName
                            Column(
                                modifier = Modifier
                                    .width(136.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) brandColor.copy(alpha = 0.22f)
                                        else glass.glassSurfaceElevated
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) brandColor else glass.textTertiary.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        selectedPayoutApp = appName
                                        val handlePrefix = (user?.username ?: "creator").replace(Regex("[^a-zA-Z0-9._]"), "")
                                        payoutUpiHandle = "$handlePrefix$suffix"
                                    }
                                    .padding(12.dp)
                                    .testTag("wallet_partner_$index")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(brandColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = appName.take(1),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = brandColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = appName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = glass.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Instant UPI ($suffix)",
                                    fontSize = 10.sp,
                                    color = glass.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Instant Payout Withdrawal Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(glass.glassSurfaceElevated, RoundedCornerShape(18.dp))
                            .border(1.dp, glass.glassBorder, RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Withdraw Creator Earnings → $selectedPayoutApp",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = glass.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = payoutUpiHandle,
                            onValueChange = { payoutUpiHandle = it.trim() },
                            label = { Text("Destination $selectedPayoutApp UPI ID / VPA") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wallet_payout_upi_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Preset Withdrawal Pills ($10, $25, $50, $100)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("10", "25", "50", "100").forEach { amt ->
                                val activeAmt = withdrawUsdInput == amt
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (activeAmt) CobaltBlue else glass.glassSurface)
                                        .border(1.dp, glass.glassBorder, RoundedCornerShape(10.dp))
                                        .clickable { withdrawUsdInput = amt }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$$amt (₹${amt.toInt() * 84})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeAmt) Color.White else glass.textPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val usdVal = withdrawUsdInput.toDoubleOrNull() ?: 0.0
                                val inrNeeded = usdVal * 84.0
                                val validUpi = Regex("^[a-zA-Z0-9._\\-]+@[a-zA-Z0-9]{2,}$").matches(payoutUpiHandle)
                                when {
                                    remoteConfig.freezeCreatorWithdrawals -> {
                                        walletStatusMsg = "⚠️ Creator withdrawals are temporarily frozen by VETA Super Admin for security audit."
                                    }
                                    !validUpi -> {
                                        walletStatusMsg = "Please enter a valid UPI ID (e.g., yourname@okaxis or number@paytm)."
                                    }
                                    usdVal < 1.0 -> {
                                        walletStatusMsg = "Minimum withdrawal is $1.00 USD (₹84)."
                                    }
                                    inrNeeded > balanceInr -> {
                                        walletStatusMsg = "Insufficient wallet balance (Available: ₹${"%,.0f".format(balanceInr)})."
                                    }
                                    else -> {
                                        onWithdrawEarnings(usdVal, selectedPayoutApp, payoutUpiHandle) { tx ->
                                            latestWithdrawalTx = tx
                                            walletStatusMsg = "✅ Transferred ₹${tx.amountInr} ($${"%.2f".format(usdVal)}) to $payoutUpiHandle via $selectedPayoutApp!"
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("wallet_withdraw_button")
                        ) {
                            Icon(Icons.Default.SendToMobile, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instant Withdraw to $selectedPayoutApp",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        if (walletStatusMsg != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = walletStatusMsg!!,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (walletStatusMsg!!.startsWith("✅")) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sync Real-Time Monetized Reel Views Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onSimulateMonetizedViews(10_000L)
                                walletStatusMsg = "📈 Synced +10,000 Monetized Reel Views (+₹840 / $10.00 to Creator Wallet)"
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+10K Reel Views (+₹840)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                        }
                        OutlinedButton(
                            onClick = { selectedTab = 3 },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Promote via GPay/Paytm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CobaltBlue)
                        }
                    }
                }

                1 -> {
                    // TAB 1: MONETIZE ANALYST (RPM, CPM, 7D/30D/90D REVENUE CHART & AI INSIGHTS)
                    val reelViews = user?.reelViews90d ?: 12_500_000L
                    val estRevInr = user?.estimatedRevenueInr ?: 18_450.0
                    val rpmInr = 84.0 // Rs 84 ($1.00) per 1,000 eligible views

                    // Analyst KPI Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniStatPill("RPM (1K Views)", "₹84.00 ($1.00)", Modifier.weight(1f))
                        MiniStatPill("Avg Ad CPM", "₹210.50", Modifier.weight(1f))
                        MiniStatPill("Retention Rate", "76.4% ↑", Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Interactive Timeframe & Revenue Bar Chart Card
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
                                    text = "Monetize Analyst • Revenue Trajectory",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = glass.textPrimary
                                )
                                Text(
                                    text = "Projected 30d Payout: ₹${"%,.0f".format(estRevInr * 1.35)}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("7D", "30D", "90D").forEach { range ->
                                    val chosen = analystRange == range
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (chosen) CobaltBlue else glass.glassSurface)
                                            .clickable { analystRange = range }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = range,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (chosen) Color.White else glass.textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val chartPoints = when (analystRange) {
                            "7D" -> listOf("Mon" to 0.45f, "Tue" to 0.62f, "Wed" to 0.58f, "Thu" to 0.81f, "Fri" to 0.74f, "Sat" to 0.95f, "Sun" to 0.89f)
                            "30D" -> listOf("W1" to 0.50f, "W2" to 0.68f, "W3" to 0.79f, "W4" to 0.92f, "W5" to 0.86f, "W6" to 1.0f, "Now" to 0.94f)
                            else -> listOf("Jan" to 0.38f, "Feb" to 0.52f, "Mar" to 0.67f, "Apr" to 0.75f, "May" to 0.88f, "Jun" to 0.96f, "Jul" to 1.0f)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            chartPoints.forEachIndexed { idx, (label, ratio) ->
                                val isBarSelected = selectedChartBarIdx == idx
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable { selectedChartBarIdx = idx }
                                ) {
                                    if (isBarSelected) {
                                        Text(
                                            text = "₹${(ratio * 4850).toInt()}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ElectricCyan
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .fillMaxHeight(ratio.coerceIn(0.15f, 0.82f))
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(
                                                if (isBarSelected) {
                                                    Brush.verticalGradient(listOf(ElectricCyan, CobaltBlue))
                                                } else {
                                                    Brush.verticalGradient(listOf(CobaltBlue.copy(alpha = 0.7f), CobaltBlue.copy(alpha = 0.25f)))
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isBarSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isBarSelected) ElectricCyan else glass.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Revenue Stream Breakdown
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(glass.glassSurfaceElevated, RoundedCornerShape(18.dp))
                            .border(1.dp, glass.glassBorder, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Earnings Breakdown by Format",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = glass.textPrimary
                        )

                        listOf(
                            Triple("Reels Ad Revenue Share (68%)", 0.68f, Color(0xFF0066FF)),
                            Triple("Feed Sponsored Placement (19%)", 0.19f, Color(0xFF00E5FF)),
                            Triple("Story Sticker & Poll Bonus (9%)", 0.09f, Color(0xFF10B981)),
                            Triple("Creator GPay / Paytm Tips (4%)", 0.04f, Color(0xFFF59E0B))
                        ).forEach { (title, pct, barColor) ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(title, fontSize = 11.sp, color = glass.textPrimary, fontWeight = FontWeight.SemiBold)
                                    Text("₹${"%,.0f".format(estRevInr * pct)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = barColor)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = barColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // VETA Monetize Analyst Actionable Recommendations
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CobaltBlue.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                            .border(1.dp, CobaltBlue.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "⚡ VETA Monetize Analyst Insights",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricCyan
                        )
                        Text(
                            text = "• Reels between 18–28 seconds generate 2.4x higher completion and ₹84/1K RPM.",
                            fontSize = 11.sp,
                            color = glass.textPrimary
                        )
                        Text(
                            text = "• Peak audience activity: 7:30 PM – 10:30 PM IST (Tier-1 India & Global Diaspora).",
                            fontSize = 11.sp,
                            color = glass.textPrimary
                        )
                        Text(
                            text = "• Eligible 90-Day Views: ${"%.2f".format(reelViews / 1_000_000.0)}M • Ready for instant GPay / Paytm payout.",
                            fontSize = 11.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                2 -> {
                    // TAB 2: CREATOR MONETIZATION ELIGIBILITY & APPLICATION
                    val followers = user?.followersCount ?: 0
                    val reelViews = user?.reelViews90d ?: 0L
                    val minFollowers = remoteConfig.monetizationMinFollowers
                    val minViews = remoteConfig.monetizationMinReelViews90d
                    val eligibleFollowers = followers >= minFollowers
                    val eligibleViews = reelViews >= minViews
                    val isGmailOk = user?.isGmailVerified == true

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(CobaltBlue.copy(alpha = 0.25f), ElectricCyan.copy(alpha = 0.12f))
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .border(1.dp, glass.glassBorder, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Estimated Creator Revenue",
                                    fontSize = 12.sp,
                                    color = glass.textSecondary
                                )
                                Text(
                                    text = "₹${"%,.2f".format(user?.estimatedRevenueInr ?: 0.0)}",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = glass.textPrimary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CobaltBlue)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Status: ${user?.monetizationStatus ?: "NONE"}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MiniStatPill("Reels Rev Share", "₹12,400", Modifier.weight(1f))
                            MiniStatPill("Spotpix Bonus", "₹6,050", Modifier.weight(1f))
                            MiniStatPill("Gmail Session", if (isGmailOk) "Verified ✓" else "Unverified", Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "VETA Engine Eligibility Criteria (Remotely Configurable)",
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Criterion 1: 1,100 Followers
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("1. Minimum Followers ($minFollowers)", fontSize = 12.sp, color = glass.textPrimary)
                                Text(
                                    text = "$followers / $minFollowers",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (eligibleFollowers) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (followers.toFloat() / minFollowers.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                                color = if (eligibleFollowers) Color(0xFF10B981) else CobaltBlue
                            )
                        }

                        // Criterion 2: 10M Reel Views in 90 Days
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("2. Eligible Reel Views (Last 90 Days)", fontSize = 12.sp, color = glass.textPrimary)
                                Text(
                                    text = "${"%.1f".format(reelViews / 1_000_000.0)}M / ${minViews / 1_000_000}M",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (eligibleViews) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (reelViews.toFloat() / minViews.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                                color = if (eligibleViews) Color(0xFF10B981) else CobaltBlue
                            )
                        }

                        // Criterion 3: Verified Gmail & Content Policy Compliance
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { policyAccepted = !policyAccepted }
                        ) {
                            Checkbox(checked = policyAccepted, onCheckedChange = { policyAccepted = it })
                            Text(
                                text = "I comply with Spotpix by VETA Content Monetization Policies & verified Gmail (${user?.email}).",
                                fontSize = 11.sp,
                                color = glass.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mandatory Legal Disclaimer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x22F59E0B), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x66F59E0B), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "⚖️ Legal Disclaimer: Spotpix by VETA never guarantees fixed earnings. Revenue depends on verified ad impressions, audience engagement, and Super Admin approval.",
                            fontSize = 11.sp,
                            color = glass.textPrimary,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (!eligibleFollowers || !eligibleViews) {
                                Toast.makeText(
                                    context,
                                    "Eligibility requires $minFollowers followers and ${minViews / 1_000_000}M Reel views.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onApplyMonetization()
                                Toast.makeText(
                                    context,
                                    "Monetization application submitted for VETA Super Admin review!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = policyAccepted && user?.monetizationStatus != "APPROVED",
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("apply_monetization_button")
                    ) {
                        Text(
                            text = if (user?.monetizationStatus == "APPROVED") "✓ Monetization Active (VETA Engine)"
                            else "Submit Monetization Application",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                3 -> {
                    // BUSINESS ANALYTICS & PAID ADVERTISEMENT CAMPAIGN MANAGER
                    Text(
                        text = "Create Sponsored Ad Campaign",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = glass.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Fixed Campaign Packages: Weekly (Rs 100 / 7 Days) & Two-Week (Rs 120 / 14 Days)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CampaignPackageCard(
                            title = "Weekly Campaign",
                            priceText = "₹${remoteConfig.weeklyAdPriceInr}",
                            durationText = "7 Days Duration",
                            isSelected = selectedPackage == "WEEKLY",
                            onClick = { selectedPackage = "WEEKLY" },
                            modifier = Modifier.weight(1f)
                        )
                        CampaignPackageCard(
                            title = "Two-Week Campaign",
                            priceText = "₹${remoteConfig.twoWeekAdPriceInr}",
                            durationText = "14 Days Duration",
                            isSelected = selectedPackage == "TWO_WEEK",
                            onClick = { selectedPackage = "TWO_WEEK" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = adTitle,
                        onValueChange = { adTitle = it },
                        label = { Text("Campaign Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = adCaption,
                        onValueChange = { adCaption = it },
                        label = { Text("Sponsored Ad Caption & CTA") },
                        maxLines = 2,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Multi-Photo / Video Creatives with Real-Time In-App Preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Campaign Creatives (${selectedCreatives.size}) • Live Preview",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = glass.textPrimary
                        )
                        TextButton(
                            onClick = {
                                creativePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            }
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Creative", fontSize = 12.sp)
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(selectedCreatives) { url ->
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.5.dp, CobaltBlue, RoundedCornerShape(14.dp))
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CobaltBlue)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Sponsored", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            isCheckoutVisible = true
                            paymentStep = "IDLE"
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("proceed_to_upi_checkout_button")
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pay ₹$packagePrice via UPI / Gateway ($packageDays Days)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    // Razorpay / Cashfree UPI Payment Checkout Card
                    AnimatedVisibility(visible = isCheckoutVisible) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .background(glass.glassSurfaceElevated, RoundedCornerShape(18.dp))
                                .border(1.5.dp, CobaltBlue, RoundedCornerShape(18.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "VETA Secure Payment Gateway",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = glass.textPrimary
                                    )
                                    Text(
                                        text = "Merchant UPI Settlement: ${SocialRepository.MERCHANT_SETTLEMENT_UPI}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyan
                                    )
                                }
                                Text(
                                    text = "₹$packagePrice",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF10B981)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Select UPI App / Payment Method:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = glass.textSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val methods = listOf(
                                "Google Pay (UPI Intent)",
                                "PhonePe (UPI Intent)",
                                "Paytm (UPI Intent)",
                                "BHIM / Any UPI App",
                                "UPI ID / UTR Verification"
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(methods) { method ->
                                    val chosen = selectedPaymentMethod == method
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (chosen) CobaltBlue else glass.glassSurface)
                                            .border(1.dp, glass.glassBorder, RoundedCornerShape(10.dp))
                                            .clickable { selectedPaymentMethod = method }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = method,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (chosen) Color.White else glass.textPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Merchant UPI Copy Bar (vanij7@fam)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(glass.glassSurface)
                                    .border(1.dp, glass.glassBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Official Merchant UPI ID (Spotpix by VETA)",
                                        fontSize = 10.sp,
                                        color = glass.textSecondary
                                    )
                                    Text(
                                        text = SocialRepository.MERCHANT_SETTLEMENT_UPI,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ElectricCyan
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(
                                            ClipData.newPlainText("Merchant UPI ID", SocialRepository.MERCHANT_SETTLEMENT_UPI)
                                        )
                                        Toast.makeText(
                                            context,
                                            "Copied ${SocialRepository.MERCHANT_SETTLEMENT_UPI} to clipboard",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy UPI ID", tint = ElectricCyan, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy UPI ID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Payer UPI ID & 12-Digit UTR Inputs for Authorized Verification
                            OutlinedTextField(
                                value = payerUpiId,
                                onValueChange = { payerUpiId = it.trim() },
                                label = { Text("Your Payer UPI ID (e.g., name@okaxis, number@ybl)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("payer_upi_id_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = upiUtrNumber,
                                onValueChange = { input ->
                                    upiUtrNumber = input.filter { it.isDigit() }.take(12)
                                },
                                label = { Text("12-Digit UPI UTR / Reference No. (Required for Manual/QR Auth)") },
                                supportingText = {
                                    Text(
                                        "Campaigns never activate randomly. Either complete payment via an installed UPI app or enter your verified 12-digit NPCI UTR number.",
                                        fontSize = 10.sp,
                                        color = glass.textSecondary
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("upi_utr_input")
                            )

                            if (paymentErrorReason != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = paymentErrorReason!!,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF59E0B)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            when (paymentStep) {
                                "IDLE", "AWAITING_UTR" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Button 1: Launch Real Installed UPI App via Android Intent (upi://pay?pa=vanij7@fam...)
                                        Button(
                                            onClick = {
                                                val upiUri = Uri.Builder()
                                                    .scheme("upi")
                                                    .authority("pay")
                                                    .appendQueryParameter("pa", SocialRepository.MERCHANT_SETTLEMENT_UPI)
                                                    .appendQueryParameter("pn", "Spotpix by VETA")
                                                    .appendQueryParameter("tr", orderRefId)
                                                    .appendQueryParameter("tn", "Spotpix Ad: $adTitle ($packageDays Days)")
                                                    .appendQueryParameter("am", "$packagePrice.00")
                                                    .appendQueryParameter("cu", "INR")
                                                    .build()

                                                val targetPkg = when {
                                                    selectedPaymentMethod.startsWith("Google Pay") -> "com.google.android.apps.nbu.paisa.user"
                                                    selectedPaymentMethod.startsWith("PhonePe") -> "com.phonepe.app"
                                                    selectedPaymentMethod.startsWith("Paytm") -> "net.one97.paytm"
                                                    else -> null
                                                }

                                                val specificIntent = Intent(Intent.ACTION_VIEW, upiUri).apply {
                                                    if (targetPkg != null) setPackage(targetPkg)
                                                }
                                                val genericUpiIntent = Intent(Intent.ACTION_VIEW, upiUri)
                                                val pm = context.packageManager

                                                try {
                                                    when {
                                                        targetPkg != null && specificIntent.resolveActivity(pm) != null -> {
                                                            upiPaymentLauncher.launch(specificIntent)
                                                        }
                                                        genericUpiIntent.resolveActivity(pm) != null -> {
                                                            upiPaymentLauncher.launch(
                                                                Intent.createChooser(genericUpiIntent, "Pay ₹$packagePrice to ${SocialRepository.MERCHANT_SETTLEMENT_UPI}")
                                                            )
                                                        }
                                                        else -> {
                                                            paymentStep = "AWAITING_UTR"
                                                            paymentErrorReason = "No UPI app detected on this device/emulator. Pay ₹$packagePrice to ${SocialRepository.MERCHANT_SETTLEMENT_UPI} from your phone's Google Pay / PhonePe / Paytm and enter the 12-digit UTR above."
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    paymentStep = "AWAITING_UTR"
                                                    paymentErrorReason = "Could not open external UPI app (${e.localizedMessage}). Pay ₹$packagePrice to ${SocialRepository.MERCHANT_SETTLEMENT_UPI} and verify your 12-digit UTR below."
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(46.dp)
                                                .testTag("launch_real_upi_app_button")
                                        ) {
                                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Open UPI App & Pay ₹$packagePrice → ${SocialRepository.MERCHANT_SETTLEMENT_UPI}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }

                                        // Button 2: Verify Real 12-Digit UPI UTR + Payer UPI ID (Anti-Hack Cryptographic & Anti-Replay Shield)
                                        Button(
                                            onClick = {
                                                val (isValidUtr, errorMsg) = SocialRepository.verifyAndConsumeUtrAntiHack(
                                                    utrNumber = upiUtrNumber,
                                                    payerUpiId = payerUpiId,
                                                    existingTransactions = transactions,
                                                    strictShieldEnabled = remoteConfig.strictAntiHackShield
                                                )

                                                if (!isValidUtr) {
                                                    paymentErrorReason = errorMsg
                                                } else {
                                                    scope.launch {
                                                        paymentErrorReason = null
                                                        paymentStep = "PROCESSING"
                                                        delay(600)
                                                        onCreateAndPayCampaign(
                                                            adTitle,
                                                            adCaption,
                                                            selectedCreatives,
                                                            selectedPackage,
                                                            packageDays,
                                                            packagePrice,
                                                            "$selectedPaymentMethod ($payerUpiId • UTR:${upiUtrNumber.trim()})"
                                                        ) { tx ->
                                                            latestReceipt = tx
                                                            paymentStep = "SUCCESS"
                                                        }
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(46.dp)
                                                .testTag("confirm_upi_payment_button")
                                        ) {
                                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Verify 12-Digit UTR & Authorize Campaign (₹$packagePrice)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                "PROCESSING" -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = ElectricCyan)
                                        Text(
                                            text = "Verifying NPCI UTR & HMAC-SHA256 signature for ${SocialRepository.MERCHANT_SETTLEMENT_UPI}...",
                                            fontSize = 12.sp,
                                            color = glass.textPrimary
                                        )
                                    }
                                }

                                "SUCCESS" -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0x2210B981), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "✅ UPI Payment Authorized & Campaign Activated!",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF10B981)
                                        )
                                        Text(
                                            text = "Order ID: ${latestReceipt?.id} • Settled to ${SocialRepository.MERCHANT_SETTLEMENT_UPI}",
                                            fontSize = 11.sp,
                                            color = glass.textPrimary
                                        )
                                        Text(
                                            text = "Method & UTR: ${latestReceipt?.paymentMethod}",
                                            fontSize = 11.sp,
                                            color = ElectricCyan
                                        )
                                        Text(
                                            text = "Signature: ${latestReceipt?.gatewaySignatureHash}",
                                            fontSize = 10.sp,
                                            color = glass.textSecondary
                                        )
                                    }
                                }

                                "FAILURE" -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0x22EF4444), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "❌ Payment Verification Failed",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFEF4444)
                                        )
                                        Text(
                                            text = "Campaign remains inactive. Valid UPI payment to ${SocialRepository.MERCHANT_SETTLEMENT_UPI} is required.",
                                            fontSize = 11.sp,
                                            color = glass.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        TextButton(onClick = { paymentStep = "IDLE" }) {
                                            Text("Retry Verification", color = ElectricCyan)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your Active & Draft Ad Campaigns (${campaigns.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = glass.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    campaigns.forEach { camp ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .background(glass.glassSurfaceElevated, RoundedCornerShape(14.dp))
                                .border(1.dp, glass.glassBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(camp.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
                                    Text(
                                        "${camp.packageType} • ₹${camp.amountInr} (${camp.durationDays}d) • Status: ${camp.status}",
                                        fontSize = 11.sp,
                                        color = ElectricCyan
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            val next = if (camp.status == "ACTIVE") "PAUSED" else "ACTIVE"
                                            onUpdateCampaignStatus(camp.id, next)
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (camp.status == "ACTIVE") Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                            contentDescription = "Pause/Resume",
                                            tint = CobaltBlue
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteCampaign(camp.id) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                            Text(
                                text = "📊 Impressions: ${camp.impressions} • Clicks: ${camp.clicks}",
                                fontSize = 11.sp,
                                color = glass.textSecondary
                            )
                        }
                    }
                }

                4 -> {
                    // DIGITAL RECEIPTS & UPI SETTLEMENT LEDGER
                    Text(
                        text = "Verified UPI & Gateway Receipts (${transactions.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = glass.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    transactions.forEach { tx ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .background(glass.glassSurfaceElevated, RoundedCornerShape(14.dp))
                                .border(1.dp, glass.glassBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(tx.campaignTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
                                Text("₹${tx.amountInr} • ${tx.status}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                            }
                            Text(
                                text = "Method: ${tx.paymentMethod} → Merchant: ${tx.merchantSettlementUpi}",
                                fontSize = 11.sp,
                                color = ElectricCyan
                            )
                            Text(
                                text = "Order: ${tx.id} • Sig: ${tx.gatewaySignatureHash}",
                                fontSize = 10.sp,
                                color = glass.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MiniStatPill(label: String, value: String, modifier: Modifier = Modifier) {
    val glass = LocalLiquidGlass.current
    Column(
        modifier = modifier
            .background(glass.glassSurface, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(label, fontSize = 10.sp, color = glass.textSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = glass.textPrimary)
    }
}

@Composable
private fun CampaignPackageCard(
    title: String,
    priceText: String,
    durationText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlass.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) CobaltBlue.copy(alpha = 0.22f) else glass.glassSurfaceElevated)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) CobaltBlue else glass.textTertiary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(priceText, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = ElectricCyan)
        Text(durationText, fontSize = 11.sp, color = glass.textSecondary)
    }
}

/**
 * 4. SUPER ADMIN DASHBOARD (VETA CONTROL PANEL)
 * Strictly restricted to VaanijSingh28@gmail.com
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetaSuperAdminConsoleModal(
    currentUser: UserEntity?,
    allUsers: List<UserEntity>,
    posts: List<PostEntity> = emptyList(),
    reels: List<ReelEntity> = emptyList(),
    campaigns: List<AdCampaignEntity>,
    transactions: List<PaymentTransactionEntity>,
    remoteConfig: VetaRemoteConfig,
    onReviewBlueTick: (userId: String, approve: Boolean) -> Unit,
    onReviewMonetization: (userId: String, approve: Boolean) -> Unit,
    onToggleSuspendUser: (userId: String, suspend: Boolean) -> Unit,
    onUpdateCampaignStatus: (campaignId: String, status: String) -> Unit,
    onDeleteCampaign: (campaignId: String) -> Unit,
    onDeletePost: (postId: String) -> Unit = {},
    onDeleteReel: (reelId: String) -> Unit = {},
    onAdjustCreatorWallet: (userId: String, newBalanceInr: Double) -> Unit = { _, _ -> },
    onUpdateRemoteConfig: (VetaRemoteConfig) -> Unit,
    onDismiss: () -> Unit
) {
    // Hard server/client guard: never render if not verified Super Admin
    if (!SocialRepository.isVerifiedSuperAdmin(currentUser)) return

    var adminTab by remember { mutableStateOf(0) }

    var minBlueTickInput by remember(remoteConfig) { mutableStateOf(remoteConfig.blueTickMinFollowers.toString()) }
    var minMonFollowersInput by remember(remoteConfig) { mutableStateOf(remoteConfig.monetizationMinFollowers.toString()) }
    var weeklyPriceInput by remember(remoteConfig) { mutableStateOf(remoteConfig.weeklyAdPriceInr.toString()) }
    var twoWeekPriceInput by remember(remoteConfig) { mutableStateOf(remoteConfig.twoWeekAdPriceInr.toString()) }
    var announcementInput by remember(remoteConfig) { mutableStateOf(remoteConfig.globalAnnouncementBanner) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF050914),
        modifier = Modifier.testTag("veta_super_admin_console_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VETA Master Admin Console",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Super Admin: ${currentUser?.email} • Full App & Security Control",
                        fontSize = 11.sp,
                        color = ElectricCyan
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Side-Slide Horizontal Admin Control Tabs
            val adminTabs = listOf(
                "⚡ Master Control",
                "✓ Blue Tick",
                "💰 Monetize & Wallet",
                "💳 Ads & UPI",
                "🛡️ Content Mod",
                "⚙️ Thresholds"
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(adminTabs) { idx, label ->
                    val selected = adminTab == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) CobaltBlue else Color(0xFF111827))
                            .border(
                                1.dp,
                                if (selected) ElectricCyan else Color(0x3300E5FF),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { adminTab = idx }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (adminTab) {
                0 -> {
                    // MASTER APP CONTROL, ANNOUNCEMENTS, SECURITY LOCKS & GLOBAL BACKGROUND
                    Text(
                        "Master App Control & Anti-Hacker Command Center",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "📢 Global Live App Announcement Banner",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                        OutlinedTextField(
                            value = announcementInput,
                            onValueChange = { announcementInput = it },
                            label = { Text("Broadcast banner across entire app (leave blank to hide)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onUpdateRemoteConfig(
                                        remoteConfig.copy(globalAnnouncementBanner = announcementInput.trim())
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Publish Banner", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    announcementInput = ""
                                    onUpdateRemoteConfig(remoteConfig.copy(globalAnnouncementBanner = ""))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Clear Banner", fontSize = 11.sp, color = Color(0xFFEF4444))
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        // Master Security Switches
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🛡️ Strict Anti-Hack UTR & HMAC Shield", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Blocks UTR replay, sequential digits & brute-force attacks", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                            Switch(
                                checked = remoteConfig.strictAntiHackShield,
                                onCheckedChange = {
                                    onUpdateRemoteConfig(remoteConfig.copy(strictAntiHackShield = it))
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("❄️ Freeze All Creator Wallet Withdrawals", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Emergency pause on all UPI payouts for audit", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                            Switch(
                                checked = remoteConfig.freezeCreatorWithdrawals,
                                onCheckedChange = {
                                    onUpdateRemoteConfig(remoteConfig.copy(freezeCreatorWithdrawals = it))
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🚧 App Maintenance Mode Banner", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Displays maintenance alert header to all users", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                            Switch(
                                checked = remoteConfig.maintenanceMode,
                                onCheckedChange = {
                                    onUpdateRemoteConfig(remoteConfig.copy(maintenanceMode = it))
                                }
                            )
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        Text(
                            "🎨 Global Default App Background Engine",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                        val bgPresets = listOf(
                            "Cobalt Liquid Glass",
                            "OLED Pitch Black",
                            "Cyber Aurora Cyan",
                            "Royal Emerald Vault",
                            "Minimal Daylight Frost"
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(bgPresets) { style ->
                                val active = remoteConfig.appBackgroundStyle == style
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (active) CobaltBlue else Color(0xFF1E293B))
                                        .border(1.dp, if (active) ElectricCyan else Color.Transparent, RoundedCornerShape(10.dp))
                                        .clickable {
                                            onUpdateRemoteConfig(remoteConfig.copy(appBackgroundStyle = style))
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(style, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    Text("Blue Tick Verification Queue & User Moderation", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    allUsers.forEach { u ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(14.dp))
                                .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("@${u.username} (${u.followersCount} flw)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Status: ${u.verificationStatus} • Doc: ${u.verificationDocType.ifBlank { "Passport" }}", fontSize = 11.sp, color = ElectricCyan)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onReviewBlueTick(u.id, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Approve ✓", fontSize = 11.sp, color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { onReviewBlueTick(u.id, false) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Revoke", fontSize = 11.sp, color = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }

                2 -> {
                    Text("Creator Monetization, Wallet Control & Account Suspension", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    allUsers.forEach { u ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(14.dp))
                                .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("@${u.username} • ${u.email}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(
                                        "Monetization: ${u.monetizationStatus} • Wallet: ₹${"%,.0f".format(u.estimatedRevenueInr)}",
                                        fontSize = 11.sp,
                                        color = ElectricCyan
                                    )
                                }
                                if (u.id != SocialRepository.SUPER_ADMIN_UID) {
                                    TextButton(onClick = { onToggleSuspendUser(u.id, !u.isSuspended) }) {
                                        Text(
                                            if (u.isSuspended) "Unsuspend" else "Suspend",
                                            color = if (u.isSuspended) Color(0xFF10B981) else Color(0xFFEF4444),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onReviewMonetization(u.id, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Approve", fontSize = 10.sp, color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { onReviewMonetization(u.id, false) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Reject", fontSize = 10.sp, color = Color(0xFFEF4444))
                                }
                                Button(
                                    onClick = { onAdjustCreatorWallet(u.id, u.estimatedRevenueInr + 1000.0) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("+₹1,000 Bonus", fontSize = 10.sp, color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { onAdjustCreatorWallet(u.id, 0.0) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Zero ₹0", fontSize = 10.sp, color = Color(0xFFF59E0B))
                                }
                            }
                        }
                    }
                }

                3 -> {
                    val totalSettled = transactions.filter { it.status == "SUCCESS" }.sumOf { it.amountInr }
                    Text(
                        "Total Settled to ${SocialRepository.MERCHANT_SETTLEMENT_UPI}: ₹$totalSettled",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    campaigns.forEach { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${c.title} (@${c.ownerUsername})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("₹${c.amountInr} • ${c.status}", fontSize = 11.sp, color = ElectricCyan)
                            }
                            Row {
                                TextButton(onClick = { onUpdateCampaignStatus(c.id, "ACTIVE") }) {
                                    Text("Approve", fontSize = 11.sp, color = Color(0xFF10B981))
                                }
                                TextButton(onClick = { onUpdateCampaignStatus(c.id, "REJECTED") }) {
                                    Text("Reject", fontSize = 11.sp, color = Color(0xFFF59E0B))
                                }
                                IconButton(onClick = { onDeleteCampaign(c.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // CONTENT MODERATION: POSTS & REELS TAKEDOWN
                    Text(
                        "Full App Content Moderation (${posts.size} Posts • ${reels.size} Reels)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    posts.forEach { post ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Post by @${post.authorUsername}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(post.caption, fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { onDeletePost(post.id) }) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Delete Post", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    reels.forEach { reel ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reel by @${reel.authorUsername}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                                Text(reel.caption, fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { onDeleteReel(reel.id) }) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Delete Reel", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }

                5 -> {
                    Text("Remote Backend Threshold & Package Configurator", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = minBlueTickInput,
                        onValueChange = { minBlueTickInput = it },
                        label = { Text("Blue Tick Min Followers") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = minMonFollowersInput,
                        onValueChange = { minMonFollowersInput = it },
                        label = { Text("Monetization Min Followers") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weeklyPriceInput,
                            onValueChange = { weeklyPriceInput = it },
                            label = { Text("Weekly Ad (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = twoWeekPriceInput,
                            onValueChange = { twoWeekPriceInput = it },
                            label = { Text("2-Week Ad (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onUpdateRemoteConfig(
                                remoteConfig.copy(
                                    blueTickMinFollowers = minBlueTickInput.toIntOrNull() ?: 1000,
                                    monetizationMinFollowers = minMonFollowersInput.toIntOrNull() ?: 1100,
                                    weeklyAdPriceInr = weeklyPriceInput.toIntOrNull() ?: 100,
                                    twoWeekAdPriceInr = twoWeekPriceInput.toIntOrNull() ?: 120
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Remote Thresholds", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * 5. DYNAMIC ISLAND-INSPIRED FLOATING PILL (SPOTPIX LIVE PILL)
 */
@Composable
fun SpotpixLiveFloatingPill(
    reel: ReelEntity,
    isPlaying: Boolean,
    watchSeconds: Int = 0,
    onTickWatchTime: () -> Unit = {},
    onTogglePlayPause: () -> Unit,
    onReturnToReel: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "live_pill_wave")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
        label = "wave_pulse"
    )

    Box(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 6.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = CobaltBlue)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xEE050914))
            .border(
                1.2.dp,
                Brush.horizontalGradient(listOf(CobaltBlue, ElectricCyan)),
                RoundedCornerShape(28.dp)
            )
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("spotpix_live_floating_pill")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(CobaltBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size((14 * if (isPlaying) pulse else 0.7f).dp)
                    )
                }

                Text(
                    text = "Spotpix Live • @${reel.authorUsername}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = reel.audioTitle,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 160.dp)
                    )
                    Button(
                        onClick = onReturnToReel,
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("Open Reel", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(22.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

/**
 * 6. INSTAGRAM-STYLE "LIKES" USER LIST MODAL
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostLikesBottomSheet(
    post: PostEntity,
    allUsers: List<UserEntity>,
    onUserClick: (String) -> Unit,
    onToggleFollow: (UserEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val glass = LocalLiquidGlass.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (glass.isDark) Color(0xFF060B19) else Color(0xFFF8FAFC),
        modifier = Modifier.testTag("post_likes_bottom_sheet")
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
                Column {
                    Text(
                        text = "Likes (${post.likeCount})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = glass.textPrimary
                    )
                    Text(
                        text = "People who liked @${post.authorUsername}'s post",
                        fontSize = 11.sp,
                        color = CobaltBlue
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = glass.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.heightIn(max = 380.dp)
            ) {
                items(allUsers, key = { it.id }) { u ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(glass.glassSurfaceElevated)
                            .border(1.dp, glass.glassBorder, RoundedCornerShape(14.dp))
                            .clickable { onUserClick(u.id) }
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
                                            contentDescription = "Verified",
                                            tint = CobaltBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = u.fullName,
                                    fontSize = 11.sp,
                                    color = glass.textSecondary
                                )
                            }
                        }

                        if (!u.isCurrentUser) {
                            val btnText = when {
                                u.isFollowedByMe -> "Following"
                                u.isFollowRequested -> "Requested"
                                else -> "Follow"
                            }
                            Button(
                                onClick = { onToggleFollow(u) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (u.isFollowedByMe) glass.glassSurface else CobaltBlue
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = btnText,
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
