package com.familylogbook.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.FeedingType
import com.familylogbook.app.ui.viewmodel.AddEntryViewModel
import com.familylogbook.app.domain.util.PersonAgeUtils.calculateAgeInYears
import com.familylogbook.app.domain.util.PersonAgeUtils.isBabyAge
import com.familylogbook.app.domain.util.PersonAgeUtils.canHaveFeeding
import com.familylogbook.app.domain.timer.TimerManager
import com.familylogbook.app.ui.component.MedicineQuickForm
import kotlinx.coroutines.launch

@Composable
fun AddEntryScreen(
    viewModel: AddEntryViewModel,
    onNavigateBack: () -> Unit,
    entryId: String? = null, // For edit mode
    initialText: String? = null // For voice input pre-filled text
) {
    val children by viewModel.children.collectAsState()
    val persons by viewModel.persons.collectAsState()
    val entities by viewModel.entities.collectAsState()
    val selectedChildId by viewModel.selectedChildId.collectAsState()
    val selectedPersonId by viewModel.selectedPersonId.collectAsState()
    val selectedEntityId by viewModel.selectedEntityId.collectAsState()
    val entryText by viewModel.entryText.collectAsState()
    val editingEntryId by viewModel.editingEntryId.collectAsState()

    // Set initial text from voice input if provided
    // Automatski spremi ako je shopping lista
    androidx.compose.runtime.LaunchedEffect(initialText) {
        if (initialText != null && initialText.isNotBlank() && entryText.isEmpty()) {
            // Try to auto-save if it's a shopping list
            val saved = viewModel.setEntryTextAndAutoSave(initialText)
            if (saved) {
                // Navigate back if auto-saved successfully
                kotlinx.coroutines.delay(300) // Small delay to show the entry was saved
                // LaunchedEffect automatski cancel-uje ako se screen unmount-uje, 
                // tako da je sigurno pozvati onNavigateBack ovdje
                onNavigateBack()
            }
        }
    }

    var showQuickInputs by remember { mutableStateOf(false) }
    var selectedQuickCategory by remember { mutableStateOf<Category?>(null) }
    var temperatureValue by remember { mutableStateOf("") }
    var amountValue by remember { mutableStateOf("") }
    var currencyValue by remember { mutableStateOf("EUR") }
    
    // Medicine quick form state
    var medicineName by remember { mutableStateOf("") }
    var medicineDosage by remember { mutableStateOf("") }
    var medicineIntervalHours by remember { mutableStateOf(6) }
    val isFeedingActive by viewModel.isFeedingActive.collectAsState()
    val feedingElapsedSeconds by viewModel.feedingElapsedSeconds.collectAsState()
    val selectedFeedingType by viewModel.selectedFeedingType.collectAsState()
    val bottleAmount by viewModel.bottleAmount.collectAsState()
    val selectedSymptoms by viewModel.selectedSymptoms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditMode = editingEntryId != null || entryId != null

    // Load entry for edit when entryId is provided
    LaunchedEffect(entryId) {
        if (entryId != null && editingEntryId == null) {
            viewModel.loadEntryForEdit(entryId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isEditMode) "Uredi zdravstveni zapis" else "Zabilježi zdravstveni događaj",
                        maxLines = 2,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                windowInsets = WindowInsets.statusBars,
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Odustani")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val text = entryText.trim()

                                // Check for timer command BEFORE saving entry
                                val timerCommand = TimerManager.detectTimerCommand(text)
                                if (timerCommand != null) {
                                    // Start timer
                                    val timerId = TimerManager.startTimer(context, timerCommand)
                                    android.util.Log.d(
                                        "AddEntryScreen",
                                        "Timer started: ${timerCommand.durationMinutes} min, ID: $timerId"
                                    )

                                    // Show success message
                                    snackbarHostState.showSnackbar(
                                        message = "⏰ Timer pokrenut: ${timerCommand.durationMinutes} min",
                                        duration = SnackbarDuration.Short
                                    )
                                }

                                val success = viewModel.saveEntry()
                                if (success) {
                                    snackbarHostState.showSnackbar(
                                        message = "✅ Zdravstveni zapis uspješno spremljen!",
                                        duration = SnackbarDuration.Short
                                    )
                                    kotlinx.coroutines.delay(500) // Small delay to show success message
                                    onNavigateBack()
                                } else {
                                    // Error message is already shown in error card
                                    snackbarHostState.showSnackbar(
                                        message = "❌ Greška pri spremanju. Provjeri poruku iznad.",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        },
                        enabled = entryText.trim().isNotEmpty() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Spremi", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val scrollState = rememberScrollState()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .imePadding() // Add padding for keyboard - prevents content from being hidden
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Quick Feeding Tracker - show ONLY if there are CHILD persons who can have feeding (< 2 years)
                val childPersons = persons.filter {
                    it.type == com.familylogbook.app.domain.model.PersonType.CHILD &&
                            it.dateOfBirth != null &&
                            canHaveFeeding(it.dateOfBirth)
                }

                // Only show feeding UI if there are children in the system who can have feeding
                if (childPersons.isNotEmpty()) {
                    if (isFeedingActive) {
                        // Show timer card when feeding is active
                        FeedingTimerCard(
                            elapsedSeconds = feedingElapsedSeconds,
                            feedingType = selectedFeedingType,
                            bottleAmount = bottleAmount,
                            onBottleAmountChange = { viewModel.setBottleAmount(it) },
                            onStop = { viewModel.stopFeeding() },
                            onSave = {
                                scope.launch {
                                    if (viewModel.saveFeedingEntry()) {
                                        onNavigateBack()
                                    }
                                }
                            }
                        )
                    } else {
                        // Show quick feeding buttons when feeding is not active but children exist
                        QuickFeedingButtons(
                            children = childPersons,
                            selectedChildId = selectedPersonId ?: selectedChildId,
                            onSelectChild = { personId ->
                                // Use setSelectedPerson for new Person model
                                viewModel.setSelectedPerson(personId)
                            },
                            onStartFeeding = { type ->
                                if (selectedPersonId != null || selectedChildId != null) {
                                    viewModel.startFeeding(type)
                                }
                            },
                            enabled = selectedPersonId != null || selectedChildId != null
                        )
                    }
                    Divider()
                }
                // If no children exist, don't show any feeding UI at all

                // Person/Entity selection
                Text(
                    text = "O kome/čemu je riječ?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                // Person selection
                if (persons.isNotEmpty()) {
                    Text(
                        text = "Osobe",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Family option
                        item {
                            PersonChip(
                                label = "Obitelj",
                                isSelected = selectedPersonId == null && selectedEntityId == null && selectedChildId == null,
                                onClick = {
                                    android.util.Log.d("AddEntryScreen", "Family clicked - clearing selection")
                                    viewModel.setSelectedPerson(null)
                                    viewModel.setSelectedEntity(null)
                                    viewModel.setSelectedChild(null)
                                },
                                modifier = Modifier.widthIn(min = 100.dp)
                            )
                        }

                        // Persons chips
                        items(persons.size) { index ->
                            val person = persons[index]
                            PersonChip(
                                label = person.name,
                                emoji = person.emoji,
                                color = Color(android.graphics.Color.parseColor(person.avatarColor)),
                                isSelected = selectedPersonId == person.id,
                                onClick = {
                                    android.util.Log.d("AddEntryScreen", "Person clicked: ${person.name}, id: ${person.id}")
                                    viewModel.setSelectedPerson(person.id)
                                    viewModel.setSelectedEntity(null)
                                    viewModel.setSelectedChild(null)
                                    android.util.Log.d("AddEntryScreen", "After click - selectedPersonId should be: ${person.id}")
                                },
                                modifier = Modifier.widthIn(min = 100.dp)
                            )
                        }
                    }
                }

                // Legacy Children (backward compatibility)
                if (children.isNotEmpty() && persons.isEmpty()) {
                    Text(
                        text = "Djeca",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ChildChip(
                            label = "Obitelj",
                            isSelected = selectedChildId == null,
                            onClick = { viewModel.setSelectedChild(null) },
                            modifier = Modifier.weight(1f)
                        )

                        children.forEach { child ->
                            ChildChip(
                                label = child.name,
                                emoji = child.emoji,
                                color = Color(android.graphics.Color.parseColor(child.avatarColor)),
                                isSelected = selectedChildId == child.id,
                                onClick = { viewModel.setSelectedChild(child.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Entity selection
                if (entities.isNotEmpty()) {
                    Text(
                        text = "Entiteti",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        entities.forEach { entity ->
                            EntityChip(
                                label = entity.name,
                                emoji = entity.emoji,
                                color = Color(android.graphics.Color.parseColor(entity.avatarColor)),
                                isSelected = selectedEntityId == entity.id,
                                onClick = {
                                    viewModel.setSelectedEntity(entity.id)
                                    viewModel.setSelectedPerson(null)
                                    viewModel.setSelectedChild(null)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Divider()

                // Baby Preset Block (shown when selected person is a baby ≤12 months)
                val selectedPerson = selectedPersonId?.let { personId ->
                    persons.find { it.id == personId }
                }
                val isBaby = remember(selectedPerson) {
                    selectedPerson?.let { person ->
                        person.type == com.familylogbook.app.domain.model.PersonType.CHILD &&
                                person.dateOfBirth != null &&
                                isBabyAge(person.dateOfBirth) // ≤12 months for baby presets
                    } ?: false
                }

                if (isBaby && selectedPersonId != null) {
                    BabyPresetBlock(
                        personName = selectedPerson?.name ?: "",
                        onPresetSelected = { preset ->
                            viewModel.setEntryText(if (entryText.isNotEmpty()) "$entryText $preset" else preset)
                        }
                    )
                    Divider()
                }

                // Category detection for quick inputs
                val detectedCategory = remember(entryText) {
                    if (entryText.isNotEmpty()) {
                        try {
                            val classifier =
                                com.familylogbook.app.domain.classifier.EntryClassifier()
                            classifier.classifyEntry(entryText).category
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                }

                // Smart Home preset block removed - no longer needed for Parent OS

                // Symptom Helper (shown when category is HEALTH or SYMPTOM) - Parent OS core feature
                if (detectedCategory == Category.HEALTH || detectedCategory == Category.SYMPTOM ||
                    selectedQuickCategory == Category.HEALTH || selectedQuickCategory == Category.SYMPTOM) {
                    SymptomCheckboxSection(
                        selectedSymptoms = selectedSymptoms,
                        onSymptomsChange = { viewModel.setSymptoms(it) }
                    )
                    Divider()
                }

                // Quick inputs toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Brzi unos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = showQuickInputs,
                        onCheckedChange = { showQuickInputs = it }
                    )
                }

                // Quick inputs for different categories
                if (showQuickInputs) {
                    QuickInputsSection(
                        selectedCategory = selectedQuickCategory,
                        onCategorySelected = { selectedQuickCategory = it },
                        temperatureValue = temperatureValue,
                        onTemperatureChange = { temperatureValue = it },
                        amountValue = amountValue,
                        onAmountChange = { amountValue = it },
                        currencyValue = currencyValue,
                        onCurrencyChange = { currencyValue = it },
                        medicineName = medicineName,
                        onMedicineNameChange = { medicineName = it },
                        medicineDosage = medicineDosage,
                        onMedicineDosageChange = { medicineDosage = it },
                        medicineIntervalHours = medicineIntervalHours,
                        onMedicineIntervalChange = { medicineIntervalHours = it },
                        selectedPersonId = selectedPersonId,
                        entryText = entryText,
                        viewModel = viewModel,
                        scope = scope,
                        onNavigateBack = onNavigateBack,
                        onAddToText = { text ->
                            viewModel.setEntryText(entryText + (if (entryText.isNotEmpty()) " " else "") + text)
                        }
                    )
                }

                // Text input
                Text(
                    text = "Što se dogodilo?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                // Text field with focus tracking for auto-scroll
                var textFieldFocused by remember { mutableStateOf(false) }
                
                OutlinedTextField(
                    value = entryText,
                    onValueChange = { viewModel.setEntryText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 300.dp)
                        .onFocusChanged { focusState ->
                            textFieldFocused = focusState.isFocused
                            // Auto-scroll to text field when focused
                            if (focusState.isFocused) {
                                scope.launch {
                                    kotlinx.coroutines.delay(100) // Small delay for keyboard animation
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }
                            }
                        },
                    placeholder = {
                        Text(
                            "Npr. Neo je imao blagu temperaturu večeras. Dao sam mu lijek."
                        )
                    },
                    minLines = 5,
                    maxLines = 15
                )

                Text(
                    text = "Aplikacija će automatski kategorizirati tvoj zdravstveni zapis i dodati relevantne oznake.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Reminder Date Picker (optional)
                ReminderDatePicker(
                    onDateSelected = { dateMillis ->
                        // Date will be extracted from text automatically, but user can also set it manually
                        // For now, we'll rely on automatic extraction from text
                    }
                )

                // Error message
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚠️ Greška",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = error,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { viewModel.clearError() }
                                ) {
                                    Text("U redu")
                                }
                                // Show retry button only for network errors
                                val isNetworkError = error.contains("mrež", ignoreCase = true) || 
                                                     error.contains("internet", ignoreCase = true) ||
                                                     error.contains("veza", ignoreCase = true) ||
                                                     error.contains("timeout", ignoreCase = true)
                                if (isNetworkError) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                viewModel.clearError()

                                                val text = entryText.trim()

                                                // Check for timer command BEFORE saving entry
                                                val timerCommand = TimerManager.detectTimerCommand(text)
                                                if (timerCommand != null) {
                                                    // Start timer
                                                    val timerId =
                                                        TimerManager.startTimer(context, timerCommand)
                                                    android.util.Log.d(
                                                        "AddEntryScreen",
                                                        "Timer started: ${timerCommand.durationMinutes} min, ID: $timerId"
                                                    )
                                                    
                                                    // Show success message
                                                    snackbarHostState.showSnackbar(
                                                        message = "⏰ Timer pokrenut: ${timerCommand.durationMinutes} min",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }

                                                val success = viewModel.saveEntry()
                                                if (success) {
                                                    snackbarHostState.showSnackbar(
                                                        message = "✅ Zdravstveni zapis uspješno spremljen!",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                    kotlinx.coroutines.delay(500)
                                                    onNavigateBack()
                                                } else {
                                                    snackbarHostState.showSnackbar(
                                                        message = "❌ Greška pri spremanju. Provjeri poruku iznad.",
                                                        duration = SnackbarDuration.Long
                                                    )
                                                }
                                            }
                                        },
                                        enabled = !isLoading && entryText.trim().isNotEmpty()
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text("Pokušaj ponovo")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Snackbar host
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        
        // Vaccination selection dialog (overlay)
        val showVaccinationDialog by viewModel.showVaccinationDialog.collectAsState()
        val selectedPersonIdForVaccination = selectedPersonId ?: selectedChildId
        val selectedPersonForVaccination = selectedPersonIdForVaccination?.let { pid ->
            persons.find { it.id == pid }
        }
        val selectedChildForVaccination = selectedPersonIdForVaccination?.let { pid ->
            children.find { it.id == pid }
        }
        var personForVaccination = selectedPersonForVaccination ?: selectedChildForVaccination
        var dateOfBirth = selectedPersonForVaccination?.dateOfBirth ?: selectedChildForVaccination?.dateOfBirth
        
        // If no person selected, get all children
        val allChildPersons = if (personForVaccination == null) {
            persons.filter { 
                it.type == com.familylogbook.app.domain.model.PersonType.CHILD && 
                it.dateOfBirth != null 
            } + children.filter { it.dateOfBirth != null }
        } else {
            emptyList()
        }
        
        if (showVaccinationDialog) {
            // If we have a selected person with date of birth, use that
            // Otherwise, show dialog to select child first
            if (personForVaccination == null && allChildPersons.isNotEmpty()) {
                // Show child selection dialog first
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = {
                        viewModel.setShowVaccinationDialog(false)
                        viewModel.setPendingEntryText(null)
                    },
                    title = { Text("Odaberi dijete") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Odaberi dijete za koje se cjepivo odnosi:",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            allChildPersons.forEach { child ->
                                val childPerson = child as? Person
                                val childLegacy = child as? Child
                                val childName = childPerson?.name ?: childLegacy?.name ?: ""
                                val childDob = childPerson?.dateOfBirth ?: childLegacy?.dateOfBirth
                                
                                OutlinedButton(
                                    onClick = {
                                        // Set selected person and show vaccination dialog
                                        if (childPerson != null) {
                                            viewModel.setSelectedPerson(childPerson.id)
                                        } else if (childLegacy != null) {
                                            viewModel.setSelectedChild(childLegacy.id)
                                        }
                                        personForVaccination = child
                                        dateOfBirth = childDob
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(childName, fontSize = 14.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.setShowVaccinationDialog(false)
                                viewModel.setPendingEntryText(null)
                            }
                        ) {
                            Text("Odustani")
                        }
                    }
                )
            } else if (personForVaccination != null && dateOfBirth != null) {
                // Show vaccination selection dialog
                val dob = dateOfBirth // Local copy to avoid smart cast issue
                val possibleVaccinations = if (dob != null) {
                    com.familylogbook.app.domain.vaccination.VaccinationCalendar
                        .getPossibleVaccinationsForAge(dob)
                } else {
                    emptyList()
                }
            
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {
                    viewModel.setShowVaccinationDialog(false)
                    viewModel.setPendingEntryText(null)
                },
                title = { Text("Odaberi cjepivo") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Nije prepoznato koje cjepivo je ${(personForVaccination as? Person)?.name ?: (personForVaccination as? Child)?.name ?: ""} primio. Odaberi cjepivo:",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (possibleVaccinations.isEmpty()) {
                            Text(
                                "Nema dostupnih cjepiva za dob ${(personForVaccination as? Person)?.name ?: (personForVaccination as? Child)?.name ?: ""}.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        } else {
                            possibleVaccinations.forEach { vaccination ->
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            if (viewModel.saveEntryWithVaccination(vaccination.shortName)) {
                                                onNavigateBack()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = vaccination.shortName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = vaccination.description,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.setShowVaccinationDialog(false)
                            viewModel.setPendingEntryText(null)
                        }
                    ) {
                        Text("Odustani")
                    }
                }
            )
            }
        }
    }
}

@Composable
fun ReminderDatePicker(
        onDateSelected: (Long?) -> Unit
    ) {
        // Simplified version - just show info that date can be written in text
        // Material3 DatePicker requires additional dependencies and is complex
        // For now, we'll rely on automatic date extraction from text
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💡 Datum podsjetnika",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Upiši datum u tekst zdravstvenog zapisa (npr. 'servis 15.12.2024', 'sutra', 'za 3 dana'). Aplikacija će automatski prepoznati datum i postaviti podsjetnik.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }

@Composable
fun PersonChip(
        label: String,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        emoji: String? = null,
        color: Color = MaterialTheme.colorScheme.secondary
    ) {
        FilterChip(
            selected = isSelected,
            onClick = {
                android.util.Log.d("PersonChip", "FilterChip clicked for: $label, isSelected: $isSelected")
                try {
                    onClick()
                    android.util.Log.d("PersonChip", "onClick callback executed successfully")
                } catch (e: Exception) {
                    android.util.Log.e("PersonChip", "Error in onClick callback: ${e.message}", e)
                }
            },
            label = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    emoji?.let {
                        Text(
                            text = it,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = label,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            },
            modifier = modifier.height(48.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = color.copy(alpha = 0.3f),
                selectedLabelColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }

@Composable
fun EntityChip(
        label: String,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        emoji: String? = null,
        color: Color = MaterialTheme.colorScheme.secondary
    ) {
        Surface(
            modifier = modifier
                .height(48.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) {
                color.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            border = if (isSelected) {
                BorderStroke(2.dp, color)
            } else {
                null
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                emoji?.let {
                    Text(
                        text = it,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }

@Composable
fun ChildChip(
        label: String,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        emoji: String? = null,
        color: Color = MaterialTheme.colorScheme.secondary
    ) {
        Surface(
            modifier = modifier
                .height(48.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) {
                color.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            border = if (isSelected) {
                BorderStroke(2.dp, color)
            } else {
                null
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                emoji?.let {
                    Text(
                        text = it,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }

@Composable
fun QuickInputsSection(
        selectedCategory: Category?,
        onCategorySelected: (Category) -> Unit,
        temperatureValue: String,
        onTemperatureChange: (String) -> Unit,
        amountValue: String,
        onAmountChange: (String) -> Unit,
        currencyValue: String,
        onCurrencyChange: (String) -> Unit,
        medicineName: String,
        onMedicineNameChange: (String) -> Unit,
        medicineDosage: String,
        onMedicineDosageChange: (String) -> Unit,
        medicineIntervalHours: Int,
        onMedicineIntervalChange: (Int) -> Unit,
        selectedPersonId: String?,
        entryText: String,
        viewModel: AddEntryViewModel,
        scope: kotlinx.coroutines.CoroutineScope,
        onNavigateBack: () -> Unit,
        onAddToText: (String) -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Brzi unos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Category selection
                Text(
                    text = "Kategorija",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        Category.HEALTH,
                        Category.MEDICINE,
                        Category.SYMPTOM,
                        Category.VACCINATION,
                        Category.FEEDING,
                        Category.SLEEP,
                        Category.DAY,
                        Category.SCHOOL
                    ).forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                            label = { 
                                Text(
                                    when (category) {
                                        Category.HEALTH -> "Zdravlje"
                                        Category.SLEEP -> "Spavanje"
                                        Category.MOOD -> "Raspoloženje"
                                        Category.DEVELOPMENT -> "Razvoj"
                                        Category.KINDERGARTEN_SCHOOL -> "Škola"
                                        Category.SCHOOL -> "Škola"
                                        Category.HOME -> "Dom"
                                        Category.MEDICINE -> "Lijekovi"
                                        Category.SYMPTOM -> "Simptomi"
                                        Category.VACCINATION -> "Cjepiva"
                                        Category.FEEDING -> "Hranjenje"
                                        Category.DAY -> "Dan"
                                        Category.AUTO -> "Auto"
                                        Category.HOUSE -> "Kuća"
                                        Category.FINANCE -> "Financije"
                                        Category.WORK -> "Posao"
                                        Category.SHOPPING -> "Kupovina"
                                        Category.SMART_HOME -> "Pametni dom"
                                        Category.OTHER -> "Ostalo"
                                    },
                                    fontSize = 12.sp
                                ) 
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Medicine quick form (for MEDICINE category) - Parent OS core feature
                if (selectedCategory == Category.MEDICINE) {
                    MedicineQuickForm(
                        medicineName = medicineName,
                        onMedicineNameChange = onMedicineNameChange,
                        medicineDosage = medicineDosage,
                        onMedicineDosageChange = onMedicineDosageChange,
                        medicineIntervalHours = medicineIntervalHours,
                        onMedicineIntervalChange = onMedicineIntervalChange,
                        onAddMedicine = {
                            if (medicineName.isNotEmpty() && selectedPersonId != null) {
                                // Save medicine entry using ViewModel
                                scope.launch {
                                    val saved = viewModel.saveMedicineEntry(
                                        medicineName = medicineName,
                                        dosage = medicineDosage.ifEmpty { "1 doza" },
                                        intervalHours = medicineIntervalHours,
                                        notes = entryText.ifEmpty { null }
                                    )
                                    if (saved) {
                                        // Clear form
                                        onMedicineNameChange("")
                                        onMedicineDosageChange("")
                                        onMedicineIntervalChange(6)
                                        onNavigateBack()
                                    }
                                }
                            }
                        }
                    )
                    Divider()
                }
                
                // Temperature input (for HEALTH or SYMPTOM) - Parent OS core feature
                if (selectedCategory == Category.HEALTH || selectedCategory == Category.SYMPTOM) {
                    OutlinedTextField(
                        value = temperatureValue,
                        onValueChange = onTemperatureChange,
                        label = { Text("Temperatura (°C)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        trailingIcon = {
                            TextButton(
                                onClick = {
                                    if (temperatureValue.isNotEmpty()) {
                                        onAddToText("Temperatura $temperatureValue°C")
                                        onTemperatureChange("")
                                    }
                                }
                            ) {
                                Text("Dodaj")
                            }
                        }
                    )
                }

                // Amount input (for FINANCE)
                if (selectedCategory == Category.FINANCE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = amountValue,
                            onValueChange = onAmountChange,
                            label = { Text("Iznos") },
                            modifier = Modifier.weight(2f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            )
                        )
                        OutlinedTextField(
                            value = currencyValue,
                            onValueChange = onCurrencyChange,
                            label = { Text("Valuta") },
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                if (amountValue.isNotEmpty()) {
                                    onAddToText("$amountValue $currencyValue")
                                    onAmountChange("")
                                }
                            },
                            modifier = Modifier.align(Alignment.Bottom)
                        ) {
                            Text("Dodaj")
                        }
                    }
                }

                // Quick phrases based on category
                Text(
                    text = "Brze fraze",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (selectedCategory) {
                        Category.AUTO -> {
                            listOf(
                                "Probušena guma",
                                "Servis auta",
                                "Registracija",
                                "Osiguranje"
                            ).forEach { phrase ->
                                SuggestionChip(
                                    onClick = { onAddToText(phrase) },
                                    label = { Text(phrase, fontSize = 12.sp) }
                                )
                            }
                        }

                        Category.HOUSE -> {
                            listOf(
                                "Pokvario se",
                                "Popravak",
                                "Filter",
                                "Čišćenje"
                            ).forEach { phrase ->
                                SuggestionChip(
                                    onClick = { onAddToText(phrase) },
                                    label = { Text(phrase, fontSize = 12.sp) }
                                )
                            }
                        }

                        Category.FINANCE -> {
                            listOf("Račun", "Plaćanje", "Struja", "Voda").forEach { phrase ->
                                SuggestionChip(
                                    onClick = { onAddToText(phrase) },
                                    label = { Text(phrase, fontSize = 12.sp) }
                                )
                            }
                        }

                        Category.WORK -> {
                            listOf("Sastanak", "Rok", "Projekt", "Klijent").forEach { phrase ->
                                SuggestionChip(
                                    onClick = { onAddToText(phrase) },
                                    label = { Text(phrase, fontSize = 12.sp) }
                                )
                            }
                        }

                        Category.MEDICINE -> {
                            listOf(
                                "Nurofen",
                                "Paracetamol",
                                "Lijek",
                                "Doza"
                            ).forEach { phrase ->
                                SuggestionChip(
                                    onClick = { onAddToText(phrase) },
                                    label = { Text(phrase, fontSize = 12.sp) }
                                )
                            }
                        }

                        Category.SYMPTOM -> {
                            listOf(
                                "Temperatura",
                                "Kašalj",
                                "Povraćanje",
                                "Simptomi"
                            ).forEach { phrase ->
                                SuggestionChip(
                                    onClick = { onAddToText(phrase) },
                                    label = { Text(phrase, fontSize = 12.sp) }
                                )
                            }
                        }

                        Category.VACCINATION -> {
                            listOf(
                                "Cjepivo",
                                "Vakcina",
                                "Cijepljenje"
                            ).forEach { phrase ->
                                SuggestionChip(
                                    onClick = { onAddToText(phrase) },
                                    label = { Text(phrase, fontSize = 12.sp) }
                                )
                            }
                        }

                        Category.DAY -> {
                            listOf(
                                "Checklist",
                                "Rutina",
                                "Podsjetnik",
                                "Dnevna obaveza"
                            ).forEach { phrase ->
                                SuggestionChip(
                                    onClick = { onAddToText(phrase) },
                                    label = { Text(phrase, fontSize = 12.sp) }
                                )
                            }
                        }

                        else -> {
                            // Default phrases
                            listOf("Danas", "Sada", "Važno").forEach { phrase ->
                                SuggestionChip(
                                    onClick = { onAddToText(phrase) },
                                    label = { Text(phrase, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
fun BabyPresetBlock(
        personName: String,
        onPresetSelected: (String) -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "👶 Brzi unos za bebu ($personName)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Klikni na gumb za brzi unos",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Preset buttons in a grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { onPresetSelected("Dojenje") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🍼 Dojenje", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onPresetSelected("Bočica") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🍼 Bočica", fontSize = 12.sp)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { onPresetSelected("Pelena") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💧 Pelena", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onPresetSelected("Grčevi") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("😢 Grčevi", fontSize = 12.sp)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { onPresetSelected("Spavanje") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("😴 Spavanje", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

// SmartHomePresetBlock and SmartHomePresetButton removed - no longer needed for Parent OS
