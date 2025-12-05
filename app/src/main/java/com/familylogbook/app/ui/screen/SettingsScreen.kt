package com.familylogbook.app.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.familylogbook.app.domain.model.Child
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
    val children by viewModel.children.collectAsState()
    val newChildName by viewModel.newChildName.collectAsState()
    val newChildEmoji by viewModel.newChildEmoji.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Dialogs state
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
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
        item {
            authManager?.let { auth ->
                AccountInfoCard(
                    authManager = auth,
                    onUpgradeClick = onNavigateToLogin,
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
                onEntityQuickAction = { entity, category ->
                    onNavigateToAddEntryForEntity(entity.id, category)
                }
            )
        }
        
        item { Divider() }
        
        // Children Section (legacy)
        item {
            Text(
                text = "Djeca (Zastarjelo)",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // Add child section
        item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Dodaj dijete",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = newChildName,
                    onValueChange = { viewModel.setNewChildName(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ime djeteta") },
                    placeholder = { Text("Unesi ime djeteta") }
                )
                
                // Emoji picker (simplified - just a few options)
                Text(
                    text = "Odaberi emoji",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val emojis = listOf("👶", "👧", "👦", "🧒", "👨", "👩")
                    emojis.forEach { emoji ->
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .then(
                                    if (newChildEmoji == emoji) {
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
                            onClick = { viewModel.setNewChildEmoji(emoji) }
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
                            viewModel.addChild()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newChildName.trim().isNotEmpty()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dodaj dijete")
                }
            }
        }
        }
        
        // Children list
        item {
            Text(
                text = "Djeca",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        if (children.isEmpty()) {
            item {
                Text(
                    text = "Još nema dodane djece",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(children) { child ->
                ChildListItem(
                    child = child,
                    onDelete = {
                        scope.launch {
                            viewModel.deleteChild(child.id)
                        }
                    }
                )
            }
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
                                
                                // Success - close dialog
                                showChangePasswordDialog = false
                                currentPassword = ""
                                newPassword = ""
                                confirmNewPassword = ""
                                errorMessage = "Lozinka je uspješno promijenjena!"
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
            action = {
                TextButton(onClick = { errorMessage = null }) {
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(PersonType.PARENT, PersonType.CHILD, PersonType.OTHER_FAMILY_MEMBER, PersonType.PET).forEach { type ->
                            FilterChip(
                                selected = newPersonType == type,
                                onClick = { viewModel.setNewPersonType(type) },
                                label = { Text(type.name.replace("_", " "), fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
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
                    
                    Button(
                        onClick = {
                            scope.launch {
                                if (viewModel.addPerson()) {
                                    showAddPerson = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = newPersonName.trim().isNotEmpty()
                    ) {
                        Text("Dodaj osobu")
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
                val entryCount = remember { 
                    kotlinx.coroutines.runBlocking { 
                        viewModel.getPersonEntryCount(person.id) 
                    } 
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
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = newEntityName.trim().isNotEmpty()
                    ) {
                        Text("Dodaj entitet")
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
                val entryCount = remember { 
                    kotlinx.coroutines.runBlocking { 
                        viewModel.getEntityEntryCount(entity.id) 
                    } 
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
    entryCount: Int = 0,
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
    entryCount: Int = 0,
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
                    TextButton(onClick = onAddService) {
                        Text("Dodaj servis", fontSize = 12.sp)
                    }
                }
                if (onAddExpense != null) {
                    TextButton(onClick = onAddExpense) {
                        Text("Dodaj trošak", fontSize = 12.sp)
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
    var notificationsEnabled by remember { mutableStateOf(true) }
    var feedingRemindersEnabled by remember { mutableStateOf(true) }
    var medicineRemindersEnabled by remember { mutableStateOf(true) }
    
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
                    Text("Omogući obavijesti", fontSize = 14.sp)
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
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
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

