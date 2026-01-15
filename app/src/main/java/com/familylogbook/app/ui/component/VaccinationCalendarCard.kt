package com.familylogbook.app.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Vaccines
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
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.PersonType
import com.familylogbook.app.domain.vaccination.VaccinationCalendar
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Vaccination Calendar Card - shows next vaccination for children.
 * Parent OS core feature.
 */
@Composable
fun VaccinationCalendarCard(
    viewModel: HomeViewModel,
    onAddVaccination: () -> Unit,
    onVaccinationClick: (LogEntry) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val persons by viewModel.persons.collectAsState()
    val entries by viewModel.entries.collectAsState()
    
    val children = persons.filter { it.type == PersonType.CHILD && it.dateOfBirth != null }
    
    // Get vaccination entries
    val vaccinationEntries = entries
        .filter { it.category == com.familylogbook.app.domain.model.Category.VACCINATION || 
                   it.vaccinationName != null }
        .sortedByDescending { it.timestamp }
    
    // Get next vaccination for each child
    val nextVaccinations = children.mapNotNull { child ->
        val childVaccinations = vaccinationEntries
            .filter { it.personId == child.id || it.childId == child.id }
            .mapNotNull { it.vaccinationName }
        
        val recommendation = child.dateOfBirth?.let { dob ->
            VaccinationCalendar.getNextVaccination(
                dateOfBirth = dob,
                givenVaccinations = childVaccinations
            )
        }
        
        if (recommendation != null) {
            Triple(child, recommendation, recommendation.recommendedDate)
        } else {
            null
        }
    }.sortedBy { it.third } // Sort by recommended date
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onAddVaccination),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
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
                        imageVector = Icons.Default.Vaccines,
                        contentDescription = "Cjepiva",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Cjepiva",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(
                    onClick = onAddVaccination,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Dodaj cjepivo"
                    )
                }
            }
            
            if (children.isEmpty()) {
                // Empty state - no children
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Dodaj dijete da vidiš kalendar cjepiva",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else if (nextVaccinations.isEmpty()) {
                // No upcoming vaccinations
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Nema nadolazećih cjepiva",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                // Show next vaccination
                nextVaccinations.take(2).forEach { (child, recommendation, _) ->
                    VaccinationItem(
                        child = child,
                        recommendation = recommendation
                    )
                }
            }
        }
    }
}

@Composable
private fun VaccinationItem(
    child: Person,
    recommendation: VaccinationCalendar.VaccinationRecommendation
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val recommendedDateText = dateFormat.format(Date(recommendation.recommendedDate))
    
    val now = System.currentTimeMillis()
    val daysUntil = (recommendation.recommendedDate - now) / (24 * 60 * 60 * 1000L)
    val timeText = when {
        daysUntil < 0 -> "Zakašnjelo"
        daysUntil == 0L -> "Danas"
        daysUntil == 1L -> "Sutra"
        daysUntil < 7 -> "Za $daysUntil dana"
        daysUntil < 30 -> "Za ${daysUntil / 7} tjedna"
        else -> "Za ${daysUntil / 30} mjeseci"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (recommendation.shouldBeGivenASAP) {
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
                    text = child.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = recommendation.vaccinationType.shortName,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Text(
                    text = "$timeText ($recommendedDateText)",
                    fontSize = 12.sp,
                    color = if (recommendation.shouldBeGivenASAP) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    },
                    fontWeight = if (recommendation.shouldBeGivenASAP) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
