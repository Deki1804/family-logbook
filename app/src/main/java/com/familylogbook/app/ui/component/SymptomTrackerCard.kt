package com.familylogbook.app.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.SymptomEntry
import java.text.SimpleDateFormat
import java.util.*

/**
 * Symptom Tracker Card - shows recent symptoms and temperature.
 * Parent OS core feature.
 */
@Composable
fun SymptomTrackerCard(
    symptomEntries: List<LogEntry>,
    onAddSymptom: () -> Unit,
    onSymptomClick: (LogEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val recentSymptoms = symptomEntries
        .filter { SymptomEntry.isSymptomEntry(it) }
        .sortedByDescending { it.timestamp }
        .take(3) // Show max 3 recent symptoms
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onAddSymptom),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = "Simptomi",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Simptomi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(
                    onClick = onAddSymptom,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Dodaj simptom"
                    )
                }
            }
            
            if (recentSymptoms.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Još nema zapisa o simptomima",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Dodaj simptom da počneš praćenje",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                // Symptom list
                recentSymptoms.forEach { entry ->
                    SymptomItem(
                        entry = entry,
                        onClick = { onSymptomClick(entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SymptomItem(
    entry: LogEntry,
    onClick: () -> Unit
) {
    val temperature = SymptomEntry.getTemperature(entry)
    val symptoms = SymptomEntry.getSymptoms(entry)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd.MM.", Locale.getDefault())
    val timestamp = entry.timestamp
    val timeText = if (isToday(timestamp)) {
        "Danas u ${timeFormat.format(Date(timestamp))}"
    } else {
        "${dateFormat.format(Date(timestamp))} u ${timeFormat.format(Date(timestamp))}"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (temperature != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🌡️",
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${temperature}°C",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (temperature >= 38.0f) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
                
                if (symptoms.isNotEmpty()) {
                    Text(
                        text = symptoms.joinToString(", "),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                
                Text(
                    text = timeText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun isToday(timestamp: Long): Boolean {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = now
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis
    
    val entryCalendar = Calendar.getInstance()
    entryCalendar.timeInMillis = timestamp
    entryCalendar.set(Calendar.HOUR_OF_DAY, 0)
    entryCalendar.set(Calendar.MINUTE, 0)
    entryCalendar.set(Calendar.SECOND, 0)
    entryCalendar.set(Calendar.MILLISECOND, 0)
    val startOfEntryDay = entryCalendar.timeInMillis
    
    return startOfEntryDay == startOfToday
}
