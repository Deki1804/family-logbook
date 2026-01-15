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
import com.familylogbook.app.domain.model.DayEntryType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import android.content.Context
import android.content.SharedPreferences

class HomeViewModel(
    private val repository: LogbookRepository,
    private val context: Context? = null
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
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Filtered entries
    private val _filteredEntries = MutableStateFlow<List<LogEntry>>(emptyList())
    val filteredEntries: StateFlow<List<LogEntry>> = _filteredEntries.asStateFlow()
    
    // Shopping deals removed - no longer needed for Parent OS
    
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
                    val matchesPerson = personId == null || entry.personId == personId
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
            _isLoading.value = true
            try {
                repository.getAllEntries().collect { entriesList ->
                    val sortedEntries = entriesList.sortedByDescending { it.timestamp }
                    android.util.Log.d("HomeViewModel", "Received ${entriesList.size} entries, sorted to ${sortedEntries.size}")
                    if (sortedEntries.isNotEmpty()) {
                        android.util.Log.d("HomeViewModel", "Latest entry: ${sortedEntries.first().id}, category: ${sortedEntries.first().category}, text: ${sortedEntries.first().rawText.take(50)}")
                    }
                    _entries.value = sortedEntries
                    _isLoading.value = false
                    
                    // Shopping deals removed - no longer needed for Parent OS
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error loading entries: ${e.message}", e)
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh all data (entries, persons, entities, children).
     * Used for pull-to-refresh functionality.
     */
    fun refreshAll() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                loadEntries()
                loadPersons()
                loadEntities()
                loadChildren()
                _isLoading.value = false
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error refreshing data: ${e.message}", e)
                _isLoading.value = false
            }
        }
    }
    
    // Shopping deals methods removed - no longer needed for Parent OS
    
    // Store current advice for detail screen
    private val _currentAdvice = MutableStateFlow<AdviceTemplate?>(null)
    val currentAdvice: StateFlow<AdviceTemplate?> = _currentAdvice.asStateFlow()
    
    fun setCurrentAdvice(advice: AdviceTemplate?) {
        _currentAdvice.value = advice
    }
    
    // Store dismissed advice IDs (user doesn't want to see them)
    // Load from SharedPreferences on init
    private val sharedPrefs: SharedPreferences? = context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val _dismissedAdviceIds = MutableStateFlow<Set<String>>(
        loadDismissedAdviceIds()
    )
    val dismissedAdviceIds: StateFlow<Set<String>> = _dismissedAdviceIds.asStateFlow()
    
    private fun loadDismissedAdviceIds(): Set<String> {
        val prefs = sharedPrefs ?: return emptySet()
        val dismissedIdsString = prefs.getString("dismissed_advice_ids", "") ?: ""
        return if (dismissedIdsString.isEmpty()) {
            emptySet()
        } else {
            dismissedIdsString.split(",").toSet()
        }
    }
    
    private fun saveDismissedAdviceIds(ids: Set<String>) {
        val prefs = sharedPrefs ?: return
        prefs.edit()
            .putString("dismissed_advice_ids", ids.joinToString(","))
            .apply()
    }
    
    fun dismissAdvice(adviceId: String) {
        val newSet = _dismissedAdviceIds.value + adviceId
        _dismissedAdviceIds.value = newSet
        saveDismissedAdviceIds(newSet)
    }
    
    // Shopping deals refresh method removed - no longer needed for Parent OS
    
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
    
    /**
     * Refreshes all data from repository.
     * Call this after account linking to reload data with new user ID.
     */
    fun refreshAllData() {
        loadEntries()
        loadChildren()
        loadPersons()
        loadEntities()
    }
    
    fun getAdviceForEntry(entry: LogEntry): AdviceTemplate? {
        return adviceEngine.findAdvice(entry.rawText, entry.category)
    }
    
    // Shopping deals advice method removed - no longer needed for Parent OS
    
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
    
    /**
     * Toggles completion state for DAY checklist items.
     * Persists via repository.updateEntry so UI updates automatically via Firestore snapshot.
     */
    fun toggleChecklistCompleted(entryId: String) {
        viewModelScope.launch {
            val entry = repository.getEntryById(entryId) ?: return@launch
            if (!com.familylogbook.app.domain.model.DayEntry.isChecklistItem(entry)) return@launch

            val updated = entry.copy(
                isCompleted = !(entry.isCompleted ?: false),
                dayEntryType = entry.dayEntryType ?: DayEntryType.CHECKLIST
            )
            repository.updateEntry(updated)
        }
    }
}

