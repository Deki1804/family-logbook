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
    val medicineIntervalHours: Int? = null, // Interval between medicine doses in hours (e.g., 6 for every 6 hours)
    val feedingType: FeedingType? = null, // Extracted feeding type
    val feedingAmount: Int? = null, // Extracted feeding amount in ml
    val reminderDate: Long? = null, // Extracted date for reminders (servis, appointments, etc.)
    val shoppingItems: List<String>? = null, // Extracted shopping list items
    val vaccinationName: String? = null, // Extracted vaccination name (e.g., "MMR", "DTP-Hib-IPV")
    val nextVaccinationDate: Long? = null, // Recommended date for next vaccination
    val nextVaccinationMessage: String? = null // Message about next vaccination
)

