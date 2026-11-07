package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY isCurrentUser DESC, followersCount DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' OR fullName LIKE '%' || :query || '%'")
    fun searchUsers(query: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isCurrentUser = 0")
    suspend fun clearCurrentUser()

    @Query("UPDATE users SET isCurrentUser = 1 WHERE id = :userId")
    suspend fun setCurrentUser(userId: String)

    @Query("UPDATE users SET isFollowedByMe = :isFollowed, isFollowRequested = 0, followersCount = followersCount + :delta WHERE id = :userId")
    suspend fun updateFollowStatus(userId: String, isFollowed: Boolean, delta: Int)

    @Query("UPDATE users SET isFollowRequested = :requested WHERE id = :userId")
    suspend fun updateFollowRequestStatus(userId: String, requested: Boolean)

    @Query("UPDATE users SET isPrivate = :isPrivate WHERE id = :userId")
    suspend fun updatePrivacyStatus(userId: String, isPrivate: Boolean)

    @Query("UPDATE users SET verificationStatus = :status, verificationDocType = :docType, verificationDocName = :docName WHERE id = :userId")
    suspend fun submitVerificationRequest(userId: String, status: String, docType: String, docName: String)

    @Query("UPDATE users SET verificationStatus = :status, isVerified = :isVerified WHERE id = :userId")
    suspend fun adminUpdateVerification(userId: String, status: String, isVerified: Boolean)

    @Query("UPDATE users SET monetizationStatus = :status, estimatedRevenueInr = :revenue WHERE id = :userId")
    suspend fun updateMonetizationStatus(userId: String, status: String, revenue: Double)

    @Query("UPDATE users SET reelViews90d = reelViews90d + :viewsDelta, estimatedRevenueInr = estimatedRevenueInr + :revenueDeltaInr WHERE id = :userId")
    suspend fun addReelViewsAndEarnings(userId: String, viewsDelta: Long, revenueDeltaInr: Double)

    @Query("UPDATE users SET isSuspended = :suspended WHERE id = :userId")
    suspend fun adminSetUserSuspended(userId: String, suspended: Boolean)
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestampMs DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE authorId = :userId ORDER BY timestampMs DESC")
    fun getPostsByAuthor(userId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE isSavedByMe = 1 ORDER BY timestampMs DESC")
    fun getSavedPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: String): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Query("UPDATE posts SET isLikedByMe = :liked, likeCount = likeCount + :delta WHERE id = :postId")
    suspend fun toggleLike(postId: String, liked: Boolean, delta: Int)

    @Query("UPDATE posts SET isSavedByMe = :saved WHERE id = :postId")
    suspend fun toggleSave(postId: String, saved: Boolean)

    @Query("UPDATE posts SET commentCount = commentCount + 1 WHERE id = :postId")
    suspend fun incrementCommentCount(postId: String)

    @Query("UPDATE posts SET isFollowingAuthor = :isFollowing WHERE authorId = :authorId")
    suspend fun updateAuthorFollowing(authorId: String, isFollowing: Boolean)

    @Query("UPDATE posts SET authorVerified = :verified WHERE authorId = :authorId")
    suspend fun updateAuthorVerified(authorId: String, verified: Boolean)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String)
}

@Dao
interface ReelDao {
    @Query("SELECT * FROM reels ORDER BY timestampMs DESC")
    fun getAllReels(): Flow<List<ReelEntity>>

    @Query("SELECT * FROM reels WHERE authorId = :userId ORDER BY timestampMs DESC")
    fun getReelsByAuthor(userId: String): Flow<List<ReelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReels(reels: List<ReelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: ReelEntity)

    @Query("UPDATE reels SET isLikedByMe = :liked, likeCount = likeCount + :delta WHERE id = :reelId")
    suspend fun toggleLike(reelId: String, liked: Boolean, delta: Int)

    @Query("UPDATE reels SET isSavedByMe = :saved WHERE id = :reelId")
    suspend fun toggleSave(reelId: String, saved: Boolean)

    @Query("UPDATE reels SET isFollowingAuthor = :isFollowing WHERE authorId = :authorId")
    suspend fun updateAuthorFollowing(authorId: String, isFollowing: Boolean)

    @Query("UPDATE reels SET authorVerified = :verified WHERE authorId = :authorId")
    suspend fun updateAuthorVerified(authorId: String, verified: Boolean)

    @Query("DELETE FROM reels WHERE id = :reelId")
    suspend fun deleteReel(reelId: String)
}

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY isSeen ASC, timestampMs DESC")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Query("UPDATE stories SET isSeen = 1 WHERE id = :storyId")
    suspend fun markStorySeen(storyId: String)

    @Query("UPDATE stories SET isLikedByMe = :liked, likeCount = likeCount + :delta WHERE id = :storyId")
    suspend fun toggleStoryLike(storyId: String, liked: Boolean, delta: Int)

    @Query("UPDATE stories SET userVotedOption = :option, pollVotesA = pollVotesA + :deltaA, pollVotesB = pollVotesB + :deltaB WHERE id = :storyId")
    suspend fun voteStoryPoll(storyId: String, option: Int, deltaA: Int, deltaB: Int)

    @Query("UPDATE stories SET isFollowingAuthor = :isFollowing WHERE authorId = :authorId")
    suspend fun updateAuthorFollowing(authorId: String, isFollowing: Boolean)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE targetId = :targetId ORDER BY timestampMs ASC")
    fun getCommentsForTarget(targetId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("UPDATE comments SET isLikedByMe = :liked, likeCount = likeCount + :delta WHERE id = :commentId")
    suspend fun toggleCommentLike(commentId: String, liked: Boolean, delta: Int)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM direct_messages WHERE conversationWithUserId = :userId ORDER BY timestampMs ASC")
    fun getMessagesForConversation(userId: String): Flow<List<DirectMessageEntity>>

    @Query("SELECT * FROM direct_messages ORDER BY timestampMs DESC")
    fun getAllMessages(): Flow<List<DirectMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DirectMessageEntity)

    @Query("UPDATE direct_messages SET isRead = 1 WHERE conversationWithUserId = :userId")
    suspend fun markMessagesAsRead(userId: String)

    @Query("UPDATE direct_messages SET isRequest = 0 WHERE conversationWithUserId = :userId")
    suspend fun acceptMessageRequest(userId: String)

    @Query("DELETE FROM direct_messages WHERE conversationWithUserId = :userId")
    suspend fun deleteConversation(userId: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestampMs DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("UPDATE notifications SET isFollowingActor = :isFollowing WHERE actorId = :actorId")
    suspend fun updateNotificationActorFollow(actorId: String, isFollowing: Boolean)
}

@Dao
interface AdCampaignDao {
    @Query("SELECT * FROM ad_campaigns ORDER BY timestampMs DESC")
    fun getAllCampaigns(): Flow<List<AdCampaignEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaigns(campaigns: List<AdCampaignEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: AdCampaignEntity)

    @Query("UPDATE ad_campaigns SET status = :status, paymentTransactionId = :txId, impressions = :impressions, clicks = :clicks WHERE id = :campaignId")
    suspend fun updateCampaignPaymentAndStatus(campaignId: String, status: String, txId: String?, impressions: Int, clicks: Int)

    @Query("UPDATE ad_campaigns SET status = :status WHERE id = :campaignId")
    suspend fun updateCampaignStatus(campaignId: String, status: String)

    @Query("DELETE FROM ad_campaigns WHERE id = :campaignId")
    suspend fun deleteCampaign(campaignId: String)
}

@Dao
interface PaymentTransactionDao {
    @Query("SELECT * FROM payment_transactions ORDER BY timestampMs DESC")
    fun getAllTransactions(): Flow<List<PaymentTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<PaymentTransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PaymentTransactionEntity)
}
