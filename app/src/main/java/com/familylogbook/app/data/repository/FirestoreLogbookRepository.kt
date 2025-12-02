package com.familylogbook.app.data.repository

import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.domain.model.FeedingType
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Mood
import com.familylogbook.app.domain.repository.LogbookRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreLogbookRepository(
    private val userId: String, // Required: user ID from Firebase Auth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : LogbookRepository {
    
    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_CHILDREN = "children"
        private const val COLLECTION_PERSONS = "persons"
        private const val COLLECTION_ENTITIES = "entities"
        private const val COLLECTION_ENTRIES = "entries"
    }
    
    // User-scoped collection paths
    private val childrenCollection = firestore
        .collection(COLLECTION_USERS)
        .document(userId)
        .collection(COLLECTION_CHILDREN)
    
    private val personsCollection = firestore
        .collection(COLLECTION_USERS)
        .document(userId)
        .collection(COLLECTION_PERSONS)
    
    private val entitiesCollection = firestore
        .collection(COLLECTION_USERS)
        .document(userId)
        .collection(COLLECTION_ENTITIES)
    
    private val entriesCollection = firestore
        .collection(COLLECTION_USERS)
        .document(userId)
        .collection(COLLECTION_ENTRIES)
    
    // ========== ENTRIES ==========
    
    override fun getAllEntries(): Flow<List<LogEntry>> = callbackFlow {
        val listener = entriesCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log error but don't crash - return empty list
                    android.util.Log.e("FirestoreLogbookRepository", "Error loading entries: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val entries = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toLogEntry()
                        } catch (e: Exception) {
                            android.util.Log.e("FirestoreLogbookRepository", "Error parsing entry ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    trySend(entries)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun addEntry(entry: LogEntry) {
        try {
            entriesCollection
                .document(entry.id)
                .set(entry.toFirestoreMap())
                .await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreLogbookRepository", "Error adding entry: ${e.message}")
            throw e // Re-throw so ViewModel can handle it
        }
    }
    
    override suspend fun deleteEntry(entryId: String) {
        entriesCollection
            .document(entryId)
            .delete()
            .await()
    }
    
    // ========== CHILDREN ==========
    
    override fun getAllChildren(): Flow<List<Child>> = callbackFlow {
        val listener = childrenCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log error but don't crash - return empty list
                    android.util.Log.e("FirestoreLogbookRepository", "Error loading children: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val children = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toChild()
                        } catch (e: Exception) {
                            android.util.Log.e("FirestoreLogbookRepository", "Error parsing child ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    trySend(children)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun addChild(child: Child) {
        childrenCollection
            .document(child.id)
            .set(child.toFirestoreMap())
            .await()
    }
    
    override suspend fun deleteChild(childId: String) {
        childrenCollection
            .document(childId)
            .delete()
            .await()
    }
    
    override suspend fun getChildById(childId: String): Child? {
        val doc = childrenCollection
            .document(childId)
            .get()
            .await()
        
        return doc.toChild()
    }
    
    // ========== PERSONS ==========
    
    override fun getAllPersons(): Flow<List<Person>> = callbackFlow {
        val listener = personsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreLogbookRepository", "Error loading persons: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val persons = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toPerson()
                        } catch (e: Exception) {
                            android.util.Log.e("FirestoreLogbookRepository", "Error parsing person ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    trySend(persons)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun addPerson(person: Person) {
        personsCollection
            .document(person.id)
            .set(person.toFirestoreMap())
            .await()
    }
    
    override suspend fun deletePerson(personId: String) {
        personsCollection
            .document(personId)
            .delete()
            .await()
    }
    
    override suspend fun getPersonById(personId: String): Person? {
        val doc = personsCollection
            .document(personId)
            .get()
            .await()
        
        return doc.toPerson()
    }
    
    // ========== ENTITIES ==========
    
    override fun getAllEntities(): Flow<List<Entity>> = callbackFlow {
        val listener = entitiesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreLogbookRepository", "Error loading entities: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val entities = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toEntity()
                        } catch (e: Exception) {
                            android.util.Log.e("FirestoreLogbookRepository", "Error parsing entity ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    trySend(entities)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun addEntity(entity: Entity) {
        entitiesCollection
            .document(entity.id)
            .set(entity.toFirestoreMap())
            .await()
    }
    
    override suspend fun deleteEntity(entityId: String) {
        entitiesCollection
            .document(entityId)
            .delete()
            .await()
    }
    
    override suspend fun getEntityById(entityId: String): Entity? {
        val doc = entitiesCollection
            .document(entityId)
            .get()
            .await()
        
        return doc.toEntity()
    }
    
    // ========== CONVERSION HELPERS ==========
    
    private fun com.google.firebase.firestore.DocumentSnapshot.toLogEntry(): LogEntry? {
        return try {
            LogEntry(
                id = id,
                personId = getString("personId"),
                entityId = getString("entityId"),
                childId = getString("childId"), // Backward compatibility
                timestamp = getLong("timestamp") ?: System.currentTimeMillis(),
                rawText = getString("rawText") ?: "",
                category = getString("category")?.let { Category.valueOf(it) } ?: Category.OTHER,
                tags = (get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                mood = getString("mood")?.let { Mood.valueOf(it) },
                hasAttachment = getBoolean("hasAttachment") ?: false,
                aiAdvice = getString("aiAdvice"),
                reminderDate = getLong("reminderDate"),
                feedingType = getString("feedingType")?.let { FeedingType.valueOf(it) },
                feedingAmount = (get("feedingAmount") as? Long)?.toInt(),
                temperature = (get("temperature") as? Double)?.toFloat(),
                medicineGiven = getString("medicineGiven"),
                medicineTimestamp = getLong("medicineTimestamp"),
                amount = (get("amount") as? Double),
                currency = getString("currency"),
                mileage = (get("mileage") as? Long)?.toInt(),
                serviceType = getString("serviceType")
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun LogEntry.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "personId" to personId,
            "entityId" to entityId,
            "childId" to childId, // Backward compatibility
            "timestamp" to timestamp,
            "rawText" to rawText,
            "category" to category.name,
            "tags" to tags,
            "mood" to mood?.name,
            "hasAttachment" to hasAttachment,
            "aiAdvice" to aiAdvice,
            "reminderDate" to reminderDate,
            "feedingType" to feedingType?.name,
            "feedingAmount" to feedingAmount?.toLong(),
            "temperature" to temperature?.toDouble(),
            "medicineGiven" to medicineGiven,
            "medicineTimestamp" to medicineTimestamp,
            "amount" to amount,
            "currency" to currency,
            "mileage" to mileage?.toLong(),
            "serviceType" to serviceType
        )
    }
    
    private fun com.google.firebase.firestore.DocumentSnapshot.toChild(): Child? {
        return try {
            Child(
                id = id,
                name = getString("name") ?: "",
                dateOfBirth = getLong("dateOfBirth"),
                avatarColor = getString("avatarColor") ?: "#FF6B6B",
                emoji = getString("emoji") ?: "👶"
            )
        } catch (e: Exception) {
            null
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
    
    // Person conversion helpers
    private fun com.google.firebase.firestore.DocumentSnapshot.toPerson(): Person? {
        return try {
            Person(
                id = id,
                name = getString("name") ?: "",
                type = getString("type")?.let { com.familylogbook.app.domain.model.PersonType.valueOf(it) } 
                    ?: com.familylogbook.app.domain.model.PersonType.CHILD,
                dateOfBirth = getLong("dateOfBirth"),
                avatarColor = getString("avatarColor") ?: "#FF6B6B",
                emoji = getString("emoji") ?: "👶",
                relationship = getString("relationship")
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun Person.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "type" to type.name,
            "dateOfBirth" to dateOfBirth,
            "avatarColor" to avatarColor,
            "emoji" to emoji,
            "relationship" to relationship
        )
    }
    
    // Entity conversion helpers
    private fun com.google.firebase.firestore.DocumentSnapshot.toEntity(): Entity? {
        return try {
            val metadataMap = get("metadata") as? Map<*, *>
            val metadata = metadataMap?.mapNotNull { (k, v) ->
                (k as? String)?.let { key -> (v as? String)?.let { value -> key to value } }
            }?.toMap() ?: emptyMap()
            
            Entity(
                id = id,
                name = getString("name") ?: "",
                type = getString("type")?.let { com.familylogbook.app.domain.model.EntityType.valueOf(it) }
                    ?: com.familylogbook.app.domain.model.EntityType.OTHER,
                emoji = getString("emoji") ?: "🚗",
                avatarColor = getString("avatarColor") ?: "#4ECDC4",
                metadata = metadata
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun Entity.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "type" to type.name,
            "emoji" to emoji,
            "avatarColor" to avatarColor,
            "metadata" to metadata
        )
    }
}

