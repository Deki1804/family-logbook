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
    
    // Filtered entries
    private val _filteredEntries = MutableStateFlow<List<LogEntry>>(emptyList())
    val filteredEntries: StateFlow<List<LogEntry>> = _filteredEntries.asStateFlow()
    
    // Shopping deals cache - stores advice for each entry ID
    private val _shoppingDealsByEntryId = MutableStateFlow<Map<String, AdviceTemplate>>(emptyMap())
    val shoppingDealsByEntryId: StateFlow<Map<String, AdviceTemplate>> = _shoppingDealsByEntryId.asStateFlow()
    
    // Track which entries have already been loaded (to avoid duplicate requests)
    private val _shoppingDealsLoaded = mutableSetOf<String>()
    
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
            repository.getAllEntries().collect { entriesList ->
                val sortedEntries = entriesList.sortedByDescending { it.timestamp }
                _entries.value = sortedEntries
                
                // Load shopping deals for new shopping entries (only once per entry)
                // Only process entries that haven't been loaded yet
                val newShoppingEntries = sortedEntries
                    .filter { it.category == Category.SHOPPING }
                    .filter { it.id !in _shoppingDealsLoaded }
                
                // Load deals for new entries (limit to max 3 at a time to avoid too many concurrent requests)
                newShoppingEntries.take(3).forEach { entry ->
                    loadShoppingDealsForEntry(entry)
                }
            }
        }
    }
    
    /**
     * Loads shopping deals for a specific entry (only once).
     * Results are cached in _shoppingDealsByEntryId.
     */
    private fun loadShoppingDealsForEntry(entry: LogEntry) {
        if (entry.id in _shoppingDealsLoaded) {
            return // Already loaded
        }
        
        // Mark as loading immediately to prevent duplicate calls
        _shoppingDealsLoaded.add(entry.id)
        
        viewModelScope.launch {
            try {
                val advice = adviceEngine.findShoppingDealsAdvice(entry.rawText)
                if (advice != null) {
                    _shoppingDealsByEntryId.value = _shoppingDealsByEntryId.value + (entry.id to advice)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Job was cancelled - this is OK, don't log as error
                android.util.Log.d("HomeViewModel", "Shopping deals loading cancelled for entry ${entry.id}")
                // Remove from loaded set so we can retry later
                _shoppingDealsLoaded.remove(entry.id)
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error loading shopping deals for entry ${entry.id}: ${e.message}")
                // Remove from loaded set so we can retry later if needed
                _shoppingDealsLoaded.remove(entry.id)
            }
        }
    }
    
    /**
     * Gets cached shopping deals advice for an entry.
     * Returns null if not found or not loaded yet.
     */
    fun getCachedShoppingDealsAdvice(entryId: String): AdviceTemplate? {
        return _shoppingDealsByEntryId.value[entryId]
    }
    
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
    
    /**
     * Manually refresh shopping deals for an entry (forces reload).
     */
    fun refreshShoppingDealsForEntry(entry: LogEntry) {
        _shoppingDealsLoaded.remove(entry.id)
        _shoppingDealsByEntryId.value = _shoppingDealsByEntryId.value - entry.id
        loadShoppingDealsForEntry(entry)
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
    
    /**
     * Gets shopping deals advice for a shopping entry.
     * DEPRECATED: Use getCachedShoppingDealsAdvice() instead.
     * This method is kept for backward compatibility but should not be used in Compose.
     */
    @Deprecated("Use getCachedShoppingDealsAdvice() instead to avoid duplicate API calls")
    suspend fun getShoppingDealsAdvice(entry: LogEntry): AdviceTemplate? {
        if (entry.category != Category.SHOPPING) {
            return null
        }
        return adviceEngine.findShoppingDealsAdvice(entry.rawText)
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
    
    suspend fun updateShoppingItemChecked(entryId: String, item: String, isChecked: Boolean) {
        val entry = repository.getEntryById(entryId) ?: return
        
        val currentChecked = entry.checkedShoppingItems ?: emptySet()
        val updatedChecked = if (isChecked) {
            currentChecked + item
        } else {
            currentChecked - item
        }
        
        val updatedEntry = entry.copy(
            checkedShoppingItems = updatedChecked
        )
        
        repository.updateEntry(updatedEntry)
    }
}

