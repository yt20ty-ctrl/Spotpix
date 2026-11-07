package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.InstaLinkDatabase
import com.example.data.model.*
import com.example.data.remote.SpotpixFirestoreRepository
import com.example.data.repository.SocialRepository
import com.example.ui.components.CreateMode
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppTab {
    HOME,
    EXPLORE,
    CREATE,
    REELS,
    PROFILE
}

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

data class UiState(
    val currentTab: AppTab = AppTab.HOME,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currentUser: UserEntity? = null,
    val isSuperAdmin: Boolean = false,
    val viewedProfileUserId: String? = null,
    val allUsers: List<UserEntity> = emptyList(),
    val posts: List<PostEntity> = emptyList(),
    val reels: List<ReelEntity> = emptyList(),
    val stories: List<StoryEntity> = emptyList(),
    val notifications: List<NotificationEntity> = emptyList(),
    val allMessages: List<DirectMessageEntity> = emptyList(),
    val savedPosts: List<PostEntity> = emptyList(),
    val campaigns: List<AdCampaignEntity> = emptyList(),
    val transactions: List<PaymentTransactionEntity> = emptyList(),
    val remoteConfig: VetaRemoteConfig = VetaRemoteConfig(),
    val activeStoryIndex: Int? = null,
    val activeCommentTargetId: String? = null,
    val activeCommentTargetTitle: String = "",
    val activeChatUserId: String? = null,
    val activeLikesPost: PostEntity? = null,
    val minimizedReel: ReelEntity? = null,
    val isLivePillPlaying: Boolean = true,
    val reelWatchSeconds: Int = 84,
    val isMessagesScreenOpen: Boolean = false,
    val isNotificationsScreenOpen: Boolean = false,
    val isUserSwitcherOpen: Boolean = false,
    val isVetaAuthOpen: Boolean = false,
    val isBlueTickModalOpen: Boolean = false,
    val isMonetizationHubOpen: Boolean = false,
    val monetizationHubInitialTab: Int = 0,
    val isSuperAdminConsoleOpen: Boolean = false,
    val isEditProfileOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isCreatorInsightsOpen: Boolean = false,
    val isCreateMenuExpanded: Boolean = false,
    val selectedCreateMode: CreateMode = CreateMode.POST,
    val shareDialogContent: String? = null,
    val likesModalTargetTitle: String? = null,
    val searchQuery: String = "",
    val searchCategory: String = "All",
    val unreadNotificationsCount: Int = 0,
    val unreadMessagesCount: Int = 1,
    val uploadQuality: String = "4K Ultra",
    val dataSaverMode: Boolean = false,
    val autoPlayVideos: Boolean = true,
    val hideLikeCountsDefault: Boolean = false,
    val twoFactorEnabled: Boolean = true,
    val chatThemeName: String = "Liquid Sapphire",
    val appBackgroundStyle: String = "Cobalt Liquid Glass"
)

class SocialViewModel(application: Application) : AndroidViewModel(application) {

    private val db = InstaLinkDatabase.getDatabase(application)
    val repository = SocialRepository(db)
    private val firestoreRepository = SpotpixFirestoreRepository(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val currentComments: StateFlow<List<CommentEntity>> = _uiState
        .map { it.activeCommentTargetId }
        .distinctUntilChanged()
        .flatMapLatest { targetId ->
            if (targetId == null) flowOf(emptyList())
            else repository.getComments(targetId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeChatMessages: StateFlow<List<DirectMessageEntity>> = _uiState
        .map { it.activeChatUserId }
        .distinctUntilChanged()
        .flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else repository.getMessages(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        repository.initializeSampleDataIfEmpty(viewModelScope)

        viewModelScope.launch {
            repository.currentUser.collect { user ->
                val isAdmin = SocialRepository.isVerifiedSuperAdmin(user)
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isSuperAdmin = isAdmin,
                        isSuperAdminConsoleOpen = if (!isAdmin) false else it.isSuperAdminConsoleOpen
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.allUsers.collect { users ->
                _uiState.update { it.copy(allUsers = users) }
            }
        }

        viewModelScope.launch {
            repository.allPosts.collect { posts ->
                _uiState.update { it.copy(posts = posts) }
            }
        }

        viewModelScope.launch {
            repository.allReels.collect { reels ->
                _uiState.update { it.copy(reels = reels) }
            }
        }

        viewModelScope.launch {
            repository.allStories.collect { stories ->
                _uiState.update { it.copy(stories = stories) }
            }
        }

        viewModelScope.launch {
            repository.allNotifications.collect { notifs ->
                val unread = notifs.count { !it.isRead }
                _uiState.update { it.copy(notifications = notifs, unreadNotificationsCount = unread) }
            }
        }

        viewModelScope.launch {
            repository.allMessages.collect { msgs ->
                val unread = msgs.count { !it.isRead }
                _uiState.update { it.copy(allMessages = msgs, unreadMessagesCount = unread) }
            }
        }

        viewModelScope.launch {
            repository.savedPosts.collect { saved ->
                _uiState.update { it.copy(savedPosts = saved) }
            }
        }

        viewModelScope.launch {
            repository.allCampaigns.collect { camps ->
                _uiState.update { it.copy(campaigns = camps) }
            }
        }

        viewModelScope.launch {
            repository.allTransactions.collect { txs ->
                _uiState.update { it.copy(transactions = txs) }
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { state ->
            // If user leaves REELS tab while reels exist, show the Dynamic Island Live Pill!
            val newMinimizedReel = when {
                tab == AppTab.REELS -> null
                state.currentTab == AppTab.REELS && state.reels.isNotEmpty() -> state.reels.first()
                else -> state.minimizedReel
            }
            state.copy(
                currentTab = tab,
                viewedProfileUserId = if (tab == AppTab.PROFILE) null else state.viewedProfileUserId,
                minimizedReel = newMinimizedReel,
                isLivePillPlaying = if (newMinimizedReel != null && state.currentTab == AppTab.REELS) true else state.isLivePillPlaying,
                isCreateMenuExpanded = false,
                isMessagesScreenOpen = false,
                isNotificationsScreenOpen = false,
                activeChatUserId = null
            )
        }
    }

    fun toggleLivePillPlayPause() {
        _uiState.update { it.copy(isLivePillPlaying = !it.isLivePillPlaying) }
    }

    fun tickReelWatchTime() {
        _uiState.update { state ->
            if (state.isLivePillPlaying || state.currentTab == AppTab.REELS) {
                state.copy(reelWatchSeconds = state.reelWatchSeconds + 1)
            } else {
                state
            }
        }
    }

    fun dismissLivePill() {
        _uiState.update { it.copy(minimizedReel = null, isLivePillPlaying = false) }
    }

    fun toggleCreateMenu() {
        _uiState.update { it.copy(isCreateMenuExpanded = !it.isCreateMenuExpanded) }
    }

    fun closeCreateMenu() {
        _uiState.update { it.copy(isCreateMenuExpanded = false) }
    }

    fun openCreateWithMode(mode: CreateMode) {
        _uiState.update {
            it.copy(
                selectedCreateMode = mode,
                currentTab = AppTab.CREATE,
                isCreateMenuExpanded = false,
                isMessagesScreenOpen = false,
                isNotificationsScreenOpen = false
            )
        }
    }

    fun openUserProfile(userId: String) {
        val currentId = _uiState.value.currentUser?.id
        _uiState.update {
            it.copy(
                viewedProfileUserId = if (userId == currentId) null else userId,
                currentTab = AppTab.PROFILE,
                isCreateMenuExpanded = false,
                isMessagesScreenOpen = false,
                isNotificationsScreenOpen = false,
                activeStoryIndex = null,
                activeLikesPost = null
            )
        }
    }

    fun clearViewedProfile() {
        _uiState.update { it.copy(viewedProfileUserId = null) }
    }

    fun switchToNextAccount(onSwitched: (String) -> Unit = {}) {
        val users = _uiState.value.allUsers
        if (users.isEmpty()) return
        val currentId = _uiState.value.currentUser?.id
        val currentIndex = users.indexOfFirst { it.id == currentId }
        val nextUser = if (currentIndex in users.indices) {
            users[(currentIndex + 1) % users.size]
        } else {
            users.first()
        }
        viewModelScope.launch {
            repository.switchCurrentUser(nextUser.id)
            _uiState.update { it.copy(viewedProfileUserId = null) }
            onSwitched(nextUser.username)
        }
    }

    // Spotpix VETA Auth & Account Modals
    fun openVetaAuth() {
        _uiState.update { it.copy(isVetaAuthOpen = true) }
    }

    fun closeVetaAuth() {
        _uiState.update { it.copy(isVetaAuthOpen = false) }
    }

    fun loginWithIdentifier(identifier: String, onResult: (Boolean, String) -> Unit) {
        val trimmed = identifier.trim().removePrefix("@")
        val users = _uiState.value.allUsers
        val matched = users.find {
            it.username.equals(trimmed, ignoreCase = true) ||
                it.email.equals(trimmed, ignoreCase = true) ||
                it.fullName.equals(trimmed, ignoreCase = true)
        }
        if (matched != null) {
            if (matched.isSuspended) {
                onResult(false, "Account @${matched.username} is suspended by VETA Super Admin.")
                return
            }
            viewModelScope.launch {
                repository.switchCurrentUser(matched.id)
                _uiState.update { it.copy(viewedProfileUserId = null, isVetaAuthOpen = false) }
                onResult(true, "Welcome back to Spotpix by VETA, @${matched.username}!")
            }
        } else {
            onResult(false, "No Spotpix account found for '$identifier'. Try Google Sign-In or Sign Up.")
        }
    }

    fun registerSpotpixAccount(
        username: String,
        fullName: String,
        email: String,
        onComplete: (String) -> Unit
    ) {
        viewModelScope.launch {
            val created = repository.registerNewSpotpixUser(username, fullName, email)
            _uiState.update { it.copy(viewedProfileUserId = null, isVetaAuthOpen = false) }
            onComplete(created.username)
        }
    }

    fun onGoogleUserAuthenticated(
        firebaseUser: FirebaseUser,
        onComplete: (UserEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            val email = firebaseUser.email ?: "${firebaseUser.uid.take(8)}@gmail.com"
            val displayName = firebaseUser.displayName ?: email.substringBefore("@")
            val photoUrl = firebaseUser.photoUrl?.toString()
            val syncedUser = repository.syncGoogleAuthenticatedUser(
                uid = firebaseUser.uid,
                displayName = displayName,
                email = email,
                photoUrl = photoUrl
            )
            try {
                firestoreRepository.createOrSyncUserProfile(
                    username = syncedUser.username,
                    displayName = syncedUser.fullName,
                    email = syncedUser.email,
                    bio = syncedUser.bio,
                    avatarUrl = syncedUser.avatarUrl
                )
            } catch (_: Exception) {
                // Non-blocking if offline or rules restrict custom role
            }
            _uiState.update {
                it.copy(
                    viewedProfileUserId = null,
                    isVetaAuthOpen = false,
                    isUserSwitcherOpen = false
                )
            }
            onComplete(syncedUser)
        }
    }

    // Blue Tick Verification Flow
    fun openBlueTickModal() {
        _uiState.update { it.copy(isBlueTickModalOpen = true) }
    }

    fun closeBlueTickModal() {
        _uiState.update { it.copy(isBlueTickModalOpen = false) }
    }

    fun submitBlueTickRequest(docType: String, docName: String) {
        val me = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.submitBlueTickVerification(me.id, docType, docName)
        }
    }

    // Creator Monetization, Wallet, Analyst & Business Ads Hub
    fun openMonetizationHub(initialTab: Int = 0) {
        _uiState.update {
            it.copy(
                isMonetizationHubOpen = true,
                monetizationHubInitialTab = initialTab
            )
        }
    }

    fun closeMonetizationHub() {
        _uiState.update { it.copy(isMonetizationHubOpen = false) }
    }

    fun applyForCreatorMonetization() {
        val me = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.submitMonetizationApplication(me)
        }
    }

    fun simulateMonetizedViews(viewsDelta: Long = 10_000L) {
        val me = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.addMonetizedReelViews(me, viewsDelta)
        }
    }

    fun withdrawCreatorEarnings(
        amountUsd: Double,
        payoutMethod: String,
        payoutDestination: String,
        onSuccess: (PaymentTransactionEntity) -> Unit
    ) {
        val me = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            val tx = repository.withdrawCreatorEarnings(
                user = me,
                amountUsd = amountUsd,
                payoutMethod = payoutMethod,
                payoutDestination = payoutDestination
            )
            onSuccess(tx)
        }
    }

    fun createAndPayAdCampaign(
        title: String,
        caption: String,
        creativeUrls: List<String>,
        packageType: String,
        durationDays: Int,
        amountInr: Int,
        paymentMethod: String,
        onSuccess: (PaymentTransactionEntity) -> Unit
    ) {
        val me = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            val draft = repository.createAdCampaignDraft(
                owner = me,
                title = title,
                caption = caption,
                creativeUrls = creativeUrls,
                packageType = packageType,
                durationDays = durationDays,
                amountInr = amountInr
            )
            val tx = repository.verifyPaymentAndActivateCampaign(
                campaign = draft,
                payer = me,
                paymentMethod = paymentMethod
            )
            onSuccess(tx)
        }
    }

    fun updateAdCampaignStatus(campaignId: String, status: String) {
        viewModelScope.launch {
            repository.updateCampaignStatus(campaignId, status)
        }
    }

    fun deleteAdCampaign(campaignId: String) {
        viewModelScope.launch {
            repository.deleteCampaign(campaignId)
        }
    }

    // VETA Super Admin Console (Strictly restricted to VaanijSingh28@gmail.com)
    fun openSuperAdminConsole() {
        val me = _uiState.value.currentUser
        if (SocialRepository.isVerifiedSuperAdmin(me)) {
            _uiState.update { it.copy(isSuperAdminConsoleOpen = true) }
        }
    }

    fun closeSuperAdminConsole() {
        _uiState.update { it.copy(isSuperAdminConsoleOpen = false) }
    }

    fun adminReviewBlueTick(targetUserId: String, approve: Boolean) {
        val me = _uiState.value.currentUser
        if (!SocialRepository.isVerifiedSuperAdmin(me)) return
        viewModelScope.launch {
            repository.adminReviewBlueTick(me, targetUserId, approve)
        }
    }

    fun adminReviewMonetization(targetUserId: String, approve: Boolean) {
        val me = _uiState.value.currentUser
        if (!SocialRepository.isVerifiedSuperAdmin(me)) return
        viewModelScope.launch {
            repository.adminReviewMonetization(me, targetUserId, approve)
        }
    }

    fun adminToggleUserSuspension(targetUserId: String, suspend: Boolean) {
        val me = _uiState.value.currentUser
        if (!SocialRepository.isVerifiedSuperAdmin(me)) return
        viewModelScope.launch {
            repository.adminToggleUserSuspension(me, targetUserId, suspend)
        }
    }

    fun adminUpdateRemoteConfig(newConfig: VetaRemoteConfig) {
        val me = _uiState.value.currentUser
        if (!SocialRepository.isVerifiedSuperAdmin(me)) return
        _uiState.update {
            it.copy(
                remoteConfig = newConfig,
                appBackgroundStyle = newConfig.appBackgroundStyle
            )
        }
    }

    fun adminDeletePost(postId: String) {
        val me = _uiState.value.currentUser
        if (!SocialRepository.isVerifiedSuperAdmin(me)) return
        viewModelScope.launch {
            repository.adminDeletePost(me, postId)
        }
    }

    fun adminDeleteReel(reelId: String) {
        val me = _uiState.value.currentUser
        if (!SocialRepository.isVerifiedSuperAdmin(me)) return
        viewModelScope.launch {
            repository.adminDeleteReel(me, reelId)
        }
    }

    fun adminAdjustCreatorWallet(targetUserId: String, newRevenueInr: Double) {
        val me = _uiState.value.currentUser
        if (!SocialRepository.isVerifiedSuperAdmin(me)) return
        viewModelScope.launch {
            repository.adminAdjustCreatorWallet(me, targetUserId, newRevenueInr)
        }
    }

    fun switchToSuperAdminAccountAndOpenConsole() {
        viewModelScope.launch {
            repository.switchCurrentUser(SocialRepository.SUPER_ADMIN_UID)
            _uiState.update {
                it.copy(
                    viewedProfileUserId = null,
                    isSettingsOpen = false,
                    isSuperAdmin = true,
                    isSuperAdminConsoleOpen = true
                )
            }
        }
    }

    fun setAppBackgroundStyle(style: String) {
        _uiState.update {
            it.copy(
                appBackgroundStyle = style,
                remoteConfig = it.remoteConfig.copy(appBackgroundStyle = style)
            )
        }
    }

    // Likes List Sheet on Posts
    fun openPostLikesSheet(post: PostEntity) {
        _uiState.update { it.copy(activeLikesPost = post) }
    }

    fun closePostLikesSheet() {
        _uiState.update { it.copy(activeLikesPost = null) }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setUploadQuality(quality: String) {
        _uiState.update { it.copy(uploadQuality = quality) }
    }

    fun setDataSaverMode(enabled: Boolean) {
        _uiState.update { it.copy(dataSaverMode = enabled) }
    }

    fun setAutoPlayVideos(enabled: Boolean) {
        _uiState.update { it.copy(autoPlayVideos = enabled) }
    }

    fun setHideLikeCountsDefault(enabled: Boolean) {
        _uiState.update { it.copy(hideLikeCountsDefault = enabled) }
    }

    fun setTwoFactorEnabled(enabled: Boolean) {
        _uiState.update { it.copy(twoFactorEnabled = enabled) }
    }

    fun setChatThemeName(themeName: String) {
        _uiState.update { it.copy(chatThemeName = themeName) }
    }

    fun setCurrentUserPrivacy(isPrivate: Boolean) {
        val me = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.updateUserPrivacy(me.id, isPrivate)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSearchCategory(category: String) {
        _uiState.update { it.copy(searchCategory = category) }
    }

    fun openStory(index: Int) {
        val stories = _uiState.value.stories
        if (index in stories.indices) {
            _uiState.update { it.copy(activeStoryIndex = index) }
            viewModelScope.launch {
                repository.markStorySeen(stories[index].id)
            }
        }
    }

    fun nextStory() {
        val current = _uiState.value.activeStoryIndex ?: return
        val stories = _uiState.value.stories
        if (current + 1 < stories.size) {
            _uiState.update { it.copy(activeStoryIndex = current + 1) }
            viewModelScope.launch {
                repository.markStorySeen(stories[current + 1].id)
            }
        } else {
            closeStory()
        }
    }

    fun previousStory() {
        val current = _uiState.value.activeStoryIndex ?: return
        if (current > 0) {
            _uiState.update { it.copy(activeStoryIndex = current - 1) }
        } else {
            closeStory()
        }
    }

    fun closeStory() {
        _uiState.update { it.copy(activeStoryIndex = null) }
    }

    fun toggleStoryLike(story: StoryEntity) {
        viewModelScope.launch {
            repository.toggleStoryLike(story.id, story.isLikedByMe)
        }
    }

    fun doubleTapLikeStory(story: StoryEntity) {
        viewModelScope.launch {
            repository.forceLikeStory(story.id, story.isLikedByMe)
        }
    }

    fun voteStoryPoll(storyId: String, option: Int) {
        viewModelScope.launch {
            repository.voteStoryPoll(storyId, option)
        }
    }

    fun openComments(targetId: String, title: String) {
        _uiState.update {
            it.copy(
                activeCommentTargetId = targetId,
                activeCommentTargetTitle = title
            )
        }
    }

    fun closeComments() {
        _uiState.update {
            it.copy(
                activeCommentTargetId = null,
                activeCommentTargetTitle = ""
            )
        }
    }

    fun openLikesModal(title: String) {
        _uiState.update { it.copy(likesModalTargetTitle = title) }
    }

    fun closeLikesModal() {
        _uiState.update { it.copy(likesModalTargetTitle = null) }
    }

    fun openMessages() {
        _uiState.update { it.copy(isMessagesScreenOpen = true, isNotificationsScreenOpen = false, isCreateMenuExpanded = false) }
    }

    fun closeMessages() {
        _uiState.update { it.copy(isMessagesScreenOpen = false, activeChatUserId = null) }
    }

    fun openChat(userId: String) {
        _uiState.update { it.copy(isMessagesScreenOpen = true, activeChatUserId = userId) }
    }

    fun closeChat() {
        _uiState.update { it.copy(activeChatUserId = null) }
    }

    fun acceptMessageRequest(userId: String) {
        viewModelScope.launch {
            repository.acceptMessageRequest(userId)
        }
    }

    fun deleteMessageConversation(userId: String) {
        viewModelScope.launch {
            repository.deleteConversation(userId)
        }
    }

    fun openNotifications() {
        _uiState.update { it.copy(isNotificationsScreenOpen = true, isMessagesScreenOpen = false, isCreateMenuExpanded = false) }
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun closeNotifications() {
        _uiState.update { it.copy(isNotificationsScreenOpen = false) }
    }

    fun openUserSwitcher() {
        _uiState.update { it.copy(isUserSwitcherOpen = true) }
    }

    fun closeUserSwitcher() {
        _uiState.update { it.copy(isUserSwitcherOpen = false) }
    }

    fun openEditProfile() {
        _uiState.update { it.copy(isEditProfileOpen = true) }
    }

    fun closeEditProfile() {
        _uiState.update { it.copy(isEditProfileOpen = false) }
    }

    fun openSettings() {
        _uiState.update { it.copy(isSettingsOpen = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(isSettingsOpen = false) }
    }

    fun openCreatorInsights() {
        _uiState.update { it.copy(isCreatorInsightsOpen = true) }
    }

    fun closeCreatorInsights() {
        _uiState.update { it.copy(isCreatorInsightsOpen = false) }
    }

    fun openShareDialog(content: String) {
        _uiState.update { it.copy(shareDialogContent = content) }
    }

    fun closeShareDialog() {
        _uiState.update { it.copy(shareDialogContent = null) }
    }

    fun togglePostLike(post: PostEntity) {
        viewModelScope.launch {
            repository.togglePostLike(post.id, post.isLikedByMe)
        }
    }

    fun doubleTapLikePost(post: PostEntity) {
        viewModelScope.launch {
            repository.forceLikePost(post.id, post.isLikedByMe)
        }
    }

    fun togglePostSave(post: PostEntity) {
        viewModelScope.launch {
            repository.togglePostSave(post.id, post.isSavedByMe)
        }
    }

    fun toggleReelLike(reel: ReelEntity) {
        viewModelScope.launch {
            repository.toggleReelLike(reel.id, reel.isLikedByMe)
        }
    }

    fun doubleTapLikeReel(reel: ReelEntity) {
        viewModelScope.launch {
            repository.forceLikeReel(reel.id, reel.isLikedByMe)
        }
    }

    fun toggleReelSave(reel: ReelEntity) {
        viewModelScope.launch {
            repository.toggleReelSave(reel.id, reel.isSavedByMe)
        }
    }

    fun toggleFollow(user: UserEntity) {
        viewModelScope.launch {
            repository.toggleUserFollow(user)
        }
    }

    fun toggleFollowByUserId(userId: String) {
        val user = _uiState.value.allUsers.find { it.id == userId } ?: return
        toggleFollow(user)
    }

    fun approveFollowRequest(userId: String) {
        viewModelScope.launch {
            repository.approveFollowRequest(userId)
        }
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            repository.switchCurrentUser(userId)
            _uiState.update { it.copy(viewedProfileUserId = null) }
            closeUserSwitcher()
        }
    }

    fun addComment(text: String) {
        val targetId = _uiState.value.activeCommentTargetId ?: return
        val me = _uiState.value.currentUser ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(targetId, me, text.trim())
        }
    }

    fun toggleCommentLike(comment: CommentEntity) {
        viewModelScope.launch {
            repository.toggleCommentLike(comment.id, comment.isLikedByMe)
        }
    }

    fun sendMessage(text: String, isVoiceNote: Boolean = false, voiceDurationSec: Int = 0) {
        val receiverId = _uiState.value.activeChatUserId ?: return
        val me = _uiState.value.currentUser ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(me, receiverId, text.trim(), isVoiceNote, voiceDurationSec)
        }
    }

    fun createStory(
        mediaUrl: String,
        filterName: String,
        overlayText: String,
        pollQuestion: String?,
        pollOptionA: String?,
        pollOptionB: String?
    ) {
        val me = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.createStory(me, mediaUrl, filterName, overlayText, pollQuestion, pollOptionA, pollOptionB)
            selectTab(AppTab.HOME)
        }
    }

    fun createPost(
        mediaUrl: String,
        caption: String,
        location: String,
        filterName: String = "Original",
        overlayText: String = "",
        mediaAspect: Float = 1.0f,
        uploadQuality: String = "HD 1080p",
        hideLikes: Boolean = false,
        isVideo: Boolean = false
    ) {
        val me = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.createPost(
                author = me,
                mediaUrl = mediaUrl,
                caption = caption,
                location = location,
                filterName = filterName,
                overlayText = overlayText,
                mediaAspect = mediaAspect,
                uploadQuality = uploadQuality,
                hideLikes = hideLikes,
                isVideo = isVideo
            )
            selectTab(AppTab.HOME)
        }
    }

    fun createReel(
        videoUrl: String,
        caption: String,
        audioTitle: String,
        filterName: String = "Original",
        overlayText: String = "",
        uploadQuality: String = "4K Ultra"
    ) {
        val me = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.createReel(
                author = me,
                videoUrl = videoUrl,
                caption = caption,
                audioTitle = audioTitle,
                filterName = filterName,
                overlayText = overlayText,
                uploadQuality = uploadQuality
            )
            selectTab(AppTab.REELS)
        }
    }

    fun updateProfile(fullName: String, username: String, bio: String, website: String, category: String) {
        val me = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            val updated = me.copy(
                fullName = fullName,
                username = username,
                bio = bio,
                website = website,
                category = category
            )
            repository.updateUserProfile(updated)
            closeEditProfile()
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetDatabaseToDefaults()
        }
    }
}
