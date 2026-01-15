package com.familylogbook.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// SmartHomeManager import removed - no longer needed for Parent OS
import com.familylogbook.app.data.speech.SpeechRecognizerHelper
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Settings
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Mood
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.ui.component.AdviceCard
import com.familylogbook.app.ui.component.TodayOverviewCard
import com.familylogbook.app.ui.component.ImportantCardsGrid
import com.familylogbook.app.ui.component.RecentEntriesList
import com.familylogbook.app.ui.component.AdvicePill
import com.familylogbook.app.ui.component.MedicineTrackerCard
import com.familylogbook.app.ui.component.SymptomTrackerCard
import com.familylogbook.app.ui.component.VaccinationCalendarCard
import com.familylogbook.app.ui.navigation.Screen
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import com.familylogbook.app.domain.timer.TimerManager
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
        // Parent OS Core Categories
        Category.MEDICINE -> "Lijekovi"
        Category.SYMPTOM -> "Simptomi"
        Category.VACCINATION -> "Cjepiva"
        Category.DAY -> "Dnevne obaveze"
        
        // Parent OS Health & Wellness
        Category.HEALTH -> "Zdravlje"
        Category.FEEDING -> "Hranjenje"
        Category.SLEEP -> "Spavanje"
        Category.MOOD -> "Raspoloženje"
        Category.DEVELOPMENT -> "Razvoj"
        Category.SCHOOL -> "Škola"
        
        // Legacy categories (kept for backward compatibility, will be removed from UI)
        Category.KINDERGARTEN_SCHOOL -> "Škola"
        Category.HOME -> "Dom"
        Category.HOUSE -> "Kuća"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAddEntry: () -> Unit,
    onNavigateToAddEntryWithText: (String) -> Unit = { onNavigateToAddEntry() },
    onNavigateToEditEntry: (String) -> Unit = {},
    onNavigateToEntryDetail: (String) -> Unit = {},
    onNavigateToPersonProfile: (String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onNavigateToEntityProfile: (String) -> Unit = {},
    onNavigateToCategoryDetail: (Category) -> Unit = {},
    onNavigateToAdvice: () -> Unit = {},
    onNavigateToAdviceDetail: (com.familylogbook.app.domain.model.AdviceTemplate) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val persons by viewModel.persons.collectAsState()
    val entities by viewModel.entities.collectAsState()
    // Shopping deals cache removed - no longer needed for Parent OS
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Speech recognition state
    var isListening by remember { mutableStateOf(false) }
    var speechHelper by remember { mutableStateOf<SpeechRecognizerHelper?>(null) }
    var showSpeechError by remember { mutableStateOf<String?>(null) }
    
    // Initialize speech helper
    LaunchedEffect(Unit) {
        speechHelper = SpeechRecognizerHelper(context)
    }
    
    // Permission launcher for microphone
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Start listening if permission granted
            speechHelper?.startListening(
                onResult = { text ->
                    isListening = false
                    if (text != null && text.isNotBlank()) {
                        // Parent OS: keep voice input as-is (no shopping list formatting)
                        onNavigateToAddEntryWithText(text)
                    }
                },
                onError = { error ->
                    isListening = false
                    showSpeechError = error
                }
            )
            isListening = true
        } else {
            showSpeechError = "Potrebna je dozvola za mikrofon da bi glasovni unos radio."
        }
    }
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedPersonId by viewModel.selectedPersonId.collectAsState()
    val selectedEntityId by viewModel.selectedEntityId.collectAsState()
    
    // SmartHomeManager removed - no longer needed for Parent OS
    var showFilterSheet by remember { mutableStateOf(false) }
    
    // LazyListState for scroll control
    val lazyListState = rememberLazyListState()
    
    // Reset scroll position to top when filters change (e.g., when navigating from Stats)
    // This ensures that when user clicks on a category in Stats, they see filtered results from the top
    LaunchedEffect(selectedCategory, selectedPersonId, selectedEntityId) {
        scope.launch {
            lazyListState.animateScrollToItem(0)
        }
    }
    
    // Count active filters (currently not used, but kept for future use)
    @Suppress("UNUSED_VARIABLE")
    val activeFilterCount = remember(selectedPersonId, selectedEntityId, selectedCategory) {
        var count = 0
        if (selectedPersonId != null) count++
        if (selectedEntityId != null) count++
        if (selectedCategory != null) count++
        count
    }
    
    // Get all entries (not filtered) for overview
    val allEntries by viewModel.entries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Pull-to-refresh state
    val pullToRefreshState = rememberPullToRefreshState()
    
    // Active timers
    val activeTimers by TimerManager.activeTimers.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zdravlje") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Postavke")
                    }
                }
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Voice input button
                FloatingActionButton(
                    onClick = {
                        val helper = speechHelper
                        if (showSpeechError != null) {
                            showSpeechError = null
                            isListening = false
                            helper?.stopListening()
                            return@FloatingActionButton
                        }
                        
                        if (helper != null && !isListening) {
                            if (helper.hasPermission()) {
                                isListening = true
                                helper.startListening(
                                    onResult = { text ->
                                        isListening = false
                                        if (text != null && text.isNotBlank()) {
                                            val formattedText = com.familylogbook.app.domain.classifier.ShoppingListFormatter.processVoiceInput(text)
                                            onNavigateToAddEntryWithText(formattedText)
                                        }
                                    },
                                    onError = { error ->
                                        isListening = false
                                        showSpeechError = error
                                    }
                                )
                            } else {
                                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        } else if (isListening) {
                            helper?.stopListening()
                            isListening = false
                        }
                    },
                    containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        if (isListening) Icons.Default.Stop else Icons.Default.RecordVoiceOver,
                        contentDescription = if (isListening) "Zaustavi snimanje" else "Glasovni unos"
                    )
                }
                
                // Add entry button
                FloatingActionButton(
                    onClick = onNavigateToAddEntry,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zabilježi zdravstveni događaj"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (allEntries.isEmpty() && !isLoading) {
                com.familylogbook.app.ui.component.HealthEmptyState(
                    modifier = Modifier.align(Alignment.Center),
                    onAddEntry = onNavigateToAddEntry
                )
            } else {
                if (isLoading && allEntries.isEmpty()) {
                    // Show loading indicator on initial load
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        com.familylogbook.app.ui.component.LoadingIndicator(
                            message = "Učitavanje zdravstvenih zapisa..."
                        )
                    }
                } else {
                    PullToRefreshBox(
                        isRefreshing = isLoading,
                        onRefresh = {
                            viewModel.refreshAll()
                        },
                        state = pullToRefreshState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 100.dp // Extra padding for FAB buttons at bottom
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            state = lazyListState
                        ) {
                // 0. Active Timers Card (if any)
                if (activeTimers.isNotEmpty()) {
                    items(activeTimers) { timer ->
                        ActiveTimerCard(
                            timer = timer,
                            onCancel = { TimerManager.cancelTimer(context, timer.id) }
                        )
                    }
                }
                
                // 1. Today Overview Card
                item {
                    val today = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }
                    val todayStart = today.timeInMillis
                    val todayEnd = todayStart + (24 * 60 * 60 * 1000L)
                    
                    val todayEntries = allEntries
                        .filter { it.timestamp >= todayStart && it.timestamp < todayEnd }
                        .sortedByDescending { it.timestamp }
                    
                    TodayOverviewCard(
                        entries = allEntries,
                        persons = persons,
                        onClick = {
                            // If only one entry today, open it directly
                            // Otherwise, scroll to today's entries
                            if (todayEntries.size == 1) {
                                onNavigateToEntryDetail(todayEntries.first().id)
                            } else if (todayEntries.isNotEmpty()) {
                                // Scroll to first today entry
                                scope.launch {
                                    val firstTodayEntry = todayEntries.first()
                                    val allSortedEntries = allEntries.sortedByDescending { it.timestamp }
                                    val index = allSortedEntries.indexOfFirst { it.id == firstTodayEntry.id }
                                    if (index >= 0) {
                                        lazyListState.animateScrollToItem(index + 2) // +2 for Today card and ImportantCardsGrid
                                    }
                                }
                            }
                        }
                    )
                }
                
                // 2. Important Cards Grid (2x2) - only shows relevant cards
                item {
                    ImportantCardsGrid(
                        persons = persons,
                        entries = allEntries,
                        onChildClick = {
                            // Navigate to first child profile
                            val firstChild = persons.firstOrNull { it.type == com.familylogbook.app.domain.model.PersonType.CHILD }
                            firstChild?.let { onNavigateToPersonProfile(it.id) }
                        },
                        onHealthClick = {
                            // Already on Health tab; show category detail for quick focus
                            onNavigateToCategoryDetail(Category.HEALTH)
                        },
                        onDayClick = {
                            onNavigateToCategoryDetail(Category.DAY)
                        },
                        onAdviceClick = {
                            // Scroll to advice pills section (or show all advice)
                            onNavigateToAdvice()
                        },
                        // Smart Home click handler removed - no longer needed for Parent OS
                    )
                }
                
                // 2.5. Medicine Tracker Card (Parent OS core feature)
                item {
                    val medicineEntries = allEntries.filter { 
                        it.category == Category.MEDICINE || 
                        (it.category == Category.HEALTH && it.medicineGiven != null)
                    }
                    
                    MedicineTrackerCard(
                        medicineEntries = medicineEntries,
                        onAddMedicine = {
                            // Navigate to AddEntry with MEDICINE category pre-selected
                            onNavigateToAddEntry()
                        },
                        onMedicineClick = { entry ->
                            onNavigateToEntryDetail(entry.id)
                        }
                    )
                }
                
                // 2.6. Symptom Tracker Card (Parent OS core feature)
                item {
                    val symptomEntries = allEntries.filter { 
                        it.category == Category.SYMPTOM || 
                        (it.category == Category.HEALTH && (it.temperature != null || !it.symptoms.isNullOrEmpty()))
                    }
                    
                    SymptomTrackerCard(
                        symptomEntries = symptomEntries,
                        onAddSymptom = {
                            // Navigate to AddEntry with SYMPTOM category pre-selected
                            onNavigateToAddEntry()
                        },
                        onSymptomClick = { entry ->
                            onNavigateToEntryDetail(entry.id)
                        }
                    )
                }
                
                // 2.7. Vaccination Calendar Card (Parent OS core feature)
                item {
                    VaccinationCalendarCard(
                        viewModel = viewModel,
                        onAddVaccination = {
                            // Navigate to AddEntry with VACCINATION category pre-selected
                            onNavigateToAddEntry()
                        },
                        onVaccinationClick = { entry ->
                            onNavigateToEntryDetail(entry.id)
                        }
                    )
                }
                
                // 3. Advice Pills (if any) - LIMITED to max 2 most relevant
                // Collect advice for recent entries
                val recentEntriesForAdvice by derivedStateOf {
                    allEntries
                        .sortedByDescending { it.timestamp }
                        .take(10) // Check more entries to find best advice
                        .filter { it.category != Category.OTHER }
                        .take(2) // Limit to max 2 regular advice pills
                }
                
                // Regular advice pills - get advice in Composable context
                items(
                    items = recentEntriesForAdvice,
                    key = { it.id }
                ) { entry ->
                    // Get advice in Composable context, not in remember
                    val dismissedAdviceIds by viewModel.dismissedAdviceIds.collectAsState()
                    val advice = viewModel.getAdviceForEntry(entry)
                    advice?.let {
                        // Skip if dismissed
                        if (it.id in dismissedAdviceIds) {
                            return@let
                        }
                        // IMPORTANT: Only show work_reminder advice if entry is actually WORK category
                        // This prevents showing work advice for entries that were incorrectly classified
                        if (it.id == "work_reminder" && entry.category != Category.WORK) {
                            // Skip this advice - entry is not WORK category
                            return@let
                        }
                        var showDeleteDialog by remember { mutableStateOf(false) }
                        AdvicePill(
                            advice = it,
                            onClick = {
                                onNavigateToAdviceDetail(it)
                            },
                            onLongClick = {
                                showDeleteDialog = true
                            }
                        )
                        if (showDeleteDialog) {
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Obriši savjet?") },
                                text = { Text("Želiš li obrisati ovaj savjet?") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            // Hide this advice by storing dismissed advice IDs
                                            viewModel.dismissAdvice(it.id)
                                            showDeleteDialog = false
                                        }
                                    ) {
                                        Text("Obriši")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Odustani")
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Shopping deals advice removed - no longer needed for Parent OS
                
                // 4. Recent Entries List
                val sortedEntries by derivedStateOf {
                    allEntries.sortedByDescending { it.timestamp }
                }
                
                item {
                    RecentEntriesList(
                        entries = sortedEntries,
                        onEntryClick = { entry ->
                            onNavigateToEntryDetail(entry.id)
                        },
                        maxItems = 10
                    )
                }
                        }
                    }
                }
            }
            
            // Speech error dialog
            showSpeechError?.let { error ->
                AlertDialog(
                onDismissRequest = { showSpeechError = null },
                title = { Text("Glasovni unos") },
                text = { 
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(error)
                        if (error.contains("klijenta", ignoreCase = true)) {
                            Text(
                                text = "\nSavjet: Pokušaj zatvoriti i ponovno otvoriti aplikaciju, ili provjeri da li Google Voice/Speech servis radi.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            showSpeechError = null
                            isListening = false
                            speechHelper?.stopListening()
                        }
                    ) {
                        Text("U redu")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showSpeechError = null
                            isListening = false
                            speechHelper?.stopListening()
                        }
                    ) {
                        Text("Odustani")
                    }
                }
                )
            }
            
            // Cleanup on dispose
            DisposableEffect(Unit) {
                onDispose {
                    speechHelper?.stopListening()
                }
            }
            
            // Filter Bottom Sheet
            if (showFilterSheet) {
                FilterBottomSheet(
                persons = persons,
                entities = entities,
                selectedPersonId = selectedPersonId,
                selectedEntityId = selectedEntityId,
                selectedCategory = selectedCategory,
                onPersonSelected = { personId -> viewModel.setSelectedPerson(personId) },
                onEntitySelected = { entityId -> viewModel.setSelectedEntity(entityId) },
                onCategorySelected = { category -> viewModel.setSelectedCategory(category) },
                onDismiss = { showFilterSheet = false }
                )
            }
        }
    }
}

@Composable
fun LogEntryCard(
    entry: LogEntry,
    person: Person?,
    entity: Entity?,
    viewModel: HomeViewModel,
    // smartHomeManager parameter removed - no longer needed for Parent OS
    onPersonClick: (String) -> Unit = {},
    onEntityClick: (String) -> Unit = {},
    onCategoryClick: (Category) -> Unit = {},
    onEditClick: (String) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    @Suppress("UNUSED_VARIABLE")
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
            Card(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { /* Regular tap - do nothing or show details */ },
                        onLongPress = { showMenu = true }
                    )
                },
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
                
                // Only show medicine info for HEALTH category
                if (entry.category == Category.HEALTH) {
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
                }
                
                // Only show feeding info for FEEDING category
                if (entry.category == Category.FEEDING) {
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
                }
                
                // Shopping list removed - no longer needed for Parent OS
                
                // Vaccination info
                entry.vaccinationName?.let { vaccinationName ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "💉 Cjepivo: $vaccinationName",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            entry.nextVaccinationMessage?.let { message ->
                                Text(
                                    text = message,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                // Smart Home action button removed - no longer needed for Parent OS
            }
        }
        
        // Context Menu (DropdownMenu) - triggered by long press
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
        
        // Smart Home Options Dialog removed - no longer needed for Parent OS
        
        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Obriši zdravstveni zapis") },
                text = { Text("Želiš li sigurno obrisati ovaj zdravstveni zapis? Ova akcija se ne može poništiti.") },
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
        
        // Advice Card - show for relevant categories
        // Shopping deals advice removed - no longer needed for Parent OS
        when (entry.category) {
            Category.OTHER -> {
                // Don't show advice for OTHER category
            }
            else -> {
                // Show regular advice for other categories
                val advice = remember(entry.id) {
                    viewModel.getAdviceForEntry(entry)
                }
                advice?.let { adv ->
                    // Additional check: Don't show feeding/health advice for non-relevant categories
                    val isRelevantAdvice = when (adv.id) {
                        "feeding" -> entry.category == Category.FEEDING
                        "fever", "colic", "crying" -> entry.category == Category.HEALTH
                        "sleep_trouble", "soothing" -> entry.category == Category.SLEEP || entry.category == Category.MOOD
                        else -> true // Allow other advice types for their categories
                    }
                    
                    if (isRelevantAdvice) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AdviceCard(
                            advice = adv,
                            category = entry.category
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: Category, 
    onClick: () -> Unit = {}
) {
    val (label, color) = when (category) {
        // Parent OS Core Categories
        Category.MEDICINE -> "Lijekovi" to Color(0xFFE63946)
        Category.SYMPTOM -> "Simptomi" to Color(0xFFFF6B6B)
        Category.VACCINATION -> "Cjepiva" to Color(0xFF4ECDC4)
        Category.DAY -> "Dnevne obaveze" to Color(0xFF6C5CE7)
        
        // Parent OS Health & Wellness
        Category.HEALTH -> "Zdravlje" to Color(0xFFFF6B6B)
        Category.FEEDING -> "Hranjenje" to Color(0xFFFFB84D)
        Category.SLEEP -> "Spavanje" to Color(0xFF4ECDC4)
        Category.MOOD -> "Raspoloženje" to Color(0xFFFFD93D)
        Category.DEVELOPMENT -> "Razvoj" to Color(0xFF95E1D3)
        Category.SCHOOL -> "Škola" to Color(0xFFAA96DA)
        
        // Legacy categories (kept for backward compatibility, will be removed from UI)
        Category.KINDERGARTEN_SCHOOL -> "Škola" to Color(0xFFAA96DA)
        Category.HOME -> "Dom" to Color(0xFFF38181)
        Category.HOUSE -> "Kuća" to Color(0xFFF38181)
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
fun MoodIndicator(
    mood: Mood
) {
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

// Legacy EmptyState - replaced with standardized component
// Keeping for backward compatibility but using new component
@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    onAddEntry: () -> Unit
) {
    com.familylogbook.app.ui.component.HealthEmptyState(
        modifier = modifier,
        onAddEntry = onAddEntry
    )
}

// Legacy function - keeping for backward compatibility
@Composable
fun LegacyEmptyState(
    modifier: Modifier = Modifier,
    onAddEntry: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Large emoji/icon
        Text(
            text = "📝",
            fontSize = 64.sp
        )
        
        Text(
            text = "Još nema zdravstvenih zapisa",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Zabilježi prvi lijek ili simptom da počneš praćenje zdravlja djece!",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Button(
            onClick = onAddEntry,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Zabilježi zdravstveni događaj")
        }
        
        // Quick tips
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💡 Savjeti:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• Koristi glasovni unos za brže dodavanje\n• Dodaj osobe u postavkama\n• Kategorije se automatski detektiraju",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
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
        // Parent OS categories only (hide legacy categories from UX)
        Category.MEDICINE,
        Category.SYMPTOM,
        Category.VACCINATION,
        Category.HEALTH,
        Category.FEEDING,
        Category.SLEEP,
        Category.MOOD,
        Category.DEVELOPMENT,
        Category.SCHOOL,
        Category.OTHER
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
        // Parent OS categories only (hide legacy categories from UX)
        Category.MEDICINE,
        Category.SYMPTOM,
        Category.VACCINATION,
        Category.HEALTH,
        Category.FEEDING,
        Category.SLEEP,
        Category.MOOD,
        Category.DEVELOPMENT,
        Category.SCHOOL,
        Category.OTHER
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

@Composable
fun ActiveTimerCard(
    timer: com.familylogbook.app.domain.timer.TimerManager.TimerInfo,
    onCancel: () -> Unit
) {
    val now = System.currentTimeMillis()
    val remainingMillis = timer.endTime - now
    val remainingMinutes = (remainingMillis / (60 * 1000)).toInt().coerceAtLeast(0)
    val remainingSeconds = ((remainingMillis % (60 * 1000)) / 1000).toInt().coerceAtLeast(0)
    
    // Auto-remove timer if expired
    LaunchedEffect(timer.id) {
        if (remainingMillis <= 0) {
            onCancel()
        }
    }
    
    // Update every second
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(timer.id) {
        while (currentTime < timer.endTime) {
            kotlinx.coroutines.delay(1000)
            currentTime = System.currentTimeMillis()
            if (currentTime >= timer.endTime) {
                onCancel()
                break
            }
        }
    }
    
    val displayMinutes = (timer.endTime - currentTime).let { 
        (it / (60 * 1000)).toInt().coerceAtLeast(0) 
    }
    val displaySeconds = (timer.endTime - currentTime).let { 
        ((it % (60 * 1000)) / 1000).toInt().coerceAtLeast(0) 
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
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
                Text(
                    text = "⏰ Timer aktivan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = String.format("%02d:%02d", displayMinutes, displaySeconds),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                timer.description?.let { desc ->
                    Text(
                        text = desc,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = "Zaustavi timer",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Upravo sada"
        diff < 3600_000 -> "Prije ${diff / 60_000} min"
        diff < 86400_000 -> "Prije ${diff / 3600_000} h"
        diff < 604800_000 -> "Prije ${diff / 86400_000} d"
        else -> {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

