package com.familylogbook.app.data.auth

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
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
    
    /**
     * Creates a new account with email and password.
     */
    suspend fun createAccountWithEmail(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: throw IllegalStateException("Failed to create account")
    }
    
    /**
     * Signs in with email and password.
     */
    suspend fun signInWithEmail(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw IllegalStateException("Failed to sign in")
    }
    
    /**
     * Upgrades anonymous account to email/password account.
     * Links the email credential to the existing anonymous account.
     */
    suspend fun linkEmailToAnonymousAccount(email: String, password: String): FirebaseUser {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("No user is currently signed in")
        
        if (!currentUser.isAnonymous) {
            throw IllegalStateException("User is not anonymous")
        }
        
        val credential = EmailAuthProvider.getCredential(email, password)
        val result = currentUser.linkWithCredential(credential).await()
        return result.user ?: throw IllegalStateException("Failed to link email")
    }
    
    /**
     * Signs in with Google credential.
     * For now, this is a placeholder - Google Sign-In requires additional setup.
     */
    suspend fun signInWithGoogleCredential(credential: AuthCredential): FirebaseUser {
        val currentUser = auth.currentUser
        
        return if (currentUser != null && currentUser.isAnonymous) {
            // Link Google credential to anonymous account
            val result = currentUser.linkWithCredential(credential).await()
            result.user ?: throw IllegalStateException("Failed to link Google account")
        } else {
            // Sign in with Google
            val result = auth.signInWithCredential(credential).await()
            result.user ?: throw IllegalStateException("Failed to sign in with Google")
        }
    }
    
    /**
     * Sends password reset email.
     */
    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }
}

