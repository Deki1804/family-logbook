package com.familylogbook.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.DayEntry
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Parent OS Day Tab Screen
 * 
 * Shows daily routines, checklists, and day-related entries.
 */
@Composable
fun DayTabScreen(
    viewModel: HomeViewModel,
    onNavigateToAddEntry: () -> Unit,
    onNavigateToEntryDetail: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val entries by viewModel.entries.collectAsState()
    val persons by viewModel.persons.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Filter day entries
    val dayEntries = entries.filter { DayEntry.isDayEntry(it) }
    
    // Separate by type
    val todayEntries = dayEntries.filter { 
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = it.timestamp
        val entryDay = calendar.get(Calendar.DAY_OF_YEAR)
        val entryYear = calendar.get(Calendar.YEAR)
        
        val now = Calendar.getInstance()
        val todayDay = now.get(Calendar.DAY_OF_YEAR)
        val todayYear = now.get(Calendar.YEAR)
        
        entryDay == todayDay && entryYear == todayYear
    }.sortedByDescending { it.timestamp }
    
    val checklistItems = dayEntries
        .filter { DayEntry.isChecklistItem(it) }
        .sortedBy { it.timestamp }
    
    val reminders = dayEntries
        .filter { DayEntry.isReminder(it) }
        .sortedBy { it.reminderDate ?: Long.MAX_VALUE }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dnevne Obaveze") },
                actions = {
                    IconButton(onClick = onNavigateToAddEntry) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Dodaj obavezu"
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Postavke"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // Today's entries
        if (todayEntries.isNotEmpty()) {
            item {
                Text(
                    text = "Danas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(todayEntries, key = { it.id }) { entry ->
                val person = entry.personId?.let { pid ->
                    persons.find { it.id == pid }
                }
                
                DayEntryCard(
                    entry = entry,
                    person = person,
                    onClick = { onNavigateToEntryDetail(entry.id) }
                )
            }
        }
        
        // Checklist section
        if (checklistItems.isNotEmpty()) {
            item {
                Text(
                    text = "Checklist",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(checklistItems, key = { it.id }) { entry ->
                val person = entry.personId?.let { pid ->
                    persons.find { it.id == pid }
                }
                
                ChecklistItem(
                    entry = entry,
                    person = person,
                    onToggle = {
                        viewModel.toggleChecklistCompleted(entry.id)
                    }
                )
            }
        }
        
        // Reminders section
        if (reminders.isNotEmpty()) {
            item {
                Text(
                    text = "Podsjetnici",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(reminders, key = { it.id }) { entry ->
                val person = entry.personId?.let { pid ->
                    persons.find { it.id == pid }
                }
                
                ReminderCard(
                    entry = entry,
                    person = person,
                    onClick = { onNavigateToEntryDetail(entry.id) }
                )
            }
        }
        
        // Empty state
        if (dayEntries.isEmpty()) {
            item {
                com.familylogbook.app.ui.component.DayEmptyState(
                    modifier = Modifier.fillMaxSize(),
                    onAddEntry = onNavigateToAddEntry
                )
            }
        }
    }
    }
}

@Composable
private fun DayEntryCard(
    entry: LogEntry,
    person: Person?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = entry.rawText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            if (person != null) {
                Text(
                    text = person.name,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            Text(
                text = timeFormat.format(Date(entry.timestamp)),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ChecklistItem(
    entry: LogEntry,
    person: Person?,
    onToggle: () -> Unit
) {
    val isCompleted = entry.isCompleted == true
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                contentDescription = if (isCompleted) "Završeno" else "Nije završeno",
                tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = entry.rawText,
                    fontSize = 16.sp,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                
                if (person != null) {
                    Text(
                        text = person.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    entry: LogEntry,
    person: Person?,
    onClick: () -> Unit
) {
    val isDue = DayEntry.isReminderDue(entry)
    val dueDate = entry.reminderDate ?: return
    
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val dueDateText = dateFormat.format(Date(dueDate))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDue) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Podsjetnik",
                tint = if (isDue) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = entry.rawText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = if (isDue) "Rok: $dueDateText (ZAKAŠNJENO)" else "Rok: $dueDateText",
                    fontSize = 12.sp,
                    color = if (isDue) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    },
                    fontWeight = if (isDue) FontWeight.Bold else FontWeight.Normal
                )
                
                if (person != null) {
                    Text(
                        text = person.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDayState(
    modifier: Modifier = Modifier,
    onAddEntry: () -> Unit
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📅",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Nema dnevnih obaveza",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Dodaj rutine, checkliste ili podsjetnike da organiziraš dane.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onAddEntry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dodaj obavezu")
        }
    }
}
