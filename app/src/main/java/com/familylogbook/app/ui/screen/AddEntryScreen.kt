package com.familylogbook.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
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
                title = { Text(if (isEditMode) "Uredi zapis" else "Dodaj zapis") },
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

                                if (viewModel.saveEntry()) {
                                    onNavigateBack()
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Family option
                        PersonChip(
                            label = "Obitelj",
                            isSelected = selectedPersonId == null && selectedEntityId == null && selectedChildId == null,
                            onClick = {
                                viewModel.setSelectedPerson(null)
                                viewModel.setSelectedEntity(null)
                                viewModel.setSelectedChild(null)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Persons chips
                        persons.forEach { person ->
                            PersonChip(
                                label = person.name,
                                emoji = person.emoji,
                                color = Color(android.graphics.Color.parseColor(person.avatarColor)),
                                isSelected = selectedPersonId == person.id,
                                onClick = {
                                    viewModel.setSelectedPerson(person.id)
                                    viewModel.setSelectedEntity(null)
                                    viewModel.setSelectedChild(null)
                                },
                                modifier = Modifier.weight(1f)
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

                // Smart Home Preset Block (shown when entry text suggests smart home or category is SMART_HOME)
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

                if (detectedCategory == Category.SMART_HOME || selectedQuickCategory == Category.SMART_HOME) {
                    SmartHomePresetBlock(
                        onCommandSelected = { command ->
                            // Ako korisnik već počeo pisati (npr. "upali"), a predložak već sadrži taj tekst (npr. "upali klimu"),
                            // zamijeni postojeći tekst umjesto da ga dodaješ
                            val currentText = entryText.trim().lowercase()
                            val commandLower = command.lowercase()

                            // Provjeri da li predložak već sadrži tekst koji je korisnik napisao
                            if (currentText.isNotEmpty() && commandLower.startsWith(currentText)) {
                                // Predložak već sadrži tekst koji je korisnik napisao, samo zamijeni
                                viewModel.setEntryText(command)
                            } else {
                                // Predložak ne sadrži tekst koji je korisnik napisao, dodaj ga
                                viewModel.setEntryText(if (entryText.isNotEmpty()) "$entryText $command" else command)
                            }
                        }
                    )
                    Divider()
                }

                // Symptom Helper (shown when category is HEALTH)
                if (detectedCategory == Category.HEALTH || selectedQuickCategory == Category.HEALTH) {
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

                OutlinedTextField(
                    value = entryText,
                    onValueChange = { viewModel.setEntryText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = {
                        Text(
                            when {
                                detectedCategory == Category.SMART_HOME || selectedQuickCategory == Category.SMART_HOME ->
                                    "Npr. 'Upali svjetla u dnevnom boravku' ili 'Postavi termostat na 22 stupnja'"

                                else ->
                                    "Npr. Neo je imao blagu temperaturu večeras. Dao sam mu lijek."
                            }
                        )
                    },
                    minLines = 5,
                    maxLines = 10
                )

                Text(
                    text = when {
                        detectedCategory == Category.SMART_HOME || selectedQuickCategory == Category.SMART_HOME ->
                            "Komanda će biti poslana Google Assistantu. Koristi jasne naredbe poput 'Upali svjetla', 'Postavi temperaturu na X stupnjeva', itd."

                        else ->
                            "Aplikacija će automatski kategorizirati tvoj zapis i dodati relevantne oznake."
                    },
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

                                            if (viewModel.saveEntry()) {
                                                onNavigateBack()
                                            }
                                        }
                                    }
                                ) {
                                    Text("Pokušaj ponovo")
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
                    text = "Upiši datum u tekst zapisa (npr. 'servis 15.12.2024', 'sutra', 'za 3 dana'). Aplikacija će automatski prepoznati datum i postaviti podsjetnik.",
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
                        Category.AUTO,
                        Category.HOUSE,
                        Category.FINANCE,
                        Category.WORK,
                        Category.SHOPPING,
                        Category.SMART_HOME
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
                                        Category.HOUSE -> "Kuća"
                                        Category.FEEDING -> "Hranjenje"
                                        Category.AUTO -> "Auto"
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

                // Temperature input (for HEALTH)
                if (selectedCategory == Category.HEALTH) {
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

                        Category.SHOPPING -> {
                            listOf(
                                "Kupovina",
                                "Namirnice",
                                "Lista",
                                "Treba kupiti"
                            ).forEach { phrase ->
                                SuggestionChip(
                                    onClick = { onAddToText(phrase) },
                                    label = { Text(phrase, fontSize = 12.sp) }
                                )
                            }
                        }

                        Category.SMART_HOME -> {
                            // Smart Home preset komande
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Česte komande:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(
                                        "Upali rumbu",
                                        "Ugasi svjetlo",
                                        "Upali svjetlo"
                                    ).forEach { phrase ->
                                        SuggestionChip(
                                            onClick = { onAddToText(phrase) },
                                            label = { Text(phrase, fontSize = 12.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(
                                        "Pusti uspavanku",
                                        "Ugasi klimu",
                                        "Upali TV"
                                    ).forEach { phrase ->
                                        SuggestionChip(
                                            onClick = { onAddToText(phrase) },
                                            label = { Text(phrase, fontSize = 12.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(
                                        "Zatvori rolete",
                                        "Otvori rolete",
                                        "Postavi termostat"
                                    ).forEach { phrase ->
                                        SuggestionChip(
                                            onClick = { onAddToText(phrase) },
                                            label = { Text(phrase, fontSize = 12.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
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

@Composable
fun SmartHomePresetBlock(
        onCommandSelected: (String) -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            )
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
                        text = "🏠 Smart Home - Brze komande",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Text(
                    text = "Odaberi komandu za brzi unos:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Preset komande u grid layoutu
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Red 1: Rumba i svjetla
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmartHomePresetButton(
                            text = "🤖 Upali rumbu",
                            onClick = { onCommandSelected("Upali rumbu") },
                            modifier = Modifier.weight(1f)
                        )
                        SmartHomePresetButton(
                            text = "💡 Upali svjetlo",
                            onClick = { onCommandSelected("Upali svjetlo") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Red 2: Ugasi komande
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmartHomePresetButton(
                            text = "💡 Ugasi svjetlo",
                            onClick = { onCommandSelected("Ugasi svjetlo") },
                            modifier = Modifier.weight(1f)
                        )
                        SmartHomePresetButton(
                            text = "❄️ Ugasi klimu",
                            onClick = { onCommandSelected("Ugasi klimu") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Red 3: Muzika i TV
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmartHomePresetButton(
                            text = "🎵 Pusti uspavanku",
                            onClick = { onCommandSelected("Pusti uspavanku u dnevnom") },
                            modifier = Modifier.weight(1f)
                        )
                        SmartHomePresetButton(
                            text = "📺 Upali TV",
                            onClick = { onCommandSelected("Upali TV") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Red 4: Rolete i klima
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmartHomePresetButton(
                            text = "🪟 Zatvori rolete",
                            onClick = { onCommandSelected("Zatvori rolete") },
                            modifier = Modifier.weight(1f)
                        )
                        SmartHomePresetButton(
                            text = "🌡️ Postavi termostat",
                            onClick = { onCommandSelected("Postavi termostat na 22 stupnjeva") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

@Composable
fun SmartHomePresetButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
