package com.familylogbook.app.domain.model

/**
 * Result of AI classification for a log entry.
 * This is what the classifier returns before creating the actual LogEntry.
 */
data class ClassifiedEntryMetadata(
    val category: Category,
    val tags: List<String>,
    val mood: Mood? = null,
    val temperature: Float? = null, // Extracted temperature in Celsius
    val medicineGiven: String? = null, // Extracted medicine name
    val feedingType: FeedingType? = null, // Extracted feeding type
    val feedingAmount: Int? = null // Extracted feeding amount in ml
)

