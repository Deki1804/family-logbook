package com.familylogbook.app.domain.model

/**
 * Result of AI classification for a log entry.
 * This is what the classifier returns before creating the actual LogEntry.
 */
data class ClassifiedEntryMetadata(
    val category: Category,
    val tags: List<String>,
    val mood: Mood? = null
)

