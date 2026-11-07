package com.example.data.remote

import com.example.base.FirestoreEmulatorTestBase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class SpotpixFirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

  private lateinit var repository: SpotpixFirestoreRepository

  @Before
  fun setUp() {
    repository = SpotpixFirestoreRepository(context)
  }

  @Test
  fun createOrSyncUserProfile_succeedsForAuthenticatedUser() = runBlocking {
    val uid = signInTestUser("creator@spotpix.veta.io")
    repository.createOrSyncUserProfile(
      username = "creator_veta",
      displayName = "VETA Creator",
      email = "creator@spotpix.veta.io",
      bio = "Visual artist on Spotpix",
      avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
    )

    val loaded = repository.getUserProfile(uid)
    assertNotNull(loaded)
    assertEquals("creator_veta", loaded?.username)
    assertEquals("USER", loaded?.role)
  }

  @Test
  fun createUpdateAndDeletePost_succeedsForOwnerAndBlocksOtherUser() = runBlocking {
    signInTestUser("alice@spotpix.veta.io")
    repository.createPost(
      postId = "post_101",
      authorUsername = "alice_veta",
      caption = "First Spotpix Reel!",
      mediaUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
      mediaType = "REEL",
    )

    val myPosts = repository.getMyPostsStream().first()
    assertEquals(1, myPosts.size)
    assertEquals("First Spotpix Reel!", myPosts.first().caption)

    repository.updatePostCaptionAndLikes("post_101", "Updated Reel Caption", 25)
    val updatedPosts = repository.getMyPostsStream().first()
    assertEquals("Updated Reel Caption", updatedPosts.first().caption)
    assertEquals(25, updatedPosts.first().likesCount)

    // Switch to another user and verify they cannot update Alice's post
    signInTestUser("bob@spotpix.veta.io")
    try {
      repository.updatePostCaptionAndLikes("post_101", "Bob unauthorized edit", 999)
      fail("Expected permission denied when Bob updates Alice's post")
    } catch (e: RuntimeException) {
      assertTrue(e.message!!.contains("\"operationType\":\"update\""))
    }

    // Switch back to Alice and delete her own post
    signInTestUser("alice@spotpix.veta.io")
    repository.deletePost("post_101")
  }
}
