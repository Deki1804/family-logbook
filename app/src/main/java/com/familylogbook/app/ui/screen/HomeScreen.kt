package com.familylogbook.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.data.smarthome.SmartHomeManager
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Mood
import com.familylogbook.app.ui.component.AdviceCard
import com.familylogbook.app.ui.navigation.Screen
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAddEntry: () -> Unit,
    onNavigateToChildProfile: (String) -> Unit = {}
) {
    val entries by viewModel.filteredEntries.collectAsState()
    val children by viewModel.children.collectAsState()
    val persons by viewModel.persons.collectAsState()
    val entities by viewModel.entities.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (entries.isEmpty()) {
            EmptyState(
                modifier = Modifier.align(Alignment.Center),
                onAddEntry = onNavigateToAddEntry
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search bar
                item {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Category filter chips
                item {
                    CategoryFilterChips(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { category ->
                            viewModel.setSelectedCategory(if (category == selectedCategory) null else category)
                        }
                    )
                }
                
                items(entries) { entry ->
                    val child = entry.childId?.let { childId ->
                        children.find { it.id == childId }
                    }
                    LogEntryCard(
                        entry = entry,
                        child = child,
                        viewModel = viewModel,
                        onChildClick = { childId ->
                            onNavigateToChildProfile(childId)
                        },
                        onDelete = { entryId ->
                            // Delete will be handled in the card with confirmation
                        }
                    )
                }
            }
        }
        
        FloatingActionButton(
            onClick = onNavigateToAddEntry,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Entry")
        }
    }
}

@Composable
fun LogEntryCard(
    entry: LogEntry,
    child: Child?,
    viewModel: HomeViewModel,
    onChildClick: (String) -> Unit = {},
    onDelete: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val smartHomeManager = remember { SmartHomeManager(context) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Obriši zapis") },
            text = { Text("Želiš li sigurno obrisati ovaj zapis?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry(entry.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Obriši", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Odustani")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Regular tap - could navigate to detail */ },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Child + Date + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (child != null) {
                        Surface(
                            color = Color(android.graphics.Color.parseColor(child.avatarColor)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onChildClick(child.id) }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = child.emoji,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Text(
                            text = child.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { onChildClick(child.id) }
                        )
                    } else {
                        Text(
                            text = "Family",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatTimestamp(entry.timestamp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    // Context menu button
                    Box {
                        IconButton(
                            onClick = { showContextMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More options",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showContextMenu,
                            onDismissRequest = { showContextMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Uredi") },
                                onClick = {
                                    showContextMenu = false
                                    // TODO: Navigate to edit screen
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Obriši", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showContextMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            // Category badge
            CategoryChip(category = entry.category)
            
            // Entry text
            Text(
                text = entry.rawText,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
            
            // Tags
            if (entry.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    entry.tags.forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            // Mood indicator
            entry.mood?.let { mood ->
                MoodIndicator(mood = mood)
            }
            
            // Temperature / Medicine info
            entry.temperature?.let { temp ->
                Text(
                    text = "🌡️ Temperatura: ${temp}°C",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            entry.medicineGiven?.let { medicine ->
                Text(
                    text = "💊 Lijek: $medicine",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Feeding info
            entry.feedingType?.let { feedingType ->
                val feedingText = when (feedingType) {
                    com.familylogbook.app.domain.model.FeedingType.BREAST_LEFT -> "🍼 Dojenje (lijeva)"
                    com.familylogbook.app.domain.model.FeedingType.BREAST_RIGHT -> "🍼 Dojenje (desna)"
                    com.familylogbook.app.domain.model.FeedingType.BOTTLE -> "🍼 Bočica${entry.feedingAmount?.let { " - ${it}ml" } ?: ""}"
                }
                Text(
                    text = feedingText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Smart Home action button
            if (entry.category == Category.SMART_HOME) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        // Execute command directly - no user voice input needed
                        smartHomeManager.executeCommand(entry.rawText)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Execute Command")
                }
            }
        }
        }
        
        // Advice Card (if applicable)
        viewModel.getAdviceForEntry(entry)?.let { advice ->
            Spacer(modifier = Modifier.height(8.dp))
            AdviceCard(
                advice = advice,
                category = entry.category
            )
        }
    }
}

@Composable
fun CategoryChip(category: Category) {
    val (label, color) = when (category) {
        Category.HEALTH -> "Health" to Color(0xFFFF6B6B)
        Category.SLEEP -> "Sleep" to Color(0xFF4ECDC4)
        Category.MOOD -> "Mood" to Color(0xFFFFD93D)
        Category.DEVELOPMENT -> "Development" to Color(0xFF95E1D3)
        Category.KINDERGARTEN_SCHOOL -> "School" to Color(0xFFAA96DA)
        Category.SCHOOL -> "School" to Color(0xFFAA96DA)
        Category.HOME -> "Home" to Color(0xFFF38181)
        Category.HOUSE -> "House" to Color(0xFFF38181)
        Category.FEEDING -> "Feeding" to Color(0xFFFFB84D)
        Category.AUTO -> "Auto" to Color(0xFFFF6B6B)
        Category.FINANCE -> "Finance" to Color(0xFF95E1D3)
        Category.WORK -> "Work" to Color(0xFFAA96DA)
        Category.SHOPPING -> "Shopping" to Color(0xFFFFB84D)
        Category.SMART_HOME -> "Smart Home" to Color(0xFF00BCD4)
        Category.OTHER -> "Other" to Color(0xFFCCCCCC)
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MoodIndicator(mood: Mood) {
    val (label, color) = when (mood) {
        Mood.VERY_BAD -> "😢 Very Bad" to Color(0xFFE63946)
        Mood.BAD -> "😞 Bad" to Color(0xFFFF6B6B)
        Mood.NEUTRAL -> "😐 Neutral" to Color(0xFFCCCCCC)
        Mood.GOOD -> "😊 Good" to Color(0xFF4ECDC4)
        Mood.VERY_GOOD -> "😄 Very Good" to Color(0xFF2A9D8F)
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    onAddEntry: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "No entries yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = "Add your first entry to get started!",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Button(onClick = onAddEntry) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Entry")
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search entries...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun CategoryFilterChips(
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Category.values().take(8).forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.name) }
            )
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

