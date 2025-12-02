package com.familylogbook.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familylogbook.app.data.export.ExportManager
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.domain.model.PersonType
import com.familylogbook.app.domain.model.EntityType
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
    
    private val _persons = MutableStateFlow<List<Person>>(emptyList())
    val persons: StateFlow<List<Person>> = _persons.asStateFlow()
    
    private val _entities = MutableStateFlow<List<Entity>>(emptyList())
    val entities: StateFlow<List<Entity>> = _entities.asStateFlow()
    
    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()
    
    private val _newChildName = MutableStateFlow("")
    val newChildName: StateFlow<String> = _newChildName.asStateFlow()
    
    private val _newChildEmoji = MutableStateFlow("👶")
    val newChildEmoji: StateFlow<String> = _newChildEmoji.asStateFlow()
    
    private val _newPersonName = MutableStateFlow("")
    val newPersonName: StateFlow<String> = _newPersonName.asStateFlow()
    
    private val _newPersonType = MutableStateFlow(PersonType.CHILD)
    val newPersonType: StateFlow<PersonType> = _newPersonType.asStateFlow()
    
    private val _newPersonEmoji = MutableStateFlow("👶")
    val newPersonEmoji: StateFlow<String> = _newPersonEmoji.asStateFlow()
    
    private val _newEntityName = MutableStateFlow("")
    val newEntityName: StateFlow<String> = _newEntityName.asStateFlow()
    
    private val _newEntityType = MutableStateFlow(EntityType.OTHER)
    val newEntityType: StateFlow<EntityType> = _newEntityType.asStateFlow()
    
    private val _newEntityEmoji = MutableStateFlow("🚗")
    val newEntityEmoji: StateFlow<String> = _newEntityEmoji.asStateFlow()
    
    init {
        loadChildren()
        loadPersons()
        loadEntities()
        loadEntries()
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
        
        return try {
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
            
            true
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error adding child: ${e.message}")
            false
        }
    }
    
    suspend fun deleteChild(childId: String): Boolean {
        return try {
            repository.deleteChild(childId)
            true
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error deleting child: ${e.message}")
            false
        }
    }
    
    // Person methods
    fun setNewPersonName(name: String) {
        _newPersonName.value = name
    }
    
    fun setNewPersonType(type: PersonType) {
        _newPersonType.value = type
    }
    
    fun setNewPersonEmoji(emoji: String) {
        _newPersonEmoji.value = emoji
    }
    
    suspend fun addPerson(): Boolean {
        val name = _newPersonName.value.trim()
        if (name.isEmpty()) {
            return false
        }
        
        return try {
            val colors = listOf("#FF6B6B", "#4ECDC4", "#FF6B9D", "#95E1D3", "#F38181", "#AA96DA", "#FCBAD3", "#A8E6CF")
            val randomColor = colors.random()
            
            val person = Person(
                name = name,
                type = _newPersonType.value,
                avatarColor = randomColor,
                emoji = _newPersonEmoji.value,
                relationship = name
            )
            
            repository.addPerson(person)
            
            // Reset form
            _newPersonName.value = ""
            _newPersonType.value = PersonType.CHILD
            _newPersonEmoji.value = "👶"
            
            true
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error adding person: ${e.message}")
            false
        }
    }
    
    suspend fun deletePerson(personId: String): Boolean {
        return try {
            // Delete all entries associated with this person
            val entriesToDelete = _entries.value.filter { 
                it.personId == personId || it.childId == personId 
            }
            entriesToDelete.forEach { entry ->
                repository.deleteEntry(entry.id)
            }
            
            // Delete the person
            repository.deletePerson(personId)
            true
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error deleting person: ${e.message}")
            false
        }
    }
    
    suspend fun getPersonEntryCount(personId: String): Int {
        return _entries.value.count { 
            it.personId == personId || it.childId == personId 
        }
    }
    
    // Entity methods
    fun setNewEntityName(name: String) {
        _newEntityName.value = name
    }
    
    fun setNewEntityType(type: EntityType) {
        _newEntityType.value = type
    }
    
    fun setNewEntityEmoji(emoji: String) {
        _newEntityEmoji.value = emoji
    }
    
    suspend fun addEntity(): Boolean {
        val name = _newEntityName.value.trim()
        if (name.isEmpty()) {
            return false
        }
        
        return try {
            val colors = listOf("#FF6B6B", "#4ECDC4", "#FF6B9D", "#95E1D3", "#F38181", "#AA96DA", "#FCBAD3", "#A8E6CF")
            val randomColor = colors.random()
            
            val entity = Entity(
                name = name,
                type = _newEntityType.value,
                avatarColor = randomColor,
                emoji = _newEntityEmoji.value
            )
            
            repository.addEntity(entity)
            
            // Reset form
            _newEntityName.value = ""
            _newEntityType.value = EntityType.OTHER
            _newEntityEmoji.value = "🚗"
            
            true
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error adding entity: ${e.message}")
            false
        }
    }
    
    suspend fun deleteEntity(entityId: String): Boolean {
        return try {
            // Delete all entries associated with this entity
            val entriesToDelete = _entries.value.filter { 
                it.entityId == entityId 
            }
            entriesToDelete.forEach { entry ->
                repository.deleteEntry(entry.id)
            }
            
            // Delete the entity
            repository.deleteEntity(entityId)
            true
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error deleting entity: ${e.message}")
            false
        }
    }
    
    suspend fun getEntityEntryCount(entityId: String): Int {
        return _entries.value.count { it.entityId == entityId }
    }
    
    /**
     * Resets all data for the current user.
     * Deletes all persons, entities, and entries.
     * Optionally reseeds sample data.
     */
    suspend fun resetAllData(reseedSample: Boolean = false): Boolean {
        return try {
            // Delete all entries
            _entries.value.forEach { entry ->
                repository.deleteEntry(entry.id)
            }
            
            // Delete all persons
            _persons.value.forEach { person ->
                repository.deletePerson(person.id)
            }
            
            // Delete all entities
            _entities.value.forEach { entity ->
                repository.deleteEntity(entity.id)
            }
            
            // Delete all children (legacy)
            _children.value.forEach { child ->
                repository.deleteChild(child.id)
            }
            
            // Optionally reseed sample data
            if (reseedSample) {
                // Note: This would require access to FirestoreSeedData or similar
                // For now, we'll just clear everything
                android.util.Log.d("SettingsViewModel", "Reset complete. Sample data reseeding not yet implemented.")
            }
            
            true
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error resetting data: ${e.message}")
            false
        }
    }
    
    fun exportToJson(): String {
        return exportManager.exportToJson(_children.value, _entries.value)
    }
    
    fun exportToCsv(): String {
        return exportManager.exportToCsv(_children.value, _entries.value)
    }
    
    suspend fun importFromJson(jsonString: String): ImportResult {
        val parsed = exportManager.parseJsonImport(jsonString)
        if (parsed == null) {
            return ImportResult.Error("Failed to parse JSON file")
        }
        
        val (children, entries) = parsed
        
        // Import children (skip if already exists)
        val existingChildIds = _children.value.map { it.id }.toSet()
        children.forEach { child ->
            if (!existingChildIds.contains(child.id)) {
                repository.addChild(child)
            }
        }
        
        // Import entries (skip if already exists)
        val existingEntryIds = _entries.value.map { it.id }.toSet()
        entries.forEach { entry ->
            if (!existingEntryIds.contains(entry.id)) {
                repository.addEntry(entry)
            }
        }
        
        return ImportResult.Success(
            childrenAdded = children.count { !existingChildIds.contains(it.id) },
            entriesAdded = entries.count { !existingEntryIds.contains(it.id) }
        )
    }
    
    sealed class ImportResult {
        data class Success(val childrenAdded: Int, val entriesAdded: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}

