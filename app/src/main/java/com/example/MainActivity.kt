package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.ui.AppTab
import com.example.ui.SocialViewModel
import com.example.ui.ThemeMode
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.InstaLinkTheme
import com.example.ui.theme.LocalLiquidGlass

class MainActivity : ComponentActivity() {
    private val viewModel: SocialViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val comments by viewModel.currentComments.collectAsStateWithLifecycle()
            val chatMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()

            val isDark = when (uiState.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            InstaLinkTheme(
                darkTheme = isDark,
                appBackgroundStyle = uiState.appBackgroundStyle
            ) {
                InstaLinkApp(
                    viewModel = viewModel,
                    comments = comments,
                    chatMessages = chatMessages
                )
            }
        }
    }
}

@Composable
fun InstaLinkApp(
    viewModel: SocialViewModel,
    comments: List<com.example.data.model.CommentEntity>,
    chatMessages: List<com.example.data.model.DirectMessageEntity>
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val glass = LocalLiquidGlass.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleAuthHelper = remember(context) {
        GoogleAuthHelper(
            context = context,
            onSignInSuccess = { firebaseUser ->
                viewModel.onGoogleUserAuthenticated(firebaseUser) { syncedUser ->
                    Toast.makeText(
                        context,
                        "Signed in with Google as @${syncedUser.username} (${syncedUser.email})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onSignInError = { errorMsg ->
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Handle back button on sub-screens, floating menu, or sub-tabs
    BackHandler(
        enabled = uiState.isCreateMenuExpanded ||
                  uiState.isMessagesScreenOpen ||
                  uiState.isNotificationsScreenOpen ||
                  uiState.viewedProfileUserId != null ||
                  uiState.currentTab != AppTab.HOME
    ) {
        when {
            uiState.isCreateMenuExpanded -> viewModel.closeCreateMenu()
            uiState.isMessagesScreenOpen -> {
                if (uiState.activeChatUserId != null) {
                    viewModel.closeChat()
                } else {
                    viewModel.closeMessages()
                }
            }
            uiState.isNotificationsScreenOpen -> viewModel.closeNotifications()
            uiState.viewedProfileUserId != null -> {
                viewModel.clearViewedProfile()
                viewModel.selectTab(AppTab.HOME)
            }
            else -> viewModel.selectTab(AppTab.HOME)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = glass.backgroundBrush)
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (uiState.remoteConfig.maintenanceMode || uiState.remoteConfig.globalAnnouncementBanner.isNotBlank()) {
                        val bannerText = if (uiState.remoteConfig.maintenanceMode) {
                            "🚧 VETA Maintenance Mode Active • ${uiState.remoteConfig.globalAnnouncementBanner.ifBlank { "Core services protected by VETA Anti-Hack Shield" }}"
                        } else {
                            "📢 ${uiState.remoteConfig.globalAnnouncementBanner}"
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (uiState.remoteConfig.maintenanceMode)
                                        androidx.compose.ui.graphics.Color(0xFFB45309)
                                    else
                                        androidx.compose.ui.graphics.Color(0xFF0066FF)
                                )
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            androidx.compose.material3.Text(
                                text = bannerText,
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = androidx.compose.ui.unit.TextUnit(11f, androidx.compose.ui.unit.TextUnitType.Sp),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }
                    if (uiState.currentTab == AppTab.HOME &&
                        !uiState.isMessagesScreenOpen &&
                        !uiState.isNotificationsScreenOpen
                    ) {
                        InstaLinkTopBar(
                            onOpenMessages = { viewModel.openMessages() },
                            onOpenNotifications = { viewModel.openNotifications() },
                            onOpenUserSwitcher = { viewModel.openUserSwitcher() },
                            onDoubleTapSwitchAccount = {
                                viewModel.switchToNextAccount { newUsername ->
                                    Toast.makeText(
                                        context,
                                        "Switched account to @$newUsername",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            unreadMessages = uiState.unreadMessagesCount,
                            unreadNotifications = uiState.unreadNotificationsCount,
                            currentUsername = uiState.currentUser?.username
                        )
                    }
                }
            },
            bottomBar = {
                // Show floating bottom navigation bar when not on full-screen sub-screens
                AnimatedVisibility(
                    visible = !uiState.isMessagesScreenOpen &&
                              !uiState.isNotificationsScreenOpen &&
                              uiState.activeStoryIndex == null,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    LiquidGlassNavBar(
                        selectedTab = uiState.currentTab,
                        isCreateMenuExpanded = uiState.isCreateMenuExpanded,
                        onTabSelected = { viewModel.selectTab(it) },
                        onToggleCreateMenu = { viewModel.toggleCreateMenu() },
                        onSelectCreateMode = { mode ->
                            viewModel.openCreateWithMode(mode)
                        },
                        onDoubleTapProfile = {
                            viewModel.switchToNextAccount { newUsername ->
                                Toast.makeText(
                                    context,
                                    "Switched account to @$newUsername",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onHoldProfile = {
                            Toast.makeText(
                                context,
                                "Account Switcher • Select or Login with Gmail",
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.openUserSwitcher()
                        },
                        userAvatarUrl = uiState.currentUser?.avatarUrl
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    uiState.isMessagesScreenOpen -> {
                        DirectMessagesScreen(
                            allUsers = uiState.allUsers,
                            allMessages = uiState.allMessages,
                            currentUser = uiState.currentUser,
                            activeChatUserId = uiState.activeChatUserId,
                            chatMessages = chatMessages,
                            chatThemeName = uiState.chatThemeName,
                            onChatThemeChange = { viewModel.setChatThemeName(it) },
                            onOpenChat = { viewModel.openChat(it) },
                            onCloseChat = { viewModel.closeChat() },
                            onCloseMessages = { viewModel.closeMessages() },
                            onUserClick = { userId ->
                                viewModel.openUserProfile(userId)
                            },
                            onAcceptRequest = { viewModel.acceptMessageRequest(it) },
                            onDeleteConversation = { viewModel.deleteMessageConversation(it) },
                            onSendMessage = { text, isVoice, duration ->
                                viewModel.sendMessage(text, isVoice, duration)
                            }
                        )
                    }

                    uiState.isNotificationsScreenOpen -> {
                        NotificationsScreen(
                            notifications = uiState.notifications,
                            onBack = { viewModel.closeNotifications() },
                            onUserClick = { userId ->
                                viewModel.openUserProfile(userId)
                            },
                            onToggleFollowActor = { actorId ->
                                viewModel.toggleFollowByUserId(actorId)
                            }
                        )
                    }

                    else -> {
                        // Smooth Horizontal Slide Transition between Tabs
                        AnimatedContent(
                            targetState = uiState.currentTab,
                            transitionSpec = {
                                val direction = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                                (slideInHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    initialOffsetX = { fullWidth -> direction * (fullWidth / 3) }
                                ) + fadeIn(tween(220))) togetherWith
                                (slideOutHorizontally(
                                    animationSpec = tween(200),
                                    targetOffsetX = { fullWidth -> -direction * (fullWidth / 4) }
                                ) + fadeOut(tween(180)))
                            },
                            label = "smooth_tab_slide"
                        ) { targetTab ->
                            when (targetTab) {
                                AppTab.HOME -> {
                                    HomeScreen(
                                        posts = uiState.posts,
                                        stories = uiState.stories,
                                        allUsers = uiState.allUsers,
                                        currentUser = uiState.currentUser,
                                        onStoryClick = { viewModel.openStory(it) },
                                        onAddStoryClick = { viewModel.openCreateWithMode(CreateMode.STORY) },
                                        onToggleLike = { viewModel.togglePostLike(it) },
                                        onDoubleTapLike = { viewModel.doubleTapLikePost(it) },
                                        onToggleSave = { viewModel.togglePostSave(it) },
                                        onToggleFollowAuthor = { authorId ->
                                            viewModel.toggleFollowByUserId(authorId)
                                        },
                                        onOpenComments = { id, title -> viewModel.openComments(id, title) },
                                        onOpenLikesSheet = { post -> viewModel.openPostLikesSheet(post) },
                                        onSharePost = { viewModel.openShareDialog("Post by @${it.authorUsername}: ${it.caption}") },
                                        onUserClick = { authorId ->
                                            viewModel.openUserProfile(authorId)
                                        }
                                    )
                                }

                                AppTab.EXPLORE -> {
                                    ExploreScreen(
                                        searchQuery = uiState.searchQuery,
                                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                        category = uiState.searchCategory,
                                        onCategoryChange = { viewModel.setSearchCategory(it) },
                                        posts = uiState.posts,
                                        allUsers = uiState.allUsers,
                                        onToggleFollow = { viewModel.toggleFollow(it) },
                                        onUserClick = { userId ->
                                            viewModel.openUserProfile(userId)
                                        },
                                        onPostClick = { post ->
                                            viewModel.openUserProfile(post.authorId)
                                        }
                                    )
                                }

                                AppTab.CREATE -> {
                                    CreatePostScreen(
                                        currentUser = uiState.currentUser,
                                        initialMode = uiState.selectedCreateMode,
                                        defaultUploadQuality = uiState.uploadQuality,
                                        defaultHideLikes = uiState.hideLikeCountsDefault,
                                        onModeChange = { viewModel.openCreateWithMode(it) },
                                        onCreateStory = { mediaUrl, filterName, overlayText, pollQ, pollA, pollB ->
                                            viewModel.createStory(mediaUrl, filterName, overlayText, pollQ, pollA, pollB)
                                        },
                                        onCreatePost = { mediaUrl, caption, location, filterName, overlayText, aspect, quality, hideLikes, isVideo ->
                                            viewModel.createPost(mediaUrl, caption, location, filterName, overlayText, aspect, quality, hideLikes, isVideo)
                                        },
                                        onCreateReel = { videoUrl, caption, audioTitle, filterName, overlayText, quality ->
                                            viewModel.createReel(videoUrl, caption, audioTitle, filterName, overlayText, quality)
                                        }
                                    )
                                }

                                AppTab.REELS -> {
                                    ReelsScreen(
                                        reels = uiState.reels,
                                        allUsers = uiState.allUsers,
                                        onToggleLike = { viewModel.toggleReelLike(it) },
                                        onDoubleTapLike = { viewModel.doubleTapLikeReel(it) },
                                        onToggleSave = { viewModel.toggleReelSave(it) },
                                        onToggleFollow = { authorId ->
                                            viewModel.toggleFollowByUserId(authorId)
                                        },
                                        onUserClick = { authorId ->
                                            viewModel.openUserProfile(authorId)
                                        },
                                        onOpenComments = { id, title -> viewModel.openComments(id, title) },
                                        onShareReel = { viewModel.openShareDialog("Reel by @${it.authorUsername}: ${it.caption}") }
                                    )
                                }

                                AppTab.PROFILE -> {
                                    val viewedId = uiState.viewedProfileUserId
                                    val profileUser = if (viewedId != null) {
                                        uiState.allUsers.find { it.id == viewedId } ?: uiState.currentUser
                                    } else {
                                        uiState.currentUser
                                    }
                                    val isOwnProfile = viewedId == null || viewedId == uiState.currentUser?.id

                                    ProfileScreen(
                                        user = profileUser,
                                        isOwnProfile = isOwnProfile,
                                        allUsers = uiState.allUsers,
                                        posts = uiState.posts,
                                        reels = uiState.reels,
                                        stories = uiState.stories,
                                        savedPosts = uiState.savedPosts,
                                        onBackFromOtherProfile = {
                                            viewModel.clearViewedProfile()
                                            viewModel.selectTab(AppTab.HOME)
                                        },
                                        onToggleFollow = { viewModel.toggleFollow(it) },
                                        onApproveFollowRequest = { userId ->
                                            viewModel.approveFollowRequest(userId)
                                        },
                                        onMessageUser = { userId ->
                                            viewModel.openChat(userId)
                                        },
                                        onOpenEditProfile = { viewModel.openEditProfile() },
                                        onOpenUserSwitcher = { viewModel.openUserSwitcher() },
                                        onDoubleTapSwitchAccount = {
                                            viewModel.switchToNextAccount { newUsername ->
                                                Toast.makeText(
                                                    context,
                                                    "Switched account to @$newUsername",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        onOpenSettings = { viewModel.openSettings() },
                                        onOpenCreatorInsights = { viewModel.openCreatorInsights() },
                                        onOpenBlueTickModal = { viewModel.openBlueTickModal() },
                                        onOpenMonetizationHub = { viewModel.openMonetizationHub(3) },
                                        onOpenCreatorWallet = { viewModel.openMonetizationHub(0) },
                                        onOpenMonetizeAnalyst = { viewModel.openMonetizationHub(1) },
                                        onOpenVetaAuth = { viewModel.openVetaAuth() },
                                        onTogglePostLike = { viewModel.togglePostLike(it) },
                                        onDoubleTapPostLike = { viewModel.doubleTapLikePost(it) },
                                        onTogglePostSave = { viewModel.togglePostSave(it) },
                                        onToggleReelLike = { viewModel.toggleReelLike(it) },
                                        onDoubleTapReelLike = { viewModel.doubleTapLikeReel(it) },
                                        onToggleReelSave = { viewModel.toggleReelSave(it) },
                                        onOpenStoryAt = { idx -> viewModel.openStory(idx) },
                                        onOpenLikesSheet = { post -> viewModel.openPostLikesSheet(post) },
                                        onShareContent = { text -> viewModel.openShareDialog(text) },
                                        onUserClick = { uid -> viewModel.openUserProfile(uid) },
                                        onPostClick = { viewModel.openComments(it.id, "Post by @${it.authorUsername}") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Full-screen Story Viewer with Double-Tap Like, Polls, & Uploader Profile Link
        val activeStoryIdx = uiState.activeStoryIndex
        if (activeStoryIdx != null && activeStoryIdx in uiState.stories.indices) {
            val activeStory = uiState.stories[activeStoryIdx]
            val storyAuthor = uiState.allUsers.find { it.id == activeStory.authorId }
            StoryViewerModal(
                story = activeStory,
                authorUser = storyAuthor,
                currentIndex = activeStoryIdx,
                totalStories = uiState.stories.size,
                onNext = { viewModel.nextStory() },
                onPrevious = { viewModel.previousStory() },
                onClose = { viewModel.closeStory() },
                onToggleLike = { viewModel.toggleStoryLike(activeStory) },
                onDoubleTapLike = { viewModel.doubleTapLikeStory(activeStory) },
                onVotePoll = { option -> viewModel.voteStoryPoll(activeStory.id, option) },
                onAuthorClick = { authorId ->
                    viewModel.openUserProfile(authorId)
                },
                onToggleFollowAuthor = { authorId ->
                    viewModel.toggleFollowByUserId(authorId)
                },
                onSendReply = { reply ->
                    viewModel.openChat(activeStory.authorId)
                    viewModel.sendMessage(reply)
                    viewModel.closeStory()
                }
            )
        }

        // Comment Drawer
        if (uiState.activeCommentTargetId != null) {
            CommentBottomSheet(
                comments = comments,
                targetTitle = uiState.activeCommentTargetTitle,
                onDismiss = { viewModel.closeComments() },
                onAddComment = { viewModel.addComment(it) },
                onToggleLike = { viewModel.toggleCommentLike(it) }
            )
        }

        // Share Dialog
        val shareContent = uiState.shareDialogContent
        if (shareContent != null) {
            ShareDialog(
                content = shareContent,
                onDismiss = { viewModel.closeShareDialog() }
            )
        }

        // User Switcher Dialog
        if (uiState.isUserSwitcherOpen) {
            UserSwitcherDialog(
                allUsers = uiState.allUsers,
                currentUser = uiState.currentUser,
                onSelectUser = { viewModel.switchUser(it) },
                onOpenGmailLogin = { viewModel.openVetaAuth() },
                onLaunchGoogleCredentialSignIn = {
                    coroutineScope.launch { googleAuthHelper.onGoogleSignInClicked() }
                },
                onDismiss = { viewModel.closeUserSwitcher() }
            )
        }

        // Edit Profile Modal
        if (uiState.isEditProfileOpen) {
            EditProfileModal(
                user = uiState.currentUser,
                onDismiss = { viewModel.closeEditProfile() },
                onSave = { fullName, username, bio, website, category ->
                    viewModel.updateProfile(fullName, username, bio, website, category)
                }
            )
        }

        // Creator Insights Modal
        if (uiState.isCreatorInsightsOpen) {
            CreatorInsightsModal(
                user = uiState.currentUser,
                onDismiss = { viewModel.closeCreatorInsights() }
            )
        }

        // Dynamic Island-Inspired Floating Pill (Spotpix Live Pill when away from Reels)
        val activeMinimizedReel = uiState.minimizedReel
        if (activeMinimizedReel != null &&
            uiState.currentTab != AppTab.REELS &&
            !uiState.isMessagesScreenOpen &&
            !uiState.isNotificationsScreenOpen &&
            uiState.activeStoryIndex == null
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = androidx.compose.ui.Alignment.TopCenter
            ) {
                SpotpixLiveFloatingPill(
                    reel = activeMinimizedReel,
                    isPlaying = uiState.isLivePillPlaying,
                    watchSeconds = uiState.reelWatchSeconds,
                    onTickWatchTime = { viewModel.tickReelWatchTime() },
                    onTogglePlayPause = { viewModel.toggleLivePillPlayPause() },
                    onReturnToReel = { viewModel.selectTab(AppTab.REELS) },
                    onDismiss = { viewModel.dismissLivePill() }
                )
            }
        }

        // Comprehensive Settings Modal with Interactive Architecture Map, Side-Slide & Quick-Jump Rail
        if (uiState.isSettingsOpen) {
            SettingsModal(
                currentUser = uiState.currentUser,
                isSuperAdmin = uiState.isSuperAdmin,
                themeMode = uiState.themeMode,
                appBackgroundStyle = uiState.appBackgroundStyle,
                uploadQuality = uiState.uploadQuality,
                dataSaverMode = uiState.dataSaverMode,
                autoPlayVideos = uiState.autoPlayVideos,
                hideLikeCountsDefault = uiState.hideLikeCountsDefault,
                twoFactorEnabled = uiState.twoFactorEnabled,
                onThemeModeChange = { viewModel.setThemeMode(it) },
                onAppBackgroundStyleChange = { viewModel.setAppBackgroundStyle(it) },
                onUploadQualityChange = { viewModel.setUploadQuality(it) },
                onDataSaverChange = { viewModel.setDataSaverMode(it) },
                onAutoPlayChange = { viewModel.setAutoPlayVideos(it) },
                onHideLikesChange = { viewModel.setHideLikeCountsDefault(it) },
                onTwoFactorChange = { viewModel.setTwoFactorEnabled(it) },
                onPrivacyChange = { viewModel.setCurrentUserPrivacy(it) },
                onOpenUserSwitcher = { viewModel.openUserSwitcher() },
                onOpenVetaAuth = { viewModel.openVetaAuth() },
                onOpenBlueTickModal = { viewModel.openBlueTickModal() },
                onOpenMonetizationHub = { viewModel.openMonetizationHub() },
                onOpenSuperAdminConsole = { viewModel.openSuperAdminConsole() },
                onSwitchToSuperAdminAndOpenConsole = { viewModel.switchToSuperAdminAccountAndOpenConsole() },
                onResetData = { viewModel.resetData() },
                onDismiss = { viewModel.closeSettings() }
            )
        }

        // Spotpix by VETA Login & Registration Modal
        if (uiState.isVetaAuthOpen) {
            SpotpixVetaAuthModal(
                allUsers = uiState.allUsers,
                currentUser = uiState.currentUser,
                onLoginWithIdentifier = { id, cb -> viewModel.loginWithIdentifier(id, cb) },
                onRegisterNewUser = { u, f, e, cb -> viewModel.registerSpotpixAccount(u, f, e, cb) },
                onSelectGoogleAccount = { userId -> viewModel.switchUser(userId) },
                onLaunchGoogleCredentialSignIn = {
                    coroutineScope.launch { googleAuthHelper.onGoogleSignInClicked() }
                },
                onDismiss = { viewModel.closeVetaAuth() }
            )
        }

        // Blue Tick Verification Modal
        if (uiState.isBlueTickModalOpen) {
            BlueTickVerificationModal(
                user = uiState.currentUser,
                remoteConfig = uiState.remoteConfig,
                onSubmitApplication = { docType, docName ->
                    viewModel.submitBlueTickRequest(docType, docName)
                },
                onDismiss = { viewModel.closeBlueTickModal() }
            )
        }

        // Creator Monetization, Wallet, Analyst & Business Paid Ads + UPI Checkout Modal
        if (uiState.isMonetizationHubOpen) {
            VetaMonetizationAndAdsModal(
                user = uiState.currentUser,
                campaigns = uiState.campaigns,
                transactions = uiState.transactions,
                remoteConfig = uiState.remoteConfig,
                initialTab = uiState.monetizationHubInitialTab,
                onApplyMonetization = { viewModel.applyForCreatorMonetization() },
                onSimulateMonetizedViews = { views -> viewModel.simulateMonetizedViews(views) },
                onWithdrawEarnings = { usd, method, dest, onSuccess ->
                    viewModel.withdrawCreatorEarnings(usd, method, dest, onSuccess)
                },
                onCreateAndPayCampaign = { title, cap, urls, pkg, days, amt, method, onSuccess ->
                    viewModel.createAndPayAdCampaign(title, cap, urls, pkg, days, amt, method, onSuccess)
                },
                onUpdateCampaignStatus = { id, st -> viewModel.updateAdCampaignStatus(id, st) },
                onDeleteCampaign = { id -> viewModel.deleteAdCampaign(id) },
                onDismiss = { viewModel.closeMonetizationHub() }
            )
        }

        // VETA Super Admin Console (Strictly restricted to Verified Super Admin)
        if (uiState.isSuperAdminConsoleOpen && uiState.isSuperAdmin) {
            VetaSuperAdminConsoleModal(
                currentUser = uiState.currentUser,
                allUsers = uiState.allUsers,
                posts = uiState.posts,
                reels = uiState.reels,
                campaigns = uiState.campaigns,
                transactions = uiState.transactions,
                remoteConfig = uiState.remoteConfig,
                onReviewBlueTick = { uid, approve -> viewModel.adminReviewBlueTick(uid, approve) },
                onReviewMonetization = { uid, approve -> viewModel.adminReviewMonetization(uid, approve) },
                onToggleSuspendUser = { uid, suspend -> viewModel.adminToggleUserSuspension(uid, suspend) },
                onUpdateCampaignStatus = { cid, st -> viewModel.updateAdCampaignStatus(cid, st) },
                onDeleteCampaign = { cid -> viewModel.deleteAdCampaign(cid) },
                onDeletePost = { pid -> viewModel.adminDeletePost(pid) },
                onDeleteReel = { rid -> viewModel.adminDeleteReel(rid) },
                onAdjustCreatorWallet = { uid, bal -> viewModel.adminAdjustCreatorWallet(uid, bal) },
                onUpdateRemoteConfig = { cfg -> viewModel.adminUpdateRemoteConfig(cfg) },
                onDismiss = { viewModel.closeSuperAdminConsole() }
            )
        }

        // Instagram-Style Post Likes User List Sheet
        val likesPost = uiState.activeLikesPost
        if (likesPost != null) {
            PostLikesBottomSheet(
                post = likesPost,
                allUsers = uiState.allUsers,
                onUserClick = { uid -> viewModel.openUserProfile(uid) },
                onToggleFollow = { u -> viewModel.toggleFollow(u) },
                onDismiss = { viewModel.closePostLikesSheet() }
            )
        }
    }
}
