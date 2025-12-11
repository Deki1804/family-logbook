package com.familylogbook.app.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.util.PersonAgeUtils
import com.familylogbook.app.domain.vaccination.VaccinationCalendar
import java.text.SimpleDateFormat
import java.util.*

/**
 * Compact overview card showing today's key highlights.
 * Displays: last feeding, next vaccination, today's entries summary, etc.
 */
@Composable
fun TodayOverviewCard(
    entries: List<LogEntry>,
    persons: List<Person>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayStart = today.timeInMillis
    val todayEnd = todayStart + (24 * 60 * 60 * 1000L)
    
    val todayEntries = entries.filter { it.timestamp >= todayStart && it.timestamp < todayEnd }
    
    // Find last feeding
    val lastFeeding = entries
        .filter { it.category == com.familylogbook.app.domain.model.Category.FEEDING && it.feedingType != null }
        .maxByOrNull { it.timestamp }
    
    // Find next vaccination
    val nextVaccination = entries
        .filter { it.nextVaccinationDate != null }
        .minByOrNull { it.nextVaccinationDate ?: Long.MAX_VALUE }
    
    // Count today's entries by category
    val shoppingCount = todayEntries.count { it.category == com.familylogbook.app.domain.model.Category.SHOPPING }
    val healthCount = todayEntries.count { it.category == com.familylogbook.app.domain.model.Category.HEALTH }
    val smartHomeCount = todayEntries.count { it.category == com.familylogbook.app.domain.model.Category.SMART_HOME }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
            Text(
                text = "📊 Danas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Highlights in compact rows
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Last feeding
                lastFeeding?.let { feeding ->
                    val person = persons.find { it.id == feeding.personId }
                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(feeding.timestamp))
                    val amountStr = feeding.feedingAmount?.let { "$it ml" } ?: ""
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🍼 Zadnje hranjenje",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${person?.name ?: "Dijete"} u $timeStr $amountStr".trim(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Next vaccination
                nextVaccination?.let { vaccination ->
                    val person = persons.find { it.id == vaccination.personId }
                    val daysUntil = ((vaccination.nextVaccinationDate ?: 0L) - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)
                    val timeStr = when {
                        daysUntil < 0L -> "Prošlo"
                        daysUntil == 0L -> "Danas"
                        daysUntil < 30L -> "Za $daysUntil dana"
                        else -> {
                            val months = daysUntil / 30L
                            "Za $months ${if (months == 1L) "mjesec" else "mjeseci"}"
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💉 Sljedeće cjepivo",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${person?.name ?: "Dijete"}: $timeStr",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Today's summary
                if (todayEntries.isNotEmpty()) {
                    val summaryParts = mutableListOf<String>()
                    if (shoppingCount > 0) summaryParts.add("$shoppingCount shopping")
                    if (healthCount > 0) summaryParts.add("$healthCount zdravlje")
                    if (smartHomeCount > 0) summaryParts.add("$smartHomeCount smart home")
                    if (summaryParts.isEmpty()) summaryParts.add("${todayEntries.size} zapisa")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📝 Danas",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = summaryParts.joinToString(", "),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📝 Danas",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Nema zapisa",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
