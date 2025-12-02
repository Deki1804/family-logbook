package com.familylogbook.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Mood
import com.familylogbook.app.ui.navigation.Screen
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAddEntry: () -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val children by viewModel.children.collectAsState()
    
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
                items(entries) { entry ->
                    val child = entry.childId?.let { childId ->
                        children.find { it.id == childId }
                    }
                    LogEntryCard(
                        entry = entry,
                        child = child
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
    child: Child?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Child + Date
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
                            modifier = Modifier.size(32.dp)
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
                            fontSize = 14.sp
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
                
                Text(
                    text = formatTimestamp(entry.timestamp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
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
        Category.HOME -> "Home" to Color(0xFFF38181)
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

