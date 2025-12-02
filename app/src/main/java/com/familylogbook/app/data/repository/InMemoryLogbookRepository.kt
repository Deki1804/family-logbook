package com.familylogbook.app.data.repository

import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.PersonType
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.domain.model.EntityType
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Mood
import com.familylogbook.app.domain.repository.LogbookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class InMemoryLogbookRepository : LogbookRepository {
    
    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    private val _children = MutableStateFlow<List<Child>>(emptyList())
    private val _persons = MutableStateFlow<List<Person>>(emptyList())
    private val _entities = MutableStateFlow<List<Entity>>(emptyList())
    
    init {
        // Seed with sample data
        seedSampleData()
    }
    
    override fun getAllEntries(): Flow<List<LogEntry>> = _entries.asStateFlow()
    
    override suspend fun addEntry(entry: LogEntry) {
        _entries.value = _entries.value + entry
    }
    
    override suspend fun deleteEntry(entryId: String) {
        _entries.value = _entries.value.filter { it.id != entryId }
    }
    
    override fun getAllChildren(): Flow<List<Child>> = _children.asStateFlow()
    
    override suspend fun addChild(child: Child) {
        _children.value = _children.value + child
    }
    
    override suspend fun deleteChild(childId: String) {
        _children.value = _children.value.filter { it.id != childId }
    }
    
    override suspend fun getChildById(childId: String): Child? {
        return _children.value.find { it.id == childId }
    }
    
    // Person methods
    override fun getAllPersons(): Flow<List<Person>> = _persons.asStateFlow()
    
    override suspend fun addPerson(person: Person) {
        _persons.value = _persons.value + person
    }
    
    override suspend fun deletePerson(personId: String) {
        _persons.value = _persons.value.filter { it.id != personId }
    }
    
    override suspend fun getPersonById(personId: String): Person? {
        return _persons.value.find { it.id == personId }
    }
    
    // Entity methods
    override fun getAllEntities(): Flow<List<Entity>> = _entities.asStateFlow()
    
    override suspend fun addEntity(entity: Entity) {
        _entities.value = _entities.value + entity
    }
    
    override suspend fun deleteEntity(entityId: String) {
        _entities.value = _entities.value.filter { it.id != entityId }
    }
    
    override suspend fun getEntityById(entityId: String): Entity? {
        return _entities.value.find { it.id == entityId }
    }
    
    private fun seedSampleData() {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        // Sample children
        val child1 = Child(
            id = "child1",
            name = "Neo",
            avatarColor = "#4ECDC4",
            emoji = "👶"
        )
        val child2 = Child(
            id = "child2",
            name = "Luna",
            avatarColor = "#FF6B9D",
            emoji = "👧"
        )
        
        _children.value = listOf(child1, child2)
        
        // Sample persons (including parents)
        val person1 = Person(
            id = "person1",
            name = "Neo",
            type = PersonType.CHILD,
            avatarColor = "#4ECDC4",
            emoji = "👶",
            relationship = "Neo"
        )
        val person2 = Person(
            id = "person2",
            name = "Mama",
            type = PersonType.PARENT,
            avatarColor = "#FF6B9D",
            emoji = "👩",
            relationship = "Mama"
        )
        val person3 = Person(
            id = "person3",
            name = "Tata",
            type = PersonType.PARENT,
            avatarColor = "#4A90E2",
            emoji = "👨",
            relationship = "Tata"
        )
        _persons.value = listOf(person1, person2, person3)
        
        // Sample entities
        val entity1 = Entity(
            id = "entity1",
            name = "Auto",
            type = EntityType.CAR,
            emoji = "🚗",
            avatarColor = "#FF6B6B"
        )
        val entity2 = Entity(
            id = "entity2",
            name = "Kuća",
            type = EntityType.HOUSE,
            emoji = "🏠",
            avatarColor = "#95E1D3"
        )
        val entity3 = Entity(
            id = "entity3",
            name = "Financije",
            type = EntityType.FINANCE,
            emoji = "💰",
            avatarColor = "#F38181"
        )
        _entities.value = listOf(entity1, entity2, entity3)
        
        // Sample entries
        calendar.timeInMillis = now - (2 * 24 * 60 * 60 * 1000L) // 2 days ago
        val entry1 = LogEntry(
            id = "entry1",
            childId = "child1",
            timestamp = calendar.timeInMillis,
            rawText = "Neo had a light fever tonight, temperatura 38.4. Gave him some medicine.",
            category = Category.HEALTH,
            tags = listOf("fever", "medicine"),
            mood = null,
            temperature = 38.4f,
            medicineGiven = "sirup"
        )
        
        calendar.timeInMillis = now - (1 * 24 * 60 * 60 * 1000L) // 1 day ago
        val entry2 = LogEntry(
            id = "entry2",
            childId = "child1",
            timestamp = calendar.timeInMillis,
            rawText = "Lost his second tooth! He was so excited.",
            category = Category.DEVELOPMENT,
            tags = listOf("tooth", "milestone"),
            mood = Mood.VERY_GOOD
        )
        
        calendar.timeInMillis = now - (12 * 60 * 60 * 1000L) // 12 hours ago
        val entry3 = LogEntry(
            id = "entry3",
            childId = "child1",
            timestamp = calendar.timeInMillis,
            rawText = "Did not sleep well last night. Woke up 3 times.",
            category = Category.SLEEP,
            tags = listOf("sleep", "wake"),
            mood = Mood.BAD
        )
        
        calendar.timeInMillis = now - (6 * 60 * 60 * 1000L) // 6 hours ago
        val entry4 = LogEntry(
            id = "entry4",
            childId = "child2",
            timestamp = calendar.timeInMillis,
            rawText = "Luna was in a very good mood today. Played all day.",
            category = Category.MOOD,
            tags = listOf("happy", "play"),
            mood = Mood.VERY_GOOD
        )
        
        calendar.timeInMillis = now - (3 * 60 * 60 * 1000L) // 3 hours ago
        val entry5 = LogEntry(
            id = "entry5",
            childId = null,
            timestamp = calendar.timeInMillis,
            rawText = "Changed the air filter in the living room.",
            category = Category.HOME,
            tags = listOf("maintenance", "filter"),
            mood = null
        )
        
        calendar.timeInMillis = now - (1 * 60 * 60 * 1000L) // 1 hour ago
        val entry6 = LogEntry(
            id = "entry6",
            childId = "child1",
            personId = "person1",
            timestamp = calendar.timeInMillis,
            rawText = "Neo ima grčeve već treću večer, plače poslije bočice.",
            category = Category.HEALTH,
            tags = listOf("colic", "crying"),
            mood = Mood.BAD
        )
        
        calendar.timeInMillis = now - (30 * 60 * 1000L) // 30 minutes ago
        val entry7 = LogEntry(
            id = "entry7",
            childId = "child1",
            personId = "person1",
            timestamp = calendar.timeInMillis,
            rawText = "Dojio Neo, lijeva dojka, oko 15 minuta.",
            category = Category.FEEDING,
            tags = listOf("breastfeeding"),
            mood = null,
            feedingType = com.familylogbook.app.domain.model.FeedingType.BREAST_LEFT
        )
        
        // New entries for expanded categories
        calendar.timeInMillis = now - (45 * 60 * 1000L) // 45 minutes ago
        val entry8 = LogEntry(
            id = "entry8",
            entityId = "entity1",
            timestamp = calendar.timeInMillis,
            rawText = "Procurila mi guma jebote. Provjerio rezervnu, sve OK.",
            category = Category.AUTO,
            tags = listOf("flat-tire", "tire")
        )
        
        calendar.timeInMillis = now - (20 * 60 * 1000L) // 20 minutes ago
        val entry9 = LogEntry(
            id = "entry9",
            entityId = "entity3",
            timestamp = calendar.timeInMillis,
            rawText = "Račun za struju došao, 80€. Dospijeće 15.12.",
            category = Category.FINANCE,
            tags = listOf("bill", "electricity"),
            amount = 80.0,
            currency = "EUR",
            reminderDate = now + (13 * 24 * 60 * 60 * 1000L) // 13 days from now
        )
        
        calendar.timeInMillis = now - (10 * 60 * 1000L) // 10 minutes ago
        val entry10 = LogEntry(
            id = "entry10",
            entityId = "entity2",
            timestamp = calendar.timeInMillis,
            rawText = "Pokvario se filter za zrak. Treba zamijeniti.",
            category = Category.HOUSE,
            tags = listOf("repair", "filter")
        )
        
        _entries.value = listOf(entry10, entry9, entry8, entry7, entry6, entry5, entry4, entry3, entry2, entry1).sortedByDescending { it.timestamp }
    }
}

