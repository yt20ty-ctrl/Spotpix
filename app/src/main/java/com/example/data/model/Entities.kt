package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val fullName: String,
    val email: String = "",
    val isGmailVerified: Boolean = true,
    val avatarUrl: String,
    val bio: String,
    val website: String = "",
    val category: String = "Digital Creator",
    val reach30d: String = "142.8K",
    val reelViews90d: Long = 10_400_000L,
    val role: String = "USER", // "USER" or "SUPER_ADMIN"
    val isVerified: Boolean = false,
    val verificationStatus: String = "NONE", // "NONE", "PENDING", "UNDER_REVIEW", "APPROVED", "REJECTED"
    val verificationDocType: String = "",
    val verificationDocName: String = "",
    val monetizationStatus: String = "NONE", // "NONE", "PENDING", "UNDER_REVIEW", "APPROVED", "REJECTED"
    val estimatedRevenueInr: Double = 0.0,
    val isSuspended: Boolean = false,
    val isPrivate: Boolean = false,
    val isFollowRequested: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val isFollowingMe: Boolean = false,
    val isFollowedByMe: Boolean = false,
    val isCurrentUser: Boolean = false
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUrl: String,
    val authorVerified: Boolean = false,
    val authorIsPrivate: Boolean = false,
    val isFollowingAuthor: Boolean = true,
    val mediaUrl: String,
    val mediaAspect: Float = 1.0f,
    val caption: String,
    val location: String = "",
    val filterName: String = "Original",
    val overlayText: String = "",
    val uploadQuality: String = "HD 1080p",
    val hideLikes: Boolean = false,
    val isVideo: Boolean = false,
    val isSponsoredAd: Boolean = false,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val timestampMs: Long = System.currentTimeMillis(),
    val isLikedByMe: Boolean = false,
    val isSavedByMe: Boolean = false
)

@Entity(tableName = "reels")
data class ReelEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUrl: String,
    val authorVerified: Boolean = false,
    val videoUrl: String,
    val thumbnailUrl: String,
    val caption: String,
    val audioTitle: String = "Original Audio",
    val filterName: String = "Original",
    val overlayText: String = "",
    val uploadQuality: String = "4K Ultra",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val timestampMs: Long = System.currentTimeMillis(),
    val isLikedByMe: Boolean = false,
    val isSavedByMe: Boolean = false,
    val isFollowingAuthor: Boolean = false
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUrl: String,
    val mediaUrl: String,
    val filterName: String = "Original",
    val overlayText: String = "",
    val pollQuestion: String? = null,
    val pollOptionA: String? = null,
    val pollOptionB: String? = null,
    val pollVotesA: Int = 0,
    val pollVotesB: Int = 0,
    val userVotedOption: Int? = null,
    val timestampMs: Long = System.currentTimeMillis(),
    val isSeen: Boolean = false,
    val hasUnseenStories: Boolean = true,
    val isLikedByMe: Boolean = false,
    val likeCount: Int = 0,
    val isFollowingAuthor: Boolean = true
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val targetId: String,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUrl: String,
    val authorVerified: Boolean = false,
    val text: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val isLikedByMe: Boolean = false
)

@Entity(tableName = "direct_messages")
data class DirectMessageEntity(
    @PrimaryKey val id: String,
    val conversationWithUserId: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val mediaUrl: String? = null,
    val isVoiceNote: Boolean = false,
    val voiceDurationSec: Int = 0,
    val isRequest: Boolean = false,
    val timestampMs: Long = System.currentTimeMillis(),
    val isRead: Boolean = true
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val actorId: String,
    val actorUsername: String,
    val actorAvatarUrl: String,
    val actorVerified: Boolean = false,
    val type: String,
    val content: String,
    val targetMediaUrl: String? = null,
    val targetPostId: String? = null,
    val timestampMs: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isFollowingActor: Boolean = false
)

@Entity(tableName = "ad_campaigns")
data class AdCampaignEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val ownerUsername: String,
    val title: String,
    val caption: String,
    val creativeUrlsCsv: String,
    val packageType: String, // "WEEKLY" or "TWO_WEEK"
    val durationDays: Int,
    val amountInr: Int,
    val status: String = "PENDING_PAYMENT", // "DRAFT", "PENDING_PAYMENT", "ACTIVE", "PAUSED", "REJECTED"
    val paymentTransactionId: String? = null,
    val impressions: Int = 0,
    val clicks: Int = 0,
    val timestampMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "payment_transactions")
data class PaymentTransactionEntity(
    @PrimaryKey val id: String,
    val campaignId: String,
    val campaignTitle: String,
    val payerUserId: String,
    val payerUsername: String,
    val amountInr: Int,
    val paymentMethod: String,
    val merchantSettlementUpi: String = "vanij7@fam",
    val gatewaySignatureHash: String,
    val status: String = "SUCCESS",
    val timestampMs: Long = System.currentTimeMillis()
)

data class VetaRemoteConfig(
    val blueTickMinFollowers: Int = 1000,
    val monetizationMinFollowers: Int = 1100,
    val monetizationMinReelViews90d: Long = 10_000_000L,
    val weeklyAdPriceInr: Int = 100,
    val twoWeekAdPriceInr: Int = 120,
    val merchantSettlementUpi: String = "vanij7@fam",
    val globalAnnouncementBanner: String = "",
    val maintenanceMode: Boolean = false,
    val freezeCreatorWithdrawals: Boolean = false,
    val strictAntiHackShield: Boolean = true,
    val appBackgroundStyle: String = "Cobalt Liquid Glass"
)

