package com.familylogbook.app.domain.model

import java.util.UUID

data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    // Person or Entity reference (backward compatible with childId)
    val personId: String? = null, // person this entry relates to
    val entityId: String? = null, // entity this entry relates to (car, house, etc.)
    @Deprecated("Use personId instead", ReplaceWith("personId"))
    val childId: String? = null, // kept for backward compatibility
    
    val timestamp: Long = System.currentTimeMillis(),
    val rawText: String,
    val category: Category = Category.OTHER,
    val tags: List<String> = emptyList(),
    val mood: Mood? = null,
    val hasAttachment: Boolean = false,
    
    // AI-generated advice (optional)
    val aiAdvice: String? = null,
    
    // Reminders (for future dates like car service, bills, etc.)
    val reminderDate: Long? = null,
    
    // Feeding tracking (for babies/toddlers)
    val feedingType: FeedingType? = null, // breast_left, breast_right, bottle
    val feedingAmount: Int? = null, // ml for bottle feeding
    
    // Health tracking
    val temperature: Float? = null, // in Celsius
    val medicineGiven: String? = null, // medicine name
    val medicineTimestamp: Long? = null, // when medicine was given
    
    // Finance tracking
    val amount: Double? = null, // for finance entries
    val currency: String? = null, // e.g., "EUR", "USD"
    
    // Auto tracking
    val mileage: Int? = null, // for car entries
    val serviceType: String? = null, // e.g., "Oil change", "Tire replacement"
)

enum class FeedingType {
    BREAST_LEFT,
    BREAST_RIGHT,
    BOTTLE
}

