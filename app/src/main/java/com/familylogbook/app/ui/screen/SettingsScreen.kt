package com.familylogbook.app.ui.screen

import android.app.DatePickerDialog as AndroidDatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.data.auth.AuthManager
// SmartHomeManager import removed - no longer needed for Parent OS
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.ui.util.DateFormatter
import com.familylogbook.app.ui.util.DateVisualTransformation
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.domain.model.PersonType
import com.familylogbook.app.domain.model.EntityType
import com.familylogbook.app.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    authManager: AuthManager? = null,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAddEntryForEntity: (String, com.familylogbook.app.domain.model.Category) -> Unit = { _, _ -> }
) {
    val scope = rememberCoroutineScope()
    
    // Force recomposition when returning from LoginScreen
    // This ensures AccountInfoCard refreshes auth status
    var refreshKey by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        // Refresh when screen is displayed
        refreshKey++
    }
    
    // Dialogs state
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) } // Local loading state for auth operations
    val viewModelSuccessMessage by viewModel.successMessage.collectAsState()
    val viewModelIsLoading by viewModel.isLoading.collectAsState()
    var localSuccessMessage by remember { mutableStateOf<String?>(null) }
    
    // Combine ViewModel success message with local success message
    val successMessage = viewModelSuccessMessage ?: localSuccessMessage
    
    // Change password state
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Postavke",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Account Section
        item(key = refreshKey) {
            authManager?.let { auth ->
                AccountInfoCard(
                    authManager = auth,
                    onUpgradeClick = {
                        refreshKey++ // Force refresh after returning from login
                        onNavigateToLogin()
                    },
                    onSignOut = { showSignOutDialog = true },
                    onDeleteAccount = { showDeleteAccountDialog = true },
                    onChangePassword = { showChangePasswordDialog = true }
                )
            }
        }
        
        item { Divider() }
        
        // Family Section
        item {
            FamilySection(
                viewModel = viewModel,
                scope = scope,
                errorMessage = errorMessage,
                onErrorMessageChange = { errorMessage = it },
                viewModelIsLoading = viewModelIsLoading,
                onEntityQuickAction = { entity, category ->
                    onNavigateToAddEntryForEntity(entity.id, category)
                }
            )
        }
        
        item { Divider() }
        
        // App Settings Section
        item {
            AppSettingsSection()
        }
        
        item { Divider() }
        
        // Export section
        item {
            ExportSection(viewModel = viewModel)
        }
        
        item { Divider() }
        
        // Advanced / Reset Section
        item {
            AdvancedSection(
                viewModel = viewModel,
                authManager = authManager,
                scope = scope
            )
        }
        
        item { Divider() }
        
        // About Section
        item {
            AboutSection()
        }
    }
    
    // Sign Out Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Odjavi se") },
            text = { 
                Text("Jesi li siguran da želiš odjaviti se? Možeš se ponovno prijaviti kasnije.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                isLoading = true
                                errorMessage = null
                                authManager?.signOut()
                                showSignOutDialog = false
                                // After sign out, app will automatically sign in anonymously
                                // or redirect to login screen
                            } catch (e: Exception) {
                                errorMessage = com.familylogbook.app.ui.util.ErrorHandler.getFriendlyErrorMessage(e)
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                ) {
                    Text("Odjavi se")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Odustani")
                }
            }
        )
    }
    
    // Delete Account Dialog (first confirmation)
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Obriši račun") },
            text = { 
                Column {
                    Text("Jesi li siguran da želiš obrisati svoj račun?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ovo će trajno obrisati:",
                        fontWeight = FontWeight.Bold
                    )
                    Text("• Tvoj Firebase račun")
                    Text("• Sve tvoje podatke (osobe, entitete, zapise)")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ova akcija se NE MOŽE poništiti!",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        showDeleteAccountConfirmDialog = true
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Nastavi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Odustani")
                }
            }
        )
    }
    
    // Delete Account Dialog (final confirmation)
    if (showDeleteAccountConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirmDialog = false },
            title = { Text("Posljednja potvrda") },
            text = { 
                Column {
                    Text(
                        text = "Jesi li STVARNO siguran?",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nakon brisanja računa, nećeš moći:")
                    Text("• Pristupiti svojim podacima")
                    Text("• Vratiti svoje podatke")
                    Text("• Koristiti isti račun")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Preporučujemo da prvo izvezeš svoje podatke (Settings → Export).")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                isLoading = true
                                errorMessage = null
                                
                                // First delete all data
                                val userId = authManager?.getCurrentUserId()
                                if (userId != null) {
                                    viewModel.resetAllData(userId = userId, reseedSample = false)
                                }
                                
                                // Then delete Firebase account
                                authManager?.deleteAccount()
                                
                                showDeleteAccountConfirmDialog = false
                                // After account deletion, app will create new anonymous account
                            } catch (e: Exception) {
                                errorMessage = com.familylogbook.app.ui.util.ErrorHandler.getFriendlyErrorMessage(e)
                                showDeleteAccountConfirmDialog = false
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("DA, OBRISI RAČUN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirmDialog = false }) {
                    Text("Odustani")
                }
            }
        )
    }
    
    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { 
                showChangePasswordDialog = false
                currentPassword = ""
                newPassword = ""
                confirmNewPassword = ""
                errorMessage = null
            },
            title = { Text("Promijeni lozinku") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                    
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { 
                            currentPassword = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Trenutna lozinka") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        enabled = !isLoading
                    )
                    
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { 
                            newPassword = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nova lozinka") },
                        placeholder = { Text("Najmanje 6 znakova") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        enabled = !isLoading
                    )
                    
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = { 
                            confirmNewPassword = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Potvrdi novu lozinku") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        enabled = !isLoading
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                isLoading = true
                                errorMessage = null
                                
                                // Validation
                                if (currentPassword.isBlank()) {
                                    errorMessage = "Molimo unesi trenutnu lozinku"
                                    isLoading = false
                                    return@launch
                                }
                                if (newPassword.length < 6) {
                                    errorMessage = "Nova lozinka mora imati najmanje 6 znakova"
                                    isLoading = false
                                    return@launch
                                }
                                if (newPassword != confirmNewPassword) {
                                    errorMessage = "Nove lozinke se ne podudaraju"
                                    isLoading = false
                                    return@launch
                                }
                                
                                // Change password
                                authManager?.changePassword(currentPassword, newPassword)
                                
                                // Success - close dialog and show success message
                                showChangePasswordDialog = false
                                currentPassword = ""
                                newPassword = ""
                                confirmNewPassword = ""
                                // Show success message
                                localSuccessMessage = "Lozinka je uspješno promijenjena!"
                            } catch (e: Exception) {
                                errorMessage = com.familylogbook.app.ui.util.ErrorHandler.getFriendlyErrorMessage(e)
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading && currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmNewPassword.isNotBlank()
                ) {
                    Text("Promijeni")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showChangePasswordDialog = false
                        currentPassword = ""
                        newPassword = ""
                        confirmNewPassword = ""
                        errorMessage = null
                    }
                ) {
                    Text("Odustani")
                }
            }
        )
    }
    
    // Error message
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(5000)
            errorMessage = null
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            action = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("U redu")
                }
            }
        ) {
            Text(message)
        }
    }
    
    // Success message from ViewModel or local
    successMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            if (viewModelSuccessMessage == null) {
                localSuccessMessage = null
            }
            viewModel.clearSuccessMessage()
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            action = {
                TextButton(onClick = { 
                    localSuccessMessage = null
                    viewModel.clearSuccessMessage()
                }) {
                    Text("U redu")
                }
            }
        ) {
            Text(message)
        }
    }
}

@Composable
fun ChildListItem(
    child: Child,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = Color(android.graphics.Color.parseColor(child.avatarColor)),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = child.emoji, fontSize = 24.sp)
                    }
                }
                Column {
                    Text(
                        text = child.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    child.dateOfBirth?.let {
                        Text(
                            text = "Rođendan: ${java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date(it))}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Obriši",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun FamilySection(
    viewModel: SettingsViewModel,
    scope: CoroutineScope,
    errorMessage: String?,
    onErrorMessageChange: (String?) -> Unit,
    viewModelIsLoading: Boolean,
    onEntityQuickAction: (Entity, com.familylogbook.app.domain.model.Category) -> Unit = { _, _ -> }
) {
    val persons by viewModel.persons.collectAsState()
    val entities by viewModel.entities.collectAsState()
    val newPersonName by viewModel.newPersonName.collectAsState()
    val newPersonType by viewModel.newPersonType.collectAsState()
    val newPersonEmoji by viewModel.newPersonEmoji.collectAsState()
    val newEntityName by viewModel.newEntityName.collectAsState()
    val newEntityType by viewModel.newEntityType.collectAsState()
    val newEntityEmoji by viewModel.newEntityEmoji.collectAsState()
    var showAddPerson by remember { mutableStateOf(false) }
    var showAddEntity by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
                text = "Obitelj",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        // Add Person button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Osobe",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = { showAddPerson = !showAddPerson },
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dodaj osobu", fontSize = 12.sp)
            }
        }
        
        // Add Person form
        if (showAddPerson) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newPersonName,
                        onValueChange = { viewModel.setNewPersonName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Ime") },
                        placeholder = { Text("Unesi ime") }
                    )
                    
                    // Person type selector
                    Text("Tip", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(PersonType.PARENT, PersonType.CHILD).forEach { type ->
                                FilterChip(
                                    selected = newPersonType == type,
                                    onClick = { viewModel.setNewPersonType(type) },
                                    label = { 
                                        Text(
                                            when (type) {
                                                PersonType.PARENT -> "Roditelj"
                                                PersonType.CHILD -> "Dijete"
                                                else -> type.name
                                            },
                                            fontSize = 13.sp
                                        ) 
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(PersonType.OTHER_FAMILY_MEMBER, PersonType.PET).forEach { type ->
                                FilterChip(
                                    selected = newPersonType == type,
                                    onClick = { viewModel.setNewPersonType(type) },
                                    label = { 
                                        Text(
                                            when (type) {
                                                PersonType.OTHER_FAMILY_MEMBER -> "Ostali članovi"
                                                PersonType.PET -> "Kućni ljubimac"
                                                else -> type.name
                                            },
                                            fontSize = 13.sp
                                        ) 
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    // Emoji picker
                    Text("Emoji", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("👶", "👧", "👦", "👨", "👩", "🧒", "🐕", "🐈").forEach { emoji ->
                            Surface(
                                modifier = Modifier
                                    .size(48.dp)
                                    .then(
                                        if (newPersonEmoji == emoji) {
                                            Modifier.background(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                CircleShape
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                shape = CircleShape,
                                color = Color.Transparent,
                                onClick = { viewModel.setNewPersonEmoji(emoji) }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 24.sp)
                                }
                            }
                        }
                    }
                    
                    // Date of birth (collect state outside if block for use in Button enabled condition)
                    val newPersonDateOfBirth by viewModel.newPersonDateOfBirth.collectAsState()
                    
                    // Date of birth picker (REQUIRED for CHILD type)
                    if (newPersonType == PersonType.CHILD) {
                        var showDatePicker by remember { mutableStateOf(false) }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Professional date picker section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Label and hint
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Datum rođenja *",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Text(
                                    text = "Koristi se za cjepiva i zdravstveni sažetak",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(start = 26.dp)
                                )
                                
                                // Date input field with manual entry and validation
                                // "Digits as truth" pattern - only store raw digits (ddMMyyyy)
                                var dobDigits by remember { mutableStateOf("") }
                                var dateError by remember { mutableStateOf<String?>(null) }
                                
                                // Update dobDigits when newPersonDateOfBirth changes externally
                                LaunchedEffect(newPersonDateOfBirth) {
                                    if (newPersonDateOfBirth != null) {
                                        dobDigits = DateFormatter.timestampToDigits(newPersonDateOfBirth!!)
                                    } else {
                                        dobDigits = ""
                                    }
                                }
                                
                                OutlinedTextField(
                                    value = dobDigits, // Store only digits as value
                                    onValueChange = { newText ->
                                        // Always extract only digits from input - this is the only truth
                                        dobDigits = newText.filter { it.isDigit() }.take(8)
                                        
                                        // Validate and parse date only if we have 8 digits (full date)
                                        if (dobDigits.length == 8) {
                                            val formatted = DateFormatter.formatDobDigits(dobDigits) // dd.MM.yyyy
                                            val parsedDate = parseDateForPerson(formatted)
                                            if (parsedDate != null) {
                                                dateError = null
                                                viewModel.setNewPersonDateOfBirth(parsedDate)
                                            } else {
                                                dateError = "Neispravan format datuma"
                                            }
                                        } else {
                                            // Clear error while typing
                                            dateError = null
                                            // Clear date if user is deleting
                                            viewModel.setNewPersonDateOfBirth(null)
                                        }
                                    },
                                    visualTransformation = DateVisualTransformation(), // Format visually
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Unesi datum") },
                                    placeholder = { Text("dd.MM.yyyy") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Event,
                                            contentDescription = null,
                                            modifier = Modifier.size(22.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (dobDigits.isNotEmpty()) {
                                                IconButton(
                                                    onClick = {
                                                        dobDigits = ""
                                                        dateError = null
                                                        viewModel.setNewPersonDateOfBirth(null)
                                                    },
                                                    modifier = Modifier.size(40.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Obriši datum",
                                                        modifier = Modifier.size(18.dp),
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                            // Optional: Calendar button to open date picker
                                            IconButton(
                                                onClick = { showDatePicker = true },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.CalendarToday,
                                                    contentDescription = "Odaberi iz kalendara",
                                                    modifier = Modifier.size(20.dp),
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    },
                                    isError = dateError != null,
                                    supportingText = {
                                        if (dateError != null) {
                                            Text(dateError!!, color = MaterialTheme.colorScheme.error)
                                        } else {
                                            Text("Format: dd.MM.yyyy (npr. 15.03.2020)")
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    )
                                )
                            }
                        }
                        
                        if (showDatePicker) {
                            PersonDatePickerDialog(
                                initialDate = newPersonDateOfBirth,
                                onDateSelected = { date ->
                                    viewModel.setNewPersonDateOfBirth(date)
                                    showDatePicker = false
                                },
                                onDismiss = { showDatePicker = false }
                            )
                        }
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                if (viewModel.addPerson()) {
                                    showAddPerson = false
                                } else {
                                    onErrorMessageChange("Greška pri dodavanju osobe. Provjeri da li su sva polja ispravno unesena.")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !viewModelIsLoading && 
                                  newPersonName.trim().isNotEmpty() && 
                                  (newPersonType != PersonType.CHILD || newPersonDateOfBirth != null)
                    ) {
                        if (viewModelIsLoading) {
                            com.familylogbook.app.ui.component.InlineLoadingIndicator()
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (viewModelIsLoading) "Spremanje..." else "Dodaj osobu")
                    }
                }
            }
        }
        
        // Persons list
        if (persons.isEmpty()) {
            Text(
                text = "Još nema dodanih osoba",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            persons.forEach { person ->
                var showDeleteDialog by remember { mutableStateOf(false) }
                var entryCount by remember { mutableStateOf(0) }
                
                // Load entry count asynchronously
                androidx.compose.runtime.LaunchedEffect(person.id) {
                    entryCount = viewModel.getPersonEntryCount(person.id)
                }
                
                PersonListItem(
                    person = person,
                    entryCount = entryCount,
                    onDelete = { showDeleteDialog = true }
                )
                
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Obriši osobu") },
                        text = { 
                            Text(
                                if (entryCount > 0) {
                                    "Ovo će obrisati osobu i svih $entryCount zapisa povezanih s njom. Ovu akciju nije moguće poništiti."
                                } else {
                                    "Želiš li sigurno obrisati ovu osobu?"
                                }
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        viewModel.deletePerson(person.id)
                                        showDeleteDialog = false
                                    }
                                }
                            ) {
                                Text("Obriši sve", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Odustani")
                            }
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        
        // Add Entity button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Entiteti",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = { showAddEntity = !showAddEntity },
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dodaj entitet", fontSize = 12.sp)
            }
        }
        
        // Add Entity form
        if (showAddEntity) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newEntityName,
                        onValueChange = { viewModel.setNewEntityName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Ime") },
                        placeholder = { Text("npr. Auto, Kuća, Financije") }
                    )
                    
                    // Entity type selector
                    Text("Tip", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EntityType.values().take(4).forEach { type ->
                            FilterChip(
                                selected = newEntityType == type,
                                onClick = { viewModel.setNewEntityType(type) },
                                label = { Text(type.name, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Emoji picker
                    Text("Emoji", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("🚗", "🏠", "💰", "🏫", "💼", "🛒", "📱", "🔧").forEach { emoji ->
                            Surface(
                                modifier = Modifier
                                    .size(48.dp)
                                    .then(
                                        if (newEntityEmoji == emoji) {
                                            Modifier.background(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                CircleShape
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                shape = CircleShape,
                                color = Color.Transparent,
                                onClick = { viewModel.setNewEntityEmoji(emoji) }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 24.sp)
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                if (viewModel.addEntity()) {
                                    showAddEntity = false
                                } else {
                                    onErrorMessageChange("Greška pri dodavanju entiteta. Provjeri da li je ime uneseno.")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !viewModelIsLoading && newEntityName.trim().isNotEmpty()
                    ) {
                        if (viewModelIsLoading) {
                            com.familylogbook.app.ui.component.InlineLoadingIndicator()
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (viewModelIsLoading) "Spremanje..." else "Dodaj entitet")
                    }
                }
            }
        }
        
        // Entities list
        if (entities.isEmpty()) {
            Text(
                text = "Još nema dodanih entiteta",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            entities.forEach { entity ->
                var showDeleteDialog by remember { mutableStateOf(false) }
                var entryCount by remember { mutableStateOf(0) }
                
                // Load entry count asynchronously
                androidx.compose.runtime.LaunchedEffect(entity.id) {
                    entryCount = viewModel.getEntityEntryCount(entity.id)
                }
                
                EntityListItem(
                    entity = entity,
                    entryCount = entryCount,
                    onDelete = { showDeleteDialog = true },
                    onAddService = if (entity.type == EntityType.CAR) {
                        { onEntityQuickAction(entity, com.familylogbook.app.domain.model.Category.AUTO) }
                    } else null,
                    onAddExpense = if (entity.type == EntityType.CAR || entity.type == EntityType.HOUSE || entity.type == EntityType.FINANCE) {
                        { onEntityQuickAction(entity, com.familylogbook.app.domain.model.Category.FINANCE) }
                    } else null
                )
                
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Obriši entitet") },
                        text = { 
                            Text(
                                if (entryCount > 0) {
                                    "Ovo će obrisati entitet i svih $entryCount zapisa povezanih s njim. Ovu akciju nije moguće poništiti."
                                } else {
                                    "Želiš li sigurno obrisati ovaj entitet?"
                                }
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        viewModel.deleteEntity(entity.id)
                                        showDeleteDialog = false
                                    }
                                }
                            ) {
                                Text("Obriši sve", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Odustani")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PersonListItem(
    person: Person,
    @Suppress("UNUSED_PARAMETER") entryCount: Int = 0,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = Color(android.graphics.Color.parseColor(person.avatarColor)),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = person.emoji, fontSize = 24.sp)
                    }
                }
                Column {
                    Text(
                        text = person.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = person.type.name.replace("_", " "),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Obriši",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EntityListItem(
    entity: Entity,
    @Suppress("UNUSED_PARAMETER") entryCount: Int = 0,
    onDelete: () -> Unit,
    onAddService: (() -> Unit)? = null,
    onAddExpense: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = Color(android.graphics.Color.parseColor(entity.avatarColor)),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = entity.emoji, fontSize = 24.sp)
                    }
                }
                Column {
                    Text(
                        text = entity.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = entity.type.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onAddService != null) {
                    TextButton(
                        onClick = onAddService,
                        modifier = Modifier.widthIn(min = 80.dp)
                    ) {
                        Text(
                            "Dodaj servis",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
                if (onAddExpense != null) {
                    TextButton(
                        onClick = onAddExpense,
                        modifier = Modifier.widthIn(min = 80.dp)
                    ) {
                        Text(
                            "Dodaj trošak",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Obriši",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AppSettingsSection() {
    val context = LocalContext.current
    val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
    val notificationsEnabledBySystem = remember { notificationManager.areNotificationsEnabled() }
    var notificationsEnabled by remember { mutableStateOf(notificationsEnabledBySystem) }
    var feedingRemindersEnabled by remember { mutableStateOf(true) }
    var medicineRemindersEnabled by remember { mutableStateOf(true) }
    
    // Request permission launcher for Android 13+
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationsEnabled = isGranted
        if (!isGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Show message that permission is needed
            Toast.makeText(
                context,
                "Dozvola za obavijesti je potrebna za podsjetnike. Možeš je omogućiti u postavkama uređaja.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
                text = "Postavke aplikacije",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        // Notifications
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Obavijesti",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Omogući obavijesti", fontSize = 14.sp)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsEnabled) {
                            Text(
                                text = "Dozvola nije dana",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { 
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                // Request permission on Android 13+
                                if (PackageManager.PERMISSION_GRANTED != 
                                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)) {
                                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    notificationsEnabled = true
                                }
                            } else {
                                notificationsEnabled = it
                            }
                        }
                    )
                }
                
                // Show button to open settings if permission denied (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsEnabled) {
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Otvori postavke obavijesti")
                    }
                }
                
                if (notificationsEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Podsjetnici za hranjenje", fontSize = 14.sp)
                        Switch(
                            checked = feedingRemindersEnabled,
                            onCheckedChange = { feedingRemindersEnabled = it }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Podsjetnici za lijekove", fontSize = 14.sp)
                        Switch(
                            checked = medicineRemindersEnabled,
                            onCheckedChange = { medicineRemindersEnabled = it }
                        )
                    }
                }
            }
        }
        
        // Theme (placeholder)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                    Text(
                        text = "Tema",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Postavke teme uskoro",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
            }
        }
        
        // Smart Home Integration removed - no longer needed for Parent OS
        
        // Language (placeholder)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                    Text(
                        text = "Jezik",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Postavke jezika uskoro",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
            }
        }
    }
}

@Composable
fun AdvancedSection(
    viewModel: SettingsViewModel,
    authManager: com.familylogbook.app.data.auth.AuthManager?,
    scope: CoroutineScope
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showResetWithSampleDialog by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Napredno",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Upravljanje podacima",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Oprez: Ove akcije će obrisati sve tvoje podatke (osobe, entitete, zapise). Ovu akciju nije moguće poništiti.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Obriši sve moje podatke")
                    }
                    
                    Button(
                        onClick = { showResetWithSampleDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text("Obriši sve i postavi demo primjer")
                    }
                }
            }
        }
    }
    
    // Reset dialog (empty)
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Obriši sve moje podatke") },
            text = { 
                Text("Ovo će obrisati SVE tvoje podatke (osobe, entitete, zapise). Ova akcija se ne može poništiti.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val userId = authManager?.getCurrentUserId()
                            viewModel.resetAllData(userId = userId, reseedSample = false)
                            showResetDialog = false
                        }
                    }
                ) {
                    Text("Obriši sve", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Odustani")
                }
            }
        )
    }
    
    // Reset with sample dialog
    if (showResetWithSampleDialog) {
        AlertDialog(
            onDismissRequest = { showResetWithSampleDialog = false },
            title = { Text("Obriši sve i postavi demo primjer") },
            text = { 
                Text("Ovo će obrisati SVE tvoje podatke i učitati demo primjer (Neo, Luna, Auto, Kuća, itd.). Ova akcija se ne može poništiti.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val userId = authManager?.getCurrentUserId()
                            if (userId != null) {
                                viewModel.resetAllData(userId = userId, reseedSample = true)
                            } else {
                                android.util.Log.e("SettingsScreen", "Cannot reset with sample data: userId is null")
                            }
                            showResetWithSampleDialog = false
                        }
                    }
                ) {
                    Text("Obriši i učitaj", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetWithSampleDialog = false }) {
                    Text("Odustani")
                }
            }
        )
    }
}

@Composable
fun AboutSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
            Text(
                text = "O aplikaciji",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "FamilyOS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Version 1.0.0",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Vaš kompletan upravitelj obiteljskog života",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pravni dokumenti",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                val context = LocalContext.current
                
                // Privacy Policy link
                Text(
                    text = "📄 Pravila privatnosti",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://deki1804.github.io/family-logbook/PRIVACY_POLICY.html")
                        }
                        context.startActivity(intent)
                    }
                )
                
                // Terms of Service link
                Text(
                    text = "📋 Uvjeti korištenja",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://github.com/Deki1804/family-logbook/blob/main/TERMS_OF_SERVICE.md")
                        }
                        context.startActivity(intent)
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Vaši podaci su sigurno i privatno pohranjeni. Nikada ne dijelimo vaše informacije.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Kontakt",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                val context = LocalContext.current
                
                Text(
                    text = "📧 LarryDJ@gmail.com",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:LarryDJ@gmail.com")
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                    }
                )
                
                Text(
                    text = "Za podršku ili povratne informacije, kontaktirajte nas.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Simple date picker dialog using Android DatePickerDialog.
 */
@Composable
fun PersonDatePickerDialog(
    initialDate: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    
    // Set initial date if provided
    initialDate?.let {
        calendar.timeInMillis = it
    }
    
    val year = calendar.get(java.util.Calendar.YEAR)
    val month = calendar.get(java.util.Calendar.MONTH)
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    
    LaunchedEffect(Unit) {
        val datePickerDialog = AndroidDatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = java.util.Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                onDateSelected(selectedCalendar.timeInMillis)
            },
            year,
            month,
            day
        )
        
        datePickerDialog.show()
        
        // Handle dismiss
        datePickerDialog.setOnDismissListener {
            onDismiss()
        }
    }
}

/**
 * Parses date string (dd.MM.yyyy) and validates it
 * Returns timestamp in milliseconds if valid, null otherwise
 */
private fun parseDateForPerson(dateString: String): Long? {
    if (dateString.length < 10) return null // Need full format: dd.MM.yyyy
    
    try {
        val parts = dateString.split(".")
        if (parts.size != 3) return null
        
        val day = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val year = parts[2].toIntOrNull() ?: return null
        
        // Validate ranges
        if (day < 1 || day > 31) return null
        if (month < 1 || month > 12) return null
        if (year < 1900 || year > java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)) return null
        
        // Validate actual date (e.g., 31.02.2020 is invalid)
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.YEAR, year)
        calendar.set(java.util.Calendar.MONTH, month - 1) // Calendar months are 0-based
        calendar.set(java.util.Calendar.DAY_OF_MONTH, day)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        // Check if date was adjusted (invalid date like 31.02 would be adjusted)
        if (calendar.get(java.util.Calendar.DAY_OF_MONTH) != day ||
            calendar.get(java.util.Calendar.MONTH) != month - 1 ||
            calendar.get(java.util.Calendar.YEAR) != year) {
            return null
        }
        
        return calendar.timeInMillis
    } catch (e: Exception) {
        return null
    }
}
