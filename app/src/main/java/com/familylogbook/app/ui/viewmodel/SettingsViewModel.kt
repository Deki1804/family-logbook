package com.familylogbook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familylogbook.app.data.export.ExportManager
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.repository.LogbookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: LogbookRepository
) : ViewModel() {
    
    private val exportManager = ExportManager()
    
    private val _children = MutableStateFlow<List<Child>>(emptyList())
    val children: StateFlow<List<Child>> = _children.asStateFlow()
    
    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()
    
    private val _newChildName = MutableStateFlow("")
    val newChildName: StateFlow<String> = _newChildName.asStateFlow()
    
    private val _newChildEmoji = MutableStateFlow("👶")
    val newChildEmoji: StateFlow<String> = _newChildEmoji.asStateFlow()
    
    init {
        loadChildren()
        loadEntries()
    }
    
    private fun loadChildren() {
        viewModelScope.launch {
            repository.getAllChildren().collect { childrenList ->
                _children.value = childrenList
            }
        }
    }
    
    private fun loadEntries() {
        viewModelScope.launch {
            repository.getAllEntries().collect { entriesList ->
                _entries.value = entriesList
            }
        }
    }
    
    fun setNewChildName(name: String) {
        _newChildName.value = name
    }
    
    fun setNewChildEmoji(emoji: String) {
        _newChildEmoji.value = emoji
    }
    
    suspend fun addChild(): Boolean {
        val name = _newChildName.value.trim()
        if (name.isEmpty()) {
            return false
        }
        
        val colors = listOf("#FF6B6B", "#4ECDC4", "#FF6B9D", "#95E1D3", "#F38181", "#AA96DA", "#FCBAD3", "#A8E6CF")
        val randomColor = colors.random()
        
        val child = Child(
            name = name,
            avatarColor = randomColor,
            emoji = _newChildEmoji.value
        )
        
        repository.addChild(child)
        
        // Reset form
        _newChildName.value = ""
        _newChildEmoji.value = "👶"
        
        return true
    }
    
    suspend fun deleteChild(childId: String) {
        repository.deleteChild(childId)
    }
    
    fun exportToJson(): String {
        return exportManager.exportToJson(_children.value, _entries.value)
    }
    
    fun exportToCsv(): String {
        return exportManager.exportToCsv(_children.value, _entries.value)
    }
}

