package com.familylogbook.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.data.smarthome.SmartHomeManager
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Mood
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.ui.component.AdviceCard
import com.familylogbook.app.ui.navigation.Screen
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class DayGroup(val label: String) {
    TODAY("Danas"),
    YESTERDAY("Jučer"),
    THIS_WEEK("Ovaj tjedan"),
    OLDER("Starije")
}

fun getCategoryDisplayName(category: Category): String {
    return when (category) {
        Category.HEALTH -> "Zdravlje"
        Category.SLEEP -> "Spavanje"
        Category.MOOD -> "Raspoloženje"
        Category.DEVELOPMENT -> "Razvoj"
        Category.KINDERGARTEN_SCHOOL -> "Škola"
        Category.SCHOOL -> "Škola"
        Category.HOME -> "Dom"
        Category.HOUSE -> "Kuća"
        Category.FEEDING -> "Hranjenje"
        Category.AUTO -> "Auto"
        Category.FINANCE -> "Financije"
        Category.WORK -> "Posao"
        Category.SHOPPING -> "Kupovina"
        Category.SMART_HOME -> "Pametni dom"
        Category.OTHER -> "Ostalo"
    }
}

fun getDayGroup(timestamp: Long): DayGroup {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = now
    
    // Start of today
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis
    
    // Start of yesterday
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val startOfYesterday = calendar.timeInMillis
    
    // Start of this week (Monday)
    calendar.timeInMillis = now
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfWeek = calendar.timeInMillis
    
    return when {
        timestamp >= startOfToday -> DayGroup.TODAY
        timestamp >= startOfYesterday -> DayGroup.YESTERDAY
        timestamp >= startOfWeek -> DayGroup.THIS_WEEK
        else -> DayGroup.OLDER
    }
}

data class EntryGroup(
    val group: DayGroup,
    val entries: List<LogEntry>
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAddEntry: () -> Unit,
    onNavigateToEditEntry: (String) -> Unit = {},
    onNavigateToPersonProfile: (String) -> Unit = {},
    onNavigateToEntityProfile: (String) -> Unit = {},
    onNavigateToCategoryDetail: (Category) -> Unit = {}
) {
    val entries by viewModel.filteredEntries.collectAsState()
    val children by viewModel.children.collectAsState()
    val persons by viewModel.persons.collectAsState()
    val entities by viewModel.entities.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedPersonId by viewModel.selectedPersonId.collectAsState()
    val selectedEntityId by viewModel.selectedEntityId.collectAsState()
    
    val context = LocalContext.current
    val smartHomeManager = remember(context) { SmartHomeManager(context) }
    var showFilterSheet by remember { mutableStateOf(false) }
    
    // LazyListState for scroll control
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Reset scroll position to top when filters change (e.g., when navigating from Stats)
    // This ensures that when user clicks on a category in Stats, they see filtered results from the top
    LaunchedEffect(selectedCategory, selectedPersonId, selectedEntityId) {
        scope.launch {
            lazyListState.animateScrollToItem(0)
        }
    }
    
    // Count active filters
    val activeFilterCount = remember(selectedPersonId, selectedEntityId, selectedCategory) {
        var count = 0
        if (selectedPersonId != null) count++
        if (selectedEntityId != null) count++
        if (selectedCategory != null) count++
        count
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (entries.isEmpty()) {
            EmptyState(
                modifier = Modifier.align(Alignment.Center),
                onAddEntry = onNavigateToAddEntry
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Compact filters section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search bar with filter button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Filter button with badge
                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (activeFilterCount > 0) {
                                    Badge(
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text("$activeFilterCount", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Active filters as chips (only show if any are active)
                    if (activeFilterCount > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Person filter
                            selectedPersonId?.let { personId ->
                                val person = persons.find { it.id == personId }
                                person?.let {
                                    AssistChip(
                                        onClick = { viewModel.setSelectedPerson(null) },
                                        label = { Text("${it.emoji} ${it.name}") },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Ukloni",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                            
                            // Entity filter
                            selectedEntityId?.let { entityId ->
                                val entity = entities.find { it.id == entityId }
                                entity?.let {
                                    AssistChip(
                                        onClick = { viewModel.setSelectedEntity(null) },
                                        label = { Text("${it.emoji} ${it.name}") },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Ukloni",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                            
                            // Category filter
                            selectedCategory?.let { category ->
                                AssistChip(
                                    onClick = { viewModel.setSelectedCategory(null) },
                                    label = { Text(getCategoryDisplayName(category)) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Ukloni",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                            
                            // Clear all button
                            TextButton(onClick = { viewModel.clearFilters() }) {
                                Text("Obriši sve", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Finance Summary Card (conditionally displayed)
                if (selectedCategory == Category.FINANCE && entries.isNotEmpty()) {
                    val financeEntries = entries.filter { it.category == Category.FINANCE }
                    if (financeEntries.isNotEmpty()) {
                        FinanceSummaryCard(financeEntries = financeEntries)
                    }
                }

                // Group entries by day
                val groupedEntries = remember(entries) {
                    entries.groupBy { getDayGroup(it.timestamp) }
                        .map { (group, entries) -> EntryGroup(group, entries) }
                        .sortedBy { group ->
                            when (group.group) {
                                DayGroup.TODAY -> 0
                                DayGroup.YESTERDAY -> 1
                                DayGroup.THIS_WEEK -> 2
                                DayGroup.OLDER -> 3
                            }
                        }
                }
                
                // Entries list with grouping
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedEntries.forEach { entryGroup ->
                        // Group header
                        item(key = "header_${entryGroup.group}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = entryGroup.group.label,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${entryGroup.entries.size}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Divider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                        
                        // Group entries
                        items(
                            items = entryGroup.entries,
                            key = { it.id }
                        ) { entry ->
                            val person = entry.personId?.let { personId ->
                                persons.find { it.id == personId }
                            } ?: entry.childId?.let { childId -> // Backward compatibility
                                children.find { it.id == childId }?.let {
                                    Person(it.id, it.name, emoji = it.emoji, avatarColor = it.avatarColor)
                                }
                            }
                            val entity = entry.entityId?.let { entityId ->
                                entities.find { it.id == entityId }
                            }
                            LogEntryCard(
                                entry = entry,
                                person = person,
                                entity = entity,
                                viewModel = viewModel,
                                smartHomeManager = smartHomeManager,
                                onPersonClick = { personId ->
                                    onNavigateToPersonProfile(personId)
                                },
                                onEntityClick = { entityId ->
                                    onNavigateToEntityProfile(entityId)
                                },
                                onCategoryClick = { category ->
                                    onNavigateToCategoryDetail(category)
                                },
                                onEditClick = onNavigateToEditEntry
                            )
                        }
                    }
                }
            }
        }
        
        FloatingActionButton(
            onClick = onNavigateToAddEntry,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Dodaj zapis")
        }
        
        // Filter Bottom Sheet
        if (showFilterSheet) {
            FilterBottomSheet(
                persons = persons,
                entities = entities,
                selectedPersonId = selectedPersonId,
                selectedEntityId = selectedEntityId,
                selectedCategory = selectedCategory,
                onPersonSelected = { viewModel.setSelectedPerson(it) },
                onEntitySelected = { viewModel.setSelectedEntity(it) },
                onCategorySelected = { viewModel.setSelectedCategory(it) },
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogEntryCard(
    entry: LogEntry,
    person: Person?,
    entity: Entity?,
    viewModel: HomeViewModel,
    smartHomeManager: SmartHomeManager,
    onPersonClick: (String) -> Unit = {},
    onEntityClick: (String) -> Unit = {},
    onCategoryClick: (Category) -> Unit = {},
    onEditClick: (String) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { /* Regular click action if any */ },
                    onLongClick = { showMenu = true }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Person/Entity and Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        person?.let {
                            Surface(
                                color = Color(android.graphics.Color.parseColor(it.avatarColor)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { onPersonClick(it.id) }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = it.emoji, fontSize = 16.sp)
                                }
                            }
                            Text(
                                text = it.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable { onPersonClick(it.id) }
                            )
                        } ?: entity?.let {
                            Surface(
                                color = Color(android.graphics.Color.parseColor(it.avatarColor)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = it.emoji, fontSize = 16.sp)
                                }
                            }
                            Text(
                                text = it.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable { onEntityClick(it.id) }
                            )
                        } ?: Text(
                            text = "Family",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = formatTimestamp(entry.timestamp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Category and Text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(category = entry.category, onClick = { onCategoryClick(entry.category) })
                    Text(
                        text = entry.rawText,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Tags
                if (entry.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        entry.tags.forEach { tag ->
                            AssistChip(
                                onClick = { viewModel.setSearchQuery(tag) },
                                label = { Text(tag, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            
            // Mood indicator
            entry.mood?.let { mood ->
                MoodIndicator(mood = mood)
            }
            
            // Temperature / Medicine info
            entry.temperature?.let { temp ->
                Text(
                    text = "🌡️ Temperatura: ${temp}°C",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Symptoms display
            entry.symptoms?.takeIf { it.isNotEmpty() }?.let { symptomsList ->
                Text(
                    text = "🏥 Simptomi: ${symptomsList.joinToString(", ")}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
                    entry.medicineGiven?.let { medicine ->
                        Column(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "💊 Lijek: $medicine",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            // Show next medicine time if available
                            entry.nextMedicineTime?.let { nextTime ->
                                val now = System.currentTimeMillis()
                                val timeUntilNext = nextTime - now
                                val hoursUntil = timeUntilNext / (60 * 60 * 1000)
                                val minutesUntil = (timeUntilNext % (60 * 60 * 1000)) / (60 * 1000)
                                
                                val timeText = when {
                                    timeUntilNext < 0 -> {
                                        val overdueHours = -hoursUntil
                                        val overdueMinutes = -minutesUntil
                                        "⏰ Preko vremena: ${overdueHours}h ${overdueMinutes}min"
                                    }
                                    hoursUntil > 0 -> "⏰ Sljedeće uzimanje: za ${hoursUntil}h ${minutesUntil}min"
                                    minutesUntil > 0 -> "⏰ Sljedeće uzimanje: za ${minutesUntil}min"
                                    else -> "⏰ Sljedeće uzimanje: sada"
                                }
                                
                                Text(
                                    text = timeText,
                                    fontSize = 11.sp,
                                    color = if (timeUntilNext < 0) {
                                        MaterialTheme.colorScheme.error
                                    } else if (timeUntilNext < 60 * 60 * 1000L) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    },
                                    fontWeight = if (timeUntilNext < 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
            
            // Feeding info
            entry.feedingType?.let { feedingType ->
                val feedingText = when (feedingType) {
                    com.familylogbook.app.domain.model.FeedingType.BREAST_LEFT -> "🍼 Dojenje (lijeva)"
                    com.familylogbook.app.domain.model.FeedingType.BREAST_RIGHT -> "🍼 Dojenje (desna)"
                    com.familylogbook.app.domain.model.FeedingType.BOTTLE -> "🍼 Bočica${entry.feedingAmount?.let { " - ${it}ml" } ?: ""}"
                }
                Text(
                    text = feedingText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Smart Home action button
            if (entry.category == Category.SMART_HOME) {
                var showErrorDialog by remember { mutableStateOf<String?>(null) }
                
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        // Execute command directly - no user voice input needed
                        val result = smartHomeManager.executeCommand(entry.rawText)
                        when (result) {
                            is com.familylogbook.app.data.smarthome.SmartHomeManager.CommandResult.Success -> {
                                // Command sent successfully
                            }
                            is com.familylogbook.app.data.smarthome.SmartHomeManager.CommandResult.Error -> {
                                showErrorDialog = result.message
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("🤖 Pošalji komandu")
                }
                
                // Error dialog
                showErrorDialog?.let { errorMessage ->
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showErrorDialog = null },
                        title = { Text("Ne mogu poslati komandu") },
                        text = { Text(errorMessage) },
                        confirmButton = {
                            TextButton(onClick = { showErrorDialog = null }) {
                                Text("U redu")
                            }
                        }
                    )
                }
            }
        }
        }
        
        // Context Menu (DropdownMenu)
        Box {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Uredi") },
                    onClick = {
                        onEditClick(entry.id)
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Obriši") },
                    onClick = {
                        showMenu = false
                        showDeleteDialog = true
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
        
        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Obriši zapis") },
                text = { Text("Želiš li sigurno obrisati ovaj zapis? Ova akcija se ne može poništiti.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            scope.launch {
                                viewModel.deleteEntry(entry.id)
                            }
                        }
                    ) {
                        Text("Obriši", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Odustani")
                    }
                }
            )
        }
        
        // Advice Card (if applicable)
        viewModel.getAdviceForEntry(entry)?.let { advice ->
            Spacer(modifier = Modifier.height(8.dp))
            AdviceCard(
                advice = advice,
                category = entry.category
            )
        }
    }
}

@Composable
fun CategoryChip(category: Category, onClick: () -> Unit = {}) {
    val (label, color) = when (category) {
        Category.HEALTH -> "Zdravlje" to Color(0xFFFF6B6B)
        Category.SLEEP -> "Spavanje" to Color(0xFF4ECDC4)
        Category.MOOD -> "Raspoloženje" to Color(0xFFFFD93D)
        Category.DEVELOPMENT -> "Razvoj" to Color(0xFF95E1D3)
        Category.KINDERGARTEN_SCHOOL -> "Škola" to Color(0xFFAA96DA)
        Category.SCHOOL -> "Škola" to Color(0xFFAA96DA)
        Category.HOME -> "Dom" to Color(0xFFF38181)
        Category.HOUSE -> "Kuća" to Color(0xFFF38181)
        Category.FEEDING -> "Hranjenje" to Color(0xFFFFB84D)
        Category.AUTO -> "Auto" to Color(0xFFFF6B6B)
        Category.FINANCE -> "Financije" to Color(0xFF95E1D3)
        Category.WORK -> "Posao" to Color(0xFFAA96DA)
        Category.SHOPPING -> "Kupovina" to Color(0xFFFFB84D)
        Category.SMART_HOME -> "Pametni dom" to Color(0xFF00BCD4)
        Category.OTHER -> "Ostalo" to Color(0xFFCCCCCC)
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
        modifier = if (onClick != {}) Modifier.clickable(onClick = onClick) else Modifier
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MoodIndicator(mood: Mood) {
    val (label, color) = when (mood) {
        Mood.VERY_BAD -> "😢 Very Bad" to Color(0xFFE63946)
        Mood.BAD -> "😞 Bad" to Color(0xFFFF6B6B)
        Mood.NEUTRAL -> "😐 Neutral" to Color(0xFFCCCCCC)
        Mood.GOOD -> "😊 Good" to Color(0xFF4ECDC4)
        Mood.VERY_GOOD -> "😄 Very Good" to Color(0xFF2A9D8F)
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    onAddEntry: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Još nema zapisa",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = "Dodaj svoj prvi zapis da počneš!",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Button(onClick = onAddEntry) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dodaj zapis")
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Pretraži zapise...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pretraži") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun FinanceSummaryCard(financeEntries: List<LogEntry>) {
    
    if (financeEntries.isEmpty()) return
    
    val totalAmount = financeEntries.sumOf { it.amount ?: 0.0 }
    val currency = financeEntries.firstOrNull()?.currency ?: "€"
    val entryCount = financeEntries.size
    
    // Group by currency if multiple currencies exist
    val amountsByCurrency = financeEntries
        .groupBy { it.currency ?: "€" }
        .mapValues { (_, entries) -> entries.sumOf { it.amount ?: 0.0 } }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰 Finance Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "$entryCount entries",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
            
            if (amountsByCurrency.size == 1) {
                // Single currency
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Total:",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = String.format("%.2f %s", totalAmount, currency),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                // Multiple currencies
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    amountsByCurrency.forEach { (curr, amount) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total ($curr):",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = String.format("%.2f %s", amount, curr),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryFilterChips(
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        Category.HEALTH, Category.FEEDING, Category.SLEEP, Category.MOOD,
        Category.DEVELOPMENT, Category.HOME, Category.AUTO, Category.FINANCE,
        Category.SCHOOL, Category.WORK, Category.SHOPPING, Category.SMART_HOME, Category.OTHER
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(getCategoryDisplayName(category)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    persons: List<Person>,
    entities: List<Entity>,
    selectedPersonId: String?,
    selectedEntityId: String?,
    selectedCategory: Category?,
    onPersonSelected: (String?) -> Unit,
    onEntitySelected: (String?) -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf(
        Category.HEALTH, Category.FEEDING, Category.SLEEP, Category.MOOD,
        Category.DEVELOPMENT, Category.HOME, Category.AUTO, Category.FINANCE,
        Category.SCHOOL, Category.WORK, Category.SHOPPING, Category.SMART_HOME, Category.OTHER
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Filteri",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Person filter
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Osoba",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(
                        selected = selectedPersonId == null,
                        onClick = { onPersonSelected(null) },
                        label = { Text("Sve") }
                    )
                    persons.forEach { person ->
                        FilterChip(
                            selected = selectedPersonId == person.id,
                            onClick = { onPersonSelected(if (selectedPersonId == person.id) null else person.id) },
                            label = { Text("${person.emoji} ${person.name}") }
                        )
                    }
                }
            }
            
            // Entity filter
            if (entities.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Entitet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        FilterChip(
                            selected = selectedEntityId == null,
                            onClick = { onEntitySelected(null) },
                            label = { Text("Sve") }
                        )
                        entities.forEach { entity ->
                            FilterChip(
                                selected = selectedEntityId == entity.id,
                                onClick = { onEntitySelected(if (selectedEntityId == entity.id) null else entity.id) },
                                label = { Text("${entity.emoji} ${entity.name}") }
                            )
                        }
                    }
                }
            }
            
            // Category filter
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Kategorija",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategorySelected(null) },
                        label = { Text("Sve") }
                    )
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategorySelected(if (selectedCategory == category) null else category) },
                            label = { Text(getCategoryDisplayName(category)) }
                        )
                    }
                }
            }
            
            // Clear all button
            Button(
                onClick = {
                    onPersonSelected(null)
                    onEntitySelected(null)
                    onCategorySelected(null)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Obriši sve filtere")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

