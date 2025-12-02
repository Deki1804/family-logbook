package com.familylogbook.app.domain.repository

import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.domain.model.LogEntry
import kotlinx.coroutines.flow.Flow

interface LogbookRepository {
    fun getAllEntries(): Flow<List<LogEntry>>
    suspend fun addEntry(entry: LogEntry)
    suspend fun deleteEntry(entryId: String)
    
    // Legacy Child methods (for backward compatibility)
    fun getAllChildren(): Flow<List<Child>>
    suspend fun addChild(child: Child)
    suspend fun deleteChild(childId: String)
    suspend fun getChildById(childId: String): Child?
    
    // New Person methods
    fun getAllPersons(): Flow<List<Person>>
    suspend fun addPerson(person: Person)
    suspend fun deletePerson(personId: String)
    suspend fun getPersonById(personId: String): Person?
    
    // Entity methods
    fun getAllEntities(): Flow<List<Entity>>
    suspend fun addEntity(entity: Entity)
    suspend fun deleteEntity(entityId: String)
    suspend fun getEntityById(entityId: String): Entity?
}

