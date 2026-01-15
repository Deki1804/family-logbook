package com.familylogbook.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.MedicineEntry
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Medicine Timeline Screen - shows when medicines were taken.
 * Parent OS core feature.
 */
@Composable
fun MedicineTimelineScreen(
    viewModel: HomeViewModel,
    personId: String? = null,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.entries.collectAsState()
    val persons by viewModel.persons.collectAsState()
    
    // Filter medicine entries
    val medicineEntries = entries
        .filter { MedicineEntry.isMedicineEntry(it) }
        .filter { entry ->
            if (personId != null) {
                entry.personId == personId || entry.childId == personId
            } else {
                true
            }
        }
        .sortedByDescending { it.medicineTimestamp ?: it.timestamp }
        .groupBy { entry ->
            // Group by date
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = entry.medicineTimestamp ?: entry.timestamp
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }
    
    if (medicineEntries.isEmpty()) {
        EmptyMedicineTimeline(
            modifier = modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            medicineEntries.forEach { (date, entriesForDate) ->
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val dayFormat = SimpleDateFormat("EEEE", Locale("hr", "HR"))
                val dateObj = Date(date)
                
                item {
                    Text(
                        text = "${dayFormat.format(dateObj)}, ${dateFormat.format(dateObj)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(entriesForDate, key = { it.id }) { entry ->
                    val person = entry.personId?.let { pid ->
                        persons.find { it.id == pid }
                    }
                    
                    MedicineTimelineItem(
                        entry = entry,
                        person = person
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicineTimelineItem(
    entry: LogEntry,
    person: Person?
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timestamp = entry.medicineTimestamp ?: entry.timestamp
    val timeText = timeFormat.format(Date(timestamp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = "Lijek",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = entry.medicineGiven ?: "Lijek",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                if (entry.medicineDosage != null) {
                    Text(
                        text = entry.medicineDosage,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                if (person != null) {
                    Text(
                        text = person.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Text(
                text = timeText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmptyMedicineTimeline(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Medication,
            contentDescription = "Lijekovi",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Još nema zapisa o lijekovima",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Dodaj lijek da počneš praćenje",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
