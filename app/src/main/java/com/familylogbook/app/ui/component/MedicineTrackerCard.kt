package com.familylogbook.app.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.MedicineEntry
import java.text.SimpleDateFormat
import java.util.*

/**
 * Medicine Tracker Card - shows active medicines and next doses.
 * Parent OS core feature.
 */
@Composable
fun MedicineTrackerCard(
    medicineEntries: List<LogEntry>,
    onAddMedicine: () -> Unit,
    onMedicineClick: (LogEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeMedicines = medicineEntries
        .filter { MedicineEntry.isMedicineEntry(it) }
        .filter { entry ->
            // Show only medicines with next dose in the future or recently due (within 1 hour)
            val nextDose = MedicineEntry.getNextDoseTime(entry)
            if (nextDose == null) return@filter false
            val now = System.currentTimeMillis()
            val oneHourAgo = now - (60 * 60 * 1000L)
            nextDose >= oneHourAgo // Show if next dose is in the future or within last hour
        }
        .sortedBy { MedicineEntry.getNextDoseTime(it) ?: Long.MAX_VALUE }
        .take(3) // Show max 3 active medicines
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onAddMedicine),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
                        imageVector = Icons.Default.Medication,
                        contentDescription = "Lijekovi",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Lijekovi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(
                    onClick = onAddMedicine,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Dodaj lijek"
                    )
                }
            }
            
            if (activeMedicines.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Nema aktivnih lijekova",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Dodaj lijek da počneš praćenje",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                // Medicine list
                activeMedicines.forEach { entry ->
                    MedicineItem(
                        entry = entry,
                        onClick = { onMedicineClick(entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicineItem(
    entry: LogEntry,
    onClick: () -> Unit
) {
    val nextDose = MedicineEntry.getNextDoseTime(entry)
    val isDue = nextDose != null && MedicineEntry.isDoseDue(entry)
    
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
                Text(
                    text = entry.medicineGiven ?: "Lijek",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (entry.medicineDosage != null) {
                    Text(
                        text = entry.medicineDosage,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                nextDose?.let { next ->
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val dateFormat = SimpleDateFormat("dd.MM.", Locale.getDefault())
                    val now = System.currentTimeMillis()
                    
                    val timeText = if (next > now) {
                        val hoursUntil = (next - now) / (60 * 60 * 1000L)
                        if (hoursUntil < 24) {
                            "Za ${hoursUntil}h"
                        } else {
                            "${dateFormat.format(Date(next))} u ${timeFormat.format(Date(next))}"
                        }
                    } else {
                        "Sada!"
                    }
                    
                    Text(
                        text = "Sljedeća doza: $timeText",
                        fontSize = 12.sp,
                        color = if (isDue) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        },
                        fontWeight = if (isDue) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            
            // Status indicator
            Surface(
                modifier = Modifier.size(12.dp),
                shape = RoundedCornerShape(6.dp),
                color = if (isDue) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            ) {}
        }
    }
}
