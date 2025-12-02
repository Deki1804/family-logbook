package com.familylogbook.app.data.repository

import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.FeedingType
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Mood
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

/**
 * Helper class to seed Firestore with sample data for testing.
 * Call this once to populate the database with initial data.
 */
object FirestoreSeedData {
    
    suspend fun seedIfEmpty(firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
        // Check if children collection is empty
        val childrenSnapshot = firestore.collection("children").get().await()
        if (childrenSnapshot.isEmpty) {
            seedChildren(firestore)
        }
        
        // Check if entries collection is empty
        val entriesSnapshot = firestore.collection("entries").get().await()
        if (entriesSnapshot.isEmpty) {
            seedEntries(firestore)
        }
    }
    
    private suspend fun seedChildren(firestore: FirebaseFirestore) {
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
        
        firestore.collection("children").document(child1.id).set(child1.toFirestoreMap()).await()
        firestore.collection("children").document(child2.id).set(child2.toFirestoreMap()).await()
    }
    
    private suspend fun seedEntries(firestore: FirebaseFirestore) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
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
            timestamp = calendar.timeInMillis,
            rawText = "Dojio Neo, lijeva dojka, oko 15 minuta.",
            category = Category.FEEDING,
            tags = listOf("breastfeeding"),
            mood = null,
            feedingType = FeedingType.BREAST_LEFT
        )
        
        val entries = listOf(entry1, entry2, entry3, entry4, entry5, entry6, entry7)
        entries.forEach { entry ->
            firestore.collection("entries").document(entry.id).set(entry.toFirestoreMap()).await()
        }
    }
    
    private fun Child.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "dateOfBirth" to dateOfBirth,
            "avatarColor" to avatarColor,
            "emoji" to emoji
        )
    }
    
    private fun LogEntry.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "childId" to childId,
            "timestamp" to timestamp,
            "rawText" to rawText,
            "category" to category.name,
            "tags" to tags,
            "mood" to mood?.name,
            "hasAttachment" to hasAttachment,
            "feedingType" to feedingType?.name,
            "feedingAmount" to feedingAmount?.toLong(),
            "temperature" to temperature?.toDouble(),
            "medicineGiven" to medicineGiven,
            "medicineTimestamp" to medicineTimestamp
        )
    }
}

