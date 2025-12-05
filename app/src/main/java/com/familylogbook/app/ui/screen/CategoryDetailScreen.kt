package com.familylogbook.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.data.smarthome.SmartHomeManager
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import com.familylogbook.app.ui.viewmodel.StatsViewModel

@Composable
fun CategoryDetailScreen(
    category: Category,
    statsViewModel: StatsViewModel,
    homeViewModel: HomeViewModel,
    onNavigateBack: () -> Unit
) {
    val entries = statsViewModel.getEntriesByCategory(category)
    val categoryName = when (category) {
        Category.HEALTH -> "Zdravlje"
        Category.SLEEP -> "Spavanje"
        Category.MOOD -> "Raspoloženje"
        Category.DEVELOPMENT -> "Razvoj"
        Category.FEEDING -> "Hranjenje"
        Category.AUTO -> "Auto"
        Category.HOUSE -> "Kuća"
        Category.FINANCE -> "Financije"
        Category.WORK -> "Posao"
        Category.SHOPPING -> "Kupovina"
        Category.SCHOOL -> "Škola"
        Category.KINDERGARTEN_SCHOOL -> "Škola"
        Category.HOME -> "Dom"
        Category.SMART_HOME -> "Pametni dom"
        Category.OTHER -> "Ostalo"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$categoryName - Zapisi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Natrag")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Još nema zapisa za $categoryName",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Ukupno: ${entries.size} zapisa",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kategorija: $categoryName",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Entries list
                items(entries) { entry ->
                    val person = entry.personId?.let { personId ->
                        homeViewModel.getPersonById(personId)
                    }
                    val entity = entry.entityId?.let { entityId ->
                        homeViewModel.getEntityById(entityId)
                    }
                    val context = LocalContext.current
                    val smartHomeManager = remember(context) { SmartHomeManager(context) }
                    LogEntryCard(
                        entry = entry,
                        person = person,
                        entity = entity,
                        viewModel = homeViewModel,
                        smartHomeManager = smartHomeManager
                    )
                }
            }
        }
    }
}

