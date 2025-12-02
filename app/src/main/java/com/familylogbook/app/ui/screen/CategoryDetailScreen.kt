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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Category.HEALTH -> "Health"
        Category.SLEEP -> "Sleep"
        Category.MOOD -> "Mood"
        Category.DEVELOPMENT -> "Development"
        Category.FEEDING -> "Feeding"
        Category.AUTO -> "Auto"
        Category.HOUSE -> "House"
        Category.FINANCE -> "Finance"
        Category.WORK -> "Work"
        Category.SHOPPING -> "Shopping"
        Category.SCHOOL -> "School"
        Category.KINDERGARTEN_SCHOOL -> "School"
        Category.HOME -> "Home"
        Category.OTHER -> "Other"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$categoryName Entries") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    text = "No $categoryName entries yet",
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
                                text = "Total: ${entries.size} entries",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Category: $categoryName",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Entries list
                items(entries) { entry ->
                    LogEntryCard(
                        entry = entry,
                        child = null,
                        viewModel = homeViewModel
                    )
                }
            }
        }
    }
}

