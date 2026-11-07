package com.example.data.remote

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

data class FirestoreUserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val username: String = "",
  val displayName: String = "",
  val email: String = "",
  val bio: String = "",
  val avatarUrl: String = "",
  val role: String = "USER",
  val blueTickVerified: Boolean = false,
  @get:PropertyName("isMonetized") @PropertyName("isMonetized") val isMonetized: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

data class FirestoreSpotpixPost(
  @DocumentId val id: String = "",
  val userId: String = "",
  val authorUsername: String = "",
  val caption: String = "",
  val mediaUrl: String = "",
  val mediaType: String = "POST",
  val likesCount: Int = 0,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

class SpotpixFirestoreRepository(context: Context) {

  private val db =
    FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
  private val usersCollection = db.collection("users")
  private val postsCollection = db.collection("posts")

  private fun requireUserId(): String =
    Firebase.auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

  suspend fun createOrSyncUserProfile(
    username: String,
    displayName: String,
    email: String,
    bio: String,
    avatarUrl: String,
  ) {
    val uid = requireUserId()
    val docRef = usersCollection.document(uid)
    try {
      val snapshot = docRef.get().await()
      if (!snapshot.exists()) {
        val profile =
          FirestoreUserProfile(
            id = uid,
            userId = uid,
            username = username.take(40).ifBlank { "spotpix_user" },
            displayName = displayName.take(80).ifBlank { "Spotpix Creator" },
            email = email,
            bio = bio.take(300),
            avatarUrl = avatarUrl.take(500),
            role = "USER",
            blueTickVerified = false,
            isMonetized = false,
          )
        docRef.set(profile).await()
      } else {
        docRef
          .update(
            mapOf(
              "displayName" to displayName.take(80).ifBlank { "Spotpix Creator" },
              "avatarUrl" to avatarUrl.take(500),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, "users/$uid")
    }
  }

  suspend fun getUserProfile(userId: String = requireUserId()): FirestoreUserProfile? {
    return try {
      usersCollection.document(userId).get().await().toObject(FirestoreUserProfile::class.java)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, "users/$userId")
    }
  }

  fun getMyPostsStream(): Flow<List<FirestoreSpotpixPost>> = flow {
    val uid = requireUserId()
    emitAll(
      postsCollection
        .whereEqualTo("userId", uid)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(FirestoreSpotpixPost::class.java) }
        .catch { e ->
          if (e is Exception) {
            handleFirestoreError(e, OperationType.LIST, "posts")
          } else {
            throw e
          }
        }
    )
  }

  suspend fun createPost(
    postId: String,
    authorUsername: String,
    caption: String,
    mediaUrl: String,
    mediaType: String = "POST",
  ) {
    val uid = requireUserId()
    val post =
      FirestoreSpotpixPost(
        id = postId,
        userId = uid,
        authorUsername = authorUsername.take(40).ifBlank { "spotpix_user" },
        caption = caption.take(1000),
        mediaUrl = mediaUrl.take(500),
        mediaType = if (mediaType in listOf("POST", "REEL", "STORY")) mediaType else "POST",
        likesCount = 0,
      )
    try {
      postsCollection.document(postId).set(post).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, "posts/$postId")
    }
  }

  suspend fun updatePostCaptionAndLikes(postId: String, caption: String, likesCount: Int) {
    try {
      postsCollection
        .document(postId)
        .update(
          mapOf(
            "caption" to caption.take(1000),
            "likesCount" to likesCount.coerceIn(0, 100000000),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, "posts/$postId")
    }
  }

  suspend fun deletePost(postId: String) {
    try {
      postsCollection.document(postId).delete().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, "posts/$postId")
    }
  }
}
