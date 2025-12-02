package com.familylogbook.app.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Manages Firebase Authentication.
 * On first launch, signs in anonymously.
 * Later can be upgraded to email/password or Google sign-in.
 */
class AuthManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    
    /**
     * Ensures user is signed in (anonymous if needed).
     * Returns the current user's UID.
     */
    suspend fun ensureSignedIn(): String {
        val currentUser = auth.currentUser
        
        return if (currentUser != null) {
            currentUser.uid
        } else {
            // Sign in anonymously
            val result = auth.signInAnonymously().await()
            result.user?.uid ?: throw IllegalStateException("Failed to sign in anonymously")
        }
    }
    
    /**
     * Gets current user UID, or null if not signed in.
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Gets current FirebaseUser.
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    /**
     * Checks if user is signed in anonymously.
     */
    fun isAnonymous(): Boolean {
        return auth.currentUser?.isAnonymous ?: false
    }
    
    /**
     * Signs out the current user.
     */
    fun signOut() {
        auth.signOut()
    }
}

