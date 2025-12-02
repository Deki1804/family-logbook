package com.familylogbook.app.ui.screen

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.data.auth.AuthManager
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.domain.model.PersonType
import com.familylogbook.app.domain.model.EntityType
import com.familylogbook.app.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    authManager: AuthManager? = null,
    onNavigateToLogin: () -> Unit = {}
) {
    val children by viewModel.children.collectAsState()
    val newChildName by viewModel.newChildName.collectAsState()
    val newChildEmoji by viewModel.newChildEmoji.collectAsState()
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Account Section
        item {
            authManager?.let { auth ->
                AccountInfoCard(
                    authManager = auth,
                    onUpgradeClick = onNavigateToLogin
                )
            }
        }
        
        item { Divider() }
        
        // Family Section
        item {
            FamilySection(
                viewModel = viewModel,
                scope = scope
            )
        }
        
        item { Divider() }
        
        // Children Section (legacy)
        item {
            Text(
                text = "Children (Legacy)",
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
                    text = "Add Child",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = newChildName,
                    onValueChange = { viewModel.setNewChildName(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Child Name") },
                    placeholder = { Text("Enter child's name") }
                )
                
                // Emoji picker (simplified - just a few options)
                Text(
                    text = "Choose Emoji",
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
                    Text("Add Child")
                }
            }
        }
        
        // Children list
        item {
            Text(
                text = "Children",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        if (children.isEmpty()) {
            item {
                Text(
                    text = "No children added yet",
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
        
        // About Section
        item {
            AboutSection()
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
                            text = "Birthday: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(it))}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun FamilySection(
    viewModel: SettingsViewModel,
    scope: androidx.coroutines.CoroutineScope
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
            text = "Family",
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
                text = "People",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = { showAddPerson = !showAddPerson },
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Person", fontSize = 12.sp)
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
                        label = { Text("Name") },
                        placeholder = { Text("Enter name") }
                    )
                    
                    // Person type selector
                    Text("Type", fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                        Text("Add Person")
                    }
                }
            }
        }
        
        // Persons list
        if (persons.isEmpty()) {
            Text(
                text = "No people added yet",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            persons.forEach { person ->
                PersonListItem(
                    person = person,
                    onDelete = {
                        scope.launch {
                            viewModel.deletePerson(person.id)
                        }
                    }
                )
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
                text = "Entities",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = { showAddEntity = !showAddEntity },
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Entity", fontSize = 12.sp)
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
                        label = { Text("Name") },
                        placeholder = { Text("e.g., Auto, Kuća, Financije") }
                    )
                    
                    // Entity type selector
                    Text("Type", fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                        Text("Add Entity")
                    }
                }
            }
        }
        
        // Entities list
        if (entities.isEmpty()) {
            Text(
                text = "No entities added yet",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            entities.forEach { entity ->
                EntityListItem(
                    entity = entity,
                    onDelete = {
                        scope.launch {
                            viewModel.deleteEntity(entity.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PersonListItem(
    person: Person,
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
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EntityListItem(
    entity: Entity,
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
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
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
            text = "App Settings",
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
                    text = "Notifications",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable notifications", fontSize = 14.sp)
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
                        Text("Feeding reminders", fontSize = 14.sp)
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
                        Text("Medicine reminders", fontSize = 14.sp)
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
                    text = "Theme",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Theme settings coming soon",
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
                    text = "Language",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Language settings coming soon",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AboutSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "About",
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
                    text = "Family Logbook",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Version 1.0.0",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Your complete family life manager",
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
                    text = "Privacy",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Your data is stored securely and privately. We never share your information.",
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
                    text = "Contact",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "For support or feedback, please contact us through the app store.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

