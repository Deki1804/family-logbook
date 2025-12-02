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
    val hasAttachment: Boolean = false,
    // Feeding tracking
    val feedingType: FeedingType? = null, // breast_left, breast_right, bottle
    val feedingAmount: Int? = null, // ml for bottle feeding
    // Health tracking
    val temperature: Float? = null, // in Celsius
    val medicineGiven: String? = null, // medicine name
    val medicineTimestamp: Long? = null // when medicine was given
)

enum class FeedingType {
    BREAST_LEFT,
    BREAST_RIGHT,
    BOTTLE
}

