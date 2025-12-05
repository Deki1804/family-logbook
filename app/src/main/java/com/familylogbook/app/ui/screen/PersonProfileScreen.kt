package com.familylogbook.app.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.PersonType
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Mood
import com.familylogbook.app.data.smarthome.SmartHomeManager
import com.familylogbook.app.ui.component.AdviceCard
import com.familylogbook.app.ui.component.StatItem
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
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
            Text("Osoba nije pronađena")
        }
        return
    }
    
    // Determine tabs based on person type
    val tabs = when (person.type) {
        PersonType.PARENT -> listOf("Pregled", "Zdravlje", "Raspoloženje", "Posao", "Bilješke")
        PersonType.CHILD -> listOf("Pregled", "Zdravlje", "Razvoj", "Hranjenje", "Spavanje")
        PersonType.PET -> listOf("Zdravlje", "Raspoloženje", "Hrana")
        PersonType.OTHER_FAMILY_MEMBER -> listOf("Pregled", "Zdravlje", "Raspoloženje", "Bilješke")
    }
    
    val pagerState = rememberPagerState(initialPage = 0) { tabs.size }
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
                tabs.forEachIndexed { index, title ->
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
                when (person.type) {
                    PersonType.PARENT -> {
                        when (page) {
                            0 -> PersonOverviewTab(person = person, entries = personEntries, viewModel = viewModel)
                            1 -> PersonHealthTab(person = person, entries = personEntries, viewModel = viewModel)
                            2 -> PersonMoodTab(person = person, entries = personEntries, viewModel = viewModel)
                            3 -> PersonWorkTab(person = person, entries = personEntries, viewModel = viewModel)
                            4 -> PersonNotesTab(person = person, entries = personEntries, viewModel = viewModel)
                            else -> {}
                        }
                    }
                    PersonType.CHILD -> {
                        when (page) {
                            0 -> PersonOverviewTab(person = person, entries = personEntries, viewModel = viewModel)
                            1 -> PersonHealthTab(person = person, entries = personEntries, viewModel = viewModel)
                            2 -> PersonDevelopmentTab(person = person, entries = personEntries, viewModel = viewModel)
                            3 -> PersonFeedingTab(person = person, entries = personEntries, viewModel = viewModel)
                            4 -> PersonSleepTab(person = person, entries = personEntries, viewModel = viewModel)
                            else -> {}
                        }
                    }
                    PersonType.PET -> {
                        when (page) {
                            0 -> PersonHealthTab(person = person, entries = personEntries, viewModel = viewModel)
                            1 -> PersonMoodTab(person = person, entries = personEntries, viewModel = viewModel)
                            2 -> PersonFoodTab(person = person, entries = personEntries, viewModel = viewModel)
                            else -> {}
                        }
                    }
                    PersonType.OTHER_FAMILY_MEMBER -> {
                        when (page) {
                            0 -> PersonOverviewTab(person = person, entries = personEntries, viewModel = viewModel)
                            1 -> PersonHealthTab(person = person, entries = personEntries, viewModel = viewModel)
                            2 -> PersonMoodTab(person = person, entries = personEntries, viewModel = viewModel)
                            3 -> PersonNotesTab(person = person, entries = personEntries, viewModel = viewModel)
                            else -> {}
                        }
                    }
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
        
        items(entries.take(10)) { entry ->
            val person = entry.personId?.let { personId ->
                viewModel.getPersonById(personId)
            }
            val entity = entry.entityId?.let { entityId ->
                viewModel.getEntityById(entityId)
            }
            val context = LocalContext.current
            val smartHomeManager = remember(context) { SmartHomeManager(context) }
            LogEntryCard(
                entry = entry,
                person = person,
                entity = entity,
                viewModel = viewModel,
                smartHomeManager = smartHomeManager
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
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Health Entries",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(healthEntries) { entry ->
            val person = entry.personId?.let { personId ->
                viewModel.getPersonById(personId)
            }
            val entity = entry.entityId?.let { entityId ->
                viewModel.getEntityById(entityId)
            }
            val context = LocalContext.current
            val smartHomeManager = remember(context) { SmartHomeManager(context) }
            LogEntryCard(
                entry = entry,
                person = person,
                entity = entity,
                viewModel = viewModel,
                smartHomeManager = smartHomeManager
            )
        }
        
        if (healthEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Još nema zapisa o zdravlju",
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
            val person = entry.personId?.let { personId ->
                viewModel.getPersonById(personId)
            }
            val entity = entry.entityId?.let { entityId ->
                viewModel.getEntityById(entityId)
            }
            val context = LocalContext.current
            val smartHomeManager = remember(context) { SmartHomeManager(context) }
            LogEntryCard(
                entry = entry,
                person = person,
                entity = entity,
                viewModel = viewModel,
                smartHomeManager = smartHomeManager
            )
        }
        
        if (developmentEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Još nema zapisa o razvoju",
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
    
    // Calculate intervals between feedings (in hours)
    val feedingIntervals = remember(feedingEntries) {
        if (feedingEntries.size >= 2) {
            feedingEntries.sortedBy { it.timestamp }.zipWithNext().map { (current, next) ->
                (current.timestamp - next.timestamp) / (1000.0 * 60 * 60) // Convert to hours
            }
        } else emptyList()
    }
    val averageInterval = if (feedingIntervals.isNotEmpty()) {
        feedingIntervals.average()
    } else null
    
    // Calculate average breastfeeding duration (extract from rawText)
    val breastfeedingEntries = feedingEntries.filter { 
        it.feedingType == com.familylogbook.app.domain.model.FeedingType.BREAST_LEFT ||
        it.feedingType == com.familylogbook.app.domain.model.FeedingType.BREAST_RIGHT
    }
    val averageBreastfeedingDuration = remember(breastfeedingEntries) {
        val durations = breastfeedingEntries.mapNotNull { entry ->
            // Try to extract duration from text like "trajalo 15 minuta" or "15 min"
            val durationRegex = Regex("""(\d+)\s*(?:min|minuta|minute)""", RegexOption.IGNORE_CASE)
            durationRegex.find(entry.rawText)?.groupValues?.get(1)?.toIntOrNull()
        }
        if (durations.isNotEmpty()) durations.average() else null
    }
    
    // Last feeding info
    val lastFeeding = feedingEntries.firstOrNull()
    val lastFeedingTime = lastFeeding?.timestamp
    val hoursSinceLastFeeding = remember(lastFeedingTime) {
        if (lastFeedingTime != null) {
            (System.currentTimeMillis() - lastFeedingTime) / (1000.0 * 60 * 60)
        } else null
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Last Feeding Card
        if (lastFeeding != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Zadnje hranjenje",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when (lastFeeding.feedingType) {
                                    com.familylogbook.app.domain.model.FeedingType.BREAST_LEFT -> "👈 Dojenje (lijeva)"
                                    com.familylogbook.app.domain.model.FeedingType.BREAST_RIGHT -> "👉 Dojenje (desna)"
                                    com.familylogbook.app.domain.model.FeedingType.BOTTLE -> "🍼 Bočica${lastFeeding.feedingAmount?.let { " - ${it}ml" } ?: ""}"
                                    null -> "🍼 Hranjenje"
                                },
                                fontSize = 14.sp
                            )
                            Text(
                                text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date(lastFeeding.timestamp)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (hoursSinceLastFeeding != null) {
                            Text(
                                text = "Prije ${String.format("%.1f", hoursSinceLastFeeding.toFloat())} sati",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
        
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
                        text = "Sažetak",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ukupno hranjenja: ${feedingEntries.size}",
                        fontSize = 14.sp
                    )
                    if (totalBottleAmount > 0) {
                        Text(
                            text = "Ukupno bočica: ${totalBottleAmount}ml",
                            fontSize = 14.sp
                        )
                    }
                    if (averageInterval != null) {
                        Text(
                            text = "Prosječni interval: ${String.format("%.1f", averageInterval.toFloat())} sati",
                            fontSize = 14.sp
                        )
                    }
                    if (averageBreastfeedingDuration != null) {
                        Text(
                            text = "Prosječno trajanje dojenja: ${String.format("%.0f", averageBreastfeedingDuration.toFloat())} min",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        
        // Feeding Intervals Chart
        if (feedingIntervals.isNotEmpty()) {
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Intervali između hranjenja",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Simple bar chart representation
                        val maxInterval = feedingIntervals.maxOrNull() ?: 1.0
                        val intervalsToShow = feedingIntervals.take(10)
                        intervalsToShow.forEachIndexed { index, interval ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(30.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(20.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            RoundedCornerShape(4.dp)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth((interval / maxInterval).coerceAtMost(1.0).toFloat())
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                }
                                Text(
                                    text = "${String.format("%.1f", interval.toFloat())}h",
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(40.dp)
                                )
                            }
                        }
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
            val person = entry.personId?.let { personId ->
                viewModel.getPersonById(personId)
            }
            val entity = entry.entityId?.let { entityId ->
                viewModel.getEntityById(entityId)
            }
            val context = LocalContext.current
            val smartHomeManager = remember(context) { SmartHomeManager(context) }
            LogEntryCard(
                entry = entry,
                person = person,
                entity = entity,
                viewModel = viewModel,
                smartHomeManager = smartHomeManager
            )
        }
        
        if (feedingEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Još nema zapisa o hranjenju",
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
            val person = entry.personId?.let { personId ->
                viewModel.getPersonById(personId)
            }
            val entity = entry.entityId?.let { entityId ->
                viewModel.getEntityById(entityId)
            }
            val context = LocalContext.current
            val smartHomeManager = remember(context) { SmartHomeManager(context) }
            LogEntryCard(
                entry = entry,
                person = person,
                entity = entity,
                viewModel = viewModel,
                smartHomeManager = smartHomeManager
            )
        }
        
        if (sleepEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Još nema zapisa o spavanju",
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
                StatItem("Zdravlje", healthCount, Color(0xFFFF6B6B))
                StatItem("Hranjenje", feedingCount, Color(0xFFFFB84D))
                StatItem("Spavanje", sleepCount, Color(0xFF4ECDC4))
                StatItem("Razvoj", developmentCount, Color(0xFF95E1D3))
            }
        }
    }
}

@Composable
fun PersonMoodTab(
    person: Person,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val moodEntries = entries.filter { it.mood != null }.sortedByDescending { it.timestamp }
    val moodCounts = moodEntries.mapNotNull { it.mood }.groupingBy { it }.eachCount()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mood summary
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
        
        // All mood entries
        item {
            Text(
                text = "All Mood Entries",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(moodEntries) { entry ->
            val person = entry.personId?.let { personId ->
                viewModel.getPersonById(personId)
            }
            val entity = entry.entityId?.let { entityId ->
                viewModel.getEntityById(entityId)
            }
            val context = LocalContext.current
            val smartHomeManager = remember(context) { SmartHomeManager(context) }
            LogEntryCard(
                entry = entry,
                person = person,
                entity = entity,
                viewModel = viewModel,
                smartHomeManager = smartHomeManager
            )
        }
        
        if (moodEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Još nema zapisa o raspoloženju",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun PersonWorkTab(
    person: Person,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val workEntries = entries.filter { it.category == Category.WORK }.sortedByDescending { it.timestamp }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Work Entries",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(workEntries) { entry ->
            val person = entry.personId?.let { personId ->
                viewModel.getPersonById(personId)
            }
            val entity = entry.entityId?.let { entityId ->
                viewModel.getEntityById(entityId)
            }
            val context = LocalContext.current
            val smartHomeManager = remember(context) { SmartHomeManager(context) }
            LogEntryCard(
                entry = entry,
                person = person,
                entity = entity,
                viewModel = viewModel,
                smartHomeManager = smartHomeManager
            )
        }
        
        if (workEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Još nema zapisa o poslu",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun PersonNotesTab(
    person: Person,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val notesEntries = entries.filter { it.category == Category.OTHER }
        .sortedByDescending { it.timestamp }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Bilješke",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(notesEntries) { entry ->
            val person = entry.personId?.let { personId ->
                viewModel.getPersonById(personId)
            }
            val entity = entry.entityId?.let { entityId ->
                viewModel.getEntityById(entityId)
            }
            val context = LocalContext.current
            val smartHomeManager = remember(context) { SmartHomeManager(context) }
            LogEntryCard(
                entry = entry,
                person = person,
                entity = entity,
                viewModel = viewModel,
                smartHomeManager = smartHomeManager
            )
        }
        
        if (notesEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Još nema bilješki",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun PersonFoodTab(
    person: Person,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val foodEntries = entries.filter { 
        it.category == Category.FEEDING || 
        (it.category == Category.OTHER && (it.rawText.lowercase().contains("hrana") || it.rawText.lowercase().contains("food")))
    }.sortedByDescending { it.timestamp }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Food Entries",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(foodEntries) { entry ->
            val person = entry.personId?.let { personId ->
                viewModel.getPersonById(personId)
            }
            val entity = entry.entityId?.let { entityId ->
                viewModel.getEntityById(entityId)
            }
            val context = LocalContext.current
            val smartHomeManager = remember(context) { SmartHomeManager(context) }
            LogEntryCard(
                entry = entry,
                person = person,
                entity = entity,
                viewModel = viewModel,
                smartHomeManager = smartHomeManager
            )
        }
        
        if (foodEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Još nema zapisa o hrani",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
