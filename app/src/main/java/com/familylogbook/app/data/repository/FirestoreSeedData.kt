package com.familylogbook.app.data.repository

import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.PersonType
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.domain.model.EntityType
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
    
    suspend fun seedIfEmpty(
        userId: String,
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ) {
        // Check all collections to see if user has any data
        val childrenCollection = firestore
            .collection("users")
            .document(userId)
            .collection("children")
        val personsCollection = firestore
            .collection("users")
            .document(userId)
            .collection("persons")
        val entitiesCollection = firestore
            .collection("users")
            .document(userId)
            .collection("entities")
        val entriesCollection = firestore
            .collection("users")
            .document(userId)
            .collection("entries")
        
        // Check if any data exists
        val childrenSnapshot = childrenCollection.get().await()
        val personsSnapshot = personsCollection.get().await()
        val entitiesSnapshot = entitiesCollection.get().await()
        val entriesSnapshot = entriesCollection.get().await()
        
        val hasData = !childrenSnapshot.isEmpty || !personsSnapshot.isEmpty || 
                     !entitiesSnapshot.isEmpty || !entriesSnapshot.isEmpty
        
        // Only seed if completely empty
        if (!hasData) {
            seedChildren(userId, firestore)
            seedPersons(userId, firestore)
            seedEntities(userId, firestore)
            seedEntries(userId, firestore)
        }
    }
    
    private suspend fun seedChildren(userId: String, firestore: FirebaseFirestore) {
        val childrenCollection = firestore
            .collection("users")
            .document(userId)
            .collection("children")
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
        
        childrenCollection.document(child1.id).set(child1.toFirestoreMap()).await()
        childrenCollection.document(child2.id).set(child2.toFirestoreMap()).await()
    }
    
    private suspend fun seedPersons(userId: String, firestore: FirebaseFirestore) {
        val personsCollection = firestore
            .collection("users")
            .document(userId)
            .collection("persons")
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        calendar.timeInMillis = now
        calendar.add(Calendar.MONTH, -8) // 8 months ago (baby)
        val person1 = Person(
            id = "person1",
            name = "Neo",
            type = PersonType.CHILD,
            dateOfBirth = calendar.timeInMillis,
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
        
        personsCollection.document(person1.id).set(person1.toFirestoreMap()).await()
        personsCollection.document(person2.id).set(person2.toFirestoreMap()).await()
        personsCollection.document(person3.id).set(person3.toFirestoreMap()).await()
    }
    
    private suspend fun seedEntities(userId: String, firestore: FirebaseFirestore) {
        val entitiesCollection = firestore
            .collection("users")
            .document(userId)
            .collection("entities")
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
        
        entitiesCollection.document(entity1.id).set(entity1.toFirestoreMap()).await()
        entitiesCollection.document(entity2.id).set(entity2.toFirestoreMap()).await()
        entitiesCollection.document(entity3.id).set(entity3.toFirestoreMap()).await()
    }
    
    private suspend fun seedEntries(userId: String, firestore: FirebaseFirestore) {
        val entriesCollection = firestore
            .collection("users")
            .document(userId)
            .collection("entries")
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        calendar.timeInMillis = now - (2 * 24 * 60 * 60 * 1000L) // 2 days ago
        val entry1 = LogEntry(
            id = "entry1",
            childId = "child1",
            personId = "person1",
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
            personId = "person1",
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
            personId = "person1",
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
            personId = null,
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
            personId = null,
            entityId = "entity2",
            timestamp = calendar.timeInMillis,
            rawText = "Changed the air filter in the living room.",
            category = Category.HOUSE,
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
            feedingType = FeedingType.BREAST_LEFT
        )
        
        val entries = listOf(entry1, entry2, entry3, entry4, entry5, entry6, entry7)
        entries.forEach { entry ->
            entriesCollection.document(entry.id).set(entry.toFirestoreMap()).await()
        }
    }
    
    private fun Child.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "dateOfBirth" to (dateOfBirth?.toLong()),
            "avatarColor" to avatarColor,
            "emoji" to emoji
        )
    }
    
    private fun Person.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "type" to type.name,
            "dateOfBirth" to (dateOfBirth?.toLong()),
            "avatarColor" to avatarColor,
            "emoji" to emoji,
            "relationship" to (relationship ?: "")
        )
    }
    
    private fun Entity.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "type" to type.name,
            "emoji" to emoji,
            "avatarColor" to avatarColor,
            "metadata" to (metadata ?: emptyMap())
        )
    }
    
    private fun LogEntry.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "personId" to personId,
            "entityId" to entityId,
            "childId" to childId,
            "timestamp" to timestamp,
            "rawText" to rawText,
            "category" to category.name,
            "tags" to tags,
            "mood" to mood?.name,
            "hasAttachment" to hasAttachment,
            "aiAdvice" to aiAdvice,
            "reminderDate" to (reminderDate?.toLong()),
            "feedingType" to feedingType?.name,
            "feedingAmount" to (feedingAmount?.toLong()),
            "temperature" to (temperature?.toDouble()),
            "medicineGiven" to medicineGiven,
            "medicineTimestamp" to (medicineTimestamp?.toLong()),
            "medicineIntervalHours" to (medicineIntervalHours?.toLong()),
            "nextMedicineTime" to (nextMedicineTime?.toLong()),
            "symptoms" to symptoms,
            "shoppingItems" to shoppingItems,
            "checkedShoppingItems" to (checkedShoppingItems?.toList() ?: emptyList()),
            "amount" to amount,
            "currency" to currency,
            "mileage" to (mileage?.toLong()),
            "serviceType" to serviceType
        )
    }
}

