package com.familylogbook.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val children by viewModel.children.collectAsState()
    val newChildName by viewModel.newChildName.collectAsState()
    val newChildEmoji by viewModel.newChildEmoji.collectAsState()
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        
        // Add child section
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
        Text(
            text = "Children",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        if (children.isEmpty()) {
            Text(
                text = "No children added yet",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

