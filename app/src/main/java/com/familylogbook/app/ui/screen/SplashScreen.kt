package com.familylogbook.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.data.auth.AuthManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authManager: AuthManager,
    onAuthReady: (String) -> Unit,
    onError: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "FamilyOS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = "Učitavanje...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
    
    LaunchedEffect(Unit) {
        try {
            // Small delay for splash screen visibility
            delay(500)
            
            // Ensure user is signed in (async, no blocking)
            val userId = authManager.ensureSignedIn()
            
            // Small delay to show completion
            delay(300)
            
            onAuthReady(userId)
        } catch (e: Exception) {
            android.util.Log.e("SplashScreen", "Auth initialization failed: ${e.message}", e)
            onError()
        }
    }
}


