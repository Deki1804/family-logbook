package com.familylogbook.app.domain.model

import java.util.UUID

data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val childId: String? = null, // null means it applies to whole family
    val timestamp: Long = System.currentTimeMillis(),
    val rawText: String,
    val category: Category = Category.OTHER,
    val tags: List<String> = emptyList(),
    val mood: Mood? = null,
    val hasAttachment: Boolean = false
)

