package com.familylogbook.app.data.repository

import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.domain.model.FeedingType
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Mood
import com.familylogbook.app.domain.repository.LogbookRepository
import com.familylogbook.app.data.util.RetryHelper
import com.familylogbook.app.ui.util.ErrorHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreLogbookRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : LogbookRepository {
    
    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_CHILDREN = "children"
        private const val COLLECTION_PERSONS = "persons"
        private const val COLLECTION_ENTITIES = "entities"
        private const val COLLECTION_ENTRIES = "entries"
    }
    
    /**
     * Gets the current user's ID dynamically from FirebaseAuth.
     * This ensures the repository always uses the correct userId even after sign-in changes.
     */
    private fun getCurrentUserId(): String {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            android.util.Log.e("FirestoreLogbookRepository", "User not authenticated! Attempting to sign in anonymously...")
            // Try to sign in anonymously as fallback (should be done in AuthManager, but this is a safety net)
            throw IllegalStateException("User not authenticated. Please sign in first. If you just opened the app, wait a moment for automatic sign-in.")
        }
        android.util.Log.d("FirestoreLogbookRepository", "Current user ID: ${currentUser.uid}, anonymous: ${currentUser.isAnonymous}")
        return currentUser.uid
    }
    
    // User-scoped collection paths - now dynamically generated
    // These methods call getCurrentUserId() which throws if user is not authenticated
    private fun getChildrenCollection(): CollectionReference {
        val userId = getCurrentUserId()
        android.util.Log.d("FirestoreLogbookRepository", "getChildrenCollection - userId: $userId")
        return firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CHILDREN)
    }
    
    private fun getPersonsCollection(): CollectionReference {
        val userId = getCurrentUserId()
        android.util.Log.d("FirestoreLogbookRepository", "getPersonsCollection - userId: $userId")
        return firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_PERSONS)
    }
    
    private fun getEntitiesCollection(): CollectionReference {
        val userId = getCurrentUserId()
        android.util.Log.d("FirestoreLogbookRepository", "getEntitiesCollection - userId: $userId")
        return firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_ENTITIES)
    }
    
    private fun getEntriesCollection(): CollectionReference {
        val userId = getCurrentUserId()
        android.util.Log.d("FirestoreLogbookRepository", "getEntriesCollection - userId: $userId")
        return firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_ENTRIES)
    }
    
    // ========== ENTRIES ==========
    
    override fun getAllEntries(): Flow<List<LogEntry>> = callbackFlow {
        // Log authentication status and get collection once
        val currentUser = auth.currentUser
        val currentUserId = try { 
            getCurrentUserId() 
        } catch (e: Exception) { 
            android.util.Log.e("FirestoreLogbookRepository", "Error getting user ID: ${e.message}")
            null
        }
        
        android.util.Log.d("FirestoreLogbookRepository", "getAllEntries - userId: $currentUserId, auth uid: ${currentUser?.uid}, isAnonymous: ${currentUser?.isAnonymous}")
        
        if (currentUserId == null || currentUser == null) {
            android.util.Log.e("FirestoreLogbookRepository", "User not authenticated")
            trySend(emptyList())
            return@callbackFlow
        }
        
        // Get collection reference once to avoid multiple getCurrentUserId() calls
        val entriesCollectionRef = getEntriesCollection()
        val listener = entriesCollectionRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log error but don't crash - return empty list
                    android.util.Log.e("FirestoreLogbookRepository", "Error loading entries: ${error.message}")
                    if (error is com.google.firebase.firestore.FirebaseFirestoreException) {
                        android.util.Log.e("FirestoreLogbookRepository", "Firestore error code: ${error.code}, currentUser: ${auth.currentUser?.uid}")
                    }
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
                    android.util.Log.d("FirestoreLogbookRepository", "Loaded ${entries.size} entries from Firestore (snapshot had ${snapshot.documents.size} documents)")
                    if (entries.isNotEmpty()) {
                        android.util.Log.d("FirestoreLogbookRepository", "First entry: ${entries.first().id}, category: ${entries.first().category}, text: ${entries.first().rawText.take(50)}")
                    }
                    trySend(entries)
                } else {
                    android.util.Log.w("FirestoreLogbookRepository", "Snapshot is null, sending empty list")
                    trySend(emptyList())
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun addEntry(entry: LogEntry) {
        RetryHelper.retryWithBackoff(
            maxRetries = 3,
            initialDelayMs = 1000,
            retryCondition = { exception ->
                // Retry on network errors and temporary Firestore errors
                ErrorHandler.isNetworkError(exception) ||
                (exception is FirebaseFirestoreException && 
                 (exception.code == FirebaseFirestoreException.Code.UNAVAILABLE ||
                  exception.code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ||
                  exception.code == FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED))
            }
        ) {
            try {
                // Verify user is authenticated and get current userId
                val currentUserId = getCurrentUserId()
                val currentUser = auth.currentUser
                android.util.Log.d("FirestoreLogbookRepository", "Adding entry as user: $currentUserId (authenticated: ${currentUser?.isAnonymous})")
                
                val entriesRef = getEntriesCollection()
                val entryMap = entry.toFirestoreMap()
                android.util.Log.d("FirestoreLogbookRepository", "Saving entry to: ${entriesRef.path}/${entry.id}")
                android.util.Log.d("FirestoreLogbookRepository", "Entry data: category=${entry.category}, personId=${entry.personId}, text=${entry.rawText.take(50)}")
                entriesRef
                    .document(entry.id)
                    .set(entryMap)
                    .await()
                android.util.Log.d("FirestoreLogbookRepository", "Entry saved successfully to Firestore: ${entry.id}")
            } catch (e: FirebaseFirestoreException) {
                android.util.Log.e("FirestoreLogbookRepository", "Firestore error adding entry: ${e.code} - ${e.message}", e)
                // Re-throw Firestore exceptions as-is for proper error handling
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FirestoreLogbookRepository", "Error adding entry: ${e.message}", e)
                throw e // Re-throw so ViewModel can handle it
            }
        }
    }
    
    override suspend fun updateEntry(entry: LogEntry) {
        RetryHelper.retryWithBackoff(
            maxRetries = 3,
            initialDelayMs = 1000,
            retryCondition = { exception ->
                // Retry on network errors and temporary Firestore errors
                ErrorHandler.isNetworkError(exception) ||
                (exception is FirebaseFirestoreException && 
                 (exception.code == FirebaseFirestoreException.Code.UNAVAILABLE ||
                  exception.code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ||
                  exception.code == FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED))
            }
        ) {
            try {
                // Verify user is authenticated - getCurrentUserId() will throw if not
                getCurrentUserId()
                
                getEntriesCollection()
                    .document(entry.id)
                    .set(entry.toFirestoreMap())
                    .await()
            } catch (e: FirebaseFirestoreException) {
                android.util.Log.e("FirestoreLogbookRepository", "Firestore error updating entry: ${e.code} - ${e.message}", e)
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FirestoreLogbookRepository", "Error updating entry: ${e.message}", e)
                throw e
            }
        }
    }
    
    override suspend fun getEntryById(entryId: String): LogEntry? {
        return try {
            val doc = getEntriesCollection().document(entryId).get().await()
            if (doc.exists()) {
                doc.toLogEntry()
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreLogbookRepository", "Error getting entry: ${e.message}")
            null
        }
    }
    
    override suspend fun deleteEntry(entryId: String) {
        getEntriesCollection()
            .document(entryId)
            .delete()
            .await()
    }
    
    // ========== CHILDREN ==========
    
    override fun getAllChildren(): Flow<List<Child>> = callbackFlow {
        // Log authentication status and get collection once
        val currentUser = auth.currentUser
        val currentUserId = try { 
            getCurrentUserId() 
        } catch (e: Exception) { 
            android.util.Log.e("FirestoreLogbookRepository", "Error getting user ID: ${e.message}")
            null
        }
        
        android.util.Log.d("FirestoreLogbookRepository", "getAllChildren - userId: $currentUserId, auth uid: ${currentUser?.uid}")
        
        if (currentUserId == null || currentUser == null) {
            android.util.Log.e("FirestoreLogbookRepository", "User not authenticated")
            trySend(emptyList())
            return@callbackFlow
        }
        
        // Get collection reference once to avoid multiple getCurrentUserId() calls
        val childrenCollectionRef = getChildrenCollection()
        val listener = childrenCollectionRef
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreLogbookRepository", "Error loading children: ${error.message}")
                    if (error is com.google.firebase.firestore.FirebaseFirestoreException) {
                        android.util.Log.e("FirestoreLogbookRepository", "Firestore error code: ${error.code}, currentUser: ${auth.currentUser?.uid}")
                    }
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
        getChildrenCollection()
            .document(child.id)
            .set(child.toFirestoreMap())
            .await()
    }
    
    override suspend fun deleteChild(childId: String) {
        getChildrenCollection()
            .document(childId)
            .delete()
            .await()
    }
    
    override suspend fun getChildById(childId: String): Child? {
        val doc = getChildrenCollection()
            .document(childId)
            .get()
            .await()
        
        return doc.toChild()
    }
    
    // ========== PERSONS ==========
    
    override fun getAllPersons(): Flow<List<Person>> = callbackFlow {
        // Log authentication status and get collection once
        val currentUser = auth.currentUser
        val currentUserId = try { 
            getCurrentUserId() 
        } catch (e: Exception) { 
            android.util.Log.e("FirestoreLogbookRepository", "Error getting user ID: ${e.message}")
            null
        }
        
        android.util.Log.d("FirestoreLogbookRepository", "getAllPersons - userId: $currentUserId, auth uid: ${currentUser?.uid}")
        
        if (currentUserId == null || currentUser == null) {
            android.util.Log.e("FirestoreLogbookRepository", "User not authenticated")
            trySend(emptyList())
            return@callbackFlow
        }
        
        // Get collection reference once to avoid multiple getCurrentUserId() calls
        val personsCollectionRef = getPersonsCollection()
        val listener = personsCollectionRef
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreLogbookRepository", "Error loading persons: ${error.message}")
                    if (error is com.google.firebase.firestore.FirebaseFirestoreException) {
                        android.util.Log.e("FirestoreLogbookRepository", "Firestore error code: ${error.code}, currentUser: ${auth.currentUser?.uid}")
                    }
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
        getPersonsCollection()
            .document(person.id)
            .set(person.toFirestoreMap())
            .await()
    }
    
    override suspend fun deletePerson(personId: String) {
        getPersonsCollection()
            .document(personId)
            .delete()
            .await()
    }
    
    override suspend fun getPersonById(personId: String): Person? {
        val doc = getPersonsCollection()
            .document(personId)
            .get()
            .await()
        
        return doc.toPerson()
    }
    
    // ========== ENTITIES ==========
    
    override fun getAllEntities(): Flow<List<Entity>> = callbackFlow {
        // Log authentication status and get collection once
        val currentUser = auth.currentUser
        val currentUserId = try { 
            getCurrentUserId() 
        } catch (e: Exception) { 
            android.util.Log.e("FirestoreLogbookRepository", "Error getting user ID: ${e.message}")
            null
        }
        
        android.util.Log.d("FirestoreLogbookRepository", "getAllEntities - userId: $currentUserId, auth uid: ${currentUser?.uid}")
        
        if (currentUserId == null || currentUser == null) {
            android.util.Log.e("FirestoreLogbookRepository", "User not authenticated")
            trySend(emptyList())
            return@callbackFlow
        }
        
        // Get collection reference once to avoid multiple getCurrentUserId() calls
        val entitiesCollectionRef = getEntitiesCollection()
        val listener = entitiesCollectionRef
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreLogbookRepository", "Error loading entities: ${error.message}")
                    if (error is com.google.firebase.firestore.FirebaseFirestoreException) {
                        android.util.Log.e("FirestoreLogbookRepository", "Firestore error code: ${error.code}, currentUser: ${auth.currentUser?.uid}")
                    }
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
        getEntitiesCollection()
            .document(entity.id)
            .set(entity.toFirestoreMap())
            .await()
    }
    
    override suspend fun deleteEntity(entityId: String) {
        getEntitiesCollection()
            .document(entityId)
            .delete()
            .await()
    }
    
    override suspend fun getEntityById(entityId: String): Entity? {
        val doc = getEntitiesCollection()
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
                dayEntryType = getString("dayEntryType")?.let { com.familylogbook.app.domain.model.DayEntryType.valueOf(it) },
                isCompleted = getBoolean("isCompleted"),
                dueDate = getLong("dueDate"),
                feedingType = getString("feedingType")?.let { FeedingType.valueOf(it) },
                feedingAmount = (get("feedingAmount") as? Long)?.toInt(),
                temperature = (get("temperature") as? Double)?.toFloat(),
                symptoms = (get("symptoms") as? List<*>)?.mapNotNull { it as? String },
                medicineGiven = getString("medicineGiven"),
                medicineDosage = getString("medicineDosage"),
                medicineTimestamp = getLong("medicineTimestamp"),
                medicineIntervalHours = (get("medicineIntervalHours") as? Long)?.toInt(),
                nextMedicineTime = getLong("nextMedicineTime"),
                shoppingItems = (get("shoppingItems") as? List<*>)?.mapNotNull { it as? String },
                checkedShoppingItems = (get("checkedShoppingItems") as? List<*>)?.mapNotNull { it as? String }?.toSet(),
                vaccinationName = getString("vaccinationName"),
                vaccinationDate = getLong("vaccinationDate"),
                nextVaccinationDate = getLong("nextVaccinationDate"),
                nextVaccinationMessage = getString("nextVaccinationMessage"),
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
            "dayEntryType" to dayEntryType?.name,
            "isCompleted" to isCompleted,
            "dueDate" to dueDate,
            "feedingType" to feedingType?.name,
            "feedingAmount" to feedingAmount?.toLong(),
            "temperature" to temperature?.toDouble(),
            "symptoms" to symptoms,
            "medicineGiven" to medicineGiven,
            "medicineDosage" to medicineDosage,
            "medicineTimestamp" to medicineTimestamp,
            "medicineIntervalHours" to medicineIntervalHours?.toLong(),
            "nextMedicineTime" to nextMedicineTime,
            "shoppingItems" to shoppingItems,
            "checkedShoppingItems" to (checkedShoppingItems?.toList() ?: emptyList()),
            "vaccinationName" to vaccinationName,
            "vaccinationDate" to vaccinationDate,
            "nextVaccinationDate" to nextVaccinationDate,
            "nextVaccinationMessage" to nextVaccinationMessage,
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

