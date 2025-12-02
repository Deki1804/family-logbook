package com.familylogbook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familylogbook.app.domain.classifier.EntryClassifier
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
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
    
    private val _children = MutableStateFlow<List<Child>>(emptyList())
    val children: StateFlow<List<Child>> = _children.asStateFlow()
    
    private val _selectedChildId = MutableStateFlow<String?>(null)
    val selectedChildId: StateFlow<String?> = _selectedChildId.asStateFlow()
    
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
    
    private var feedingTimerJob: Job? = null
    
    init {
        loadChildren()
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
    
    fun setSelectedChild(childId: String?) {
        _selectedChildId.value = childId
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
    
    suspend fun saveFeedingEntry(): Boolean {
        val childId = _selectedChildId.value
        if (childId == null) {
            return false // Must select a child for feeding
        }
        
        val startTime = _feedingStartTime.value ?: System.currentTimeMillis()
        val feedingType = _selectedFeedingType.value ?: return false
        
        return try {
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
            android.util.Log.e("AddEntryViewModel", "Error saving feeding entry: ${e.message}")
            false // Return false on error
        }
    }
    
    suspend fun saveEntry(): Boolean {
        val text = _entryText.value.trim()
        if (text.isEmpty()) {
            return false
        }
        
        return try {
            val classification = classifier.classifyEntry(text)
            val entry = LogEntry(
                childId = _selectedChildId.value,
                rawText = text,
                category = classification.category,
                tags = classification.tags,
                mood = classification.mood,
                temperature = classification.temperature,
                medicineGiven = classification.medicineGiven,
                medicineTimestamp = classification.medicineGiven?.let { System.currentTimeMillis() },
                feedingType = classification.feedingType,
                feedingAmount = classification.feedingAmount
            )
            
            repository.addEntry(entry)
            
            // Reset form
            _entryText.value = ""
            _selectedChildId.value = null
            
            true
        } catch (e: Exception) {
            android.util.Log.e("AddEntryViewModel", "Error saving entry: ${e.message}")
            false // Return false on error - UI should show error message
        }
    }
}

