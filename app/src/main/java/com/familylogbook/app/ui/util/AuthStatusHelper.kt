package com.familylogbook.app.ui.util

import com.google.firebase.auth.FirebaseAuth

/**
 * Helper to check authentication status and provide user-friendly messages.
 */
object AuthStatusHelper {
    
    /**
     * Checks if user is authenticated.
     */
    fun isAuthenticated(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }
    
    /**
     * Gets authentication status message for user.
     */
    fun getAuthStatusMessage(): String {
        val user = FirebaseAuth.getInstance().currentUser
        return when {
            user == null -> "Nisi prijavljen/na. Aplikacija će automatski prijaviti anonimno."
            user.isAnonymous -> "Prijavljen/na anonimno. Nadogradi račun u postavkama za trajno spremanje podataka."
            else -> "Prijavljen/na kao: ${user.email ?: "Korisnik"}"
        }
    }
    
    /**
     * Gets user ID or null if not authenticated.
     */
    fun getUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
}
