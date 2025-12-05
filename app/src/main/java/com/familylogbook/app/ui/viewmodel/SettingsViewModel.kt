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
     * 
     * @param userId The user ID for reseeding sample data (required if reseedSample = true)
     * @param reseedSample If true, seeds sample data after deletion
     */
    suspend fun resetAllData(userId: String? = null, reseedSample: Boolean = false): Boolean {
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
                if (userId != null) {
                    try {
                        com.familylogbook.app.data.repository.FirestoreSeedData.seedIfEmpty(userId)
                        android.util.Log.i("SettingsViewModel", "Sample data reseeded for user $userId")
                    } catch (e: Exception) {
                        android.util.Log.e("SettingsViewModel", "Error reseeding sample data: ${e.message}", e)
                        // Don't fail the reset if seeding fails
                    }
                } else {
                    android.util.Log.w("SettingsViewModel", "Cannot reseed sample data: userId is null")
                }
            }
            
            true
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error resetting data: ${e.message}")
            false
        }
    }
    
    fun exportToJson(): String {
        return exportManager.exportToJson(
            children = _children.value,
            persons = _persons.value,
            entities = _entities.value,
            entries = _entries.value
        )
    }
    
    fun exportToCsv(): String {
        return exportManager.exportToCsv(
            children = _children.value,
            persons = _persons.value,
            entities = _entities.value,
            entries = _entries.value
        )
    }
    
    suspend fun importFromJson(jsonString: String): ImportResult {
        val parsed = exportManager.parseJsonImport(jsonString)
        if (parsed == null) {
            return ImportResult.Error("Failed to parse JSON file")
        }
        
        val exportData = parsed
        
        // Import persons first (v2.0)
        val existingPersonIds = _persons.value.map { it.id }.toSet()
        var personsAdded = 0
        exportData.persons.forEach { person ->
            if (!existingPersonIds.contains(person.id)) {
                repository.addPerson(person)
                personsAdded++
            }
        }
        
        // Import entities (v2.0)
        val existingEntityIds = _entities.value.map { it.id }.toSet()
        var entitiesAdded = 0
        exportData.entities.forEach { entity ->
            if (!existingEntityIds.contains(entity.id)) {
                repository.addEntity(entity)
                entitiesAdded++
            }
        }
        
        // Import children (legacy support)
        val existingChildIds = _children.value.map { it.id }.toSet()
        var childrenAdded = 0
        exportData.children.forEach { child ->
            if (!existingChildIds.contains(child.id)) {
                repository.addChild(child)
                childrenAdded++
            }
        }
        
        // Import entries last (after persons/entities are imported so references are valid)
        val existingEntryIds = _entries.value.map { it.id }.toSet()
        var entriesAdded = 0
        exportData.entries.forEach { entry ->
            if (!existingEntryIds.contains(entry.id)) {
                repository.addEntry(entry)
                entriesAdded++
            }
        }
        
        return ImportResult.Success(
            childrenAdded = childrenAdded,
            personsAdded = personsAdded,
            entitiesAdded = entitiesAdded,
            entriesAdded = entriesAdded
        )
    }
    
    sealed class ImportResult {
        data class Success(
            val childrenAdded: Int = 0,
            val personsAdded: Int = 0,
            val entitiesAdded: Int = 0,
            val entriesAdded: Int = 0
        ) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}

