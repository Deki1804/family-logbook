package com.familylogbook.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.data.auth.AuthManager
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AccountInfoCard(
    authManager: AuthManager,
    onUpgradeClick: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onChangePassword: () -> Unit = {}
) {
    // Use state to force recomposition when auth state changes
    var currentUser by remember { mutableStateOf(authManager.getCurrentUser()) }
    var isAnonymous by remember { mutableStateOf(authManager.isAnonymous()) }
    var userId by remember { mutableStateOf(authManager.getCurrentUserId()) }
    
    // Listen to Firebase Auth state changes
    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
            isAnonymous = firebaseAuth.currentUser?.isAnonymous ?: false
            userId = firebaseAuth.currentUser?.uid
        }
        
        // Add listener
        auth.addAuthStateListener(authStateListener)
        
        // Initial refresh
        currentUser = authManager.getCurrentUser()
        isAnonymous = authManager.isAnonymous()
        userId = authManager.getCurrentUserId()
        
        // Remove listener on dispose
        onDispose {
            auth.removeAuthStateListener(authStateListener)
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Račun",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (isAnonymous) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "🔒 Anonimni račun",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "Tvoji podaci su pohranjeni lokalno. Nadogradi da sačuvaš račun trajno.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "✅ Prijavljen",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        currentUser?.email?.let { email ->
                            Text(
                                text = email,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
            
            userId?.let {
                Text(
                    text = "ID korisnika: ${it.take(8)}...",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            // Action buttons
            if (isAnonymous) {
                OutlinedButton(
                    onClick = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nadogradi račun")
                }
            } else {
                // For logged-in users, show account management options
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (authManager.hasEmailPasswordProvider()) {
                        OutlinedButton(
                            onClick = onChangePassword,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Promijeni lozinku")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Odjavi se")
                    }
                }
            }
            
            // Delete account button (always visible, but more prominent for non-anonymous)
            if (!isAnonymous) {
                OutlinedButton(
                    onClick = onDeleteAccount,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Obriši račun")
                }
            }
        }
    }
}

