package com.example.base

import android.content.Context
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
abstract class FirestoreEmulatorTestBase {

  protected lateinit var context: Context
  protected lateinit var db: FirebaseFirestore
  protected lateinit var auth: FirebaseAuth
  private lateinit var projectId: String
  private lateinit var databaseId: String

  companion object {
    @JvmStatic
    @BeforeClass
    fun checkEmulatorsReachable() {
      listOf(8085 to "Firestore", 9099 to "Auth").forEach { (port, name) ->
        try {
          Socket("127.0.0.1", port).use {}
        } catch (e: Exception) {
          throw IllegalStateException(
            "$name emulator is NOT listening on 127.0.0.1:$port. " +
              "Start emulators before running Gradle tests.",
            e,
          )
        }
      }
    }
  }

  @Before
  open fun setUpBase() {
    context = ApplicationProvider.getApplicationContext()

    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    projectId = FirebaseApp.getInstance().options.projectId ?: "demo-test-project"
    databaseId = context.getString(R.string.firestore_database_id)

    db = FirebaseFirestore.getInstance(databaseId)
    auth = Firebase.auth

    try {
      db.useEmulator("127.0.0.1", 8085)
      auth.useEmulator("127.0.0.1", 9099)
    } catch (_: IllegalStateException) {}

    val settings =
      FirebaseFirestoreSettings.Builder()
        .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
        .build()
    try {
      db.firestoreSettings = settings
    } catch (_: IllegalStateException) {}

    clearEmulatorData()
  }

  @After
  open fun tearDownBase() {
    try {
      auth.signOut()
      org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
    } catch (_: Exception) {}
    try {
      runBlocking { withTimeout(5_000) { db.terminate().await() } }
    } catch (_: Exception) {}
    try {
      org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
    } catch (_: Exception) {}
    FirebaseApp.clearInstancesForTest()
  }

  protected suspend fun signInTestUser(
    email: String = "user_${UUID.randomUUID()}@example.com",
    emailVerified: Boolean = true,
  ): String {
    val sub = UUID.nameUUIDFromBytes(email.toByteArray()).toString()
    val header =
      Base64.encodeToString(
        """{"alg":"none","typ":"JWT"}""".toByteArray(),
        Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
      )
    val payloadJson =
      JSONObject()
        .apply {
          put("iss", "https://accounts.google.com")
          put("aud", projectId)
          put("sub", sub)
          put("email", email)
          put("email_verified", emailVerified)
          put("name", email.substringBefore("@"))
          put("iat", System.currentTimeMillis() / 1000)
          put("exp", (System.currentTimeMillis() / 1000) + 3600)
        }
        .toString()
    val payload =
      Base64.encodeToString(
        payloadJson.toByteArray(),
        Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
      )
    val fakeIdToken = "$header.$payload."
    val credential = GoogleAuthProvider.getCredential(fakeIdToken, null)
    val result = auth.signInWithCredential(credential).await()
    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
    return result.user!!.uid
  }

  protected fun clearEmulatorData() {
    val encodedDb = java.net.URLEncoder.encode(databaseId, "UTF-8")
    deleteUrl(
      "http://127.0.0.1:8085/emulator/v1/projects/$projectId/databases/$encodedDb/documents"
    )
    deleteUrl("http://127.0.0.1:9099/emulator/v1/projects/$projectId/accounts")
  }

  private fun deleteUrl(urlString: String) {
    val conn = URL(urlString).openConnection() as HttpURLConnection
    conn.connectTimeout = 2_000
    conn.readTimeout = 2_000
    conn.requestMethod = "DELETE"
    try {
      conn.responseCode
    } finally {
      conn.disconnect()
    }
  }
}
