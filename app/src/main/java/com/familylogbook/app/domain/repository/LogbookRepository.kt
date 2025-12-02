package com.familylogbook.app.domain.repository

import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.LogEntry
import kotlinx.coroutines.flow.Flow

interface LogbookRepository {
    fun getAllEntries(): Flow<List<LogEntry>>
    suspend fun addEntry(entry: LogEntry)
    suspend fun deleteEntry(entryId: String)
    
    fun getAllChildren(): Flow<List<Child>>
    suspend fun addChild(child: Child)
    suspend fun deleteChild(childId: String)
    suspend fun getChildById(childId: String): Child?
}

