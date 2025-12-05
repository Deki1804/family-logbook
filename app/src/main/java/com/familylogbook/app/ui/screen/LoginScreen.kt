package com.familylogbook.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.data.auth.AuthManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authManager: AuthManager,
    isAnonymous: Boolean,
    onUpgradeSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSignUp) "Kreiraj račun" else "Prijavi se") },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Odustani")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isAnonymous) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "🔒 Nadogradi svoj račun",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Text(
                text = if (isSignUp) "Kreiraj trajni račun" else "Prijavi se na svoj račun",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            
            // Error message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = error,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            // Success message
            successMessage?.let { success ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = success,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                placeholder = { Text("tvoj@email.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !isLoading
            )
            
            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Lozinka") },
                placeholder = { Text("Unesi lozinku") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !isLoading
            )
            
            // Confirm password (only for sign up)
            if (isSignUp) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Potvrdi lozinku") },
                    placeholder = { Text("Potvrdi lozinku") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !isLoading
                )
            }
            
            // Sign in / Sign up button
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        successMessage = null
                        
                        try {
                            // Validate email format
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                errorMessage = "Molimo unesi valjanu email adresu"
                                isLoading = false
                                return@launch
                            }
                            
                            if (isSignUp) {
                                // Validate passwords match
                                if (password != confirmPassword) {
                                    errorMessage = "Lozinke se ne podudaraju"
                                    isLoading = false
                                    return@launch
                                }
                                
                                if (password.length < 6) {
                                    errorMessage = "Lozinka mora imati najmanje 6 znakova"
                                    isLoading = false
                                    return@launch
                                }
                                
                                if (isAnonymous) {
                                    // Upgrade anonymous account
                                    authManager.linkEmailToAnonymousAccount(email, password)
                                    successMessage = "Račun je uspješno nadograđen!"
                                } else {
                                    // Create new account
                                    authManager.createAccountWithEmail(email, password)
                                    successMessage = "Račun je uspješno kreiran!"
                                }
                            } else {
                                // Sign in
                                authManager.signInWithEmail(email, password)
                                successMessage = "Uspješno prijavljen!"
                            }
                            
                            // Wait a bit to show success message, then navigate
                            kotlinx.coroutines.delay(1000)
                            onUpgradeSuccess()
                        } catch (e: Exception) {
                            errorMessage = getFriendlyErrorMessage(e)
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && 
                    (!isSignUp || confirmPassword.isNotBlank())
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isSignUp) "Kreiraj račun" else "Prijavi se")
                }
            }
            
            // Toggle between sign in and sign up
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSignUp) "Već imaš račun? " else "Nemaš račun? ",
                    fontSize = 14.sp
                )
                Text(
                    text = if (isSignUp) "Prijavi se" else "Registriraj se",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        isSignUp = !isSignUp
                        errorMessage = null
                        successMessage = null
                    }
                )
            }
            
            // Forgot password
            if (!isSignUp) {
                TextButton(onClick = {
                    scope.launch {
                        if (email.isBlank()) {
                            errorMessage = "Molimo unesi svoj email prvo"
                            return@launch
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            errorMessage = "Please enter a valid email address"
                            return@launch
                        }
                        try {
                            authManager.sendPasswordResetEmail(email)
                            successMessage = "Email za reset lozinke poslan! Provjeri svoj inbox."
                        } catch (e: Exception) {
                            errorMessage = getFriendlyErrorMessage(e)
                        }
                    }
                }) {
                    Text("Zaboravljena lozinka?")
                }
            }
        }
    }
}

/**
 * Converts Firebase Auth exceptions to user-friendly error messages.
 */
private fun getFriendlyErrorMessage(exception: Exception): String {
    val message = exception.message ?: ""
    return when {
        message.contains("email address is badly formatted", ignoreCase = true) -> 
            "Molimo unesi valjanu email adresu"
        message.contains("password is too weak", ignoreCase = true) -> 
            "Lozinka je previše slaba. Molimo koristi jaču lozinku."
        message.contains("email address is already in use", ignoreCase = true) -> 
            "Ovaj email je već registriran. Pokušaj se prijaviti umjesto toga."
        message.contains("there is no user record", ignoreCase = true) -> 
            "Nije pronađen račun s ovim emailom. Provjeri svoj email ili se registriraj."
        message.contains("password is invalid", ignoreCase = true) || 
        message.contains("wrong password", ignoreCase = true) -> 
            "Netočna lozinka. Molimo pokušaj ponovo."
        message.contains("network", ignoreCase = true) -> 
            "Greška mreže. Molimo provjeri svoju internetsku vezu."
        message.contains("too many requests", ignoreCase = true) -> 
            "Previše pokušaja. Molimo pričekaj trenutak i pokušaj ponovo."
        else -> exception.message ?: "Došlo je do greške. Molimo pokušaj ponovo."
    }
}

