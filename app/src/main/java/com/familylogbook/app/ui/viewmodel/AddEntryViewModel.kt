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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddEntryViewModel(
    private val repository: LogbookRepository,
    private val classifier: EntryClassifier
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
    
    fun clearError() {
        _errorMessage.value = null
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
        _selectedPersonId.value = personId
        _selectedChildId.value = null
        _selectedEntityId.value = null
    }
    
    fun setSelectedEntity(entityId: String?) {
        _selectedEntityId.value = entityId
        _selectedPersonId.value = null
        _selectedChildId.value = null
    }
    
    fun setEntryText(text: String) {
        _entryText.value = text
    }
    
    fun startFeeding(feedingType: FeedingType) {
        _isFeedingActive.value = true
        _selectedFeedingType.value = feedingType
        _feedingStartTime.value = System.currentTimeMillis()
        _feedingElapsedSeconds.value = 0L
        
        // Start timer
        feedingTimerJob?.cancel()
        feedingTimerJob = viewModelScope.launch {
            while (_isFeedingActive.value) {
                delay(1000)
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
            
            // Get AI advice if available (use symptoms if available for better advice)
            val adviceEngine = com.familylogbook.app.domain.classifier.AdviceEngine()
            val symptomsList = if (_selectedSymptoms.value.isNotEmpty()) _selectedSymptoms.value.toList() else null
            val advice = adviceEngine.findAdvice(text, classification.category, symptomsList)
            
            // Calculate next medicine time if interval is provided
            val now = System.currentTimeMillis()
            val nextMedicineTime = classification.medicineIntervalHours?.let { intervalHours ->
                now + (intervalHours * 60 * 60 * 1000L)
            }
            
            val entry = LogEntry(
                id = editingId ?: existingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                childId = _selectedChildId.value,
                personId = _selectedPersonId.value ?: _selectedChildId.value,
                entityId = _selectedEntityId.value,
                timestamp = existingEntry?.timestamp ?: now, // Preserve original timestamp when editing
                rawText = text,
                category = classification.category,
                tags = classification.tags,
                mood = classification.mood,
                temperature = classification.temperature,
                medicineGiven = classification.medicineGiven,
                medicineTimestamp = classification.medicineGiven?.let { now },
                medicineIntervalHours = classification.medicineIntervalHours,
                nextMedicineTime = nextMedicineTime,
                reminderDate = classification.reminderDate, // Use extracted reminder date
                feedingType = classification.feedingType,
                feedingAmount = classification.feedingAmount,
                symptoms = if (_selectedSymptoms.value.isNotEmpty()) _selectedSymptoms.value.toList() else null,
                aiAdvice = advice?.title, // Store advice title for now
                amount = existingEntry?.amount, // Preserve amount if editing
                currency = existingEntry?.currency // Preserve currency if editing
            )
            
            if (editingId != null) {
                repository.updateEntry(entry)
            } else {
                repository.addEntry(entry)
            }
            
            // Reset form
            _entryText.value = ""
            _selectedChildId.value = null
            _selectedPersonId.value = null
            _selectedEntityId.value = null
            _selectedSymptoms.value = emptySet()
            _editingEntryId.value = null
            
            true
        } catch (e: Exception) {
            android.util.Log.e("AddEntryViewModel", "Error saving entry: ${e.message}", e)
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
            val childId = _selectedChildId.value
            if (childId == null) {
                _errorMessage.value = "Molimo odaberi dijete za hranjenje."
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
                childId = childId,
                personId = childId,
                timestamp = startTime,
                rawText = text,
                category = Category.FEEDING,
                tags = classification.tags,
                mood = null,
                feedingType = feedingType,
                feedingAmount = if (feedingType == FeedingType.BOTTLE) _bottleAmount.value.toIntOrNull() else null
            )
            
            repository.addEntry(entry)
            
            // Reset feeding state
            stopFeeding()
            _selectedFeedingType.value = null
            _bottleAmount.value = ""
            _feedingStartTime.value = null
            _feedingElapsedSeconds.value = 0L
            
            true
        } catch (e: Exception) {
            android.util.Log.e("AddEntryViewModel", "Error saving feeding entry: ${e.message}", e)
            _errorMessage.value = com.familylogbook.app.ui.util.ErrorHandler.getFriendlyErrorMessage(e)
            false
        } finally {
            _isLoading.value = false
        }
    }
}

