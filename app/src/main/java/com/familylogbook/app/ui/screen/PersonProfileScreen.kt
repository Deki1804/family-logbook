package com.familylogbook.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Mood
import com.familylogbook.app.ui.component.AdviceCard
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun PersonProfileScreen(
    personId: String,
    viewModel: HomeViewModel
) {
    val persons by viewModel.persons.collectAsState()
    val entries by viewModel.entries.collectAsState()
    
    val person = persons.find { it.id == personId }
    val personEntries = entries.filter { 
        it.personId == personId || it.childId == personId 
    }.sortedByDescending { it.timestamp }
    
    if (person == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Person not found")
        }
        return
    }
    
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color(android.graphics.Color.parseColor(person.avatarColor)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = person.emoji, fontSize = 24.sp)
                            }
                        }
                        Text(person.name)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Overview", "Health", "Development", "Feeding", "Sleep").forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }
            
            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> PersonOverviewTab(person = person, entries = personEntries, viewModel = viewModel)
                    1 -> PersonHealthTab(person = person, entries = personEntries, viewModel = viewModel)
                    2 -> PersonDevelopmentTab(person = person, entries = personEntries, viewModel = viewModel)
                    3 -> PersonFeedingTab(person = person, entries = personEntries, viewModel = viewModel)
                    4 -> PersonSleepTab(person = person, entries = personEntries, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PersonOverviewTab(
    person: Person,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val healthCount = entries.count { it.category == Category.HEALTH }
    val feedingCount = entries.count { it.category == Category.FEEDING }
    val sleepCount = entries.count { it.category == Category.SLEEP }
    val developmentCount = entries.count { it.category == Category.DEVELOPMENT }
    val moodCounts = entries.mapNotNull { it.mood }.groupingBy { it }.eachCount()
    val last7DaysEntries = entries.filter { 
        System.currentTimeMillis() - it.timestamp < 7 * 24 * 60 * 60 * 1000L 
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary stats
        item {
            PersonStatsSummary(
                healthCount = healthCount,
                feedingCount = feedingCount,
                sleepCount = sleepCount,
                developmentCount = developmentCount
            )
        }
        
        // Last 7 days summary
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
                        text = "Last 7 Days",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${last7DaysEntries.size} entries",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        // Mood trend
        if (moodCounts.isNotEmpty()) {
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
                            text = "Mood Overview",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        moodCounts.forEach { (mood, count) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(mood.name, fontSize = 14.sp)
                                Text("$count", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        
        // Recent entries
        item {
            Text(
                text = "Recent Entries",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(entries.take(5)) { entry ->
            LogEntryCard(
                entry = entry,
                child = null,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun PersonHealthTab(
    person: Person,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val healthEntries = entries.filter { it.category == Category.HEALTH }.sortedByDescending { it.timestamp }
    val temperatureEntries = healthEntries.filter { it.temperature != null }
    val medicineEntries = healthEntries.filter { it.medicineGiven != null }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Temperature chart
        if (temperatureEntries.isNotEmpty()) {
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
                            text = "Temperature History",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        temperatureEntries.take(7).forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                                        .format(java.util.Date(entry.timestamp)),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${entry.temperature}°C",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Medicines log
        if (medicineEntries.isNotEmpty()) {
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
                            text = "Medicines",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        medicineEntries.take(10).forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = entry.medicineGiven ?: "",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                                        .format(java.util.Date(entry.timestamp)),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // All health entries
        item {
            Text(
                text = "All Health Entries",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(healthEntries) { entry ->
            LogEntryCard(
                entry = entry,
                child = null,
                viewModel = viewModel
            )
        }
        
        if (healthEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No health entries yet",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun PersonDevelopmentTab(
    person: Person,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val developmentEntries = entries.filter { it.category == Category.DEVELOPMENT }.sortedByDescending { it.timestamp }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Development Milestones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(developmentEntries) { entry ->
            LogEntryCard(
                entry = entry,
                child = null,
                viewModel = viewModel
            )
        }
        
        if (developmentEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No development entries yet",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun PersonFeedingTab(
    person: Person,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val feedingEntries = entries.filter { it.category == Category.FEEDING }.sortedByDescending { it.timestamp }
    val bottleFeedings = feedingEntries.filter { it.feedingType == com.familylogbook.app.domain.model.FeedingType.BOTTLE }
    val totalBottleAmount = bottleFeedings.sumOf { it.feedingAmount?.toLong() ?: 0L }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary
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
                        text = "Feeding Summary",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total feedings: ${feedingEntries.size}",
                        fontSize = 14.sp
                    )
                    if (totalBottleAmount > 0) {
                        Text(
                            text = "Total bottle amount: ${totalBottleAmount}ml",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        
        // All feeding entries
        item {
            Text(
                text = "All Feeding Entries",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(feedingEntries) { entry ->
            LogEntryCard(
                entry = entry,
                child = null,
                viewModel = viewModel
            )
        }
        
        if (feedingEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No feeding entries yet",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun PersonSleepTab(
    person: Person,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val sleepEntries = entries.filter { it.category == Category.SLEEP }.sortedByDescending { it.timestamp }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Sleep Entries",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(sleepEntries) { entry ->
            LogEntryCard(
                entry = entry,
                child = null,
                viewModel = viewModel
            )
        }
        
        if (sleepEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No sleep entries yet",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun PersonStatsSummary(
    healthCount: Int,
    feedingCount: Int,
    sleepCount: Int,
    developmentCount: Int
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Health", healthCount, Color(0xFFFF6B6B))
                StatItem("Feeding", feedingCount, Color(0xFFFFB84D))
                StatItem("Sleep", sleepCount, Color(0xFF4ECDC4))
                StatItem("Development", developmentCount, Color(0xFF95E1D3))
            }
        }
    }
}

