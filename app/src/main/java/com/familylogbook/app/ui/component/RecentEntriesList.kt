package com.familylogbook.app.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.LogEntry
import java.text.SimpleDateFormat
import java.util.*

/**
 * Compact list of recent entries.
 * Shows: icon + title + short description + time
 */
@Composable
fun RecentEntriesList(
    entries: List<LogEntry>,
    onEntryClick: (LogEntry) -> Unit,
    onShoppingItemChecked: ((String, String, Boolean) -> Unit)? = null, // entryId, item, isChecked
    modifier: Modifier = Modifier,
    maxItems: Int = 10
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "📋 Zadnji zapisi",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        if (entries.isEmpty()) {
            Text(
                text = "Nema zapisa",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                entries.take(maxItems).forEach { entry ->
                    RecentEntryItem(
                        entry = entry,
                        onClick = { onEntryClick(entry) },
                        onShoppingItemChecked = onShoppingItemChecked?.let { checker ->
                            { item, isChecked -> checker(entry.id, item, isChecked) }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentEntryItem(
    entry: LogEntry,
    onClick: () -> Unit,
    onShoppingItemChecked: ((String, Boolean) -> Unit)? = null, // item, isChecked
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header row with icon, title, time
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Surface(
                    color = getCategoryColor(entry.category).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getCategoryIcon(entry.category),
                            fontSize = 20.sp
                        )
                    }
                }
                
                // Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = getEntryTitle(entry),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = getEntryDescription(entry),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
                
                // Time
                Text(
                    text = formatTime(entry.timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // Shopping items with checkboxes (if shopping entry)
            if (entry.category == Category.SHOPPING && 
                entry.shoppingItems != null && 
                entry.shoppingItems.isNotEmpty() &&
                onShoppingItemChecked != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    entry.shoppingItems.take(3).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = entry.checkedShoppingItems?.contains(item) == true,
                                onCheckedChange = { isChecked ->
                                    onShoppingItemChecked(item, isChecked)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = item,
                                fontSize = 12.sp,
                                color = if (entry.checkedShoppingItems?.contains(item) == true) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                style = if (entry.checkedShoppingItems?.contains(item) == true) {
                                    MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                } else {
                                    MaterialTheme.typography.bodySmall
                                }
                            )
                        }
                    }
                    if (entry.shoppingItems.size > 3) {
                        Text(
                            text = "... i još ${entry.shoppingItems.size - 3}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(start = 28.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getCategoryIcon(category: Category): String {
    return when (category) {
        Category.HEALTH -> "🏥"
        Category.FEEDING -> "🍼"
        Category.SLEEP -> "😴"
        Category.MOOD -> "😊"
        Category.SHOPPING -> "🛒"
        Category.SMART_HOME -> "🏠"
        Category.AUTO -> "🚗"
        Category.HOUSE -> "🏡"
        Category.FINANCE -> "💰"
        Category.WORK -> "💼"
        else -> "📝"
    }
}

private fun getCategoryColor(category: Category): Color {
    return when (category) {
        Category.HEALTH -> Color(0xFFFF6B6B)
        Category.FEEDING -> Color(0xFF4ECDC4)
        Category.SLEEP -> Color(0xFF95E1D3)
        Category.MOOD -> Color(0xFFFFD93D)
        Category.SHOPPING -> Color(0xFFFF6B6B)
        Category.SMART_HOME -> Color(0xFF95E1D3)
        Category.AUTO -> Color(0xFFAA96DA)
        Category.HOUSE -> Color(0xFF95E1D3)
        Category.FINANCE -> Color(0xFFFF6B6B)
        Category.WORK -> Color(0xFFAA96DA)
        else -> Color(0xFF95E1D3)
    }
}

private fun getEntryTitle(entry: LogEntry): String {
    return when (entry.category) {
        Category.FEEDING -> {
            val amount = entry.feedingAmount?.let { "$it ml" } ?: ""
            "Hranjenje $amount".trim()
        }
        Category.SHOPPING -> {
            val count = entry.shoppingItems?.size ?: 0
            "Shopping lista ($count stavki)"
        }
        Category.HEALTH -> {
            entry.temperature?.let { "Temperatura: ${it}°C" } ?: "Zdravlje"
        }
        else -> entry.rawText.take(30) + if (entry.rawText.length > 30) "..." else ""
    }
}

private fun getEntryDescription(entry: LogEntry): String {
    return when {
        entry.category == Category.SHOPPING && !entry.shoppingItems.isNullOrEmpty() -> {
            entry.shoppingItems.take(3).joinToString(", ")
        }
        entry.rawText.length > 50 -> entry.rawText.take(50) + "..."
        else -> entry.rawText
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)
    
    return when {
        minutes < 1 -> "Sada"
        minutes < 60 -> "$minutes min"
        hours < 24 -> "$hours h"
        days < 7 -> "$days d"
        else -> SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(timestamp))
    }
}
