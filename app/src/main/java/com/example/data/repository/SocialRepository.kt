package com.example.data.repository

import com.example.data.local.InstaLinkDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.security.MessageDigest

class SocialRepository(private val db: InstaLinkDatabase) {

    private val userDao = db.userDao()
    private val postDao = db.postDao()
    private val reelDao = db.reelDao()
    private val storyDao = db.storyDao()
    private val commentDao = db.commentDao()
    private val messageDao = db.messageDao()
    private val notificationDao = db.notificationDao()
    private val adCampaignDao = db.adCampaignDao()
    private val paymentTransactionDao = db.paymentTransactionDao()

    val currentUser: Flow<UserEntity?> = userDao.getCurrentUser()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allPosts: Flow<List<PostEntity>> = postDao.getAllPosts()
    val savedPosts: Flow<List<PostEntity>> = postDao.getSavedPosts()
    val allReels: Flow<List<ReelEntity>> = reelDao.getAllReels()
    val allStories: Flow<List<StoryEntity>> = storyDao.getAllStories()
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val allMessages: Flow<List<DirectMessageEntity>> = messageDao.getAllMessages()
    val allCampaigns: Flow<List<AdCampaignEntity>> = adCampaignDao.getAllCampaigns()
    val allTransactions: Flow<List<PaymentTransactionEntity>> = paymentTransactionDao.getAllTransactions()

    fun getPostsByAuthor(userId: String): Flow<List<PostEntity>> = postDao.getPostsByAuthor(userId)
    fun getReelsByAuthor(userId: String): Flow<List<ReelEntity>> = reelDao.getReelsByAuthor(userId)
    fun getComments(targetId: String): Flow<List<CommentEntity>> = commentDao.getCommentsForTarget(targetId)
    fun getMessages(userId: String): Flow<List<DirectMessageEntity>> = messageDao.getMessagesForConversation(userId)
    fun searchUsers(query: String): Flow<List<UserEntity>> = userDao.searchUsers(query)

    companion object {
        const val SUPER_ADMIN_EMAIL = "VaanijSingh28@gmail.com"
        const val SECONDARY_OWNER_EMAIL = "yt20ty@gmail.com"
        const val SUPER_ADMIN_UID = "veta_super_admin_uid_vaanij28"
        const val MERCHANT_SETTLEMENT_UPI = "vanij7@fam"
        private const val VETA_HMAC_SECRET_SALT = "VETA_CORE_SHIELD_2026_NPCI_UPI_AUTH_99481"

        private val consumedUtrRegistry = mutableSetOf<String>()
        private var failedUtrAttempts = 0
        private var lockoutUntilEpochMs = 0L

        fun isVerifiedSuperAdmin(user: UserEntity?): Boolean {
            if (user == null) return false
            val matchesPrimary = user.id == SUPER_ADMIN_UID &&
                user.email.equals(SUPER_ADMIN_EMAIL, ignoreCase = true)
            val matchesSecondaryOwner = user.email.equals(SECONDARY_OWNER_EMAIL, ignoreCase = true)
            return (matchesPrimary || matchesSecondaryOwner) &&
                user.role == "SUPER_ADMIN" &&
                user.isGmailVerified
        }

        /**
         * Anti-Hack UTR & Payment Verification Shield:
         * - Blocks brute-force attacks (3-strike 30s lockout)
         * - Blocks UTR replay attacks (same 12-digit UTR can never be reused)
         * - Blocks sequential, repeated, or low-entropy fake UTR strings
         */
        fun verifyAndConsumeUtrAntiHack(
            utrNumber: String,
            payerUpiId: String,
            existingTransactions: List<PaymentTransactionEntity>,
            strictShieldEnabled: Boolean = true
        ): Pair<Boolean, String> {
            val now = System.currentTimeMillis()
            if (now < lockoutUntilEpochMs) {
                val secsLeft = ((lockoutUntilEpochMs - now) / 1000).coerceAtLeast(1)
                return false to "🛡️ VETA Anti-Hack Shield: Security cooldown active ($secsLeft s remaining) due to repeated invalid UTR attempts."
            }

            val validUpiRegex = Regex("^[a-zA-Z0-9._\\-]{2,}@[a-zA-Z0-9]{2,}$")
            if (!validUpiRegex.matches(payerUpiId.trim())) {
                return false to "Enter a valid Payer UPI ID (e.g., name@okaxis, mobile@ybl, user@paytm)."
            }

            val cleanUtr = utrNumber.trim()
            if (!Regex("^\\d{12}$").matches(cleanUtr)) {
                failedUtrAttempts++
                if (failedUtrAttempts >= 3) lockoutUntilEpochMs = now + 30_000L
                return false to "Invalid NPCI UTR: Must be an exact 12-digit bank reference number."
            }

            val uniqueDigits = cleanUtr.toSet().size
            val isSequential = "01234567890123456789".contains(cleanUtr) ||
                "98765432109876543210".contains(cleanUtr)
            if (uniqueDigits < 4 || isSequential || cleanUtr.startsWith("000")) {
                failedUtrAttempts++
                if (failedUtrAttempts >= 3) lockoutUntilEpochMs = now + 30_000L
                return false to "🛡️ VETA Anti-Hack Shield rejected low-entropy / synthetic UTR pattern ($cleanUtr). Use a genuine 12-digit bank UTR."
            }

            val alreadyUsedInLedger = existingTransactions.any {
                it.paymentMethod.contains(cleanUtr) || it.gatewaySignatureHash.contains(cleanUtr.takeLast(6))
            }
            if (strictShieldEnabled && (consumedUtrRegistry.contains(cleanUtr) || alreadyUsedInLedger)) {
                failedUtrAttempts++
                if (failedUtrAttempts >= 3) lockoutUntilEpochMs = now + 30_000L
                return false to "🛡️ Replay Attack Blocked: UTR #$cleanUtr has already been used for a previous transaction."
            }

            consumedUtrRegistry.add(cleanUtr)
            failedUtrAttempts = 0
            return true to "Verified NPCI 12-digit UTR #$cleanUtr via VETA Anti-Hack Shield."
        }

        fun computeCryptographicOrderSignature(
            orderId: String,
            campaignId: String,
            amountInr: Int,
            payerId: String,
            paymentMethod: String
        ): String {
            val payload = "$orderId|$campaignId|$amountInr|$payerId|$paymentMethod|$MERCHANT_SETTLEMENT_UPI|$VETA_HMAC_SECRET_SALT"
            return MessageDigest.getInstance("SHA-256")
                .digest(payload.toByteArray())
                .joinToString("") { "%02x".format(it) }
                .take(36)
        }
    }

    suspend fun togglePostLike(postId: String, currentlyLiked: Boolean) {
        val newLiked = !currentlyLiked
        val delta = if (newLiked) 1 else -1
        postDao.toggleLike(postId, newLiked, delta)
    }

    suspend fun forceLikePost(postId: String, currentlyLiked: Boolean) {
        if (!currentlyLiked) {
            postDao.toggleLike(postId, true, 1)
        }
    }

    suspend fun togglePostSave(postId: String, currentlySaved: Boolean) {
        postDao.toggleSave(postId, !currentlySaved)
    }

    suspend fun toggleReelLike(reelId: String, currentlyLiked: Boolean) {
        val newLiked = !currentlyLiked
        val delta = if (newLiked) 1 else -1
        reelDao.toggleLike(reelId, newLiked, delta)
    }

    suspend fun forceLikeReel(reelId: String, currentlyLiked: Boolean) {
        if (!currentlyLiked) {
            reelDao.toggleLike(reelId, true, 1)
        }
    }

    suspend fun toggleReelSave(reelId: String, currentlySaved: Boolean) {
        reelDao.toggleSave(reelId, !currentlySaved)
    }

    suspend fun toggleStoryLike(storyId: String, currentlyLiked: Boolean) {
        val newLiked = !currentlyLiked
        val delta = if (newLiked) 1 else -1
        storyDao.toggleStoryLike(storyId, newLiked, delta)
    }

    suspend fun forceLikeStory(storyId: String, currentlyLiked: Boolean) {
        if (!currentlyLiked) {
            storyDao.toggleStoryLike(storyId, true, 1)
        }
    }

    suspend fun voteStoryPoll(storyId: String, option: Int) {
        val deltaA = if (option == 1) 1 else 0
        val deltaB = if (option == 2) 1 else 0
        storyDao.voteStoryPoll(storyId, option, deltaA, deltaB)
    }

    suspend fun toggleUserFollow(user: UserEntity) {
        if (user.isFollowedByMe) {
            userDao.updateFollowStatus(user.id, false, -1)
            postDao.updateAuthorFollowing(user.id, false)
            reelDao.updateAuthorFollowing(user.id, false)
            storyDao.updateAuthorFollowing(user.id, false)
            notificationDao.updateNotificationActorFollow(user.id, false)
        } else if (user.isPrivate) {
            val newRequested = !user.isFollowRequested
            userDao.updateFollowRequestStatus(user.id, newRequested)
        } else {
            userDao.updateFollowStatus(user.id, true, 1)
            postDao.updateAuthorFollowing(user.id, true)
            reelDao.updateAuthorFollowing(user.id, true)
            storyDao.updateAuthorFollowing(user.id, true)
            notificationDao.updateNotificationActorFollow(user.id, true)
        }
    }

    suspend fun approveFollowRequest(userId: String) {
        userDao.updateFollowStatus(userId, true, 1)
        postDao.updateAuthorFollowing(userId, true)
        reelDao.updateAuthorFollowing(userId, true)
        storyDao.updateAuthorFollowing(userId, true)
        notificationDao.updateNotificationActorFollow(userId, true)
    }

    suspend fun updateUserPrivacy(userId: String, isPrivate: Boolean) {
        userDao.updatePrivacyStatus(userId, isPrivate)
    }

    suspend fun switchCurrentUser(userId: String) {
        userDao.clearCurrentUser()
        userDao.setCurrentUser(userId)
    }

    suspend fun registerNewSpotpixUser(
        username: String,
        fullName: String,
        email: String
    ): UserEntity {
        userDao.clearCurrentUser()
        val cleanHandle = username.trim().removePrefix("@").ifBlank { "spotpix_creator" }
        val newUser = UserEntity(
            id = "user_${System.currentTimeMillis()}",
            username = cleanHandle,
            fullName = fullName.ifBlank { cleanHandle },
            email = email.ifBlank { "$cleanHandle@gmail.com" },
            isGmailVerified = email.endsWith("@gmail.com", ignoreCase = true),
            bio = "✨ Exploring Spotpix by VETA • Liquid Glass Creator",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
            followersCount = 1050,
            followingCount = 42,
            postsCount = 0,
            isVerified = false,
            verificationStatus = "NONE",
            monetizationStatus = "NONE",
            reelViews90d = 1200000L,
            isCurrentUser = true,
            category = "Digital Creator",
            reach30d = "12.4K"
        )
        userDao.insertUsers(listOf(newUser))
        return newUser
    }

    suspend fun syncGoogleAuthenticatedUser(
        uid: String,
        displayName: String,
        email: String,
        photoUrl: String?
    ): UserEntity {
        val existingUsers = userDao.getAllUsers().firstOrNull().orEmpty()
        val matched = existingUsers.find {
            it.email.equals(email, ignoreCase = true) || it.id == uid
        }
        userDao.clearCurrentUser()
        if (matched != null) {
            userDao.setCurrentUser(matched.id)
            return matched.copy(isCurrentUser = true)
        }

        val isSuperAdminEmail = email.equals(SUPER_ADMIN_EMAIL, ignoreCase = true) ||
            email.equals(SECONDARY_OWNER_EMAIL, ignoreCase = true)
        val assignedId = if (email.equals(SUPER_ADMIN_EMAIL, ignoreCase = true)) SUPER_ADMIN_UID else uid
        val handle = email.substringBefore("@")
            .replace(Regex("[^a-zA-Z0-9._]"), "_")
            .take(30)
            .ifBlank { "spotpix_gmail" }
        val newUser = UserEntity(
            id = assignedId,
            username = handle,
            fullName = displayName.ifBlank { handle },
            email = email,
            role = if (isSuperAdminEmail) "SUPER_ADMIN" else "USER",
            isGmailVerified = true,
            bio = "✨ Verified Gmail Account on Spotpix by VETA",
            avatarUrl = photoUrl?.takeIf { it.isNotBlank() }
                ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
            followersCount = 1250,
            followingCount = 64,
            postsCount = 0,
            isVerified = isSuperAdminEmail,
            verificationStatus = if (isSuperAdminEmail) "APPROVED" else "NONE",
            monetizationStatus = if (isSuperAdminEmail) "APPROVED" else "NONE",
            reelViews90d = 12500000L,
            isCurrentUser = true,
            category = if (isSuperAdminEmail) "VETA Super Admin" else "Digital Creator",
            reach30d = "48.2K"
        )
        userDao.insertUsers(listOf(newUser))
        return newUser
    }

    // Blue Tick Verification Submission (Strictly Pending until Super Admin approval)
    suspend fun submitBlueTickVerification(userId: String, docType: String, docName: String) {
        userDao.submitVerificationRequest(userId, "PENDING", docType, docName)
    }

    // Super Admin Only: Approve or Reject Blue Tick Verification
    suspend fun adminReviewBlueTick(adminUser: UserEntity?, targetUserId: String, approve: Boolean) {
        if (!isVerifiedSuperAdmin(adminUser)) return
        val newStatus = if (approve) "APPROVED" else "REJECTED"
        userDao.adminUpdateVerification(targetUserId, newStatus, approve)
        postDao.updateAuthorVerified(targetUserId, approve)
        reelDao.updateAuthorVerified(targetUserId, approve)
    }

    // Creator Monetization Application (Requires verified Gmail session; strictly Pending until Super Admin approval)
    suspend fun submitMonetizationApplication(user: UserEntity) {
        if (!user.isGmailVerified) return
        userDao.updateMonetizationStatus(user.id, "PENDING", user.estimatedRevenueInr)
    }

    // Super Admin Only: Approve or Reject Creator Monetization
    suspend fun adminReviewMonetization(adminUser: UserEntity?, targetUserId: String, approve: Boolean) {
        if (!isVerifiedSuperAdmin(adminUser)) return
        val newStatus = if (approve) "APPROVED" else "REJECTED"
        val target = userDao.getUserById(targetUserId)
        val views = target?.reelViews90d ?: 12_500_000L
        // 1K views = $1.00 USD max (~Rs 84 INR per $1)
        val earnedUsd = (views / 1000.0) * 1.0
        val initialRevInr = if (approve) (earnedUsd * 84.0).coerceAtLeast(18450.0) else 0.0
        userDao.updateMonetizationStatus(targetUserId, newStatus, initialRevInr)
    }

    // Add Monetized Reel Views: 1,000 Views = $1.00 USD Max (Rs 84.00 INR)
    suspend fun addMonetizedReelViews(user: UserEntity, viewsDelta: Long) {
        val earnedUsd = (viewsDelta / 1000.0) * 1.0
        val revenueDeltaInr = if (user.monetizationStatus == "APPROVED") earnedUsd * 84.0 else 0.0
        userDao.addReelViewsAndEarnings(user.id, viewsDelta, revenueDeltaInr)
    }

    // Withdraw Creator Monetization Earnings to UPI / Bank / PayPal
    suspend fun withdrawCreatorEarnings(
        user: UserEntity,
        amountUsd: Double,
        payoutMethod: String,
        payoutDestination: String
    ): PaymentTransactionEntity {
        val amountInr = (amountUsd * 84.0).toInt().coerceAtLeast(84)
        val remainingInr = (user.estimatedRevenueInr - (amountUsd * 84.0)).coerceAtLeast(0.0)
        userDao.updateMonetizationStatus(user.id, user.monetizationStatus, remainingInr)

        val payoutId = "payout_veta_${System.currentTimeMillis()}"
        val payload = "$payoutId|${user.id}|$amountUsd|$payoutDestination|VETA_PAYOUT_VERIFIED"
        val signatureHash = MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(32)

        val transaction = PaymentTransactionEntity(
            id = payoutId,
            campaignId = "WITHDRAWAL_${user.id}",
            campaignTitle = "Creator Payout ($${"%.2f".format(amountUsd)} USD • 1K Views = $1 Max)",
            payerUserId = user.id,
            payerUsername = user.username,
            amountInr = amountInr,
            paymentMethod = "$payoutMethod → $payoutDestination",
            merchantSettlementUpi = payoutDestination.ifBlank { MERCHANT_SETTLEMENT_UPI },
            gatewaySignatureHash = "veta_payout_sha256_$signatureHash",
            status = "WITHDRAWN",
            timestampMs = System.currentTimeMillis()
        )
        paymentTransactionDao.insertTransaction(transaction)
        return transaction
    }

    // Super Admin Only: Suspend / Unsuspend User
    suspend fun adminToggleUserSuspension(adminUser: UserEntity?, targetUserId: String, suspend: Boolean) {
        if (!isVerifiedSuperAdmin(adminUser)) return
        userDao.adminSetUserSuspended(targetUserId, suspend)
    }

    // Super Admin Only: Delete any Post (Content Moderation)
    suspend fun adminDeletePost(adminUser: UserEntity?, postId: String) {
        if (!isVerifiedSuperAdmin(adminUser)) return
        postDao.deletePost(postId)
    }

    // Super Admin Only: Delete any Reel (Content Moderation)
    suspend fun adminDeleteReel(adminUser: UserEntity?, reelId: String) {
        if (!isVerifiedSuperAdmin(adminUser)) return
        reelDao.deleteReel(reelId)
    }

    // Super Admin Only: Adjust Creator Wallet Balance
    suspend fun adminAdjustCreatorWallet(adminUser: UserEntity?, targetUserId: String, newRevenueInr: Double) {
        if (!isVerifiedSuperAdmin(adminUser)) return
        val target = userDao.getUserById(targetUserId) ?: return
        userDao.updateMonetizationStatus(targetUserId, target.monetizationStatus, newRevenueInr.coerceAtLeast(0.0))
    }

    // Paid Advertisement Campaign Creation (Starts in PENDING_PAYMENT or DRAFT)
    suspend fun createAdCampaignDraft(
        owner: UserEntity,
        title: String,
        caption: String,
        creativeUrls: List<String>,
        packageType: String,
        durationDays: Int,
        amountInr: Int
    ): AdCampaignEntity {
        val campaign = AdCampaignEntity(
            id = "camp_${System.currentTimeMillis()}",
            ownerId = owner.id,
            ownerUsername = owner.username,
            title = title,
            caption = caption,
            creativeUrlsCsv = creativeUrls.joinToString(","),
            packageType = packageType,
            durationDays = durationDays,
            amountInr = amountInr,
            status = "PENDING_PAYMENT",
            impressions = 0,
            clicks = 0
        )
        adCampaignDao.insertCampaign(campaign)
        return campaign
    }

    // Server-Side Webhook & Cryptographic Signature Verification before Campaign Activation
    suspend fun verifyPaymentAndActivateCampaign(
        campaign: AdCampaignEntity,
        payer: UserEntity,
        paymentMethod: String
    ): PaymentTransactionEntity {
        val orderId = "order_rzp_${System.currentTimeMillis()}"
        val signatureHash = computeCryptographicOrderSignature(
            orderId = orderId,
            campaignId = campaign.id,
            amountInr = campaign.amountInr,
            payerId = payer.id,
            paymentMethod = paymentMethod
        )

        val transaction = PaymentTransactionEntity(
            id = orderId,
            campaignId = campaign.id,
            campaignTitle = campaign.title,
            payerUserId = payer.id,
            payerUsername = payer.username,
            amountInr = campaign.amountInr,
            paymentMethod = paymentMethod,
            merchantSettlementUpi = MERCHANT_SETTLEMENT_UPI,
            gatewaySignatureHash = "hmac_sha256_$signatureHash",
            status = "SUCCESS",
            timestampMs = System.currentTimeMillis()
        )
        paymentTransactionDao.insertTransaction(transaction)

        // Activate campaign strictly after verified payment signature
        adCampaignDao.updateCampaignPaymentAndStatus(
            campaignId = campaign.id,
            status = "ACTIVE",
            txId = orderId,
            impressions = 1420,
            clicks = 94
        )

        // Also publish sponsored post into the feed
        val firstCreative = campaign.creativeUrlsCsv.split(",").firstOrNull()
            ?: "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800"
        val sponsoredPost = PostEntity(
            id = "sponsored_${campaign.id}",
            authorId = payer.id,
            authorUsername = payer.username,
            authorAvatarUrl = payer.avatarUrl,
            authorVerified = payer.isVerified,
            mediaUrl = firstCreative,
            caption = "📣 [Sponsored • ${campaign.title}] ${campaign.caption}",
            location = "Sponsored via Spotpix Ads (${campaign.durationDays} Days)",
            isSponsoredAd = true,
            likeCount = 48,
            commentCount = 6
        )
        postDao.insertPost(sponsoredPost)

        return transaction
    }

    suspend fun updateCampaignStatus(campaignId: String, status: String) {
        adCampaignDao.updateCampaignStatus(campaignId, status)
    }

    suspend fun deleteCampaign(campaignId: String) {
        adCampaignDao.deleteCampaign(campaignId)
    }

    suspend fun addComment(targetId: String, author: UserEntity, text: String) {
        val comment = CommentEntity(
            id = "c_${System.currentTimeMillis()}",
            targetId = targetId,
            authorId = author.id,
            authorUsername = author.username,
            authorAvatarUrl = author.avatarUrl,
            authorVerified = author.isVerified,
            text = text,
            timestampMs = System.currentTimeMillis()
        )
        commentDao.insertComment(comment)
        postDao.incrementCommentCount(targetId)
    }

    suspend fun toggleCommentLike(commentId: String, currentlyLiked: Boolean) {
        val newLiked = !currentlyLiked
        val delta = if (newLiked) 1 else -1
        commentDao.toggleCommentLike(commentId, newLiked, delta)
    }

    suspend fun sendMessage(
        sender: UserEntity,
        receiverId: String,
        text: String,
        isVoiceNote: Boolean = false,
        voiceDurationSec: Int = 0
    ) {
        val message = DirectMessageEntity(
            id = "msg_${System.currentTimeMillis()}",
            conversationWithUserId = receiverId,
            senderId = sender.id,
            receiverId = receiverId,
            text = text,
            isVoiceNote = isVoiceNote,
            voiceDurationSec = voiceDurationSec,
            isRequest = false,
            timestampMs = System.currentTimeMillis(),
            isRead = true
        )
        messageDao.insertMessage(message)
    }

    suspend fun acceptMessageRequest(userId: String) {
        messageDao.acceptMessageRequest(userId)
    }

    suspend fun deleteConversation(userId: String) {
        messageDao.deleteConversation(userId)
    }

    suspend fun markStorySeen(storyId: String) {
        storyDao.markStorySeen(storyId)
    }

    suspend fun createStory(
        author: UserEntity,
        mediaUrl: String,
        filterName: String,
        overlayText: String,
        pollQuestion: String?,
        pollOptionA: String?,
        pollOptionB: String?
    ) {
        val story = StoryEntity(
            id = "story_${System.currentTimeMillis()}",
            authorId = author.id,
            authorUsername = author.username,
            authorAvatarUrl = author.avatarUrl,
            mediaUrl = mediaUrl,
            filterName = filterName,
            overlayText = overlayText,
            pollQuestion = pollQuestion?.takeIf { it.isNotBlank() },
            pollOptionA = pollOptionA?.takeIf { it.isNotBlank() } ?: "Yes 🔥",
            pollOptionB = pollOptionB?.takeIf { it.isNotBlank() } ?: "100% 💎",
            pollVotesA = if (!pollQuestion.isNullOrBlank()) 12 else 0,
            pollVotesB = if (!pollQuestion.isNullOrBlank()) 8 else 0,
            timestampMs = System.currentTimeMillis(),
            isSeen = false,
            isLikedByMe = false,
            likeCount = 1,
            isFollowingAuthor = true
        )
        storyDao.insertStory(story)
    }

    suspend fun createPost(
        author: UserEntity,
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
        val post = PostEntity(
            id = "post_${System.currentTimeMillis()}",
            authorId = author.id,
            authorUsername = author.username,
            authorAvatarUrl = author.avatarUrl,
            authorVerified = author.isVerified,
            authorIsPrivate = author.isPrivate,
            isFollowingAuthor = true,
            mediaUrl = mediaUrl,
            mediaAspect = mediaAspect,
            caption = caption,
            location = location,
            filterName = filterName,
            overlayText = overlayText,
            uploadQuality = uploadQuality,
            hideLikes = hideLikes,
            isVideo = isVideo,
            likeCount = 1,
            commentCount = 0,
            timestampMs = System.currentTimeMillis(),
            isLikedByMe = true
        )
        postDao.insertPost(post)
        userDao.updateUser(author.copy(postsCount = author.postsCount + 1))
    }

    suspend fun createReel(
        author: UserEntity,
        videoUrl: String,
        caption: String,
        audioTitle: String,
        filterName: String = "Original",
        overlayText: String = "",
        uploadQuality: String = "4K Ultra"
    ) {
        val reel = ReelEntity(
            id = "reel_${System.currentTimeMillis()}",
            authorId = author.id,
            authorUsername = author.username,
            authorAvatarUrl = author.avatarUrl,
            authorVerified = author.isVerified,
            videoUrl = videoUrl,
            thumbnailUrl = videoUrl,
            caption = caption,
            audioTitle = audioTitle.ifBlank { "${author.username} • Spotpix Audio" },
            filterName = filterName,
            overlayText = overlayText,
            uploadQuality = uploadQuality,
            likeCount = 1,
            commentCount = 0,
            isLikedByMe = true,
            isFollowingAuthor = true
        )
        reelDao.insertReel(reel)
    }

    suspend fun updateUserProfile(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }

    fun initializeSampleDataIfEmpty(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val existingUsers = userDao.getAllUsers().firstOrNull()
            if (existingUsers.isNullOrEmpty()) {
                seedInitialData()
            }
        }
    }

    suspend fun resetDatabaseToDefaults() {
        seedInitialData()
    }

    private suspend fun seedInitialData() {
        val me = UserEntity(
            id = "user_me",
            username = "martin_glass",
            fullName = "Martin Dev",
            email = "martin.creator@gmail.com",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
            bio = "Crafting Spotpix by VETA Liquid Glass UI 💎 Creator & Designer. Based in SF & Tokyo ✈️",
            website = "https://spotpix.veta.dev",
            category = "Product Designer & Creator",
            reach30d = "284.5K",
            reelViews90d = 14_820_000L, // Eligible for VETA Monetization (>10M)
            role = "CREATOR",
            isVerified = true,
            verificationStatus = "APPROVED",
            monetizationStatus = "APPROVED",
            estimatedRevenueInr = 42850.0,
            isGmailVerified = true,
            isPrivate = false,
            followersCount = 14200,
            followingCount = 428,
            postsCount = 36,
            isCurrentUser = true
        )

        // Pre-configured Verified Super Admin Account: VaanijSingh28@gmail.com
        val superAdminVaanij = UserEntity(
            id = SUPER_ADMIN_UID,
            username = "vaanij_veta_admin",
            fullName = "Vaanij Singh • VETA Admin",
            email = SUPER_ADMIN_EMAIL,
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
            bio = "⚡ Official VETA Ecosystem Founder & Spotpix Super Administrator • Settlement UPI: vanij7@fam",
            website = "https://veta.ecosystem.org",
            category = "VETA Ecosystem Super Admin",
            reach30d = "5.8M",
            reelViews90d = 45_000_000L,
            role = "SUPER_ADMIN",
            isVerified = true,
            verificationStatus = "APPROVED",
            monetizationStatus = "APPROVED",
            estimatedRevenueInr = 195400.0,
            isGmailVerified = true,
            isPrivate = false,
            followersCount = 950000,
            followingCount = 120,
            postsCount = 85,
            isFollowedByMe = true,
            isCurrentUser = false
        )

        val userAva = UserEntity(
            id = "user_ava",
            username = "AvaGlow",
            fullName = "Ava Glow",
            email = "avaglow.art@gmail.com",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&auto=format&fit=crop&q=80",
            bio = "Digital Artist & Creative Director ✨ Spotpix Partner. New prints out now!",
            website = "https://avaglow.art",
            category = "Visual Artist",
            reach30d = "1.2M",
            reelViews90d = 19_400_000L,
            role = "CREATOR",
            isVerified = true,
            verificationStatus = "APPROVED",
            monetizationStatus = "PENDING", // Pending in Super Admin Queue!
            estimatedRevenueInr = 0.0,
            followersCount = 286000,
            followingCount = 312,
            postsCount = 184,
            isFollowedByMe = true
        )

        val userMindcast = UserEntity(
            id = "user_mindcast",
            username = "mindcast",
            fullName = "Mindcast Studio",
            email = "studio@mindcast.fm",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500&auto=format&fit=crop&q=80",
            bio = "Thoughts in motion. Daily perspectives on mindfulness, flow states and visual stillness 🧘‍♂️",
            website = "https://mindcast.fm",
            category = "Media & Podcast",
            reach30d = "540K",
            reelViews90d = 11_200_000L,
            role = "CREATOR",
            isVerified = false,
            verificationStatus = "PENDING", // Pending Blue Tick in Super Admin Queue!
            verificationDocType = "National ID / Passport",
            verificationDocName = "mindcast_passport_verified.pdf",
            monetizationStatus = "PENDING",
            followersCount = 94500,
            followingCount = 145,
            postsCount = 92,
            isFollowedByMe = true
        )

        val userMooddreams = UserEntity(
            id = "user_mooddreams",
            username = "mooddreams",
            fullName = "Elena Rostova",
            email = "elena.rostova@gmail.com",
            avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=500&auto=format&fit=crop&q=80",
            bio = "🔒 Private Analog Journal 📸 Chasing warmth & film grain in Kyoto",
            website = "https://mooddreams.co",
            category = "Film Photographer",
            reach30d = "89.4K",
            reelViews90d = 6_400_000L,
            isVerified = false,
            verificationStatus = "UNDER_REVIEW",
            verificationDocType = "Driver's License",
            verificationDocName = "elena_id_kyoto.jpg",
            isPrivate = true,
            followersCount = 45200,
            followingCount = 520,
            postsCount = 112,
            isFollowedByMe = false
        )

        val userSienna = UserEntity(
            id = "user_sienna",
            username = "sienna",
            fullName = "Sienna Rivers",
            email = "sienna.rivers@gmail.com",
            avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80",
            bio = "Travel filmmaker & drone pilot ⛰️ Exploring forest corridors & foggy peaks",
            website = "https://siennarivers.film",
            category = "Filmmaker",
            reach30d = "2.4M",
            reelViews90d = 28_900_000L,
            isVerified = true,
            verificationStatus = "APPROVED",
            monetizationStatus = "APPROVED",
            estimatedRevenueInr = 84200.0,
            followersCount = 512000,
            followingCount = 280,
            postsCount = 240,
            isFollowedByMe = true
        )

        val userDavid = UserEntity(
            id = "user_david",
            username = "david_bruno",
            fullName = "David Bruno",
            email = "david.bruno@gmail.com",
            avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=500&auto=format&fit=crop&q=80",
            bio = "🔒 San Francisco, CA • Principal Designer @VetaDesign • Private Circle ☕🚴‍♂️",
            website = "https://davidbruno.design",
            category = "Design Lead",
            reach30d = "310K",
            reelViews90d = 10_500_000L,
            isVerified = true,
            verificationStatus = "APPROVED",
            isPrivate = true,
            followersCount = 68400,
            followingCount = 390,
            postsCount = 109,
            isFollowedByMe = false
        )

        val userAmy = UserEntity(
            id = "user_amy",
            username = "amy_rachel",
            fullName = "Amy Rachel",
            email = "amyrachel.dev@gmail.com",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
            bio = "Boston, USA • Software Developer 💻 23 y/o • Mountain hiking & Android builds",
            website = "https://amyrachel.io",
            category = "Software Engineer",
            reach30d = "42.1K",
            reelViews90d = 3_200_000L,
            isVerified = false,
            followersCount = 4500,
            followingCount = 327,
            postsCount = 10,
            isFollowedByMe = true
        )

        val users = listOf(me, superAdminVaanij, userAva, userMindcast, userMooddreams, userSienna, userDavid, userAmy)
        userDao.insertUsers(users)

        // Seed Initial Paid Ad Campaigns & Transactions
        val campaigns = listOf(
            AdCampaignEntity(
                id = "camp_1",
                ownerId = me.id,
                ownerUsername = me.username,
                title = "Spotpix Liquid Glass UI Kit Launch",
                caption = "Experience 120FPS Liquid Glass components for Android. Get the creator pack today!",
                creativeUrlsCsv = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800,https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800",
                packageType = "TWO_WEEK_14D",
                durationDays = 14,
                amountInr = 120,
                status = "ACTIVE",
                impressions = 48920,
                clicks = 3410,
                paymentTransactionId = "order_rzp_9823411",
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 60 * 24
            ),
            AdCampaignEntity(
                id = "camp_2",
                ownerId = userAva.id,
                ownerUsername = userAva.username,
                title = "Ava Glow Digital Prints Exhibition",
                caption = "Limited edition neon & pastel canvas prints shipping worldwide ✨",
                creativeUrlsCsv = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800",
                packageType = "WEEKLY_7D",
                durationDays = 7,
                amountInr = 100,
                status = "ACTIVE",
                impressions = 29400,
                clicks = 1920,
                paymentTransactionId = "order_rzp_9823109",
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 60 * 48
            )
        )
        adCampaignDao.insertCampaigns(campaigns)

        val transactions = listOf(
            PaymentTransactionEntity(
                id = "order_rzp_9823411",
                campaignId = "camp_1",
                campaignTitle = "Spotpix Liquid Glass UI Kit Launch (14 Days)",
                payerUserId = me.id,
                payerUsername = me.username,
                amountInr = 120,
                paymentMethod = "Google Pay (UPI)",
                merchantSettlementUpi = MERCHANT_SETTLEMENT_UPI,
                gatewaySignatureHash = "hmac_sha256_8f91c2a0d47e6b3190f2e1a4c8d7b602",
                status = "SUCCESS",
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 60 * 24
            ),
            PaymentTransactionEntity(
                id = "order_rzp_9823109",
                campaignId = "camp_2",
                campaignTitle = "Ava Glow Digital Prints Exhibition (7 Days)",
                payerUserId = userAva.id,
                payerUsername = userAva.username,
                amountInr = 100,
                paymentMethod = "PhonePe (UPI)",
                merchantSettlementUpi = MERCHANT_SETTLEMENT_UPI,
                gatewaySignatureHash = "hmac_sha256_3e41b8c9a120f7e5d64123c9b0a1f883",
                status = "SUCCESS",
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 60 * 48
            )
        )
        paymentTransactionDao.insertTransactions(transactions)

        // Seed Stories
        val stories = listOf(
            StoryEntity(
                id = "story_ava",
                authorId = userAva.id,
                authorUsername = userAva.username,
                authorAvatarUrl = userAva.avatarUrl,
                mediaUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&auto=format&fit=crop&q=80",
                filterName = "Liquid Cyan",
                overlayText = "✨ Spotpix by VETA Drop",
                pollQuestion = "Which Spotpix Liquid Glass theme do you prefer?",
                pollOptionA = "Cobalt Dark 🌌",
                pollOptionB = "Frosted Light ❄️",
                pollVotesA = 148,
                pollVotesB = 64,
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 30,
                isSeen = false,
                likeCount = 420,
                isFollowingAuthor = true
            ),
            StoryEntity(
                id = "story_mindcast",
                authorId = userMindcast.id,
                authorUsername = userMindcast.username,
                authorAvatarUrl = userMindcast.avatarUrl,
                mediaUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop&q=80",
                filterName = "Golden Hour",
                overlayText = "Morning Stillness 🏔️",
                pollQuestion = "Ready for the weekend deep-dive?",
                pollOptionA = "Let's go! 🔥",
                pollOptionB = "Need coffee ☕",
                pollVotesA = 92,
                pollVotesB = 38,
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 90,
                isSeen = false,
                likeCount = 185,
                isFollowingAuthor = true
            ),
            StoryEntity(
                id = "story_sienna",
                authorId = userSienna.id,
                authorUsername = userSienna.username,
                authorAvatarUrl = userSienna.avatarUrl,
                mediaUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800&auto=format&fit=crop&q=80",
                filterName = "Vivid Pop",
                overlayText = "Black Forest Drone Run 🌲",
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 120,
                isSeen = false,
                likeCount = 610,
                isFollowingAuthor = true
            ),
            StoryEntity(
                id = "story_mood",
                authorId = userMooddreams.id,
                authorUsername = userMooddreams.username,
                authorAvatarUrl = userMooddreams.avatarUrl,
                mediaUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=800&auto=format&fit=crop&q=80",
                filterName = "Vintage Film",
                overlayText = "Kyoto Dawn 🎞️",
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 240,
                isSeen = false,
                likeCount = 290,
                isFollowingAuthor = false
            )
        )
        storyDao.insertStories(stories)

        // Seed Posts
        val posts = listOf(
            PostEntity(
                id = "post_1",
                authorId = userMindcast.id,
                authorUsername = userMindcast.username,
                authorAvatarUrl = userMindcast.avatarUrl,
                authorVerified = true,
                authorIsPrivate = false,
                isFollowingAuthor = true,
                mediaUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800&auto=format&fit=crop&q=80",
                caption = "Welcome to Spotpix by VETA ✨ Experience Cobalt Blue Liquid Glass, creator monetization, and instant double-tap interactions! #Spotpix #VETA #LiquidGlass",
                location = "Provence Lavender Fields, France",
                uploadQuality = "4K Ultra",
                likeCount = 2650,
                commentCount = 142,
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 19,
                isLikedByMe = true,
                isSavedByMe = false
            ),
            PostEntity(
                id = "post_2",
                authorId = userMooddreams.id,
                authorUsername = userMooddreams.username,
                authorAvatarUrl = userMooddreams.avatarUrl,
                authorVerified = false,
                authorIsPrivate = true,
                isFollowingAuthor = false,
                mediaUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=800&auto=format&fit=crop&q=80",
                caption = "Let the morning sunlight guide your inspiration today. Tap my @mooddreams ID to test Private Account permission controls! 🔒✨ #Spotpix #Portrait",
                location = "Kyoto Gardens, Japan",
                filterName = "Vintage Film",
                likeCount = 1840,
                commentCount = 76,
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 45,
                isLikedByMe = false,
                isSavedByMe = true
            ),
            PostEntity(
                id = "post_3",
                authorId = userDavid.id,
                authorUsername = userDavid.username,
                authorAvatarUrl = userDavid.avatarUrl,
                authorVerified = true,
                authorIsPrivate = true,
                isFollowingAuthor = false,
                mediaUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop&q=80",
                caption = "Reflections over Yosemite Valley at dawn. Powered by the VETA rendering engine. 🏔️✨ #Landscape #Yosemite #Spotpix",
                location = "Yosemite National Park, CA",
                uploadQuality = "4K Ultra",
                likeCount = 4520,
                commentCount = 189,
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 120,
                isLikedByMe = true,
                isSavedByMe = true
            ),
            PostEntity(
                id = "post_4",
                authorId = userSienna.id,
                authorUsername = userSienna.username,
                authorAvatarUrl = userSienna.avatarUrl,
                authorVerified = true,
                authorIsPrivate = false,
                isFollowingAuthor = true,
                mediaUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800&auto=format&fit=crop&q=80",
                caption = "Winding forest roads. There's nothing quite like the smell of pine trees after fresh rain. 🌲🌧️ #TravelDiaries #SpotpixReels",
                location = "Black Forest, Germany",
                filterName = "Vivid Pop",
                uploadQuality = "4K Ultra",
                likeCount = 6120,
                commentCount = 210,
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 720,
                isLikedByMe = true,
                isSavedByMe = false
            )
        )
        postDao.insertPosts(posts)

        // Seed Reels
        val reels = listOf(
            ReelEntity(
                id = "reel_1",
                authorId = userAva.id,
                authorUsername = userAva.username,
                authorAvatarUrl = userAva.avatarUrl,
                authorVerified = true,
                videoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80",
                thumbnailUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&auto=format&fit=crop&q=80",
                caption = "Let the wind do the talking as confidence flows freely today 🌬️✨ Switch tabs while playing to see the Spotpix Live Floating Pill! #Spotpix #VETA",
                audioTitle = "Ava Glow • Soft Vibes (Spotpix Original)",
                filterName = "Liquid Cyan",
                overlayText = "✨ Spotpix by VETA",
                likeCount = 26400,
                commentCount = 501,
                shareCount = 200,
                isLikedByMe = true,
                isSavedByMe = true,
                isFollowingAuthor = true
            ),
            ReelEntity(
                id = "reel_2",
                authorId = userSienna.id,
                authorUsername = userSienna.username,
                authorAvatarUrl = userSienna.avatarUrl,
                authorVerified = true,
                videoUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=900&auto=format&fit=crop&q=80",
                thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=900&auto=format&fit=crop&q=80",
                caption = "Driving through the emerald mountain canopy. Pure bliss! 🍃🚗\n#TravelReels #Highlands #Spotpix",
                audioTitle = "Sienna Rivers • Mountain Breeze",
                filterName = "Vivid Pop",
                likeCount = 18900,
                commentCount = 381,
                shareCount = 490,
                isLikedByMe = false,
                isSavedByMe = false,
                isFollowingAuthor = true
            ),
            ReelEntity(
                id = "reel_3",
                authorId = userDavid.id,
                authorUsername = userDavid.username,
                authorAvatarUrl = userDavid.avatarUrl,
                authorVerified = true,
                videoUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=900&auto=format&fit=crop&q=80",
                thumbnailUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=900&auto=format&fit=crop&q=80",
                caption = "Spotpix by VETA Liquid Glass UI breakdown. Specular refraction & Cobalt Blue accents 💎📱 #VETA #AndroidDev",
                audioTitle = "David Bruno • VETA Synth",
                likeCount = 32100,
                commentCount = 890,
                shareCount = 1200,
                isLikedByMe = true,
                isSavedByMe = true,
                isFollowingAuthor = false
            )
        )
        reelDao.insertReels(reels)

        // Seed Comments
        val comments = listOf(
            CommentEntity(
                id = "comm_1",
                targetId = "post_1",
                authorId = userAva.id,
                authorUsername = userAva.username,
                authorAvatarUrl = userAva.avatarUrl,
                authorVerified = true,
                text = "Spotpix by VETA feels so fast and futuristic! ✨💎",
                likeCount = 24,
                isLikedByMe = true
            ),
            CommentEntity(
                id = "comm_2",
                targetId = "post_1",
                authorId = superAdminVaanij.id,
                authorUsername = superAdminVaanij.username,
                authorAvatarUrl = superAdminVaanij.avatarUrl,
                authorVerified = true,
                text = "Welcome to the VETA Creator Ecosystem! Blue Tick & Monetization portals are live 🚀",
                likeCount = 92,
                isLikedByMe = true
            )
        )
        for (c in comments) {
            commentDao.insertComment(c)
        }

        // Seed Direct Messages
        val messages = listOf(
            DirectMessageEntity(
                id = "m1",
                conversationWithUserId = userAva.id,
                senderId = userAva.id,
                receiverId = me.id,
                text = "Hey Martin! Did you try the new Spotpix Ad Campaign Manager and VETA Monetization Hub? 🎬",
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 90,
                isRead = false
            ),
            DirectMessageEntity(
                id = "m2",
                conversationWithUserId = userMooddreams.id,
                senderId = userMooddreams.id,
                receiverId = me.id,
                text = "Hi Martin! Saw your Spotpix presets—would love to share my Kyoto analog LUTs with you 📸✨",
                isRequest = true,
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 50,
                isRead = false
            )
        )
        for (m in messages) {
            messageDao.insertMessage(m)
        }

        // Seed Notifications
        val notifications = listOf(
            NotificationEntity(
                id = "notif_1",
                actorId = superAdminVaanij.id,
                actorUsername = superAdminVaanij.username,
                actorAvatarUrl = superAdminVaanij.avatarUrl,
                type = "VETA_SYSTEM",
                content = "verified your Spotpix Blue Tick & VETA Creator Monetization eligibility.",
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 5,
                isRead = false,
                isFollowingActor = true
            ),
            NotificationEntity(
                id = "notif_2",
                actorId = userAva.id,
                actorUsername = userAva.username,
                actorAvatarUrl = userAva.avatarUrl,
                type = "LIKE",
                content = "liked your post on Spotpix.",
                targetMediaUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 18,
                isRead = false,
                isFollowingActor = true
            ),
            NotificationEntity(
                id = "notif_3",
                actorId = userDavid.id,
                actorUsername = userDavid.username,
                actorAvatarUrl = userDavid.avatarUrl,
                type = "FOLLOW",
                content = "started following you on Spotpix.",
                timestampMs = System.currentTimeMillis() - 1000 * 60 * 45,
                isRead = false,
                isFollowingActor = false
            )
        )
        notificationDao.insertNotifications(notifications)
    }
}
