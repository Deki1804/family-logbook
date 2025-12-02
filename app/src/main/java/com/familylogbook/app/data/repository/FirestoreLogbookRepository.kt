package com.familylogbook.app.data.repository

import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Child
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
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : LogbookRepository {
    
    companion object {
        private const val COLLECTION_CHILDREN = "children"
        private const val COLLECTION_ENTRIES = "entries"
    }
    
    // ========== ENTRIES ==========
    
    override fun getAllEntries(): Flow<List<LogEntry>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_ENTRIES)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val entries = snapshot.documents.mapNotNull { doc ->
                        doc.toLogEntry()
                    }
                    trySend(entries)
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun addEntry(entry: LogEntry) {
        firestore.collection(COLLECTION_ENTRIES)
            .document(entry.id)
            .set(entry.toFirestoreMap())
            .await()
    }
    
    override suspend fun deleteEntry(entryId: String) {
        firestore.collection(COLLECTION_ENTRIES)
            .document(entryId)
            .delete()
            .await()
    }
    
    // ========== CHILDREN ==========
    
    override fun getAllChildren(): Flow<List<Child>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_CHILDREN)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val children = snapshot.documents.mapNotNull { doc ->
                        doc.toChild()
                    }
                    trySend(children)
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    override suspend fun addChild(child: Child) {
        firestore.collection(COLLECTION_CHILDREN)
            .document(child.id)
            .set(child.toFirestoreMap())
            .await()
    }
    
    override suspend fun deleteChild(childId: String) {
        firestore.collection(COLLECTION_CHILDREN)
            .document(childId)
            .delete()
            .await()
    }
    
    override suspend fun getChildById(childId: String): Child? {
        val doc = firestore.collection(COLLECTION_CHILDREN)
            .document(childId)
            .get()
            .await()
        
        return doc.toChild()
    }
    
    // ========== CONVERSION HELPERS ==========
    
    private fun com.google.firebase.firestore.DocumentSnapshot.toLogEntry(): LogEntry? {
        return try {
            LogEntry(
                id = id,
                childId = getString("childId"),
                timestamp = getLong("timestamp") ?: System.currentTimeMillis(),
                rawText = getString("rawText") ?: "",
                category = getString("category")?.let { Category.valueOf(it) } ?: Category.OTHER,
                tags = (get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                mood = getString("mood")?.let { Mood.valueOf(it) },
                hasAttachment = getBoolean("hasAttachment") ?: false,
                feedingType = getString("feedingType")?.let { FeedingType.valueOf(it) },
                feedingAmount = (get("feedingAmount") as? Long)?.toInt(),
                temperature = (get("temperature") as? Double)?.toFloat(),
                medicineGiven = getString("medicineGiven"),
                medicineTimestamp = getLong("medicineTimestamp")
            )
        } catch (e: Exception) {
            null
        }
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
}

