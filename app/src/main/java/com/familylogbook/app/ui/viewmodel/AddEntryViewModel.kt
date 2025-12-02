package com.familylogbook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familylogbook.app.domain.classifier.EntryClassifier
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.repository.LogbookRepository
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
    
    init {
        loadChildren()
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
    
    suspend fun saveEntry(): Boolean {
        val text = _entryText.value.trim()
        if (text.isEmpty()) {
            return false
        }
        
        val classification = classifier.classifyEntry(text)
        val entry = LogEntry(
            childId = _selectedChildId.value,
            rawText = text,
            category = classification.category,
            tags = classification.tags,
            mood = classification.mood
        )
        
        repository.addEntry(entry)
        
        // Reset form
        _entryText.value = ""
        _selectedChildId.value = null
        
        return true
    }
}

