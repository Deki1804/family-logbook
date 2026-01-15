package com.familylogbook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familylogbook.app.domain.classifier.EntryClassifier
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.domain.model.FeedingType
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.repository.LogbookRepository
import com.familylogbook.app.domain.util.PersonAgeUtils.canHaveFeeding
import com.familylogbook.app.domain.timer.TimerManager
import android.content.Context
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddEntryViewModel(
    private val repository: LogbookRepository,
    val classifier: EntryClassifier // Made public for access in AddEntryScreen
) : ViewModel() {
    
    // Legacy Child support
    private val _children = MutableStateFlow<List<Child>>(emptyList())
    val children: StateFlow<List<Child>> = _children.asStateFlow()
    
    private val _selectedChildId = MutableStateFlow<String?>(null)
    val selectedChildId: StateFlow<String?> = _selectedChildId.asStateFlow()
    
    // New Person and Entity support
    private val _persons = MutableStateFlow<List<Person>>(emptyList())
    val persons: StateFlow<List<Person>> = _persons.asStateFlow()
    
    private val _entities = MutableStateFlow<List<Entity>>(emptyList())
    val entities: StateFlow<List<Entity>> = _entities.asStateFlow()
    
    private val _selectedPersonId = MutableStateFlow<String?>(null)
    val selectedPersonId: StateFlow<String?> = _selectedPersonId.asStateFlow()
    
    private val _selectedEntityId = MutableStateFlow<String?>(null)
    val selectedEntityId: StateFlow<String?> = _selectedEntityId.asStateFlow()
    
    private val _entryText = MutableStateFlow("")
    val entryText: StateFlow<String> = _entryText.asStateFlow()
    
    // Feeding tracking
    private val _isFeedingActive = MutableStateFlow(false)
    val isFeedingActive: StateFlow<Boolean> = _isFeedingActive.asStateFlow()
    
    private val _feedingStartTime = MutableStateFlow<Long?>(null)
    val feedingStartTime: StateFlow<Long?> = _feedingStartTime.asStateFlow()
    
    private val _feedingElapsedSeconds = MutableStateFlow(0L)
    val feedingElapsedSeconds: StateFlow<Long> = _feedingElapsedSeconds.asStateFlow()
    
    private val _selectedFeedingType = MutableStateFlow<FeedingType?>(null)
    val selectedFeedingType: StateFlow<FeedingType?> = _selectedFeedingType.asStateFlow()
    
    private val _bottleAmount = MutableStateFlow("")
    val bottleAmount: StateFlow<String> = _bottleAmount.asStateFlow()
    
    // Edit mode support
    private val _editingEntryId = MutableStateFlow<String?>(null)
    val editingEntryId: StateFlow<String?> = _editingEntryId.asStateFlow()
    
    // Symptom tracking (for health entries)
    private val _selectedSymptoms = MutableStateFlow<Set<String>>(emptySet())
    val selectedSymptoms: StateFlow<Set<String>> = _selectedSymptoms.asStateFlow()
    
    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Vaccination selection state (if vaccination name not detected)
    private val _showVaccinationDialog = MutableStateFlow(false)
    val showVaccinationDialog: StateFlow<Boolean> = _showVaccinationDialog.asStateFlow()
    
    private val _pendingEntryText = MutableStateFlow<String?>(null)
    val pendingEntryText: StateFlow<String?> = _pendingEntryText.asStateFlow()
    
    fun setShowVaccinationDialog(show: Boolean) {
        _showVaccinationDialog.value = show
    }
    
    fun setPendingEntryText(text: String?) {
        _pendingEntryText.value = text
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Extracts person name from text (e.g., "neo je primio cjepivo" -> "neo").
     * Returns null if no person name found.
     */
    private fun extractPersonNameFromText(text: String, persons: List<Person>): String? {
        val lowerText = text.lowercase()
        
        // Try to find person name in text by matching against known person names
        for (person in persons) {
            val personNameLower = person.name.lowercase()
            // Check if person name appears in text (as whole word or at start of sentence)
            if (lowerText.contains(personNameLower)) {
                // Make sure it's not part of another word
                val nameIndex = lowerText.indexOf(personNameLower)
                val beforeChar = if (nameIndex > 0) lowerText[nameIndex - 1] else ' '
                val afterIndex = nameIndex + personNameLower.length
                val afterChar = if (afterIndex < lowerText.length) lowerText[afterIndex] else ' '
                
                // If name is surrounded by spaces or at start/end, it's likely a person name
                if ((beforeChar == ' ' || nameIndex == 0) && 
                    (afterChar == ' ' || afterIndex == lowerText.length || 
                     afterChar == 'j' || afterChar == 'a' || afterChar == 'e' || afterChar == 'i' || afterChar == 'o' || afterChar == 'u')) {
                    return person.name
                }
            }
        }
        
        return null
    }
    
    /**
     * Saves entry with manually selected vaccination name.
     * Called after user selects vaccination from dialog.
     */
    suspend fun saveEntryWithVaccination(vaccinationName: String): Boolean {
        val pendingText = _pendingEntryText.value ?: return false
        _pendingEntryText.value = null
        _showVaccinationDialog.value = false
        
        // Temporarily set entry text with vaccination name appended
        val originalText = _entryText.value
        _entryText.value = "$pendingText $vaccinationName"
        
        // Now save entry (classification will pick up the vaccination name)
        val result = saveEntry()
        
        // Restore original text if save failed
        if (!result) {
            _entryText.value = originalText
        }
        
        return result
    }
    
    private var feedingTimerJob: Job? = null
    
    init {
        loadChildren()
        loadPersons()
        loadEntities()
    }
    
    override fun onCleared() {
        super.onCleared()
        feedingTimerJob?.cancel()
    }
    
    private fun loadChildren() {
        viewModelScope.launch {
            repository.getAllChildren().collect { childrenList ->
                _children.value = childrenList
            }
        }
    }
    
    private fun loadPersons() {
        viewModelScope.launch {
            repository.getAllPersons().collect { personsList ->
                _persons.value = personsList
                
                // If feeding is active, check if selected child can still have feeding
                if (_isFeedingActive.value) {
                    val selectedPersonId = _selectedPersonId.value ?: _selectedChildId.value
                    val selectedPerson = selectedPersonId?.let { pid ->
                        personsList.find { it.id == pid }
                    }
                    
                    val canStillHaveFeeding = selectedPerson != null &&
                                             selectedPerson.type == com.familylogbook.app.domain.model.PersonType.CHILD &&
                                             selectedPerson.dateOfBirth != null &&
                                             canHaveFeeding(selectedPerson.dateOfBirth)
                    
                    // Stop feeding if child no longer exists or is too old
                    if (!canStillHaveFeeding) {
                        stopFeeding()
                    }
                }
            }
        }
    }
    
    private fun loadEntities() {
        viewModelScope.launch {
            repository.getAllEntities().collect { entitiesList ->
                _entities.value = entitiesList
            }
        }
    }
    
    fun setSelectedChild(childId: String?) {
        _selectedChildId.value = childId
        _selectedPersonId.value = childId // Also set as personId for backward compatibility
        _selectedEntityId.value = null
    }
    
    fun setSelectedPerson(personId: String?) {
        android.util.Log.d("AddEntryViewModel", "setSelectedPerson called with: $personId")
        _selectedPersonId.value = personId
        _selectedChildId.value = null
        _selectedEntityId.value = null
        android.util.Log.d("AddEntryViewModel", "After setSelectedPerson - _selectedPersonId.value = ${_selectedPersonId.value}")
    }
    
    fun setSelectedEntity(entityId: String?) {
        _selectedEntityId.value = entityId
        _selectedPersonId.value = null
        _selectedChildId.value = null
    }
    
    fun setEntryText(text: String) {
        _entryText.value = text
    }
    
    /**
     * Sets entry text and automatically saves if it's a shopping list with items.
     * Used for voice input that's already formatted as shopping list.
     */
    suspend fun setEntryTextAndAutoSave(text: String): Boolean {
        _entryText.value = text
        
        if (text.isBlank()) return false
        
        // Klasificiraj tekst
        val classification = classifier.classifyEntry(text)
        
        // Ako je shopping lista s ekstraktovanim stavkama, automatski spremi
        if (classification.category == Category.SHOPPING && 
            classification.shoppingItems != null && 
            classification.shoppingItems.isNotEmpty()) {
            
            android.util.Log.d("AddEntryViewModel", "Auto-saving shopping list with ${classification.shoppingItems.size} items")
            
            // Automatski spremi shopping entry
            return saveEntry()
        }
        
        return false
    }
    
    fun startFeeding(feedingType: FeedingType) {
        // Safety check: only start feeding if there are children and one is selected
        val selectedPersonId = _selectedPersonId.value ?: _selectedChildId.value
        val selectedPerson = selectedPersonId?.let { pid ->
            _persons.value.find { it.id == pid }
        }
        
        // Check if selected person is a child < 2 years old
        val canStartFeeding = selectedPerson != null &&
                              selectedPerson.type == com.familylogbook.app.domain.model.PersonType.CHILD &&
                              selectedPerson.dateOfBirth != null &&
                              canHaveFeeding(selectedPerson.dateOfBirth)
        
        if (!canStartFeeding) {
            return // Don't start feeding if no valid child selected or child is too old
        }
        
        _isFeedingActive.value = true
        _selectedFeedingType.value = feedingType
        _feedingStartTime.value = System.currentTimeMillis()
        _feedingElapsedSeconds.value = 0L
        
        // Start timer
        feedingTimerJob?.cancel()
        feedingTimerJob = viewModelScope.launch {
            while (_isFeedingActive.value) {
                delay(1000)
                // viewModelScope automatski cancel-uje job kada se ViewModel uništi,
                // tako da je sigurno update-ovati state ovdje
                _feedingElapsedSeconds.value = _feedingElapsedSeconds.value + 1
            }
        }
    }
    
    fun stopFeeding() {
        _isFeedingActive.value = false
        feedingTimerJob?.cancel()
    }
    
    fun setBottleAmount(amount: String) {
        _bottleAmount.value = amount
    }
    
    fun setSymptoms(symptoms: Set<String>) {
        _selectedSymptoms.value = symptoms
    }
    
    fun setEditingEntry(entryId: String?) {
        _editingEntryId.value = entryId
    }
    
    suspend fun loadEntryForEdit(entryId: String): Boolean {
        return try {
            val entry = repository.getEntryById(entryId)
            if (entry != null) {
                _editingEntryId.value = entryId
                _entryText.value = entry.rawText
                _selectedChildId.value = entry.childId
                _selectedPersonId.value = entry.personId
                _selectedEntityId.value = entry.entityId
                _selectedSymptoms.value = entry.symptoms?.toSet() ?: emptySet()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("AddEntryViewModel", "Error loading entry for edit: ${e.message}")
            false
        }
    }
    
    suspend fun saveEntry(): Boolean {
        val text = _entryText.value.trim()
        if (text.isEmpty()) {
            return false
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            val editingId = _editingEntryId.value
            
            // If editing, load existing entry to preserve timestamp and other fields
            val existingEntry = if (editingId != null) {
                repository.getEntryById(editingId)
            } else {
                null
            }
            
            val classification = classifier.classifyEntry(text)
            
            // Check if selected person is a child/baby - only then allow feeding info
            var selectedPersonId = _selectedPersonId.value ?: _selectedChildId.value
            var selectedPerson = selectedPersonId?.let { pid -> 
                _persons.value.find { it.id == pid }
            }
            
            // If no person selected, try to auto-detect from text (e.g., "neo je primio cjepivo")
            if (selectedPerson == null) {
                val detectedPersonName = extractPersonNameFromText(text, _persons.value)
                if (detectedPersonName != null) {
                    val foundPerson = _persons.value.find { 
                        it.name.equals(detectedPersonName, ignoreCase = true) 
                    }
                    if (foundPerson != null) {
                        selectedPerson = foundPerson
                        selectedPersonId = foundPerson.id
                        _selectedPersonId.value = foundPerson.id
                    }
                }
            }
            
            // Check if this is a vaccination entry but vaccination name is not detected
            // IMPORTANT: classifier often returns Category.VACCINATION directly, so handle both HEALTH and VACCINATION.
            val lowerText = text.lowercase()
            val looksLikeVaccinationByKeywords =
                lowerText.contains("cjepivo") ||
                    lowerText.contains("cjepiv") ||
                    lowerText.contains("vakcina") ||
                    lowerText.contains("vaccination") ||
                    lowerText.contains("primio") ||
                    lowerText.contains("primila")

            val isVaccinationEntry =
                classification.category == Category.VACCINATION ||
                    (classification.category == Category.HEALTH && looksLikeVaccinationByKeywords)
            
            // If vaccination entry but no vaccination name detected, try to show dialog
            if (isVaccinationEntry && classification.vaccinationName == null) {
                // If person is selected and is a child, show dialog
                if (selectedPerson != null &&
                    selectedPerson.type == com.familylogbook.app.domain.model.PersonType.CHILD &&
                    selectedPerson.dateOfBirth != null) {
                    
                    // Store pending entry text and show dialog
                    _pendingEntryText.value = text
                    _showVaccinationDialog.value = true
                    _isLoading.value = false
                    return false // Don't save yet, wait for user to select vaccination
                }
                // If no person selected but we have children, show dialog with all children
                else if (selectedPerson == null) {
                    val childPersons = _persons.value.filter { 
                        it.type == com.familylogbook.app.domain.model.PersonType.CHILD && 
                        it.dateOfBirth != null 
                    }
                    if (childPersons.isNotEmpty()) {
                        // Store pending entry text and show dialog
                        _pendingEntryText.value = text
                        _showVaccinationDialog.value = true
                        _isLoading.value = false
                        return false // Don't save yet, wait for user to select vaccination
                    }
                }
            }
            
            // Calculate next vaccination if this is a vaccination entry for a child
            var nextVaccinationDate: Long? = null
            var nextVaccinationMessage: String? = null
            
            if (classification.vaccinationName != null && selectedPerson != null &&
                selectedPerson.type == com.familylogbook.app.domain.model.PersonType.CHILD &&
                selectedPerson.dateOfBirth != null) {
                
                // Get all previous vaccination entries for this child
                val allEntriesFlow = repository.getAllEntries()
                val allEntries = allEntriesFlow.first()
                val childVaccinationEntries = allEntries.filter { entry ->
                    (entry.personId == selectedPersonId || entry.childId == selectedPersonId) &&
                    entry.vaccinationName != null &&
                    entry.id != editingId // Exclude the entry being edited
                }
                val givenVaccinations = childVaccinationEntries.mapNotNull { it.vaccinationName }
                
                // If this is a new vaccination (not editing existing), add it to the list
                val vaccinationsForCalculation = if (editingId == null && classification.vaccinationName != null) {
                    givenVaccinations + classification.vaccinationName
                } else {
                    givenVaccinations
                }
                
                // Get next vaccination recommendation
                val dob = selectedPerson.dateOfBirth // Local copy to avoid smart cast issue
                val recommendation = if (dob != null) {
                    com.familylogbook.app.domain.vaccination.VaccinationCalendar.getNextVaccination(
                        dateOfBirth = dob,
                        givenVaccinations = vaccinationsForCalculation
                    )
                } else {
                    null
                }
                
                if (recommendation != null) {
                    nextVaccinationDate = recommendation.recommendedDate
                    nextVaccinationMessage = recommendation.message
                }
            }
            
            // Only allow feeding if:
            // 1. Person is selected AND
            // 2. Person is a CHILD type AND
            // 3. Person has dateOfBirth AND
            // 4. Person is less than 2 years old
            val personDob = selectedPerson?.dateOfBirth // Local copy to avoid smart cast issue
            val canHaveFeedingInfo = selectedPerson != null && 
                                     selectedPerson.type == com.familylogbook.app.domain.model.PersonType.CHILD &&
                                     personDob != null &&
                                     canHaveFeeding(personDob)
            
            // Get AI advice if available (use symptoms if available for better advice)
            val adviceEngine = com.familylogbook.app.domain.classifier.AdviceEngine()
            val symptomsList = if (_selectedSymptoms.value.isNotEmpty()) _selectedSymptoms.value.toList() else null
            val advice = adviceEngine.findAdvice(text, classification.category, symptomsList)
            
            // Calculate next medicine time if interval is provided
            val now = System.currentTimeMillis()
            val nextMedicineTime = classification.medicineIntervalHours?.let { intervalHours ->
                now + (intervalHours * 60 * 60 * 1000L)
            }
            
            // Don't allow FEEDING category if no child/baby is selected or child is too old
            // Change category to OTHER if classifier detected FEEDING but user has no children or child is >= 2 years
            val finalCategory = if (classification.category == Category.FEEDING && !canHaveFeedingInfo) {
                Category.OTHER
            } else {
                classification.category
            }
            
            val entry = LogEntry(
                id = editingId ?: existingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                childId = _selectedChildId.value,
                personId = selectedPersonId,
                entityId = _selectedEntityId.value,
                timestamp = existingEntry?.timestamp ?: now, // Preserve original timestamp when editing
                rawText = text,
                category = finalCategory, // Use adjusted category
                tags = classification.tags,
                mood = classification.mood,
                temperature = classification.temperature,
                medicineGiven = classification.medicineGiven,
                medicineTimestamp = classification.medicineGiven?.let { now },
                medicineIntervalHours = classification.medicineIntervalHours,
                nextMedicineTime = nextMedicineTime,
                reminderDate = classification.reminderDate, // Use extracted reminder date
                // Only set feeding info if:
                // 1. Category is FEEDING (after adjustment) AND
                // 2. A child/baby person is selected (< 2 years old)
                feedingType = if (finalCategory == Category.FEEDING && canHaveFeedingInfo) classification.feedingType else null,
                feedingAmount = if (finalCategory == Category.FEEDING && canHaveFeedingInfo) classification.feedingAmount else null,
                symptoms = if (_selectedSymptoms.value.isNotEmpty()) _selectedSymptoms.value.toList() else null,
                shoppingItems = classification.shoppingItems ?: existingEntry?.shoppingItems,
                checkedShoppingItems = existingEntry?.checkedShoppingItems ?: emptySet(), // Preserve checked items when editing
                aiAdvice = advice?.title, // Store advice title for now
                amount = existingEntry?.amount, // Preserve amount if editing
                currency = existingEntry?.currency, // Preserve currency if editing
                vaccinationName = classification.vaccinationName ?: existingEntry?.vaccinationName,
                vaccinationDate = if (classification.vaccinationName != null) {
                    existingEntry?.vaccinationDate ?: now
                } else {
                    existingEntry?.vaccinationDate
                },
                nextVaccinationDate = nextVaccinationDate ?: existingEntry?.nextVaccinationDate,
                nextVaccinationMessage = nextVaccinationMessage ?: existingEntry?.nextVaccinationMessage
            )
            
            if (editingId != null) {
                repository.updateEntry(entry)
                android.util.Log.d("AddEntryViewModel", "Entry updated successfully: ${entry.id}, category: ${entry.category}, personId: ${entry.personId}, text: ${entry.rawText.take(50)}")
            } else {
                repository.addEntry(entry)
                android.util.Log.d("AddEntryViewModel", "Entry added successfully: ${entry.id}, category: ${entry.category}, personId: ${entry.personId}, text: ${entry.rawText.take(50)}")
            }
            
            // Reset form
            _entryText.value = ""
            _selectedChildId.value = null
            _selectedPersonId.value = null
            _selectedEntityId.value = null
            _selectedSymptoms.value = emptySet()
            _editingEntryId.value = null
            _errorMessage.value = null // Clear any previous errors
            
            true
        } catch (e: Exception) {
            android.util.Log.e("AddEntryViewModel", "Error saving entry: ${e.message}", e)
            _errorMessage.value = com.familylogbook.app.ui.util.ErrorHandler.getFriendlyErrorMessage(e)
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Saves a medicine entry directly (Parent OS core feature).
     * This bypasses text classification and creates a medicine entry directly.
     */
    suspend fun saveMedicineEntry(
        medicineName: String,
        dosage: String,
        intervalHours: Int,
        notes: String? = null
    ): Boolean {
        val selectedPersonId = _selectedPersonId.value ?: _selectedChildId.value
        if (selectedPersonId == null) {
            _errorMessage.value = "Molimo odaberi osobu za lijek."
            return false
        }
        
        if (medicineName.isEmpty()) {
            _errorMessage.value = "Molimo unesi naziv lijeka."
            return false
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            val medicineEntry = com.familylogbook.app.domain.model.MedicineEntry.create(
                personId = selectedPersonId,
                medicineName = medicineName,
                dosage = dosage.ifEmpty { "1 doza" },
                givenAt = System.currentTimeMillis(),
                intervalHours = intervalHours,
                notes = notes
            )
            
            repository.addEntry(medicineEntry)
            android.util.Log.d("AddEntryViewModel", "Medicine entry added successfully: ${medicineEntry.id}")
            
            // Reset form
            _entryText.value = ""
            _selectedSymptoms.value = emptySet()
            _errorMessage.value = null // Clear any previous errors
            
            true
        } catch (e: Exception) {
            android.util.Log.e("AddEntryViewModel", "Error saving medicine entry: ${e.message}", e)
            _errorMessage.value = com.familylogbook.app.ui.util.ErrorHandler.getFriendlyErrorMessage(e)
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun saveFeedingEntry(): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            // Use personId first, fallback to childId for backward compatibility
            val personId = _selectedPersonId.value ?: _selectedChildId.value
            if (personId == null) {
                _errorMessage.value = "Molimo odaberi osobu za hranjenje."
                return false
            }
            
            val startTime = _feedingStartTime.value ?: System.currentTimeMillis()
            val feedingType = _selectedFeedingType.value
            if (feedingType == null) {
                _errorMessage.value = "Molimo odaberi tip hranjenja."
                return false
            }
            
            val durationMinutes = _feedingElapsedSeconds.value / 60
            
            val text = when (feedingType) {
                FeedingType.BREAST_LEFT -> "Dojenje (lijeva dojka), trajalo ${durationMinutes} minuta."
                FeedingType.BREAST_RIGHT -> "Dojenje (desna dojka), trajalo ${durationMinutes} minuta."
                FeedingType.BOTTLE -> {
                    val amount = _bottleAmount.value.toIntOrNull() ?: 0
                    "Bočica ${amount}ml, trajalo ${durationMinutes} minuta."
                }
            }
            
            val classification = classifier.classifyEntry(text)
            val entry = LogEntry(
                personId = personId,
                childId = _selectedChildId.value, // Legacy support
                timestamp = startTime,
                rawText = text,
                category = Category.FEEDING,
                tags = classification.tags,
                mood = null,
                feedingType = feedingType,
                feedingAmount = if (feedingType == FeedingType.BOTTLE) _bottleAmount.value.toIntOrNull() else null
            )
            
            repository.addEntry(entry)
            android.util.Log.d("AddEntryViewModel", "Feeding entry added successfully: ${entry.id}")
            
            // Reset feeding state
            stopFeeding()
            _selectedFeedingType.value = null
            _bottleAmount.value = ""
            _feedingStartTime.value = null
            _feedingElapsedSeconds.value = 0L
            _selectedChildId.value = null
            _selectedPersonId.value = null
            _errorMessage.value = null // Clear any previous errors
            
            true
        } catch (e: Exception) {
            android.util.Log.e("AddEntryViewModel", "Error saving feeding entry: ${e.message}", e)
            _errorMessage.value = com.familylogbook.app.ui.util.ErrorHandler.getFriendlyErrorMessage(e)
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Saves a symptom entry directly (Parent OS core feature).
     * This bypasses text classification and creates a symptom entry directly.
     */
    suspend fun saveSymptomEntry(
        temperature: Float? = null,
        symptoms: List<String> = emptyList(),
        notes: String? = null
    ): Boolean {
        val selectedPersonId = _selectedPersonId.value ?: _selectedChildId.value
        if (selectedPersonId == null) {
            _errorMessage.value = "Molimo odaberi osobu za simptom."
            return false
        }
        
        if (temperature == null && symptoms.isEmpty()) {
            _errorMessage.value = "Molimo unesi temperaturu ili simptome."
            return false
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            val symptomEntry = com.familylogbook.app.domain.model.SymptomEntry.create(
                personId = selectedPersonId,
                temperature = temperature,
                symptoms = symptoms,
                timestamp = System.currentTimeMillis(),
                notes = notes
            )
            
            repository.addEntry(symptomEntry)
            android.util.Log.d("AddEntryViewModel", "Symptom entry added successfully: ${symptomEntry.id}")
            
            // Reset form
            _entryText.value = ""
            _selectedSymptoms.value = emptySet()
            _errorMessage.value = null // Clear any previous errors
            
            true
        } catch (e: Exception) {
            android.util.Log.e("AddEntryViewModel", "Error saving symptom entry: ${e.message}", e)
            _errorMessage.value = com.familylogbook.app.ui.util.ErrorHandler.getFriendlyErrorMessage(e)
            false
        } finally {
            _isLoading.value = false
        }
    }
}

