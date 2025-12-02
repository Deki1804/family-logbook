package com.familylogbook.app.data.repository

import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
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
        
        // Sample entries
        calendar.timeInMillis = now - (2 * 24 * 60 * 60 * 1000L) // 2 days ago
        val entry1 = LogEntry(
            id = "entry1",
            childId = "child1",
            timestamp = calendar.timeInMillis,
            rawText = "Neo had a light fever tonight. Gave him some medicine.",
            category = Category.HEALTH,
            tags = listOf("fever", "medicine"),
            mood = null
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
        
        _entries.value = listOf(entry5, entry4, entry3, entry2, entry1).sortedByDescending { it.timestamp }
    }
}

