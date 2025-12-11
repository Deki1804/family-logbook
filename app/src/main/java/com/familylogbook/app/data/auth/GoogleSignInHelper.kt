package com.familylogbook.app.data.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Helper class for Google Sign-In integration.
 * Handles the Google Sign-In flow and credential management.
 */
class GoogleSignInHelper(private val context: Context) {
    
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Creates a GoogleSignInClient configured for Firebase Authentication.
     */
    fun getSignInClient(): GoogleSignInClient {
        // Get default web client ID from google-services.json
        // Firebase automatically configures this
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getDefaultWebClientId())
            .requestEmail()
            .build()
        
        return GoogleSignIn.getClient(context, gso)
    }
    
    /**
     * Gets the default web client ID from google-services.json.
     * This is required for Firebase Authentication with Google Sign-In.
     * The Google Services plugin automatically generates this resource from google-services.json.
     */
    private fun getDefaultWebClientId(): String {
        // The Google Services plugin automatically generates R.string.default_web_client_id
        // from google-services.json file. We access it via resources.
        return try {
            val resources = context.resources
            val clientIdResourceId = resources.getIdentifier(
                "default_web_client_id",
                "string",
                context.packageName
            )
            
            if (clientIdResourceId != 0) {
                resources.getString(clientIdResourceId)
            } else {
                android.util.Log.e(
                    "GoogleSignInHelper",
                    "default_web_client_id not found. Make sure google-services.json is in app/ folder and Google Services plugin is applied."
                )
                throw IllegalStateException(
                    "default_web_client_id not found. " +
                    "Please ensure google-services.json is properly configured and rebuild the app."
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("GoogleSignInHelper", "Error getting default_web_client_id", e)
            throw IllegalStateException(
                "Failed to get Google Sign-In client ID. " +
                "Make sure google-services.json is in app/ folder and Google Services plugin is applied. " +
                "Error: ${e.message}"
            )
        }
    }
    
    /**
     * Creates an Intent to start the Google Sign-In flow.
     * Use this with ActivityResultLauncher.
     */
    fun getSignInIntent(): Intent {
        return getSignInClient().signInIntent
    }
    
    /**
     * Handles the result from Google Sign-In activity.
     * Returns the GoogleSignInAccount if successful, null otherwise.
     */
    fun handleSignInResult(data: Intent?): GoogleSignInAccount? {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            account
        } catch (e: ApiException) {
            android.util.Log.e("GoogleSignInHelper", "Google sign-in failed", e)
            null
        }
    }
    
    /**
     * Gets Firebase Auth credential from GoogleSignInAccount.
     */
    fun getFirebaseCredential(account: GoogleSignInAccount): com.google.firebase.auth.AuthCredential {
        val idToken = account.idToken
            ?: throw IllegalStateException("Google ID token is null")
        return GoogleAuthProvider.getCredential(idToken, null)
    }
}
