package com.example.ui.components

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class GoogleAuthHelper(
  private val context: Context,
  private val onSignInSuccess: (FirebaseUser) -> Unit,
  private val onSignInError: (String) -> Unit,
) {
  private val credentialManager = CredentialManager.create(context)
  private val auth = Firebase.auth

  suspend fun attemptAutoSignIn() {
    if (auth.currentUser != null) {
      auth.currentUser?.let { onSignInSuccess(it) }
      return
    }

    val clientId = context.getString(R.string.default_web_client_id)
    val googleIdOption =
      GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    try {
      val result = credentialManager.getCredential(context, request)
      handleSignInResult(result)
    } catch (_: NoCredentialException) {
      Log.d("GoogleAuthHelper", "No authorized accounts available for silent sign-in.")
    } catch (e: GetCredentialException) {
      Log.w("GoogleAuthHelper", "Silent sign-in failed: ${e.message}")
    }
  }

  suspend fun onGoogleSignInClicked() {
    val clientId = context.getString(R.string.default_web_client_id)
    val signInWithGoogleOption =
      GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()

    val request =
      GetCredentialRequest.Builder().addCredentialOption(signInWithGoogleOption).build()

    try {
      val result = credentialManager.getCredential(context, request)
      handleSignInResult(result)
    } catch (_: GetCredentialCancellationException) {
      Log.d("GoogleAuthHelper", "User cancelled the Google Sign-In dialog.")
    } catch (e: GetCredentialException) {
      Log.e("GoogleAuthHelper", "Google Sign-In failed", e)
      onSignInError(e.localizedMessage ?: "Google Sign-In failed. Please try again.")
    }
  }

  private suspend fun handleSignInResult(result: GetCredentialResponse) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
      try {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCredential =
          GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        val authResult = auth.signInWithCredential(firebaseCredential).await()
        authResult.user?.let { user -> onSignInSuccess(user) }
          ?: onSignInError("Authentication succeeded, but no user profile was returned.")
      } catch (e: Exception) {
        Log.e("GoogleAuthHelper", "Firebase authentication failed", e)
        onSignInError(e.localizedMessage ?: "Failed to authenticate with Spotpix Firebase.")
      }
    } else {
      Log.w("GoogleAuthHelper", "Unexpected credential type: ${credential.type}")
      onSignInError("Unsupported credential type selected.")
    }
  }

  suspend fun signOut() {
    auth.signOut()
    try {
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    } catch (e: Exception) {
      Log.e("GoogleAuthHelper", "Failed to clear Credential Manager state", e)
    }
  }
}
