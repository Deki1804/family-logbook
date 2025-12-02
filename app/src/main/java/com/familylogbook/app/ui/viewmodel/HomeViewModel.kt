package com.familylogbook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familylogbook.app.domain.classifier.AdviceEngine
import com.familylogbook.app.domain.model.AdviceTemplate
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.repository.LogbookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: LogbookRepository
) : ViewModel() {
    
    private val adviceEngine = AdviceEngine()
    
    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()
    
    // Legacy Child support (for backward compatibility)
    private val _children = MutableStateFlow<List<Child>>(emptyList())
    val children: StateFlow<List<Child>> = _children.asStateFlow()
    
    // New Person and Entity support
    private val _persons = MutableStateFlow<List<Person>>(emptyList())
    val persons: StateFlow<List<Person>> = _persons.asStateFlow()
    
    private val _entities = MutableStateFlow<List<Entity>>(emptyList())
    val entities: StateFlow<List<Entity>> = _entities.asStateFlow()
    
    // Filters
    private val _selectedPersonId = MutableStateFlow<String?>(null)
    val selectedPersonId: StateFlow<String?> = _selectedPersonId.asStateFlow()
    
    private val _selectedEntityId = MutableStateFlow<String?>(null)
    val selectedEntityId: StateFlow<String?> = _selectedEntityId.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Filtered entries
    private val _filteredEntries = MutableStateFlow<List<LogEntry>>(emptyList())
    val filteredEntries: StateFlow<List<LogEntry>> = _filteredEntries.asStateFlow()
    
    init {
        loadEntries()
        loadChildren()
        loadPersons()
        loadEntities()
        
        // Setup filtered entries
        viewModelScope.launch {
            combine(
                _entries,
                _selectedPersonId,
                _selectedEntityId,
                _selectedCategory,
                _searchQuery
            ) { entries, personId, entityId, category, query ->
                entries.filter { entry ->
                    val matchesPerson = personId == null || entry.personId == personId || entry.childId == personId
                    val matchesEntity = entityId == null || entry.entityId == entityId
                    val matchesCategory = category == null || entry.category == category
                    val matchesSearch = query.isEmpty() || 
                        entry.rawText.contains(query, ignoreCase = true) ||
                        entry.tags.any { it.contains(query, ignoreCase = true) }
                    matchesPerson && matchesEntity && matchesCategory && matchesSearch
                }
            }.collect { _filteredEntries.value = it }
        }
    }
    
    private fun loadEntries() {
        viewModelScope.launch {
            repository.getAllEntries().collect { entriesList ->
                _entries.value = entriesList.sortedByDescending { it.timestamp }
            }
        }
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
    
    fun setSelectedPerson(personId: String?) {
        _selectedPersonId.value = personId
        _selectedEntityId.value = null // Clear entity when person is selected
    }
    
    fun setSelectedEntity(entityId: String?) {
        _selectedEntityId.value = entityId
        _selectedPersonId.value = null // Clear person when entity is selected
    }
    
    fun setSelectedCategory(category: Category?) {
        _selectedCategory.value = category
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun clearFilters() {
        _selectedPersonId.value = null
        _selectedEntityId.value = null
        _selectedCategory.value = null
        _searchQuery.value = ""
    }
    
    fun refreshEntries() {
        loadEntries()
    }
    
    fun getAdviceForEntry(entry: LogEntry): AdviceTemplate? {
        return adviceEngine.findAdvice(entry.rawText, entry.category)
    }
    
    fun getPersonById(personId: String): Person? {
        return _persons.value.find { it.id == personId }
    }
    
    fun getEntityById(entityId: String): Entity? {
        return _entities.value.find { it.id == entityId }
    }
    
    fun getChildById(childId: String): Child? {
        return _children.value.find { it.id == childId }
    }
    
    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            repository.deleteEntry(entryId)
        }
    }
}

