package com.familylogbook.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.PersonType
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.MedicineEntry
import com.familylogbook.app.domain.model.SymptomEntry
import com.familylogbook.app.domain.util.PersonAgeUtils
import com.familylogbook.app.domain.vaccination.VaccinationCalendar
import com.familylogbook.app.ui.viewmodel.HomeViewModel

/**
 * Parent OS Child Tab Screen
 * 
 * Shows list of children and their profiles.
 * This is a placeholder - will be fully implemented in Phase 2.
 */
@Composable
fun ChildTabScreen(
    viewModel: HomeViewModel,
    onNavigateToPersonProfile: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val persons by viewModel.persons.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val children = persons.filter { it.type == PersonType.CHILD }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Djeca") },
                actions = {
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
    if (children.isEmpty()) {
        com.familylogbook.app.ui.component.ChildrenEmptyState(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            onAddChild = onNavigateToSettings
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            items(children, key = { it.id }) { child ->
                val childEntries = entries.filter { 
                    it.personId == child.id || it.childId == child.id 
                }
                
                // Get next vaccination
                val vaccinationEntries = childEntries
                    .filter { it.vaccinationName != null }
                    .mapNotNull { it.vaccinationName }
                
                val nextVaccination = child.dateOfBirth?.let { dob ->
                    VaccinationCalendar.getNextVaccination(
                        dateOfBirth = dob,
                        givenVaccinations = vaccinationEntries
                    )
                }
                
                // Get active medicines
                val activeMedicines = childEntries
                    .filter { MedicineEntry.isMedicineEntry(it) }
                    .filter { 
                        val nextDose = MedicineEntry.getNextDoseTime(it)
                        nextDose != null && nextDose >= System.currentTimeMillis() - (60 * 60 * 1000L)
                    }
                    .size
                
                // Get recent symptoms
                val recentSymptoms = childEntries
                    .filter { SymptomEntry.isSymptomEntry(it) }
                    .filter { 
                        System.currentTimeMillis() - it.timestamp < 7 * 24 * 60 * 60 * 1000L 
                    }
                    .size
                
                ChildCard(
                    child = child,
                    nextVaccination = nextVaccination,
                    activeMedicines = activeMedicines,
                    recentSymptoms = recentSymptoms,
                    totalEntries = childEntries.size,
                    onClick = { onNavigateToPersonProfile(child.id) }
                )
            }
        }
    }
    }
}

@Composable
private fun ChildCard(
    child: Person,
    nextVaccination: VaccinationCalendar.VaccinationRecommendation?,
    activeMedicines: Int,
    recentSymptoms: Int,
    totalEntries: Int,
    onClick: () -> Unit
) {
    val ageText = child.dateOfBirth?.let { dob ->
        val ageYears = PersonAgeUtils.calculateAgeInYears(dob)
        when {
            ageYears < 1.0 -> {
                val ageMonths = (ageYears * 12).toInt()
                "${ageMonths} ${if (ageMonths == 1) "mjesec" else "mjeseci"}"
            }
            ageYears < 2.0 -> {
                val months = ((ageYears - 1.0) * 12).toInt()
                if (months == 0) {
                    "1 godina"
                } else {
                    "1 godina ${months} ${if (months == 1) "mjesec" else "mjeseci"}"
                }
            }
            else -> {
                val years = ageYears.toInt()
                val months = ((ageYears - years) * 12).toInt()
                if (months == 0) {
                    "$years ${if (years == 1) "godina" else "godina"}"
                } else {
                    "$years ${if (years == 1) "godina" else "godina"} ${months} ${if (months == 1) "mjesec" else "mjeseci"}"
                }
            }
        }
    } ?: "Dob nije postavljena"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Child icon/avatar
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = child.emoji.ifEmpty { "👶" },
                            fontSize = 24.sp
                        )
                    }
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = child.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        text = ageText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (activeMedicines > 0) {
                    StatBadge(
                        label = "Lijekovi",
                        value = "$activeMedicines",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                if (recentSymptoms > 0) {
                    StatBadge(
                        label = "Simptomi",
                        value = "$recentSymptoms",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                StatBadge(
                    label = "Zapisi",
                    value = "$totalEntries",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Next vaccination
            nextVaccination?.let { vaccination ->
                val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                val recommendedDateText = dateFormat.format(java.util.Date(vaccination.recommendedDate))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = if (vaccination.shouldBeGivenASAP) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💉",
                            fontSize = 16.sp
                        )
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Sljedeće cjepivo: ${vaccination.vaccinationType.shortName}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = recommendedDateText,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun EmptyChildState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "👶",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Još nema djece",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Dodaj dijete u postavkama da počneš praćenje.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
